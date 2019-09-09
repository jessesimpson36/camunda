/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package org.camunda.optimize.service.engine.importing.fetcher.instance;

import org.camunda.optimize.rest.engine.EngineContext;
import org.camunda.optimize.service.engine.importing.index.page.TimestampBasedImportPage;
import org.camunda.optimize.service.util.BackoffCalculator;
import org.camunda.optimize.service.util.OptimizeDateTimeFormatterFactory;
import org.camunda.optimize.service.util.configuration.ConfigurationService;
import org.camunda.optimize.service.util.configuration.ConfigurationServiceBuilder;
import org.camunda.optimize.service.util.configuration.EngineConstantsUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DecisionInstanceFetcherTest {

  private ConfigurationService configurationService = ConfigurationServiceBuilder.createDefaultConfiguration();

  @Mock
  private EngineContext engineContext;
  @Mock
  private BackoffCalculator backoffCalculator;
  @Mock(answer = Answers.RETURNS_SELF)
  private Client engineClient;
  @Mock(answer = Answers.RETURNS_SELF)
  private WebTarget target;
  @Mock(answer = Answers.RETURNS_SELF)
  private Invocation.Builder requestBuilder;

  private DecisionInstanceFetcher underTest;

  @Before
  public void before() {
    underTest = new DecisionInstanceFetcher(engineContext);
    underTest.setBackoffCalculator(backoffCalculator);
    underTest.setConfigurationService(configurationService);
    underTest.setDateTimeFormatter(new OptimizeDateTimeFormatterFactory().getObject());

    when(engineContext.getEngineClient()).thenReturn(engineClient);
    when(engineContext.getEngineAlias())
      .thenReturn(configurationService.getConfiguredEngines().keySet().iterator().next());
    when(engineClient.target(anyString())).thenReturn(target);
    when(target.request(anyString())).thenReturn(requestBuilder);
    when(requestBuilder.get(any(GenericType.class))).thenReturn(new ArrayList<>());
  }

  @Test
  public void testFetchHistoricInstancePageUsesMaxPageSize() {
    final int maxPageSize = 2233;
    configurationService.setEngineImportDecisionInstanceMaxPageSize(maxPageSize);

    underTest.fetchHistoricDecisionInstances(new TimestampBasedImportPage());

    verify(target, times(1)).queryParam(eq(EngineConstantsUtil.MAX_RESULTS_TO_RETURN), eq((long) maxPageSize));
  }
}
