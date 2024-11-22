package com.papa.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class PapaChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(PapaChatApplication.class, args);
	}

}
