package com.apex.clear_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ClearEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClearEngineApplication.class, args);
	}

}
