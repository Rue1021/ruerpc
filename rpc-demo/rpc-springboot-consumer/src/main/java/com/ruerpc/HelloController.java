package com.ruerpc;

import com.ruerpc.annotation.RueRPCService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rue
 * @date 2025/6/19 19:35
 */
@RestController
public class HelloController {

    @RueRPCService
    private Hello hello;

    @GetMapping("/hello")
    public String hello() {
        //return hello.sayHi(">>>>>>>>>provider<<<<<<<<<<<");
        return null;
    }

}
