package org.example.javaweb_ss14_bai4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReserveApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReserveApplication.class, args);
    }
}
