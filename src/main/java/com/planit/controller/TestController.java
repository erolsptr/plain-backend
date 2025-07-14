package com.planit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Bu class'ın bir REST controller olduğunu belirtir.
public class TestController {

    @GetMapping("/api/hello") // http://localhost:8080/api/hello adresine gelen GET isteklerini karşılar.
    public String sayHello() {
        return "Merhaba, Spring Boot!";
    }
}