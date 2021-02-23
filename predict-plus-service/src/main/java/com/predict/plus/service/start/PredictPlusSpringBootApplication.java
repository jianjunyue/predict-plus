package com.predict.plus.service.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
 
@ComponentScan(basePackages = "com.predict.plus.service")
@SpringBootApplication
public class PredictPlusSpringBootApplication {

	public static void main(String[] args) {
 
	        SpringApplication.run(PredictPlusSpringBootApplication.class, args);

	}

}
