package com.ruerpc.exceptions;

/**
 * @author Rue
 * @date 2025/5/22 19:50
 */
public class DiscoveryException extends RuntimeException {
  public DiscoveryException() {
  }

  public DiscoveryException(String message) {
    super(message);
  }

  public DiscoveryException(Throwable cause) {
    super(cause);
  }
}
