package com.crm.karma.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;

/**
 * Strips all HTML from user-provided text. No HTML markup is accepted anywhere in the API.
 */
public final class HtmlSanitizer {

  private static final Document.OutputSettings OUTPUT_SETTINGS =
    new Document.OutputSettings().prettyPrint(false);

  private HtmlSanitizer() {
  }

  /**
   * Removes every HTML tag / comment from {@code input}, leaving plain text.
   * Returns {@code null} when {@code input} is null.
   */
  public static String sanitize(String input) {
    if (input == null) return null;
    

    String cleaned = Jsoup.clean(input, "", Safelist.none(), OUTPUT_SETTINGS);
    return Parser.unescapeEntities(cleaned, false);
  }
}
