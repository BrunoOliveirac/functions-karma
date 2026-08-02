package com.crm.karma.configs;

import com.crm.karma.utils.HtmlSanitizer;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/** Globally strips HTML from every JSON string bound into request bodies. */
@JacksonComponent
public class SanitizeHtmlDeserializer extends ValueDeserializer<String> {

  @Override
  public String deserialize(JsonParser parser, DeserializationContext context)
    throws JacksonException {
    return HtmlSanitizer.sanitize(parser.getValueAsString());
  }
}
