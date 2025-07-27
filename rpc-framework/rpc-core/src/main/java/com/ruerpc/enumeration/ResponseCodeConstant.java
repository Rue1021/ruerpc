package com.ruerpc.enumeration;

/**
 * @author Rue
 * @date 2025/7/27 09:24
 * <p>
 * RPC 响应码常量 - 专为switch-case优化设计
 */
public final class ResponseCodeConstant {

    private ResponseCodeConstant() {
    } // 防止实例化

    // 成功类响应码
    public static final byte SUCCESS = 20;
    public static final byte SUCCESS_HEARTBEAT = 21;

    // 错误类响应码
    public static final byte RATE_LIMIT = 31;
    public static final byte RESOURCE_NOT_FOUND = 44;
    public static final byte FAILED = 50;
    public static final byte CLOSING = 51;

    /**
     * 获取响应码描述信息
     */
    public static String getDescription(byte code) {
        switch (code) {
            case SUCCESS:
                return "OK";
            case SUCCESS_HEARTBEAT:
                return "heartbeat OK";
            case RATE_LIMIT:
                return "service rate limited";
            case RESOURCE_NOT_FOUND:
                return "resource not exist";
            case FAILED:
                return "method call failed";
            case CLOSING:
                return "service provider is closing";
            default:
                return "unknown response code";
        }
    }

    /**
     * 检查是否为成功码
     */
    public static boolean isSuccess(byte code) {
        return code == SUCCESS || code == SUCCESS_HEARTBEAT;
    }

    /**
     * 检查是否需要重试
     */
    public static boolean shouldRetry(byte code) {
        switch (code) {
            case RATE_LIMIT:   // 限流可以稍后重试
            case CLOSING:      // 服务关闭可能是临时状态
                return true;
            default:
                return false;
        }
    }

}
