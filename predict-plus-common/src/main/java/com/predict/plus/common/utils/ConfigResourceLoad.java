package com.predict.plus.common.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.tuhu.algo.etl.features.pojo.Feature;

public class ConfigResourceLoad {

	public static Properties loadConfig(String configFile) {
		Properties properties = new Properties();
		try {
			Map<String, String> mapConfig = readConfigFile(Map.class, new HashMap<String, String>(), configFile);
			mapConfig.keySet().forEach(key -> {
				properties.setProperty(key, mapConfig.get(key));
			});

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}

	public static <T> T readConfigFile(Class<T> clazz, T defaultValue, String configFile) {
		try {
			File file = new File(ConfigResourceLoad.class.getClassLoader()
					.getResource("config/" + configFile + ".properties").getFile());

			String readConfig = FileUtils.readFileToString(file);
			T t = JSONHelper.deserialize(readConfig, clazz, defaultValue);
			return t;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public static <T> T readJsonFile(Class<T> clazz, T defaultValue, String configFile) {
		try {
			File file = new File(ConfigResourceLoad.class.getClassLoader().getResource("json/" + configFile).getFile());

			String readConfig = FileUtils.readFileToString(file);
			T t = JSONHelper.deserialize(readConfig, clazz, defaultValue);
			return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public static <T> T readStrategyFile(Class<T> clazz, T defaultValue, String resource) {
		try {
			File file = new File(ConfigResourceLoad.class.getClassLoader().getResource(resource).getFile());
			String readConfig = FileUtils.readFileToString(file);
			T t = JSONHelper.deserialize(readConfig, clazz, defaultValue);
			return t;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	public static List<Map<String,Object>> getFeatures() {
		List<Map<String,Object>> features = null;// featureConfigResponse.getFeatures();
		features = ConfigResourceLoad.readJsonFile(List.class, new ArrayList<Map<String,Object>>(), "features.json");
	 
		features.forEach(action->{
			
			System.out.println(action.keySet());
		});
	 
		return features ;
	}

}
