package com.crm.karma.utils;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

public class NormalizerImage {

  private static final int MAX_AVATAR_LENGTH = 200_000;

  private static final Pattern AVATAR_DATA_URL = Pattern.compile(
      "^data:image/(png|jpe?g|webp);base64,[A-Za-z0-9+/=\\r\\n]+$"
  );

  public static String normalizeImage(String image) {
    if (!StringUtils.hasText(image)) return null;

    String trimmed = image.trim();

    if (trimmed.length() > MAX_AVATAR_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is too large");
    }

    if (!AVATAR_DATA_URL.matcher(trimmed).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image format");
    }

    return trimmed.replaceAll("\\s", "");
  }
}

