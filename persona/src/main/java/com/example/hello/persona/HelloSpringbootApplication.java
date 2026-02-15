package com.example.hello.persona;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.example.hello.persona",
    "com.example.hello.common",
    "com.example.hello.region.mx",
    "com.example.hello.region.chl",
    "com.example.hello.region.canada",
    "com.example.hello.region.us"
})
@EntityScan(basePackages = "com.mp.flashpicks.common.entity")
@EnableJpaRepositories(basePackages = "com.mp.flashpicks.common.repository")
public class HelloSpringbootApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloSpringbootApplication.class, args);
    }
}
