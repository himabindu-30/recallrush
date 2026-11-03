package com.hima.recallrush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * Running this class makes Spring Boot:
 *   1. Start the embedded Tomcat server (port 8080)
 *   2. Scan for and create the controller, service and repository beans
 *   3. Connect to the database and create the tables
 */
@SpringBootApplication // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class RecallRushApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecallRushApplication.class, args);
    }
}
