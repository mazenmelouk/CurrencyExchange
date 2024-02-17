package com.melouk.personal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("dev")
class CurrencyExchangeApplicationIT{
    @Test
    void contextLoads() {
        /*
        * A basic empty test that validates the spring context is initialized without errors ie application can start
        */
    }
}