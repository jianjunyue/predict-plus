package com.predict.plus.core.platform;

import java.util.Map;

import com.predict.plus.algo.common.FeatureConfig;
import com.predict.plus.algo.model.LightGBMModel;
import com.predict.plus.algo.model.TensorflowModel;

import lombok.Data;
import ml.dmlc.xgboost4j.java.Booster;

 
@Data
public class BoosterAndFeatureConfigModel {

    private Integer version;

    /* XGBoost模型 */
    private Booster booster;

    /* LightGBM模型 */
    private LightGBMModel lightGBMModel;

    /* 深度模型-Model */
    private TensorflowModel tensorFlowModel;


    private FeatureConfig featureConfig;

    private Map<String, String> featureId2NameMap;
    

    public BoosterAndFeatureConfigModel() {
    }

    public BoosterAndFeatureConfigModel(FeatureConfig featureConfig) {
        this.featureConfig = featureConfig;
    }
}
