package com.predict.plus.core.platform.feature.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/07/15 17:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeatureVectorModel {

    private String pid;

    private float[] feature;

}
