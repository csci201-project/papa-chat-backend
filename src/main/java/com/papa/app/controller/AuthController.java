package com.papa.app.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.papa.app.User;
import com.papa.app.service.DatabaseService;

@RestController
@RequestMapping("/login")
public class AuthController {
    private final DatabaseService databaseService;

    public AuthController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @PostMapping
    public String loginUser(@RequestBody User user) throws IOException {
    	ObjectMapper om = new ObjectMapper();
    	ObjectNode preparedResponse = om.createObjectNode();

		String username = user.getUsername();
		String password = user.getPassword();

    	if(!this.databaseService.getConnection().authenticateUser(username, null)) {
			System.out.println("User " + username + " does not exist");
			preparedResponse.put("verified", false);
			preparedResponse.put("message", "User does not exist.");
		} else if(!this.databaseService.getConnection().authenticateUser(username, password)) {
			System.out.println("User: " + username + " exists, but password: " + password + " is incorrect");
			preparedResponse.put("verified", false);
			preparedResponse.put("message", "Password is incorrect.");
		} else {
			System.out.println("User " + username + ", with password: " + password + " Found");
			preparedResponse.put("verified", true);
			preparedResponse.put("message", "Login successful.");
		}

        //DBConnection.closeDBConnection();
        return preparedResponse.toString();
    }
}
