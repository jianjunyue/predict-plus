package com.tuhu.algo.etl.features.pojo;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Feature implements Serializable {
    /**
     * sort by this field
     */
    private int id;
    /**
     * 特征名称
     */
    private String name;
    /**
     * 原始特征列表
     */
    private List<String> rawList;
    /**
     * 算子表达式
     */
    private String operator;
    /**
     * 特殊默认值,逗号隔开，内容必须为浮点数，如果不填，采用以下策略
     * 1. 如果returnValueNum为多于1个。按照顺序填充返回向量，缺少的填0，多了的放弃。
     * 2. 如果returnValueNum为1个。不填这个值得话默认填0，填的话默认填这个值
     */
    private List<Double> defaultValues;
    /**
     * 返回向量维度，不填默认为1,
     */
    private int returnValueNum = 1;
    /**
     * 返回向量的坐标，从0开始，数量与returnValueNum一致。
     */
    private List<Integer> returnValueLoc;

    /**
     * 如果想控制结果向量的default值，就设置defaultValues
     * 如果想控制计算的时候填充默认值的，就设置defaultRowMap
     * 计算的时候会先使用defaultRowMap填充计算表达式需要的默认值，如果还是存在缺少的值，就放回defaultValues
     */
    private String defaultRawMap;
    
    /**
     * 控制异常值检测
     * 1. 检测返回false则使用默认值填充。
     */
    private String outlierCheck;
}