package com.predict.plus.common.utils;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * JSON序列化/反序列化
 * 
 */
public final class JSONHelper {
    private JSONHelper() {
    }

    /**
     * 序列化
     *
     * @param object
     * @return JSON字符串
     * @throws IOException
     */
    public static String serialize(Object object) throws IOException {
        return JSONObject.toJSONString(object,
                SerializerFeature.IgnoreNonFieldGetter,
                SerializerFeature.UseISO8601DateFormat);
    }

    /**
     * 反序列化
     *
     * @param json
     * @param clazz
     * @return
     * @throws IOException
     */
    public static <T> T deserialize(String json, Class<T> clazz, T defaultValue) throws IOException {
        if (StringUtils.isEmpty(json)) {
            return defaultValue;
        }
        return JSONObject.parseObject(json, clazz);
    }

    /**
     * 反序列化(jsonArray)
     *
     * @param jsonArray
     * @param clazz
     * @param defaultValue
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> deserializeArray(String jsonArray, Class<T> clazz, List<T> defaultValue) {
        if (StringUtils.isBlank(jsonArray)) {
            return defaultValue;
        }
        return JSONObject.parseArray(jsonArray, clazz);
    }
}