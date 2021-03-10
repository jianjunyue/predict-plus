package com.predict.plus.core.model;


import lombok.Data;

import java.util.List;

/**
 * <p>执行优先级集合</p>
 *
 * @Author: fc.w
 * @Date: 2020/11/05 18:45
 */
@Data
public class ExecutorPriority {

    private List<String> highestPriorityList;
    private List<String> mediumPriorityList;
    private List<String> lowPriorityList;

}
