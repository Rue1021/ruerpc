package com.ruerpc.enumeration;

import lombok.AllArgsConstructor;

/**
 * @author Rue
 * @date 2025/6/4 15:38
 *
 * 标记请求类型
 */
public enum RequestType {
    REQUEST((byte)1, "normal request"), HEART_BEAT((byte)2, "heart beat request");

    private byte id;
    private String type;

    RequestType(byte id, String type) {
        this.id = id;
        this.type = type;
    }

    public byte getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
