package com.thecodecompanyinc.social_media_service.exception;

public class DuplicateKeyException extends RuntimeException {
  public DuplicateKeyException(String message) {
    super(message);
  }
}
