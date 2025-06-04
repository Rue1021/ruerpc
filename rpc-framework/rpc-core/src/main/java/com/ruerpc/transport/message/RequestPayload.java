package com.ruerpc.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Rue
 * @date 2025/5/28 14:43
 * 用来描述调用方请求的接口方法的描述
 * hello.sayHi("ruerpc");
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestPayload implements Serializable {

    //接口的名字 -> com.ruerpc.Hello
    private String interfaceName;

    //方法名字 -> sayHi
    private String methodName;

    //参数列表 -> 参数类型(用来确定重载方法) 和具体的参数(用来执行方法调用)
    private Class<?>[] parametersType;  // -> java.long.String
    private Object[] parametersValue;   // -> "ruerpc"

    //返回值的封装  -> java.long.String
    private Class<?> returnType;

}
