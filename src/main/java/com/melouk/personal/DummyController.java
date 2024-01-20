package com.melouk.personal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
