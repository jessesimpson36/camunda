/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.broker.system.configuration;

import io.camunda.zeebe.broker.Loggers;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import org.slf4j.Logger;
import org.springframework.util.unit.DataSize;

public class DiskCfg implements ConfigurationEntry {

  private static final Logger LOG = Loggers.SYSTEM_LOGGER;

  private static final boolean DEFAULT_DISK_MONITORING_ENABLED = true;
  private static final String DISABLED_DISK_FREESPACE = "0%";
  private static final Duration DEFAULT_DISK_USAGE_MONITORING_DELAY = Duration.ofSeconds(1);
  private boolean enableMonitoring = DEFAULT_DISK_MONITORING_ENABLED;
  private Duration monitoringInterval = DEFAULT_DISK_USAGE_MONITORING_DELAY;
  private FreeSpaceCfg freeSpace = new FreeSpaceCfg();

  @Override
  public void init(final BrokerCfg globalConfig, final String brokerBase) {
    freeSpace.init(globalConfig, brokerBase);

    if (!enableMonitoring) {
      LOG.info(
          "Disk usage monitoring is disabled, setting required freespace to {}",
          DISABLED_DISK_FREESPACE);
      freeSpace.setReplication(DISABLED_DISK_FREESPACE);
      freeSpace.setProcessing(DISABLED_DISK_FREESPACE);
    }
  }

  public boolean isEnableMonitoring() {
    return enableMonitoring;
  }

  public void setEnableMonitoring(final boolean enableMonitoring) {
    this.enableMonitoring = enableMonitoring;
  }

  public Duration getMonitoringInterval() {
    return monitoringInterval;
  }

  public void setMonitoringInterval(final Duration monitoringInterval) {
    this.monitoringInterval = monitoringInterval;
  }

  public FreeSpaceCfg getFreeSpace() {
    return freeSpace;
  }

  public void setFreeSpace(final FreeSpaceCfg freeSpace) {
    this.freeSpace = freeSpace;
  }

  @Override
  public String toString() {
    return "DiskCfg{" + "enableMonitoring=" + enableMonitoring + ", freeSpace=" + freeSpace + '}';
  }

  static class FreeSpaceCfg implements ConfigurationEntry {

    private static final String DEFAULT_PROCESSING_FREESPACE = "2GB";
    private static final String DEFAULT_REPLICATION_FREESPACE = "1GB";
    private String processing = DEFAULT_PROCESSING_FREESPACE;
    private String replication = DEFAULT_REPLICATION_FREESPACE;

    public String getProcessing() {
      return processing;
    }

    public void setProcessing(final String processing) {
      this.processing = processing;
    }

    public String getReplication() {
      return replication;
    }

    public void setReplication(final String replication) {
      this.replication = replication;
    }

    public long getMinFreeSpaceForProcessing(final String dataDirectory) {
      return parse(dataDirectory, processing);
    }

    public long getMinFreeSpaceForReplication(final String dataDirectory) {
      return parse(dataDirectory, replication);
    }

    private long parse(final String dataDirectory, final String watermark) {
      if (watermark.endsWith("%")) {
        final NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        try {
          final Number minFreeRatio = defaultFormat.parse(watermark);
          final var directoryFile = new File(dataDirectory);
          return Math.round(minFreeRatio.doubleValue() * directoryFile.getTotalSpace());
        } catch (final ParseException e) {
          throw new IllegalArgumentException(e);
        }
      } else {
        return DataSize.parse(watermark).toBytes();
      }
    }

    @Override
    public String toString() {
      return "FreeSpaceCfg{"
          + "processing='"
          + processing
          + '\''
          + ", replication='"
          + replication
          + '\''
          + '}';
    }
  }
}
