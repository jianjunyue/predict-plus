package com.predict.plus.algo.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.predict.plus.algo.features.FeaturePreprocessType;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.*;

@Slf4j
public class PreprocessFeatureExecutor implements Serializable {
    Map<String, Integer> featurePreprocessType = Maps.newConcurrentMap();
    Map<String, Map<String, Double>> normalizeFeatures = Maps.newConcurrentMap();
    Map<String, List<Double>> quantileFeatures = Maps.newConcurrentMap();

    public PreprocessFeatureExecutor(List<PreprocessFeature> preprocessFeatures) {
        for (PreprocessFeature preprocessFeature : preprocessFeatures) {
            featurePreprocessType.put(preprocessFeature.getName(), preprocessFeature.getPreprocessType());
            if (Arrays.asList(FeaturePreprocessType.STANDARD.getCode(), FeaturePreprocessType.MIN_MAX.getCode()).contains(preprocessFeature.getPreprocessType())) {
                normalizeFeatures.put(preprocessFeature.getName(), JSON.parseObject(preprocessFeature.getFeatureMeta(), new TypeReference<Map<String, Double>>() {
                }));
            } else if (Arrays.asList(FeaturePreprocessType.QUANTILE.getCode()).contains(preprocessFeature.getPreprocessType())) {
                List<Double> quantileRawList = JSON.parseObject(preprocessFeature.getFeatureMeta(), new TypeReference<List<Double>>() {
                });
                List<Double> quantileList = (quantileRawList.size() >= 2) ? quantileRawList : new ArrayList<>(2);
                quantileList.set(0, Double.NEGATIVE_INFINITY);
                quantileList.set(quantileList.size() - 1, Double.POSITIVE_INFINITY);
                quantileFeatures.put(preprocessFeature.getName(), quantileList);
            }
        }
    }

    public Map<String, Object> transform(Map<String, Object> rawData) {
        for (Map.Entry<String, Integer> entry : featurePreprocessType.entrySet()) {
            String name = entry.getKey();

            if (entry.getValue() == FeaturePreprocessType.STANDARD.getCode()) {
                rawData.put(name, standard(name, rawData.get(name)));
            } else if (entry.getValue() == FeaturePreprocessType.MIN_MAX.getCode()) {
                rawData.put(name, minMax(name, rawData.get(name)));
            } else if (entry.getValue() == FeaturePreprocessType.QUANTILE.getCode()) {
                rawData.put(name, quantile(name, rawData.get(name)));
            }
        }

        return rawData;
    }

    public Object standard(String name, Object data) {
        Map<String, Double> standardMap = normalizeFeatures.get(name);

        if (data == null) {
            return null;
        }
        return (Double.parseDouble(data.toString()) - standardMap.get("mean")) / standardMap.get("std");
    }

    public Object minMax(String name, Object data) {
        Map<String, Double> minMaxMap = normalizeFeatures.get(name);

        if (minMaxMap.get("max") - minMaxMap.get("min") == 0 || data == null) {
            return data;
        }
        return (Double.parseDouble(data.toString()) - minMaxMap.get("mix")) / (minMaxMap.get("max") - minMaxMap.get("min"));
    }

    public Object quantile(String name, Object data) {
        System.out.println("feature name is " + name + " and object is " + JSON.toJSONString(data));
        List<Double> quantileList = quantileFeatures.get(name);
        if (data == null) {
            return quantileList.size() - 1;
        }

        int index = Collections.binarySearch(quantileList, Double.parseDouble(data.toString()));
        return (index < 0) ? Math.abs(index) - 2 : index;
    }

    public Map<String, Integer> getFeaturePreprocessType() {
        return featurePreprocessType;
    }

    public void setFeaturePreprocessType(Map<String, Integer> featurePreprocessType) {
        this.featurePreprocessType = featurePreprocessType;
    }

    public Map<String, Map<String, Double>> getNormalizeFeatures() {
        return normalizeFeatures;
    }

    public void setNormalizeFeatures(Map<String, Map<String, Double>> normalizeFeatures) {
        this.normalizeFeatures = normalizeFeatures;
    }

    public Map<String, List<Double>> getQuantileFeatures() {
        return quantileFeatures;
    }

    public void setQuantileFeatures(Map<String, List<Double>> quantileFeatures) {
        this.quantileFeatures = quantileFeatures;
    }
}
