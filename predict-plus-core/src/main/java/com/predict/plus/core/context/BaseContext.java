package com.predict.plus.core.context;

import java.util.HashMap;
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
    private Map<String, Long> costTimeMap=new HashMap<>();

    /**
     * 日志开关
     */
    private boolean logSwitch;

    /**
     * debug日志开关
     */
    private boolean logDebugSwitch;

}
