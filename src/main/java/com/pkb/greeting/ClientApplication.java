package com.pkb.greeting;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

public class ClientApplication {
    public static void main(String[] args) {
        WebClient client = WebClient.create("http://localhost:8080");

        Flux<String> employeeFlux = client.get()
                .uri("/test")
                .retrieve()
                .bodyToFlux(String.class);

        System.out.println(employeeFlux.blockLast());
    }
}
