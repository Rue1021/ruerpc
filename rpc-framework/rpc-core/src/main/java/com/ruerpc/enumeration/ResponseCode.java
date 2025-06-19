package com.ruerpc.enumeration;

/**
 * @author Rue
 * @date 2025/6/4 16:11
 *
 * 响应码类型
 * 成功码  20 (方法成功调用)  21 (心跳成功返回)
 * 负载码  31 (服务器负载过高，被限流)
 * 错误码 客户端错误  44
 * 错误码 服务端错误  50 (请求的方法不存在)
 */
public enum ResponseCode {

    SUCCESS((byte)20, "OK"),
    SUCCESS_HEARTBEAT((byte)21, "heartbeat OK"),
    RATE_LIMIT((byte)31, "service rate limited"),
    RESOURCE_NOT_FOUND((byte)44, "resource not exist"),
    FAILED((byte)50, "method call failed");

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
