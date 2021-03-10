package com.predict.plus.common.utils;


import java.util.Map;

/**
 * <p>耗时监控</p>
 *
 * @Author: fc.w
 * @Date: 2020/09/24 18:21
 */
public class TimeMonitorUtils {

    private static ThreadLocal<Long> tl = new ThreadLocal<>();

    public static void start() {
        tl.set(System.currentTimeMillis());
    }

    public static void finish(String methodName, Map<String, Long> costTimeMap) {
//        long finishTime = System.currentTimeMillis();
//        costTimeMap.put(methodName, finishTime - tl.get());
    }


}