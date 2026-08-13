package ng.edu.futa.uclear;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class UclearApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(UclearApplication.class)
            .lazyInitialization(true)   // beans init on first use, not at startup
            .run(args);
    }
}
