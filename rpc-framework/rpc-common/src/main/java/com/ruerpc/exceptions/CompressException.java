package com.ruerpc.exceptions;

/**
 * @author Rue
 * @date 2025/5/22 19:50
 */
public class CompressException extends RuntimeException {
  public CompressException() {
  }

  public CompressException(String message) {
    super(message);
  }

  public CompressException(Throwable cause) {
    super(cause);
  }
}
