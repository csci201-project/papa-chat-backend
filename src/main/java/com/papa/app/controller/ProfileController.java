package com.papa.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.papa.app.DBConnection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final DBConnection dbConnection = new DBConnection();

    @GetMapping("/{username}/history")
    public ResponseEntity<?> getChatHistory(@PathVariable String username) {
        List<String> history = dbConnection.getChatHistory(username);
        if (history != null) {
            return ResponseEntity.ok(history);
        }
        return ResponseEntity.notFound().build();
    }
}