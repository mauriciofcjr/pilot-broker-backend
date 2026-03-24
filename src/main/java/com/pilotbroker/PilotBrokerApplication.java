package com.pilotbroker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PilotBrokerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PilotBrokerApplication.class, args);
    }
}
