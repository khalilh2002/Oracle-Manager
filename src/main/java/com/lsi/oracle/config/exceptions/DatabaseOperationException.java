package com.lsi.oracle.config.exceptions;

// Custom Exception
public class DatabaseOperationException extends RuntimeException {
  public DatabaseOperationException(String message, Throwable cause) {
    super(message, cause);
  }
}
