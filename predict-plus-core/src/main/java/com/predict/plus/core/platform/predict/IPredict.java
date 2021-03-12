package com.predict.plus.core.platform.predict;
 
import java.util.List;

import com.predict.plus.core.context.PredictContext;
import com.predict.plus.core.platform.BoosterAndFeatureConfigModel;
import com.predict.plus.core.platform.feature.model.FeatureVectorModel;
import com.predict.plus.facade.response.PredictScore;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 10:12
 */
public interface IPredict {

    List<PredictScore> modelPredict(PredictContext context,
                                     List<FeatureVectorModel> prdTotalFeatureList,
                                     BoosterAndFeatureConfigModel boosterAndFeatureConfigModel,
                                     Float missingValue);

}
