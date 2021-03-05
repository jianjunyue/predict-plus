package com.predict.plus.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.predict.plus.common.init.PredictContextInit;
import com.predict.plus.common.init.PredictPhaseConfigBuilder;
import com.predict.plus.facade.request.ModelPredictRequest;
import com.predict.plus.facade.response.ModelPredictResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/predictPlatform")
public class PredictPlatformController {

	@Autowired
	private PredictContextInit predictContextInit;
 
	
    @PostMapping("/predict")
    public ModelPredictResponse predict(@Validated @RequestBody ModelPredictRequest request) {
    	
    	ModelPredictResponse response = new ModelPredictResponse();
    	
    	System.out.println("--------------------------PredictPlatformController--------------------------------");
    	
    	predictContextInit.initContext(request);
    	
    	return response;
    	 
    	
    }

}
