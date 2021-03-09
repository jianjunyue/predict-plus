package com.predict.plus.algo.operator;
 

import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
//import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;

import org.apache.commons.collections4.CollectionUtils;

public class NumberOperator implements Serializable {
    /**
     * 数值分割
     * @param num
     * @param barrelSize 分桶数量 不能等于0
     * @param min 值域最小值
     * @param max 值域最大值
     * @return 当前所在的桶位置
     */
    public static double cut(double num,int barrelSize,double min,double max){
        double modMagic = (max - min)/barrelSize;
        if(num < min){
            return 0;
        }else if(num >= max){
            return barrelSize+1;
        }
        return Math.floor((num-min)/modMagic)+1;
    }

    /**
     * 数值切分，返回的是barrelSize+2各向量
     * 在cut(double num,int barrelSize,double min,double max)前面填上1
     * @param num
     * @param barrelSize 不能等于0
     * @param min
     * @param max
     * @return
     */
    public static double[] cutWithFill(double num, int barrelSize, double min, double max) {
        double[] result = new double[barrelSize+2];
        double loc = cut(num, barrelSize, min, max);
        for (int i = 0;i <= loc;i ++) {
            result[i] = 1;
        }
        return result;
    }

    /**
     * 0-1缩放
     * 小于min 返回0，大于max返回1
     * @param num
     * @param min 值域最小值
     * @param max 值域最大值
     * @return
     */
    public static double scaleZeroOne(double num,double min,double max){
        double div = max - min;
        if(num < min){
            return 0;
        }else if(num > max){
            return 1;
        }
        return (num - min)/div;
    }

    /**
     * 安全的除法运算
     * @param x 分子
     * @param y 分母
     * @param defaultValue 当分母为0的时候使用的默认值
     * @return
     */
    public static double div(double x,double y, double defaultValue){
        if(y == 0.0){
            return defaultValue;
        }else{
            return x/y;
        }
    }

    /**
     * 安全的除法运算
     * @param x 分子
     * @param y 分母
     * @return
     */
    public static double div(double x,double y){
        if(y == 0.0){
            return -1;
        }else{
            return x/y;
        }
    }

    /**
     * 数据切分算子
     * @param number 待切分的数值
     * @param binBound 入参需保证从小到大排序
     * @return
     * bins = Lists.newArrayList(1.0,100.0,1000.0,10000.0);
     * (-inf,1.0) = 0
     * [1,100) = 1
     * [100,1000) = 2
     * [1000,10000) = 3
     * [10000,inf) = 4
     */
    public static double cutWithRange(double number, List<Double> binBound){
        int insertPoint = Collections.binarySearch(binBound,number);
        if(insertPoint == -1){
            return 0;
        }
        if (insertPoint < 0) {
            return -insertPoint - 1;
        }
        if (insertPoint >= 0) {
            return insertPoint + 1;
        }
        return insertPoint;
    }

    /**
     * 数据切分算子
     *
     * @param number   待切分的数值
     * @param binBound 入参需保证从小到大排序
     * @return result[cutWithRange(number, binBound)] = 1
     */
    public static double[] cutWithRangeAndOneHot(double number, List<Double> binBound) {
        double v = cutWithRange(number, binBound);
        double[] result = new double[binBound.size() + 1];
        result[Double.valueOf(v).intValue()] = 1;
        return result;
    }

    /**
     * 数据切分算子
     *
     * @param number   待切分的数字
     * @param binBound 入参需保证从小到大排序
     * @return
     */
    public static double[] cutWithRangeAndFill(double number, List<Double> binBound) {
        double v = cutWithRange(number, binBound);
        double[] result = new double[binBound.size() + 1];

        for (int i = 0;i <= v; i++) {
            result[i] = 1;
        }
        return result;
    }

    /**
     * 多项式生成
     * @param degree >= 1
     * @param rawData
     * @return
     */
    public static double[] polynomial(int degree, double... rawData) {
//        if (degree <= 1) {
            return rawData;
//        }
//        PolynomialFunction polynomialFunction = new PolynomialFunction(rawData);
//        PolynomialFunction multi = new PolynomialFunction(rawData);
//
//        for (int i = 1; i < degree; i++) {
//            polynomialFunction = polynomialFunction.multiply(multi);
//        }
//        return polynomialFunction.getCoefficients();
    }

    /**
     * 异常值填充
     * @param feature 原始特征
     * @param min 值域最小值
     * @param max 值域最大值
     * @param defaultValue 特征异常时采用的默认值
     * @return
     */
    public static double outlierFill(double feature, double min, double max, double defaultValue) {
        if (feature >= min && feature <= max) {
            return feature;
        } else {
            return defaultValue;
        }
    }

    static Map<String, BiFunction<Double, Double, Double>> funcMap = new HashMap<>();
    static {
        funcMap.put("+", (x, y) -> x+y);
        funcMap.put("-", (x, y) -> x-y);
        funcMap.put("*", (x, y) -> x*y);
        funcMap.put("/", (x, y) -> x/y);
    }
    /**
     * 使用 lambda 表达式交叉
     * ps: 使用 / 的时候注意分母为0
     * @return
     */
    public static double[] interWithFunc(String funcSign, double... features) {
        BiFunction<Double, Double, Double> func = funcMap.get(funcSign);
        double[] result = new double[features.length * (features.length - 1) / 2];
        int loc = 0;
        for (int i = 0; i < features.length; i++) {
            for (int j = i + 1; j < features.length; j++) {
                result[loc++] = func.apply(features[i], features[j]);
            }
        }
        return result;
    }

    public static void main(String args[]) {
        System.out.println(new NumberOperator().cutWithRangeAndOneHot(6.99, Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0)));
    }

    /**
     * 向量点乘
     * @throws Exception 
     */
    public static double dot(List<Double> vector1, List<Double> vector2, double fillNa) {
        if (CollectionUtils.isEmpty(vector1) || CollectionUtils.isEmpty(vector2)) {
            return 0;
        }
        if (vector1.size() != vector2.size()) {
        	return 0;
//            throw new Exception("dot 算子要求长度匹配");
        }
        double result = 0;
        for (int i = 0; i < vector1.size(); i++) {
            Double d1 = vector1.get(i);
            Double d2 = vector2.get(i);
            if (d1 == null) {
                d1 = fillNa;
            }

            if (d2 == null) {
                d2 = fillNa;
            }

            result += d1 * d2;
        }
        return result;
    }

    public static double dot(double[] vector1, double[] vector2) {
        return dot(Arrays.stream(vector1).boxed().collect(Collectors.toList()),
                Arrays.stream(vector2).boxed().collect(Collectors.toList()), 0);
    }


}
