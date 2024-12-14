package com.papa.app;
import java.io.IOException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginServlet {

	private DBConnection DBConnection = new DBConnection();
	private ObjectMapper om = new ObjectMapper();

    @PostMapping
    public String loginUser(@RequestBody User user) throws IOException {
        
    	ObjectNode preparedResponse = om.createObjectNode();
    	
		String username = user.getUsername();
		String password = user.getPassword();
    	
    	if(!DBConnection.authenticateUser(username, null)) {
			System.out.println("User " + username + " does not exist");
			preparedResponse.put("verified", false);
			preparedResponse.put("message", "User does not exist.");
		} else if(!DBConnection.authenticateUser(username, password)) {
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
