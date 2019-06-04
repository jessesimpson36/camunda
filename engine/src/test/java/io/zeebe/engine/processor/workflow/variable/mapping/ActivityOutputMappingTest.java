/*
 * Zeebe Workflow Engine
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.zeebe.engine.processor.workflow.variable.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.zeebe.engine.util.EngineRule;
import io.zeebe.exporter.api.record.Record;
import io.zeebe.exporter.api.record.value.WorkflowInstanceRecordValue;
import io.zeebe.model.bpmn.Bpmn;
import io.zeebe.model.bpmn.builder.SubProcessBuilder;
import io.zeebe.model.bpmn.builder.ZeebeVariablesMappingBuilder;
import io.zeebe.protocol.intent.WorkflowInstanceIntent;
import io.zeebe.test.util.MsgPackUtil;
import io.zeebe.test.util.record.RecordingExporter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.agrona.DirectBuffer;
import org.assertj.core.groups.Tuple;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ActivityOutputMappingTest {

  private static final String PROCESS_ID = "process";

  @ClassRule public static final EngineRule ENGINE_RULE = new EngineRule();

  @Parameter(0)
  public String initialVariables;

  @Parameter(1)
  public Consumer<SubProcessBuilder> mappings;

  @Parameter(2)
  public List<Tuple> expectedScopeVariables;

  @Parameters(name = "from {0} to {2}")
  public static Object[][] parameters() {
    return new Object[][] {
      {"{'x': 1}", mapping(b -> b.zeebeOutput("x", "y")), scopeVariables(tuple("y", "1"))},
      {"{'x': 1, 'y': 2}", mapping(b -> b.zeebeOutput("y", "z")), scopeVariables(tuple("z", "2"))},
      {
        "{'x': 1}",
        mapping(b -> b.zeebeInput("x", "y").zeebeOutput("y", "z")),
        scopeVariables(tuple("z", "1"))
      },
      {
        "{'x': 1}",
        mapping(b -> b.zeebeInput("x", "y").zeebeOutput("x", "z")),
        scopeVariables(tuple("z", "1"))
      },
      {
        "{'x': {'y': 2}}",
        mapping(b -> b.zeebeOutput("x", "z")),
        scopeVariables(tuple("z", "{\"y\":2}"))
      },
      {"{'x': {'y': 2}}", mapping(b -> b.zeebeOutput("x.y", "z")), scopeVariables(tuple("z", "2"))},
    };
  }

  @Test
  public void shouldApplyOutputMappings() {
    // given
    final String jobType = UUID.randomUUID().toString();

    final long workflowKey =
        ENGINE_RULE
            .deploy(
                Bpmn.createExecutableProcess(PROCESS_ID)
                    .startEvent()
                    .subProcess(
                        "sub",
                        b -> {
                          b.embeddedSubProcess()
                              .startEvent()
                              .serviceTask("task", t -> t.zeebeTaskType(jobType))
                              .endEvent();

                          mappings.accept(b);
                        })
                    .endEvent()
                    .done())
            .getValue()
            .getDeployedWorkflows()
            .get(0)
            .getWorkflowKey();

    // when
    final DirectBuffer variables = MsgPackUtil.asMsgPack(initialVariables);
    final long workflowInstanceKey =
        ENGINE_RULE
            .createWorkflowInstance(r -> r.setKey(workflowKey).setVariables(variables))
            .getValue()
            .getInstanceKey();

    ENGINE_RULE.job().ofInstance(workflowInstanceKey).withType(jobType).complete();

    // then
    final Record<WorkflowInstanceRecordValue> taskCompleted =
        RecordingExporter.workflowInstanceRecords(WorkflowInstanceIntent.ELEMENT_COMPLETED)
            .withWorkflowInstanceKey(workflowInstanceKey)
            .withElementId("task")
            .getFirst();

    assertThat(
            RecordingExporter.variableRecords()
                .withWorkflowInstanceKey(workflowInstanceKey)
                .skipUntil(r -> r.getPosition() > taskCompleted.getPosition())
                .withScopeKey(workflowInstanceKey)
                .limit(expectedScopeVariables.size()))
        .extracting(Record::getValue)
        .extracting(v -> tuple(v.getName(), v.getValue()))
        .hasSize(expectedScopeVariables.size())
        .containsAll(expectedScopeVariables);
  }

  private static Consumer<ZeebeVariablesMappingBuilder<SubProcessBuilder>> mapping(
      Consumer<ZeebeVariablesMappingBuilder<SubProcessBuilder>> mappingBuilder) {
    return mappingBuilder;
  }

  private static List<Tuple> scopeVariables(Tuple... variables) {
    return Arrays.asList(variables);
  }
}
