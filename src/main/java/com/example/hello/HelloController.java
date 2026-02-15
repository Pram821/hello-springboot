package com.example.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello, Spring Boot!";
    }

    @GetMapping("/hello2")
    public String hello2() {
        return "Hello 2nd";
    }

    @GetMapping("/hello3")
    public String hello3() {
        return "Hello 3rd";
    }

    @GetMapping("/hello4")
    public String hello4() {
        return "Hello 4th";
    }

    @GetMapping("/hello5")
    public String hello5() {
        return "Hello 5th";
    }
}
