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
package io.camunda.client.impl;

import io.camunda.client.CredentialsProvider.StatusCode;
import org.apache.hc.core5.http.HttpStatus;

public final class HttpStatusCode implements StatusCode {

  private final int code;

  public HttpStatusCode(final int code) {
    this.code = code;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public boolean isUnauthorized() {
    return code == HttpStatus.SC_UNAUTHORIZED;
  }
}
