package com.ruerpc.exceptions;

/**
 * @author Rue
 * @date 2025/5/22 19:50
 */
public class ResponseException extends RuntimeException {

  private byte code;
  private String msg;

  public ResponseException(byte code, String msg) {
    super(msg);
    this.code = code;
    this.msg = msg;
  }
}
