package com.rddk.mdingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point that bootstraps the Spring Boot application and the embedded web server.
 *
 * @author richard.dudek
 * @since 0.0.1
 */
@SpringBootApplication
public class MdIngestionApplication {

    /**
     * Starts the Spring application context.
     *
     * @param args command-line arguments passed to the Spring runtime
     */
    public static void main(String[] args) {
        SpringApplication.run(MdIngestionApplication.class, args);
    }
}
