package com.predict.plus.common.config;

import java.util.List;
import java.util.Properties;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON; 
import com.predict.plus.common.constant.CommonConstant;
import com.predict.plus.common.utils.ConfigResourceLoad;
import com.predict.plus.common.utils.JSONHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ApolloConfig {
	
	private static String apolloCon="apolloConfig";

    private ApolloConfig() {
        throw new IllegalStateException("Utility class");
    }

    public static String getParameter(String para) {

		Properties config =ConfigResourceLoad.loadConfig(apolloCon); 
        return config.getProperty(para, null);
         
    }

    public static String getParameter(String para, String defaultValue) {
        if (StringUtils.isBlank(para)) {
            return defaultValue;
        }
		Properties config = ConfigResourceLoad.loadConfig(apolloCon); 
        return config.getProperty(para, defaultValue);
    }

    public static String getProperties(String configName, String key) {
        try {
    		Properties config =ConfigResourceLoad.loadConfig(apolloCon); 
            return config.getProperty(key, null);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * get switch status
     *
     * @param configName
     * @param key
     * @return
     */
    public static boolean switchStatus(String configName, String key) {
        String switchStatus = ApolloConfig.getProperties(configName, key);
        return CommonConstant.ONE.equals(switchStatus);
    }

    /**
     * get switch status
     *
     * @param key
     * @return
     */
    public static boolean switchStatus(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        String switchStatus = ApolloConfig.getParameter(key);
        return CommonConstant.ONE.equals(switchStatus);
    }

    /**
     * 是否命中白名单
     *
     * @param key
     * @param uid
     * @return
     */
    public static boolean isHitWhiteList(String key, String uid) {
        boolean flag = Boolean.FALSE;
        if (StringUtils.isBlank(key) || StringUtils.isBlank(uid)) {
            return Boolean.FALSE;
        }
        String whiteListString = getParameter(key);
        if (StringUtils.isNotBlank(whiteListString)) {
            try {
                List<String> whitelist = JSONHelper.deserializeArray(whiteListString, String.class, null);
                if (CollectionUtils.isNotEmpty(whitelist)) {
                    if (whitelist.contains(uid.toLowerCase())) {
                        flag = Boolean.TRUE;
                    }
                }
            } catch (Exception e) {
                log.warn("isHitWhiteList error e={}", e.getMessage());
            }
        }
        return flag;
    }

    /**
     * 获取整型的配置值
     * @param key
     * @return
     */
    public static Integer getParameterWithInteger(String key) {
        String param = getParameter(key);
        if (StringUtils.isNotBlank(param)) {
            return Integer.parseInt(param);
        }

        return null;
    }

    /**
     * 获取集合配置值
     * @param key
     * @return
     */
    public static List<String> getParameterList(String key) {
        String param = getParameter(key);
        if (StringUtils.isNotBlank(param)) {
            return JSON.parseArray(param, String.class);
        }

        return null;
    }

    /**
     *
     * @param namespace
     * @param key
     * @return
     */
    public static String getParamterByNamespace(String namespace, String key) {
        return ApolloConfig.getProperties(namespace, key);
    }

}
