package org.vinhpham.qrcheckinapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QrCheckinApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QrCheckinApiApplication.class, args);
    }

}
