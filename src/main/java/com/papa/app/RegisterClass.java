package com.papa.app;

import java.io.IOException;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/registerClass")
public class RegisterClass {

    private DBConnection DBConnection = new DBConnection();

    @PostMapping
    public void registerClass(@RequestBody String topic) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String className = objectMapper.readTree(topic).get("newTopic").asText();
        System.out.println("Added: " + className);
        DBConnection.registerClass(className);
        DBConnection.enrollUser("admin", className, "admin");
        return;
    }
}