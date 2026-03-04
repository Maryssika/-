package com.ovz.platform;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class PlatformApplication {

    @Value("${spring.datasource.h2.url}")
    private String h2Url;

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

    @PostConstruct
    public void init() {
        System.out.println("H2 URL from properties: " + h2Url);
    }
}