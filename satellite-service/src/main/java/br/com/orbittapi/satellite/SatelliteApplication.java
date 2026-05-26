package br.com.orbittapi.satellite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SatelliteApplication {

    public static void main(String[] args) {
        SpringApplication.run(SatelliteApplication.class, args);
    }
}
