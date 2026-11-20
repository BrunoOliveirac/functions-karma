package com.crm.karma.configs;

import com.crm.karma.utils.HtmlSanitizer;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;

/**
 * Strips HTML from query / form string bindings (e.g. paginated search {@code query}).
 */
@ControllerAdvice
public class SanitizeHtmlAdvice {

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
      @Override
      public void setAsText(String text) {
        setValue(HtmlSanitizer.sanitize(text));
      }
    });
  }
}
