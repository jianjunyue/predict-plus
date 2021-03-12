package com.predict.plus.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.flow.StrategyManager;
import com.predict.plus.core.init.PredictContextInit;
import com.predict.plus.facade.request.ModelPredictRequest;
import com.predict.plus.facade.response.ModelPredictResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/predictPlatform")
public class PredictPlatformController {

	@Autowired
	private PredictContextInit predictContextInit;
	
	@Autowired
	private StrategyManager strategyManager;
 
	
    @PostMapping("/predict")
    public ModelPredictResponse predict(@Validated @RequestBody ModelPredictRequest request) {
    	
    	ModelPredictResponse response = new ModelPredictResponse();
    	
    	System.out.println("--------------------------PredictPlatformController--------------------------------");
    	
    	PredictContext context =	predictContextInit.initContext(request);
    	
    	strategyManager.run(context);
    	
    	response.setPidScoreModelList(context.getPredictScores());
    	
    	return response;
    	 
    	
    }

}
