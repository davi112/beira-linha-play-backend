package br.icei.beiralinhaplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class BeiraLinhaPlayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeiraLinhaPlayApplication.class, args);
    }
}
