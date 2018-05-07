package org.camunda.optimize.service.es.schema.type;

import org.camunda.optimize.service.es.schema.StrictTypeMappingCreator;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ProcessDefinitionType extends StrictTypeMappingCreator {

  public static final String PROCESS_DEFINITION_ID = "id";
  public static final String DEFINITION_KEY = "key";
  public static final String ENGINE = "engine";
  public static final String VERSION = "version";

  @Override
  public String getType() {
    return configurationService.getProcessDefinitionType();
  }

  @Override
  protected XContentBuilder addProperties(XContentBuilder xContentBuilder) throws IOException {
    return xContentBuilder
      .startObject(PROCESS_DEFINITION_ID)
        .field("type", "keyword")
      .endObject()
      .startObject(DEFINITION_KEY)
        .field("type", "keyword")
      .endObject()
      .startObject(VERSION)
        .field("type", "long")
      .endObject()
      .startObject(ENGINE)
        .field("type", "keyword")
      .endObject()
      .startObject("name")
        .field("type", "keyword")
      .endObject();
  }

}
