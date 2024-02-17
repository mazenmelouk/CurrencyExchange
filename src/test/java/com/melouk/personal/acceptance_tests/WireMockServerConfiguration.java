package com.melouk.personal.acceptance_tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Configuration
public class WireMockServerConfiguration {
    @Bean
    @SuppressWarnings("unused")
    public WireMockServer wireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(options().bindAddress("localhost").port(8081));
        wireMockServer.start();
        return wireMockServer;
    }
}
