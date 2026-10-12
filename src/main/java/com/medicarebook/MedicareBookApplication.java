package com.medicarebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MedicareBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedicareBookApplication.class, args);
    }
}
