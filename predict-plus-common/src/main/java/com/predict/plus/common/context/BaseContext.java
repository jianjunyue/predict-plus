package com.predict.plus.common.context;

import java.util.Map;
  
import lombok.Data;

@Data
public class BaseContext {

    /**
     * rankId
     */
    private String rankId;

    /**
     * requestId
     */
    private String requestId;

    /**
     * userId
     */
    private String userId;

    /**
     * 耗时记录
     */
    private Map<String, Long> costTimeMap;

    /**
     * 日志开关
     */
    private boolean logSwitch;

    /**
     * debug日志开关
     */
    private boolean logDebugSwitch;

}