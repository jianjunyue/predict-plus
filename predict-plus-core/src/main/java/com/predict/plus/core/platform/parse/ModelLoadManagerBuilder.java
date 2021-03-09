package com.predict.plus.core.platform.parse;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

import com.predict.plus.common.constant.BusinessConstant;
import com.predict.plus.core.platform.parse.iface.IModelParse;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 15:48
 */
@Component
@Slf4j
public class ModelLoadManagerBuilder implements ApplicationContextAware {

    private static final Map<String, IModelParse> modelParseMap = Maps.newHashMap();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        IModelParse xgboostModelParse = (IModelParse)applicationContext.getBean("xgboostModelParse");
        IModelParse lightGBMModelParse = (IModelParse)applicationContext.getBean("lightGBMModelParse");
        IModelParse tensorFlowModelParse = (IModelParse)applicationContext.getBean("tensorFlowModelParse");
        IModelParse tensorFlowSeqModelParse = (IModelParse)applicationContext.getBean("tensorFlowSeqModelParse");

        modelParseMap.put(BusinessConstant.XGBOOST,xgboostModelParse );
        modelParseMap.put(BusinessConstant.LIGHTGBM, lightGBMModelParse);
        modelParseMap.put(BusinessConstant.DEEP_MODEL, tensorFlowModelParse);
        modelParseMap.put(BusinessConstant.DEEP_SEQ, tensorFlowSeqModelParse);
    }

    public IModelParse getModelParseInstance(String modelType) {
        return modelParseMap.getOrDefault(modelType, null);
    }

}
