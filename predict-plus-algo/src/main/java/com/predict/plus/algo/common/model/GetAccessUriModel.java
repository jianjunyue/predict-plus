package com.predict.plus.algo.common.model;


import lombok.Data;

import java.util.List;

/**
 * <p>获取私有资源访问链接对接</p>
 *
 * @Author: fc.w
 * @Date: 2020/09/01 13:49
 */
@Data
public class GetAccessUriModel {

    private List<String> result;
    private Integer elapsedMilliseconds;
    private Boolean success;

}