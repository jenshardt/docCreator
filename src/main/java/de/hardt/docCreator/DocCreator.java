package de.hardt.docCreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DocCreator {

    public static void main(String[] args) {
        SpringApplication.run(DocCreator.class, args);
    }
}
