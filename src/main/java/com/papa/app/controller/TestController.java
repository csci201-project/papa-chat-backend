package com.papa.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello/{name}")
    public String sayHello(@PathVariable String name) {
        return "Hello, " + name + "!";
    }
}
