package com.ruerpc.exceptions;

/**
 * @author Rue
 * @date 2025/5/22 19:50
 */
public class LoadBalanceException extends RuntimeException {
  public LoadBalanceException() {
  }

  public LoadBalanceException(String message) {
    super(message);
  }

  public LoadBalanceException(Throwable cause) {
    super(cause);
  }
}
