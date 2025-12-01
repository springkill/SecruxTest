package com.secrux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entrypoint that exposes every original scenario via REST controllers.
 */
@SpringBootApplication
public class SecruxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecruxApplication.class, args);
    }
}
