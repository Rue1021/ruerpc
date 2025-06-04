package com.ruerpc.enumeration;

/**
 * @author Rue
 * @date 2025/6/4 16:11
 */
public enum ResponseCode {

    SUCCESS((byte)1, "OK"), FAILED((byte)2, "failed");

    private byte code;
    private String description;

    ResponseCode(byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public byte getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
