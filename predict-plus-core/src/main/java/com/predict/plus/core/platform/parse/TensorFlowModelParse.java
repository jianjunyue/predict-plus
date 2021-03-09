package com.predict.plus.core.platform.parse;
 
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.predict.plus.algo.common.model.KerasModelType;
import com.predict.plus.algo.model.TensorflowModel;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.core.platform.parse.iface.IModelParse;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 18:46
 */
@Slf4j
@Component
public class TensorFlowModelParse implements IModelParse {

    @Override
    public BoosterAndFeatureConfigModel modelParse(InputStream inputStream, Integer version) {
        try {
            if (null != inputStream) {
                // 模型加载
                TensorflowModel tensorflowModel = new TensorflowModel(inputStream);
                BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = new BoosterAndFeatureConfigModel();
                boosterAndFeatureConfigModel.setTensorFlowModel(tensorflowModel);
                return boosterAndFeatureConfigModel;
            }
        } catch (Exception e) {
            log.error("TenserflowModelParse-模型文件解析异常-version:{}-Exception", version, e);
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        log.error("TenserflowModelParse-模型文件解析异常-version:{}", version);
        return null;
    }

}
