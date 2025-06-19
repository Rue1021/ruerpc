package com.ruerpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rue
 * @date 2025/6/19 13:11
 */
@SpringBootApplication
@RestController
public class ApplicationProvider {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationProvider.class, args);
    }

    @GetMapping("/test")
    public String hello() {
        return "hello provider from springboot";
    }
}
