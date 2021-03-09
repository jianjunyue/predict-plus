package com.predict.plus.algo.operator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 注意不能有重名方法
 */
public class UtilsOperator {

    private final static String y_M_d = "yyyy-MM-dd";
    private final static String yMd = "yyyyMMdd";
    private final static String y_M_d_H_m_s = "yyyy-MM-dd HH:mm:ss";
    private final static String y_M_d_H_m_s_1S = "yyyy-MM-dd HH:mm:ss.S";
    private final static String y_M_d_H_m_s_2S = "yyyy-MM-dd HH:mm:ss.SS";
    private final static String y_M_d_H_m_s_3S = "yyyy-MM-dd HH:mm:ss.SSS";


    /**
     * 解析时间
     *
     * @param date
     * @return
     */
    public static Date toDate(String date) {
        if (StringUtils.length(date) == y_M_d.length()) {
            return toDate(date, y_M_d);
        } else if (StringUtils.length(date) == yMd.length()) {
            return toDate(date, yMd);
        } else if (StringUtils.length(date) == y_M_d_H_m_s.length()) {
            return toDate(date, y_M_d_H_m_s);
        } else if (StringUtils.length(date) == y_M_d_H_m_s_1S.length()) {
            return toDate(date, y_M_d_H_m_s_1S);
        } else if (StringUtils.length(date) == y_M_d_H_m_s_2S.length()) {
            return toDate(date, y_M_d_H_m_s_2S);
        } else if (StringUtils.length(date) == y_M_d_H_m_s_3S.length()) {
            return toDate(date, y_M_d_H_m_s_3S);
        } else {
            return null;
        }
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 判断是否是数值
     *
     * @param sentence
     * @return
     */
    public static boolean isNumeric(String sentence) {
        return NumberUtils.isDigits(sentence);
    }

    /**
     * 按照自定义时间格式解析时间
     *
     * @param date
     * @param pattern
     * @return
     */
    public static Date toDate(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (Exception e) {
            return null;
        }
    }
}
