package com.predict.plus.core.platform.predict;

import com.google.common.collect.Maps;
import com.predict.plus.common.constant.BusinessConstant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
 
/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 10:39
 */
@Component
@Slf4j
public class PredictModelStrategyBuilder implements ApplicationContextAware {

    private static final Map<String, IPredict> predictModelMap = Maps.newHashMap();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        IPredict xgboostPredict = (IPredict) applicationContext.getBean("xgboostPredict");
        IPredict lightGBMPredict = (IPredict) applicationContext.getBean("lightGBMPredict");
        IPredict tensorFlowModelPredict = (IPredict) applicationContext.getBean("tensorFlowPredict");

        predictModelMap.put(BusinessConstant.XGBOOST, xgboostPredict);
        predictModelMap.put(BusinessConstant.LIGHTGBM, lightGBMPredict);
        predictModelMap.put(BusinessConstant.DEEP_MODEL, tensorFlowModelPredict);
        predictModelMap.put(BusinessConstant.DEEP_SEQ, tensorFlowModelPredict);
    }

    public IPredict build(String modelType) {
        return predictModelMap.getOrDefault(modelType, null);
    }

}
