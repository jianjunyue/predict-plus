package com.predict.plus.core.common;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.predict.plus.core.cache.DataCacheManagerHelper;
import com.predict.plus.core.model.ExecutorPriority;
import com.predict.plus.core.model.FeatureDependModel;
import com.predict.plus.core.model.PlatformModelConfig;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/13 19:28
 */
@Slf4j
@Component
public class DimensionPriorityExecutorGraph {

    public void executorGraph(Map<String, PlatformModelConfig> platformModelConfigMap) {
        if (MapUtils.isEmpty(platformModelConfigMap)) {
            return;
        }

        try {
            /*
             * 计算执行优先级, 分三种情况
             * 1. 该维度没有依赖其他维度，优先级最高
             * 2. 该组合维度依赖其他维度，优先级中
             * 3. 该组合维度依赖其他组合维度， 优先级低
             * */
            Map<String, ExecutorPriority> executorPriorityMap = new HashMap<>();
            for (Map.Entry<String, PlatformModelConfig> entry : platformModelConfigMap.entrySet()) {
                PlatformModelConfig platformModelConfig = entry.getValue();
                Map<String, FeatureDependModel> featureDependMap = platformModelConfig.getFeatureDependMap();
                if (MapUtils.isEmpty(featureDependMap)) {
                    continue;
                }

                List<String> highestPriorityList = Lists.newArrayList();
                List<String> mediumPriorityList = Lists.newArrayList();
                List<String> lowPriorityList = Lists.newArrayList();
                for (Map.Entry<String, FeatureDependModel> priorityEntry : featureDependMap.entrySet()) {
                    FeatureDependModel featureDependModel = priorityEntry.getValue();
                    if (null == featureDependModel) {
                        highestPriorityList.add(priorityEntry.getKey());
                    } else {
                        Map<String, List<String>> dependDimensionAndFeatureMap = featureDependModel.getDependDimensionMap();
                        Set<String> dependDimensionSet = dependDimensionAndFeatureMap.keySet();
                        boolean priority = false;
                        for (String dependDimension : dependDimensionSet) {
                            if (featureDependMap.containsKey(dependDimension)) {
                                FeatureDependModel otherFeatureDependModel = featureDependMap.get(dependDimension);
                                if (null == otherFeatureDependModel) {
                                    priority = true;
                                } else {
                                    break;
                                }
                            }
                        }

                        if (priority) {
                            mediumPriorityList.add(priorityEntry.getKey());
                        } else {
                            lowPriorityList.add(priorityEntry.getKey());
                        }
                    }
                }

                ExecutorPriority executorPriority = new ExecutorPriority();
                if (CollectionUtils.isNotEmpty(highestPriorityList)) executorPriority.setHighestPriorityList(highestPriorityList);
                if (CollectionUtils.isNotEmpty(mediumPriorityList)) executorPriority.setMediumPriorityList(mediumPriorityList);
                if (CollectionUtils.isNotEmpty(lowPriorityList)) executorPriority.setLowPriorityList(lowPriorityList);

                executorPriorityMap.put(entry.getKey(), executorPriority);
                log.info("DimensionPriorityExecutorGraph-highestPriorityList:{}-mediumPriorityList:{}-lowPriorityList:{}",
                        JSON.toJSONString(highestPriorityList), JSON.toJSONString(mediumPriorityList), JSON.toJSONString(lowPriorityList));
            }

            DataCacheManagerHelper.executorPriorityMap.putAll(executorPriorityMap);
            log.info("DimensionPriorityExecutorGraph-executorPriorityMap:{}", JSON.toJSONString(executorPriorityMap));
        } catch (Exception e) {
            log.error("DimensionPriorityExecutorGraph-Exception", e);
        }

    }


}