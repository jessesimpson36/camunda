/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
package io.camunda.operate.schema.templates;

import org.springframework.stereotype.Component;

@Component
public class ListViewTemplate extends AbstractTemplateDescriptor {

  public static final String INDEX_NAME = "list-view";

  public static final String ID = "id";
  public static final String KEY = "key";
  public static final String PROCESS_INSTANCE_KEY = "processInstanceKey";
  public static final String BPMN_PROCESS_ID = "bpmnProcessId";
  public static final String PROCESS_VERSION = "processVersion";
  public static final String PROCESS_KEY = "processDefinitionKey";
  public static final String PROCESS_NAME = "processName";
  public static final String START_DATE = "startDate";
  public static final String END_DATE = "endDate";
  public static final String STATE = "state";
  public static final String PARENT_PROCESS_INSTANCE_ID = "parentProcessInstanceId";
  public static final String PARENT_FLOW_NODE_INSTANCE_KEY = "parentFlowNodeInstanceId";

  public static final String ACTIVITY_ID = "activityId";
  public static final String ACTIVITY_STATE = "activityState";
  public static final String ACTIVITY_TYPE = "activityType";

  public static final String INCIDENT_KEY = "incidentKey";
  public static final String INCIDENT_JOB_KEY = "incidentJobKey";
  public static final String ERROR_MSG = "errorMessage";

  public static final String VAR_NAME = "varName";
  public static final String VAR_VALUE = "varValue";
  public static final String SCOPE_KEY = "scopeKey";

  public static final String BATCH_OPERATION_IDS = "batchOperationIds";

  public static final String JOIN_RELATION = "joinRelation";
  public static final String PROCESS_INSTANCE_JOIN_RELATION = "processInstance";
  public static final String ACTIVITIES_JOIN_RELATION = "activity";
  public static final String VARIABLES_JOIN_RELATION = "variable";

  @Override
  public String getIndexName() {
    return INDEX_NAME;
  }

  @Override
  public String getVersion() {
     return "1.1.0";
  }

}
