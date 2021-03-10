package com.predict.plus.core.model;


import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/04 16:38
 */
@Data
public class PlatformModelConfig {

    // 特征维度
    private Set<String> featureDimension;

    // 特征依赖
    private Map<String, FeatureDependModel> featureDependMap;

    // 特征维度标识符：redis key组装时的维度标志, 主要用于特征同步状态检测
    private List<String> dimensionIdentifierList;

    // 特征维度标识符：redis key组装时的维度标志。 例：数据同步脚本JSON的rediskey=version@uid@aaaaa，那么用户维度USER -> uid，
    private Map<String, String> dimensionIdentifierMap;

    // 维度特征key的分隔符
    private String keySeparator;

    // 模型类型
    private String modelType;

    // 缺省值： NaN或0
    private String missingValue;

    private String keyPrefix;

    private String isCache;
}