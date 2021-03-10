package com.predict.plus.core.common;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.predict.plus.common.constant.ApolloNameSpaceConstant;
import com.predict.plus.common.utils.ConfigResourceLoad;
import com.predict.plus.core.cache.DataCacheManagerHelper;
import com.predict.plus.core.model.FeatureDependModel;
import com.predict.plus.core.model.PlatformModelConfig;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 新版-Apollo模型配置信息加载
 * </p>
 *
 * <h
 * 
 * @Author: fc.w
 * @Date: 2020/11/04 16:18
 */
@Slf4j
@Component
public class PlatformModelConfigLoad {

	@Autowired
	private DimensionPriorityExecutorGraph dimensionPriorityExecutorGraph;

	@PostConstruct
	public void init() {
		Properties config = ConfigResourceLoad.loadConfig(ApolloNameSpaceConstant.MODEL_CONFIG_NAMESPACE);
		Set<String> modelNameKeys = config.keySet().stream().map(key -> key.toString()).collect(Collectors.toSet());
		if (CollectionUtils.isNotEmpty(modelNameKeys)) {
			Map<String, PlatformModelConfig> platformModelConfigMap = modelNameKeys.stream().map(modelName -> {
				String value = config.get(modelName) == null ? "" : config.get(modelName).toString();
				PlatformModelConfig platformModelConfig = configLoad(value);
				if (null != platformModelConfig) {
					return new Pair<String, PlatformModelConfig>(modelName, platformModelConfig);
				}
				return null;
			}).filter(c -> null != c).collect(Collectors.toMap(Pair::getKey, Pair::getValue));

			log.info("PlatformModelConfigLoad-platformModelConfigMap:{}", JSON.toJSONString(platformModelConfigMap));
			DataCacheManagerHelper.platformModelConfigMap = platformModelConfigMap;

			// 计算执行优先级
			dimensionPriorityExecutorGraph.executorGraph(platformModelConfigMap);
		}
	}

	private static PlatformModelConfig configLoad(String value) {
		PlatformModelConfig platformModelConfig = new PlatformModelConfig();
		try {
			if (StringUtils.isNotEmpty(value)) {
				JSONObject valueObj = JSON.parseObject(value);
				// 获取特征维度
				JSONObject featureDependObj = valueObj.getJSONObject("feature_depend");
				if (!featureDependObj.isEmpty()) {
					Set<String> featureDimensionKeySet = featureDependObj.keySet();
					platformModelConfig.setFeatureDimension(featureDimensionKeySet);
					// 获取维度依赖关系，以及依赖的特征
					Map<String, FeatureDependModel> featureDependMap = Maps.newHashMap();
					for (String featureDimensionKey : featureDimensionKeySet) {
						JSONObject featureObj = featureDependObj.getJSONObject(featureDimensionKey);
						if (!featureObj.isEmpty()) {
							// 获取依赖的特征维度
							Set<String> dependKeySet = featureObj.keySet();
							FeatureDependModel featureDependModel = new FeatureDependModel();
							Map<String, List<String>> dependDimensionFeatureMap = Maps.newHashMap();
							for (String dependKey : dependKeySet) {
								if ("keys_sequence".equals(dependKey)) {
									List<String> keySequences = featureObj.parseArray(featureObj.getString(dependKey),
											String.class);
									featureDependModel.setKeySequences(keySequences);
								} else {
									List<String> featureList = JSONObject.parseArray(featureObj.getString(dependKey),
											String.class);
									dependDimensionFeatureMap.put(dependKey, featureList);
								}
							}
							featureDependModel.setDependDimensionMap(dependDimensionFeatureMap);
							featureDependMap.put(featureDimensionKey, featureDependModel);
						} else {
							featureDependMap.put(featureDimensionKey, null);
						}
					}
					platformModelConfig.setFeatureDependMap(featureDependMap);
				}

				String dimensionIdentifier = valueObj.getString("dimension_identifier");
				if (StringUtils.isNotEmpty(dimensionIdentifier)) {
					// 特征维度标识符：redis key组装时的维度标志。 例：数据同步脚本JSON的rediskey=version@uid@aaaaa，那么用户维度USER
					// -> uid，
					Map<String, String> dimensionIdentifierMap = JSON.parseObject(dimensionIdentifier,
							new TypeReference<Map<String, String>>() {
							});
					if (MapUtils.isNotEmpty(dimensionIdentifierMap)) {
						List<String> dimensionIdentifierList = dimensionIdentifierMap.entrySet().stream()
								.map(entry -> entry.getValue().toLowerCase()).collect(Collectors.toList());
						log.info("PlatformModelConfigLoad-dimensionIdentifierList:{}",
								JSON.toJSONString(dimensionIdentifierList));
						platformModelConfig.setDimensionIdentifierList(dimensionIdentifierList);
						platformModelConfig.setDimensionIdentifierMap(dimensionIdentifierMap);
					}
				}

				String keySeparator = valueObj.getString("key_separator");
				if (StringUtils.isNotEmpty(keySeparator)) {
					platformModelConfig.setKeySeparator(keySeparator);
				}

				String modelType = valueObj.getString("model_type");
				if (StringUtils.isNotEmpty(modelType)) {
					platformModelConfig.setModelType(modelType);
				}

				String messingValue = valueObj.getString("missing_value");
				if (StringUtils.isNotEmpty(messingValue)) {
					platformModelConfig.setMissingValue(messingValue);
				}

				String keyPrefix = valueObj.getString("key_prefix");
				{
					if (StringUtils.isNotEmpty(keyPrefix)) {
						platformModelConfig.setKeyPrefix(keyPrefix);
					}
				}
				String isCache = valueObj.getString("is_cache");
				{
					if (StringUtils.isNotEmpty(isCache)) {
						platformModelConfig.setIsCache(isCache);
					}
				}
			}
		} catch (Exception e) {
			log.error("PlatformModelConfigLoad-configLoad-Exception", e);
		}

		return platformModelConfig;
	}

	public static void main(String[] args) {
		String value = "{\"feature_depend\":{\"USER\":{},\"PRODUCT\":{},\"U_P\":{\"USER\":[\"feature1\",\"feature4\"],\"PRODUCT\":[\"feature3\"],\"keys_sequence\":[\"feature1\",\"feature4\",\"feature3\"]},\"D_P\":{\"U_P\":[\"feature8\",\"feature9\"],\"keys_sequence\":[\"feature8\",\"feature9\"]}},\"dimension_identifier\":{\"USER\":\"uid\",\"PRODUCT\":\"pid\",\"U_P\":\"up\",\"D_P\":\"dp\"},\"key_separator\":\"#\",\"model_type\":\"XGBOOST\",\"missing_value\":\"NaN\"}";
		configLoad(value);
	}

}
