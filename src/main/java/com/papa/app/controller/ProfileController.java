package com.papa.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.papa.app.DBConnection;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final DBConnection dbConnection = new DBConnection();

    @GetMapping("/{username}/history")
    public ResponseEntity<?> getChatHistory(@PathVariable String username) {
        System.out.println("Fetching chat history for user: " + username); // Debug log
        List<String> history = dbConnection.getChatHistory(username);
        if (history != null) {
            System.out.println("Found " + history.size() + " messages"); // Debug log
            return ResponseEntity.ok(history);
        }
        System.out.println("No history found for user: " + username); // Debug log
        return ResponseEntity.notFound().build();
    }
}