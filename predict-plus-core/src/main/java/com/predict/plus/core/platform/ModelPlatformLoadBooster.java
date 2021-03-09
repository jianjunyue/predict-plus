package com.predict.plus.core.platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.predict.plus.algo.common.model.ApolloModelInfoModel;
import com.predict.plus.algo.common.model.GetAccessUriModel;
import com.predict.plus.common.utils.ConfigResourceLoad;
import com.predict.plus.core.cache.DataCacheManagerHelper;
import com.predict.plus.core.common.ModelFileServerLoad;
import com.predict.plus.core.platform.parse.ModelLoadManagerBuilder;
import com.predict.plus.core.platform.parse.iface.IModelParse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ModelPlatformLoadBooster {
	private static final String MODEL_FILE_NAMESPACE = "MLP.app-openApi";
	private static final String MODEL_CONFIG_NAMESPACE = "MLP.Algo-platform-config";

	@Autowired
	private ModelLoadManagerBuilder modelLoadManagerBuilder;
	@Autowired
	private ModelFileServerLoad modelFileServerLoad;

	@PostConstruct
	public void init() {
		try {
			// 模型文件信息
			Properties modelFileConfig = ConfigResourceLoad.loadConfig(MODEL_FILE_NAMESPACE);

			// 模型上线配置的维度信息，只获取key模型名称，只加载线上需要用的模型
			Properties modelDimensionConfig = ConfigResourceLoad.loadConfig(MODEL_CONFIG_NAMESPACE);

			Set<String> onlineModelNameSet = modelDimensionConfig.keySet().stream().map(key -> key.toString())
					.collect(Collectors.toSet());
			if (CollectionUtils.isNotEmpty(onlineModelNameSet)) {
				for (String modelName : onlineModelNameSet) {
					// 获取模型地址、版本、模型类型
					String modelInfo = modelFileConfig.getProperty(modelName, "");
					// 模型加载
					loadModel(modelName, modelInfo);
				}
			}
		} catch (Exception e) {
			log.error("ModelPlatformLoadBooster-init-Exception", e);
		}
	}

	/**
	 * 模型加载
	 * 
	 * @param modelName
	 * @param modelInfoStr
	 */
	private void loadModel(String modelName, String modelInfoStr) {
		log.info("ModelPlatformLoadBooster-loadModel-modelName:{}-modelInfoStr:{}", modelName, modelInfoStr);
		try {
			if (StringUtils.isNotEmpty(modelInfoStr)) {
				// 模型地址信息解析
				ApolloModelInfoModel modelInfo = JSON.parseObject(modelInfoStr, ApolloModelInfoModel.class);
				String modelPath = modelInfo.getModelPath();
				Integer version = modelInfo.getVersion();
				String modelType = modelInfo.getModelType();

				// 去模型文件服务器下载文件
//				GetAccessUriModel getAccessUriModel = modelFileServerLoad.getAccessUriModel(modelPath);
//				File file = modelFileServerLoad.downloadFile(getAccessUriModel, modelPath);
				
				File file = new File(modelFileServerLoad.parentPath +modelPath);

				// 加载模型
				BoosterAndFeatureConfigModel boosterModel = unZipAndLoadModelByType(file, modelType, modelName,
						version);

				// 特征version和booster映射
				LinkedHashMap<Integer, BoosterAndFeatureConfigModel> versionBoosterMap = DataCacheManagerHelper.boosterAndFeatureConfigModelMap
						.getOrDefault(modelName, new LinkedHashMap<>());
				versionBoosterMap.put(version, boosterModel);
				if (versionBoosterMap.size() > 1) {
					versionBoosterMap = versionBoosterMap.entrySet().stream()
							.sorted(Collections.reverseOrder(Map.Entry.comparingByKey())).collect(Collectors
									.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				}
				DataCacheManagerHelper.boosterAndFeatureConfigModelMap.put(modelName, versionBoosterMap);
			} else {
				log.error("ModelPlatformLoadBooster-loadModel-{}-模型文件路径不存在, 请确认Apollo命名空间中是否存在该Key！", modelName);
			}
		} catch (Exception e) {
			log.error("ModelPlatformLoadBooster-loadModel-Exception", e);
		}

	}

	/**
	 * zip文件转数据流
	 * 
	 * @param srcFile
	 */
	private BoosterAndFeatureConfigModel unZipAndLoadModelByType(File srcFile, String modelType, String modelName,
			Integer version) {
		// 判断源文件是否存在
		if (!srcFile.exists()) {
			throw new RuntimeException(srcFile.getPath() + "所指文件不存在");
		}

		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(srcFile);
			Enumeration<?> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				log.info("LightGBMModelLoadParse-unZipAndXgboostLoad-模型文件名：{}", entry.getName());
				InputStream inputStream = zipFile.getInputStream(entry);

				// 根据模型类型加载模型
				IModelParse iModelParse = modelLoadManagerBuilder.getModelParseInstance(modelType);
				if (null != iModelParse) {
					BoosterAndFeatureConfigModel boosterAndFeatureConfigModel = iModelParse.modelParse(inputStream,
							version);
					boosterAndFeatureConfigModel.setVersion(version);
					return boosterAndFeatureConfigModel;
				} else {
					log.error("ModelPlatformLoadBooster-unZipAndXgboostLoad模型类型不存在modelType:{}-modelName:{}", modelType,
							modelName);
				}

				// 关闭流
				inputStream.close();
			}
		} catch (Exception e) {
			log.error("LightGBMModelLoadParse-unZipAndXgboostLoad-Exception", e);
		} finally {
			// 关闭流
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		log.error("ModelPlatformLoadBooster-unZipAndXgboostLoad模型文件解析异常-modelName:{}-modelType:{}-version:{}",
				modelName, modelType, version);
		return null;
	}
}
