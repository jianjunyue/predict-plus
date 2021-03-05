package com.predict.plus.common.model;
  
/**
 * 推荐场景
 * */ 
public enum Module {
    PLATFROM_PHASE("platfrom_phase", null),
    
    HOME("home", null),
    CHANNEL("channel", null),
    CHANNEL_FOOD("channel:food", Module.CHANNEL),
    
    /**
     * 夜宵活动
     */
    NIGHT_SNACK_ACTIVITY("night_snack_activity", null);
    
    
	/**
	 * 推荐场景
	 * */
    private String moduleName;

    /**
     * 对应父推荐场景
     * */
    private Module parentModule;

    Module(String moduleName, Module parentModule) {
        this.moduleName = moduleName;
        this.parentModule = parentModule;
    }
    
    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Module getParentModule() {
        return parentModule;
    }

    public void setParentModule(Module parentModule) {
        this.parentModule = parentModule;
    }
 
}
