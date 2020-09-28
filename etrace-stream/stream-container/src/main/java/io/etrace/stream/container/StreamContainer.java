package io.etrace.stream.container;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
@EnableScheduling
public class StreamContainer {
    public final static String HTTP_PORT = "stream.http.port";

    public static void main(String[] args) {
        SpringApplication.run(StreamContainer.class, args);
    }
}
