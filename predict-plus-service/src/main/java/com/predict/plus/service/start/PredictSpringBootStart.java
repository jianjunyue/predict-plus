package com.predict.plus.service.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.predict.plus.common.utils.ConfigResourceLoad;
 
@ComponentScan(basePackages = {"com.predict.plus.service","com.predict.plus.common"})
@SpringBootApplication
public class PredictSpringBootStart {

	public static void main(String[] args) {
 
	        SpringApplication.run(PredictSpringBootStart.class, args);
//	        ConfigResourceLoad.loadConfig();
	}

}
