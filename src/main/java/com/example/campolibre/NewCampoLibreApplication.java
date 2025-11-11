package com.example.campolibre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NewCampoLibreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewCampoLibreApplication.class, args);
    }

}