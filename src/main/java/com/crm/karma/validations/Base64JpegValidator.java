package com.crm.karma.validations;

import java.util.Base64;

public class Base64JpegValidator {
  private static final String JPEG_PREFIX = "data:image/jpeg;base64,";

  private Base64JpegValidator() {
  }

  public static void decodeAndValidateJpeg(String rawBase64) {
    if (rawBase64 == null || rawBase64.isBlank()) {
      throw new IllegalArgumentException("Base64 image cannot be empty!");
    }

    String base64Data = rawBase64.trim();

    if (base64Data.startsWith("data:")) {
      if (!base64Data.startsWith(JPEG_PREFIX)) {
        throw new IllegalArgumentException("Only images in JEPG format are allowed!");
      }

      base64Data = base64Data.substring(JPEG_PREFIX.length());
    }

    byte[] decoded;
    try {
      decoded = Base64.getDecoder().decode(base64Data);
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid base64 string!", ex);
    }

    boolean isJpeg = decoded.length >= 3
        && (decoded[0] & 0xFF) == 0xFF
        && (decoded[1] & 0xFF) == 0xD8
        && (decoded[2] & 0xFF) == 0xFF;

    if (!isJpeg) {
      throw new IllegalArgumentException("The decoded content is not a valid JPEG!");
    }
  }
}
