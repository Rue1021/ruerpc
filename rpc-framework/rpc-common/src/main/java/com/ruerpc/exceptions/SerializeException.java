package com.ruerpc.exceptions;

/**
 * @author Rue
 * @date 2025/5/22 19:50
 */
public class SerializeException extends RuntimeException {
  public SerializeException() {
  }

  public SerializeException(String message) {
    super(message);
  }

  public SerializeException(Throwable cause) {
    super(cause);
  }
}
