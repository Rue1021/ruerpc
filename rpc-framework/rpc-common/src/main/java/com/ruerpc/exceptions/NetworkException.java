package com.ruerpc.exceptions;

/**
 * @author Rue
 * @date 2025/5/22 17:35
 */
public class NetworkException extends RuntimeException {
    public NetworkException(String message) {
        super(message);
    }

    public NetworkException() {
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
}
