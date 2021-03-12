package com.predict.plus.core.platform.feature.model;

import lombok.Data;

import java.util.List;

/**
 * <p> pid和模型特征 </p>
 *
 * @Author: fc.w
 * @Date: 2020/5/8 15:15
 */

@Data
public class FeatureVec {

    private String pid;

    private List<Float> feature;

}
