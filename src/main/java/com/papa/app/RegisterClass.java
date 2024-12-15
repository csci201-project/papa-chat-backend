package com.papa.app;
import java.io.IOException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registerClass")
public class RegisterClass {

	private DBConnection DBConnection = new DBConnection();

    @PostMapping
    public void registerClass(String topic) throws IOException {   
    	DBConnection.registerClass(topic);
    	DBConnection.enrollUser("admin",topic,"admin");
        return;
    }
}