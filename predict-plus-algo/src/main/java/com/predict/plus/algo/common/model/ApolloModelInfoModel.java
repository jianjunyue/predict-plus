package com.predict.plus.algo.common.model;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/09/01 20:08
 */
@Data
public class ApolloModelInfoModel implements Serializable {

    @ApiModelProperty("模型文件地址")
    private String modelPath;

    @ApiModelProperty("特征版本")
    private Integer version;

    @ApiModelProperty("模型类型")
    private String modelType;

}

