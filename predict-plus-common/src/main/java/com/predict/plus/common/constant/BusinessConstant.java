package com.predict.plus.common.constant;

public class BusinessConstant {

    public static String LOG_FINDPAGE_SWITCH = "log_findPage_switch";

    public static String LOG_GUESS_CTR_SWITCH = "log_guess_ctr_switch";

    public static String LOG_MODEL_PREDICT_SWITCH = "log_model_predict_switch";

    public static String LOG_DEBUG_PLATFORM_SWITCH = "log_debug_platform_switch";

    public static String GUESS_CTR_MODEL_VERSION = "guess_ctr_model_version";

    public static String ALGORITHM_PLATFORM_MODELNAME_LIST = "algorithm_platform_modelName_list";

    public static String MODEL_PREDICT_BATCH_COUNT = "model_predict_batch_count";

    public static String MODEL_FEATURE_BATCH_COUNT = "model_feature_batch_count";

    public static String predictPlatformContextInit = "predictPlatformContextInit";
    public static String onlineContextFeaturePhase = "onlineContextFeaturePhase";
    public static String featureReadyStatusPhase = "featureReadyStatusPhase";
    public static String featurePlatformPhase = "featurePlatformPhase";
    public static String getDimensionDataPhase = "getDimensionDataPhase";
    public static String rawFeatureCombinePhase = "rawFeatureCombinePhase";
    public static String rawFeature2ModelFeaturePhase = "rawFeature2ModelFeaturePhase";
    public static String predictPhase = "predictPhase";
    public static String sortPhase = "sortPhase";

    /**
     * 特征维度定义
     */
    public final static String USER = "USER";
    public final static String CROSS = "CROSS";
    public final static String PRODUCT = "PRODUCT";

    /**
     * 模型类型
     */
    public final static String XGBOOST = "XGBoost";
    public final static String LIGHTGBM = "LightGBM";
    public final static String DEEP_MODEL = "DeepModel";
    public final static String DEEP_SEQ = "DeepSeq";

    /**
     * 模型名称
     */
    public final static String SECKILL_PAGE_CVR_MODEL = "seckill_page_cvr_model";
    public final static String TIRE_CTR_RANKING = "tr_ctr_ranking_lgb";
    public final static String TIRE_CVR_RANKING = "tr_cvr_ranking_lgb";
    public final static String TIRE_CTCVR_RANKING = "tr_ctcvr_ranking_lgb";
    public final static String BAOYANG_OIL_SUGGEST_MODEL = "oil_suggest";

    public final static String TR_CTR_RANKING_LGB_V2_2 = "tr_ctr_ranking_lgb_v2_2";
    public final static String TR_CVR2_RANKING_LGB_V2_2 = "tr_cvr2_ranking_lgb_v2_2";

}
