package com.predict.plus.core.platform.feature.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/11/06 17:58
 */
@Data
public class CombineKeyDimension {

    @ApiModelProperty("pid组合维度key")
    private Map<String, String> combineAndPidKeyMap;

    @ApiModelProperty("uid组合维度key")
    private String uidCombineKey;

    @ApiModelProperty("组合维度依赖的最小维度")
    private String dimension;

}
