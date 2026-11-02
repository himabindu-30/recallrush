package com.hima.recallrush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MAIN CLASS = project start button.
 *
 * Idi run chesthe Spring Boot:
 *   1. Tomcat server start chestundi (port 8080)
 *   2. @RestController, @Service, @Repository classes ni vethiki objects create chestundi
 *   3. Database tho connect avthundi, tables create chestundi
 */
@SpringBootApplication // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class RecallRushApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecallRushApplication.class, args);
    }
}
