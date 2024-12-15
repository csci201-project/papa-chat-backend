package com.papa.app;
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
@RequestMapping("/registerVerification")
public class RegistrationController {

	private DBConnection DBConnection = new DBConnection();
	private ObjectMapper om = new ObjectMapper();

    @PostMapping
    public String registerUser(@RequestBody User user) throws IOException {
        
    	//User user = om.fromJson(request.getReader(), User.class);
    	ObjectNode preparedResponse = om.createObjectNode();
    	
    	int success = DBConnection.registerUser(user.getUsername(), user.getPassword());
    	if(success == 1) {
    		// User already exists
    		System.out.println("User " + user.getUsername() + " already exists");
            preparedResponse.put("verified", false);
            preparedResponse.put("message", "This username is already taken.");
    	} else if (success == 0) {
    		// User registered successfully
            System.out.println("User " + user.getUsername() + ", with password: " + user.getPassword() + " registered successfully");
            preparedResponse.put("verified", true);
            preparedResponse.put("message", "Registration successful.");
        } else {
        	// User registration failed
            System.out.println("Registration failed");
            preparedResponse.put("verified", false);
            preparedResponse.put("message", "Registration unsuccessful.");
        }
       
        //DBConnection.closeDBConnection();
        return preparedResponse.toString();
    }
}