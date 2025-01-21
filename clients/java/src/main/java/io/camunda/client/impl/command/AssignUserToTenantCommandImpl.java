/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.camunda.client.impl.command;

import io.camunda.client.api.CamundaFuture;
import io.camunda.client.api.command.AssignUserToTenantCommandStep1;
import io.camunda.client.api.command.FinalCommandStep;
import io.camunda.client.api.response.AssignUserToTenantResponse;
import io.camunda.client.impl.http.HttpCamundaFuture;
import io.camunda.client.impl.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.config.RequestConfig;

public final class AssignUserToTenantCommandImpl implements AssignUserToTenantCommandStep1 {

  private final String tenantId;
  private String userName;
  private final HttpClient httpClient;
  private final RequestConfig.Builder httpRequestConfig;

  public AssignUserToTenantCommandImpl(final HttpClient httpClient, final String tenantId) {
    this.httpClient = httpClient;
    this.tenantId = tenantId;
    httpRequestConfig = httpClient.newRequestConfig();
  }

  @Override
  public AssignUserToTenantCommandStep1 userName(final String userName) {
    this.userName = userName;
    return this;
  }

  @Override
  public FinalCommandStep<AssignUserToTenantResponse> requestTimeout(
      final Duration requestTimeout) {
    httpRequestConfig.setResponseTimeout(requestTimeout.toMillis(), TimeUnit.MILLISECONDS);
    return this;
  }

  @Override
  public CamundaFuture<AssignUserToTenantResponse> send() {
    final HttpCamundaFuture<AssignUserToTenantResponse> result = new HttpCamundaFuture<>();
    final String endpoint = String.format("/tenants/%s/users/%s", tenantId, userName);
    httpClient.put(endpoint, null, httpRequestConfig.build(), result);
    return result;
  }
}
