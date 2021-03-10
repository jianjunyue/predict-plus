package com.predict.plus.core.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>组合维度-维度依赖和key序列</p>
 *
 * @Author: fc.w
 * @Date: 2020/11/04 18:36
 */
@Data
public class FeatureDependModel {

    private Map<String, List<String>> dependDimensionMap;

    private List<String> keySequences;

}
