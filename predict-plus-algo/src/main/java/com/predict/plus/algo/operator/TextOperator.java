package com.predict.plus.algo.operator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject; 
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TextOperator implements Serializable {

    /**
     * 周几
     * @param date
     * @return
     */
    public static double dayOfWeek(String date){

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = getDateFormat(date);
        try{
            calendar.setTime(simpleDateFormat.parse(date));
            return calendar.get(Calendar.DAY_OF_WEEK);
        }catch (Exception e){
            return -1;
        }
    }

    /**
     * 每天的第几分钟
     * @param date
     * @return
     */
    public static double minOfDay(String date){
        return Integer.valueOf(date.substring(11,13))*60+ Integer.valueOf(date.substring(14,16));
    }

    public static double weekOfYear(String date){
        SimpleDateFormat simpleDateFormat = getDateFormat(date);
        Calendar calendar = Calendar.getInstance();
        try{
            calendar.setTime(simpleDateFormat.parse(date));
            return calendar.get(Calendar.WEEK_OF_YEAR);
        }catch (Exception e){
            return -1;
        }
    }

    /**
     * 每天的第几个小时
     * @param date
     * @return
     */
    public static int hourOfDay(String date){
        SimpleDateFormat simpleDateFormat = getDateFormat(date);
        try{
            return simpleDateFormat.parse(date).getHours();
        }catch (Exception e){
            return -1;
        }
    }

    /**
     * 每月的第几周
     * @param date
     * @return
     */
    public static double weekOfMonth(String date){
        SimpleDateFormat simpleDateFormat = getDateFormat(date);
        Calendar calendar = Calendar.getInstance();
        try{
            calendar.setTime(simpleDateFormat.parse(date));
            return calendar.get(Calendar.WEEK_OF_MONTH);
        }catch (Exception e){
            return -1;
        }
    }

    /**
     * 时间相隔的天数
     * @param start
     * @param end
     * @return
     */
    public static double durationOfDate(String start, String end){
        SimpleDateFormat simpleDateFormat = getDateFormat(start);
        try {
            long endDate = simpleDateFormat.parse(end).getTime();
            long startDate = simpleDateFormat.parse(start).getTime();
            return (endDate - startDate) / 86400000;
        } catch (Exception e) {
            return -1;
        }
    }

    private static SimpleDateFormat getDateFormat(String date) {
        if (StringUtils.length(date) == 19) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } else {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    }

    /**
     * 关键词提取
     * 分词工具: HanLP
     * 按照词表的顺序填充1，其他填0。
     * 注意词表如果太大每次都需要重新构建词表会比较慢。
     * @param document 提取文档
     * @param keyWordList 关键词表
     * @return
     */
    public static double[] toKeyWord(String document, List<String> keyWordList){
        String documentLower = document.toLowerCase();
        double[] result = new double[keyWordList.size()+1];

//        List<Term> terms = HanLP.segment(documentLower);
//        Map<String, Integer> map = new HashMap<>();
//        for (int i = 0; i < keyWordList.size(); i++) {
//            map.put(keyWordList.get(i).toLowerCase(), i);
//        }
//
//        boolean flag = false;
//        for(Term term:terms){
//            Integer value = map.get(term.word);
//            if(value != null){
//                result[value] = 1;
//                flag = true;
//            }
//        }
//        if(! flag) {
//            result[keyWordList.size()] = 1;
//        }
        return result;
    }


    /**
     * 普通的OneHot编码,放回cateList.size()+1个数字
     * 1. 按照类目词表的顺序填充，如果是在词表中的类目，在对应位置上填充1,
     * 如果不在词表中，就在最后一位填充1。
     *
     * @param cate
     * @param cateList 类目词表
     * @return
     */
    public static double[] toOneHot(String cate, List<String> cateList, double defaultValue) {
        double[] one = new double[cateList.size() + 1];

        if (defaultValue != 0) {
            for (int i = 0; i < one.length; i++) {
                one[i] = defaultValue;
            }
        }

        int size = cateList.size();
        for (int i = 0; i < size; i++) {
            if (cate.equals(cateList.get(i))) {
                one[i] = 1;
                return one;
            }
        }
        one[cateList.size()] = 1;
        return one;
    }

    public static double[] toOneHot(String cate, List<String> cateList) {
        return toOneHot(cate, cateList, 0);
    }

    /**
     * 带顺序的OneHot编码，类目之间存在大小关系
     * 1. 如果当前类目在词表的X位置，则在 <= X 的位置上全填上1
     * 2. 如果不在词表的类目中，则在向量最后一位上填1
     *
     * @param cate
     * @param cateList
     * @return
     */
    public static double[] toOneHotOrder(String cate, List<String> cateList, double defaultValue) {
        int size = cateList.size();
        double[] one = new double[size + 1];
        double[] defaultValues = new double[size + 1];
        if (defaultValue != 0) {
            for (int i = 0; i < size + 1; i++) {
                one[i] = defaultValue;
                defaultValues[i] = defaultValue;
            }
        }

        for (int i = 0; i < size; i++) {
            one[i + 1] = 1;
            if (cate.equals(cateList.get(i))) {
                return one;
            }
        }
        defaultValues[size] = 1;
        return defaultValues;
    }

    public static double[] toOneHotOrder(String cate, List<String> cateList) {
        return toOneHotOrder(cate, cateList, 0);
    }

    /**
     * 隐向量解析，默认填0,隐向量默认 <b>,</b> 隔开
     *
     * @param data 向量
     * @param n    隐向量topn
     * @return
     */
    public static double[] hiddenVector(String data, int n) {
        String[] vector = data.split(",");
        double[] result = new double[n];

        for(int i = 0; i < n; i++){
            result[i] = Double.parseDouble(vector[i]);
        }

        return result;
    }

    public static double targetEncoder(String category, Map<String, Double> map, double defaultValue) {
        return map.getOrDefault(category, defaultValue);
    }

    public static double[] categoryInter(int maxSize, Map<String, Integer> interMap, String delimit, String... field) {
        double[] result = new double[maxSize + 1];
        String join = String.join(delimit, field);
        Integer loc = interMap.getOrDefault(join, maxSize);
        result[loc] = 1;
        return result;
    }

    public static double[] multiOneHot(String sentence, List<String> cateList, double defaultValue) {
        double[] result = new double[cateList.size() + 1];
        boolean flag = false;
        for (int i = 0; i < cateList.size() + 1; i++) {
            result[i] = defaultValue;
        }
        for (int i = 0; i < cateList.size(); i++) {
            if (sentence.lastIndexOf(cateList.get(i)) != -1) {
                result[i] = 1;
                flag = true;
            }
        }

        if (!flag) {
            result[cateList.size()] = 1;
        }
        return result;
    }

    public static double[] multiOneHot(String sentence, List<String> cateList) {
        return multiOneHot(sentence, cateList, 0);
    }

    /**
     * 类目交叉Hash编码
     *
     * @param maxSize
     * @param field
     * @return
     */
    public static double[] categoryHashInter(int maxSize, String... field) {
        double[] result = new double[maxSize];
        String join = String.join("-", field);
        result[join.hashCode()%maxSize] = 1;
        return result;
    }

    /**
     * 类目交叉hash编码
     * @param field
     * @param hashSize
     * @return
     */
    public static double[] categoryHashEncode(String field, int hashSize) {
        double[] result = new double[hashSize];
        result[field.hashCode()%hashSize] = 1;
        return result;
    }

    /**
     * 文本信息中的浮点数提取
     * @param field
     * @param loc 取第几个文本
     * @return
     */
    public static double extractFloat(String field, int loc) {
        String[] split = field.split("[^0-9.]");
        if (split.length > loc) {
            return Double.valueOf(split[loc]);
        }
        return 0;
    }

    public static double[] hashCode(String field, int hashSize){
        double[] hashArray = new double[hashSize];
        hashArray[field.hashCode()%hashSize] = 1;
        return hashArray;
    }

    /**
     * 将map的数据按词表转换成向量
     * @param json json 格式的数据
     * @param keyList 词表
     * @return
     */
    public static double[] map2Vector(String json, List<String> keyList) {
        double[] result = new double[keyList.size()+1];
        JSONObject map = JSON.parseObject(json);
        int i = 0;
        for (String s : keyList) {
            Double d = map.getDouble(s);
            if (d != null) {
                result[i] = d;
            }
            i++;
        }
        return result;
    }

    private static final Pattern chinesePattern = Pattern.compile("[\u4e00-\u9fa5]");
    private static final Pattern englishPattern = Pattern.compile("[A-Za-z0-9.]+");

    public static void main(String[] args) {
        String d = " 1.23";
        System.out.println(Double.valueOf(d));

    }

    /**
     * 统计字数量，
     * eg:
     * word -> 1
     * word1 shihu -> 2
     * shihu 石胡 -> 3
     * shihu 2.0 -> 2
     *
     * @param sentence
     * @return
     */
    public static int wordCount(String sentence) {
        if (StringUtils.isBlank(sentence)) {
            return 0;
        }
        return wordCount(sentence, chinesePattern) + wordCount(sentence, englishPattern);
    }

    private static int wordCount(String sentence, Pattern pattern) {
        Matcher matcher = pattern.matcher(sentence);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 是否是数值
     * 1 => 1
     * asd => 0
     *
     * @param sentence
     * @return
     * @see UtilsOperator#isNumeric
     */
    @Deprecated
    public static double isNumeric(String sentence) {
        return NumberUtils.isDigits(sentence) ? 1.0 : 0.0;
    }

}
