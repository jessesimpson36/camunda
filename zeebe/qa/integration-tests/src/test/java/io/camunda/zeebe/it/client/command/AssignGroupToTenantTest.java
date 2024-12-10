/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.it.client.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.ProblemException;
import io.camunda.zeebe.it.util.ZeebeAssertHelper;
import io.camunda.zeebe.qa.util.cluster.TestStandaloneBroker;
import io.camunda.zeebe.qa.util.junit.ZeebeIntegration;
import io.camunda.zeebe.qa.util.junit.ZeebeIntegration.TestZeebe;
import io.camunda.zeebe.test.util.junit.AutoCloseResources;
import io.camunda.zeebe.test.util.junit.AutoCloseResources.AutoCloseResource;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@ZeebeIntegration
@AutoCloseResources
class AssignGroupToTenantTest {

  @TestZeebe
  private final TestStandaloneBroker zeebe = new TestStandaloneBroker().withRecordingExporter(true);

  @AutoCloseResource private ZeebeClient client;

  private long tenantKey;
  private long groupKey;

  @BeforeEach
  void initClientAndInstances() {
    client = zeebe.newClientBuilder().defaultRequestTimeout(Duration.ofSeconds(15)).build();
    tenantKey =
        client
            .newCreateTenantCommand()
            .tenantId("tenant-id")
            .name("Tenant Name")
            .send()
            .join()
            .getTenantKey();

    groupKey = client.newCreateGroupCommand().name("group").send().join().getGroupKey();
  }

  @Test
  void shouldAssignGroupToTenant() {
    // when
    client.newAssignGroupToTenantCommand(tenantKey).groupKey(groupKey).send().join();

    // then
    ZeebeAssertHelper.assertGroupAssignedToTenant(
        tenantKey,
        tenant -> {
          assertThat(tenant.getTenantKey()).isEqualTo(tenantKey);
          assertThat(tenant.getEntityKey()).isEqualTo(groupKey);
        });
  }

  @Test
  void shouldRejectIfTenantDoesNotExist() {
    // given
    final long nonExistentTenantKey = 999999L;

    // when / then
    assertThatThrownBy(
            () ->
                client
                    .newAssignGroupToTenantCommand(nonExistentTenantKey)
                    .groupKey(groupKey)
                    .send()
                    .join())
        .isInstanceOf(ProblemException.class)
        .hasMessageContaining("Failed with code 404: 'Not Found'")
        .hasMessageContaining(
            "Expected to add entity to tenant with key '%d', but no tenant with this key exists."
                .formatted(nonExistentTenantKey));
  }

  @Test
  void shouldRejectIfGroupDoesNotExist() {
    // given
    final long nonExistentGroupKey = 888888L;

    // when / then
    assertThatThrownBy(
            () ->
                client
                    .newAssignGroupToTenantCommand(tenantKey)
                    .groupKey(nonExistentGroupKey)
                    .send()
                    .join())
        .isInstanceOf(ProblemException.class)
        .hasMessageContaining("Failed with code 404: 'Not Found'")
        .hasMessageContaining(
            "Expected to add entity with key '%d' to tenant with key '%d', but the entity doesn't exist."
                .formatted(nonExistentGroupKey, tenantKey));
  }

  @Test
  void shouldRejectIfAlreadyAssigned() {
    // given
    client.newAssignGroupToTenantCommand(tenantKey).groupKey(groupKey).send().join();

    // when / then
    assertThatThrownBy(
            () -> client.newAssignGroupToTenantCommand(tenantKey).groupKey(groupKey).send().join())
        .isInstanceOf(ProblemException.class)
        .hasMessageContaining("Failed with code 400: 'Bad Request'")
        .hasMessageContaining(
            "Expected to add entity with key '%d' to tenant with key '%d', but the entity is already assigned to the tenant."
                .formatted(groupKey, tenantKey));
  }
}
