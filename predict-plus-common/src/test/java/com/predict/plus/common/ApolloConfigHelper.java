//package com.predict.plus.common;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//import com.alibaba.fastjson.JSON;
//import com.ctrip.framework.apollo.Config;
//
//public class ApolloConfigHelper {
//
//	public static void getConfig(Config config) {
//		getConfig(config, "");
//	}
//
//	public static void getConfig(Config config, String Config_FILE_NAMESPACE) {
//		Map<String, String> configMap = new HashMap<>();
//		Set<String> keys = config.getPropertyNames();
//		keys.forEach(key -> {
//			String value = config.getProperty(key, null);
//			configMap.put(key, value);
//		});
//		String json = JSON.toJSONString(configMap);
//		System.out.println(JSON.toJSONString(configMap));
//		if (Config_FILE_NAMESPACE.length() > 0) {
//			dataToFile(json, Config_FILE_NAMESPACE);
//		} else {
//			dataToFile(json);
//		}
//	}
//
//	public static void dataToFile(String json, String Config_FILE_NAMESPACE) {
//		try {
//			File file = new File(
//					"D:\\Users\\lejianjun\\git\\predict-plus\\predict-plus-common\\src\\main\\resources\\config\\"
//							+ Config_FILE_NAMESPACE + ".properties");
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//			FileWriter fw = new FileWriter(file.getAbsoluteFile());
//			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write(json);// 换行符，等价于 = bw.newLine();
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void dataToFile(String json) {
//		try {
//			File file = new File(
//					"D:\\Users\\lejianjun\\git\\predict-plus\\predict-plus-common\\src\\main\\resources\\apolloConfig.properties");
//			if (!file.exists()) {
//				file.createNewFile();
//			}
//			FileWriter fw = new FileWriter(file.getAbsoluteFile());
//			BufferedWriter bw = new BufferedWriter(fw);
//			bw.write(json);// 换行符，等价于 = bw.newLine();
//			bw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void main(String[] args) { 
//        
////        ApolloConfigHelper.getConfig(config,"application");
//	}
//
//}
