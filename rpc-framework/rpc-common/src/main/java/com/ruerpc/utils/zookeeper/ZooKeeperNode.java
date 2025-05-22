package com.ruerpc.utils.zookeeper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Rue
 * @date 2025/5/22 15:49
 */
@Data //添加Getter Setter toString方法
@AllArgsConstructor //添加全参构造
@NoArgsConstructor //添加无参构造
public class ZooKeeperNode {
    private String nodePath;
    private byte[] data;
}
