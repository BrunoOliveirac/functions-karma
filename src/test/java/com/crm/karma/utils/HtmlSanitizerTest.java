package com.crm.karma.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HtmlSanitizerTest {

  @Test
  void stripsCommonHtmlTags() {
    assertEquals("hello", HtmlSanitizer.sanitize("<b>hello</b>"));
    assertEquals("a bold word", HtmlSanitizer.sanitize("a <strong>bold</strong> word"));
    assertEquals("linebreak", HtmlSanitizer.sanitize("line<br/>break"));
    assertEquals("", HtmlSanitizer.sanitize("<img src=x onerror=alert(1)>"));
  }

  @Test
  void preservesPlainTextAndNull() {
    assertNull(HtmlSanitizer.sanitize(null));
    assertEquals("Tom & Jerry", HtmlSanitizer.sanitize("Tom & Jerry"));
    assertEquals("safe  text", HtmlSanitizer.sanitize("safe <!-- comment --> text"));
  }
}
