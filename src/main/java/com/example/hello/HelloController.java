package com.example.hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller-center")
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/")
    public String hello() {
        log.info("GET /seller-center/ called");
        return "Hello, Spring Boot!";
    }

    @GetMapping("/hello2")
    public String hello2() {
        log.info("GET /seller-center/hello2 called");
        return "Hello 2nd";
    }

    @GetMapping("/hello3")
    public String hello3() {
        log.info("GET /seller-center/hello3 called");
        return "Hello 3rd";
    }

    @GetMapping("/hello4")
    public String hello4() {
        log.info("GET /seller-center/hello4 called");
        return "Hello 4th";
    }

    @GetMapping("/hello5")
    public String hello5() {
        log.info("GET /seller-center/hello5 called");
        return "Hello 5th";
    }
}
