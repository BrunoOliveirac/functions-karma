package com.crm.karma.configs;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Deserializes strings without HTML sanitization (e.g. avatar data URLs). */
public class PassthroughStringDeserializer extends ValueDeserializer<String> {

  @Override
  public String deserialize(JsonParser parser, DeserializationContext context)
    throws JacksonException {
    return parser.getValueAsString();
  }
}
