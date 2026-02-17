package com.aioversms.queryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class QueryServiceApplication {

	public static void main(String[] args) {
		// 1. Load the .env file
        // "ignoreIfMissing" ensures it doesn't crash in Docker (where we use real env vars)
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
		// 2. Feed the variables into Java's System Properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
		SpringApplication.run(QueryServiceApplication.class, args);
	}

}
