package com.melouk.personal.acceptance_tests;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthEndpointStepsDefinition {


    @Autowired
    private RestTemplate restTemplate;

    private ResponseEntity<Map> healthResponse;

    @When("I query application health endpoint")
    public void queryHealthEndpoint() {
        healthResponse = restTemplate.getForEntity("http://localhost:8080/actuator/health", Map.class);
    }

    @Then("Status is up")
    public void statusIsUp() {
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(healthResponse.getBody()).isEqualTo(Map.of("status","UP"));
    }
}
