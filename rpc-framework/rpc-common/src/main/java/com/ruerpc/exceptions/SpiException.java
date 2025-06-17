package com.ruerpc.exceptions;

/**
 * @author Rue
 * @date 2025/5/22 19:50
 */
public class SpiException extends RuntimeException {
  public SpiException() {
  }

  public SpiException(String message, Throwable cause) {
    super(message, cause);
  }

  public SpiException(String message) {
    super(message);
  }

  public SpiException(Throwable cause) {
    super(cause);
  }
}
