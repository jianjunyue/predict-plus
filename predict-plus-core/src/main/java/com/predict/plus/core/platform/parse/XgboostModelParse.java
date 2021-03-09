package com.predict.plus.core.platform.parse;
 
import lombok.extern.slf4j.Slf4j;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.XGBoost;
import org.springframework.stereotype.Component;

import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.core.platform.parse.iface.IModelParse;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 14:06
 */
@Slf4j
@Component
public class XgboostModelParse implements IModelParse {

    @Override
    public BoosterAndFeatureConfigModel modelParse(InputStream inputStream, Integer version) {
        try {
            if (null != inputStream) {
                // 模型加载
                Booster booster = XGBoost.loadModel(inputStream);
                BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = new BoosterAndFeatureConfigModel();
                boosterAndFeatureConfigModel.setBooster(booster);
                return boosterAndFeatureConfigModel;
            }
        } catch (Exception e) {
            log.error("XgboostModelLoadParse-模型文件解析异常-version:{}-Exception", version, e);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        log.error("XgboostModelLoadParse-模型文件解析异常-version:{}", version);
        return null;
    }

}
