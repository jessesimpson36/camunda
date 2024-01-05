/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.backup.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import io.camunda.zeebe.backup.api.Backup;
import io.camunda.zeebe.backup.api.BackupIdentifier;
import io.camunda.zeebe.backup.api.BackupIdentifierWildcard;
import io.camunda.zeebe.backup.api.BackupStatus;
import io.camunda.zeebe.backup.api.BackupStatusCode;
import io.camunda.zeebe.backup.api.BackupStore;
import io.camunda.zeebe.backup.azure.manifest.Manifest;
import io.camunda.zeebe.backup.common.BackupStatusImpl;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BackupStore} for Azure. Stores all backups in a given bucket.
 *
 * <p>All created object keys are prefixed by the {@link BackupIdentifier}, with the following
 * scheme: {@code basePath/partitionId/checkpointId/nodeId}.
 */
public final class AzureBackupStore implements BackupStore {
  public static final String SNAPSHOT_FILESET_NAME = "snapshot";
  public static final String SEGMENTS_FILESET_NAME = "segments";
  private static final Logger LOG = LoggerFactory.getLogger(AzureBackupStore.class);
  private final ExecutorService executor;
  private final FileSetManager fileSetManager;
  private final ManifestManager manifestManager;

  public AzureBackupStore(final AzureBackupConfig config) {
    this(config, buildClient(config));
  }

  public AzureBackupStore(final AzureBackupConfig config, final BlobServiceClient client) {
    executor = Executors.newVirtualThreadPerTaskExecutor();
    final BlobContainerClient blobContainerClient =
        client.getBlobContainerClient(config.containerName());

    fileSetManager = new FileSetManager(blobContainerClient);
    manifestManager = new ManifestManager(blobContainerClient);
  }

  public static BlobServiceClient buildClient(final AzureBackupConfig config) {

    // BlobServiceClientBuilder has their own validations, for building the client
    if (config.connectionString() != null) {
      return new BlobServiceClientBuilder()
          .connectionString(config.connectionString())
          .buildClient();
    } else {
      return new BlobServiceClientBuilder()
          .endpoint(config.endpoint())
          .credential(new StorageSharedKeyCredential(config.accountName(), config.accountKey()))
          .buildClient();
    }
  }

  @Override
  public CompletableFuture<Void> save(final Backup backup) {
    return CompletableFuture.runAsync(
        () -> {
          final var persistedManifest = manifestManager.createInitialManifest(backup);
          try {
            fileSetManager.save(backup.id(), SNAPSHOT_FILESET_NAME, backup.snapshot());
            fileSetManager.save(backup.id(), SEGMENTS_FILESET_NAME, backup.segments());
            manifestManager.completeManifest(persistedManifest);
          } catch (final Exception e) {
            manifestManager.markAsFailed(persistedManifest.manifest().id(), e.getMessage());
            throw e;
          }
        },
        executor);
  }

  @Override
  public CompletableFuture<BackupStatus> getStatus(final BackupIdentifier id) {
    return CompletableFuture.supplyAsync(
        () -> {
          final var manifest = manifestManager.getManifest(id);
          if (manifest == null) {
            return BackupStatusImpl.doesNotExist(id);
          }
          return toStatus(manifest);
        },
        executor);
  }

  @Override
  public CompletableFuture<Collection<BackupStatus>> list(final BackupIdentifierWildcard wildcard) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompletableFuture<Void> delete(final BackupIdentifier id) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompletableFuture<Backup> restore(final BackupIdentifier id, final Path targetFolder) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CompletableFuture<BackupStatusCode> markFailed(
      final BackupIdentifier id, final String failureReason) {
    return CompletableFuture.supplyAsync(
        () -> {
          manifestManager.markAsFailed(id, failureReason);
          return BackupStatusCode.FAILED;
        },
        executor);
  }

  @Override
  public CompletableFuture<Void> closeAsync() {
    return CompletableFuture.runAsync(
        () -> {
          try {
            executor.shutdown();
            final var closed = executor.awaitTermination(1, TimeUnit.MINUTES);
            if (!closed) {
              LOG.warn("Failed to orderly shutdown Azure Store Executor within one minute.");
              executor.shutdownNow();
            }
          } catch (final Exception e) {
            LOG.error("Failed to shutdown of Azure Store Executor.");
            throw new RuntimeException(e);
          }
        });
  }

  public static void validateConfig(final AzureBackupConfig config) {
    if (config.connectionString() == null
        && (config.accountKey() == null
            || config.accountName() == null
            || config.endpoint() == null)) {
      throw new IllegalArgumentException(
          "Connection string, or all of connection information (account name, account key, and endpoint) must be provided.");
    }
    if (config.containerName() == null) {
      throw new IllegalArgumentException("Container name cannot be null.");
    }
  }

  private static BackupStatus toStatus(final Manifest manifest) {
    return switch (manifest.statusCode()) {
      case IN_PROGRESS ->
          new BackupStatusImpl(
              manifest.id(),
              Optional.ofNullable(manifest.descriptor()),
              BackupStatusCode.IN_PROGRESS,
              Optional.empty(),
              Optional.ofNullable(manifest.createdAt()),
              Optional.ofNullable(manifest.modifiedAt()));
      case COMPLETED ->
          new BackupStatusImpl(
              manifest.id(),
              Optional.ofNullable(manifest.descriptor()),
              BackupStatusCode.COMPLETED,
              Optional.empty(),
              Optional.ofNullable(manifest.createdAt()),
              Optional.ofNullable(manifest.modifiedAt()));
      case FAILED ->
          new BackupStatusImpl(
              manifest.id(),
              Optional.ofNullable(manifest.descriptor()),
              BackupStatusCode.FAILED,
              Optional.ofNullable(manifest.asFailed().failureReason()),
              Optional.ofNullable(manifest.createdAt()),
              Optional.ofNullable(manifest.modifiedAt()));
    };
  }
}
