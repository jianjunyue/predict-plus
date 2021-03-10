package com.predict.plus.core.common;

import com.alibaba.fastjson.JSON;
//import com.alicp.jetcache.Cache;
//import com.alicp.jetcache.CacheLoader;
//import com.alicp.jetcache.RefreshPolicy;
//import com.alicp.jetcache.anno.CacheType;
//import com.alicp.jetcache.anno.CreateCache;
import com.google.common.collect.Maps; 
import lombok.extern.slf4j.Slf4j;
 
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p></p>
 *
 * @Author: fc.w
 * @Date: 2020/12/04 13:53
 */
@Component
@Slf4j
public class DataReadyStatusCacheStrategy {

//    @RenaultField(cacheName = "mlpPredict")
//    private com.tuhu.renault.Cache<String, String> redisCache;
//
//    @CreateCache(name = "dataReadyStatusCache", expire = 60, timeUnit = TimeUnit.MINUTES,
//            cacheType = CacheType.LOCAL, localLimit = 2000)
//    private Cache<String, String> cache;
//
//    @PostConstruct
//    public void init() {
//        RefreshPolicy policy = RefreshPolicy.newPolicy(30, TimeUnit.MINUTES)
//                .stopRefreshAfterLastAccess(1, TimeUnit.HOURS);
//        cache.config().setRefreshPolicy(policy);
//        cache.config().setLoader(new CacheLoader<String, String>() {
//
//            @Override
//            public String load(String key) throws Throwable {
//                return loadByKey(key);
//            }
//
//            @Override
//            public Map<String, String> loadAll(Set<String> keys) throws Throwable {
//                log.info("DataReadyStatusCacheStrategy-loadAll-keySet:{}", JSON.toJSONString(keys));
//                Map<String, String> dsStatusMap = Maps.newHashMap();
//                if (CollectionUtils.notEmpty(keys)) {
//                    dsStatusMap = loadAllByKeys(keys);
//                }
//                return dsStatusMap;
//            }
//        });
//    }

    public Map<String, String> multiGet(Set<String> keys) {
//        if (CollectionUtils.isNotEmpty(keys) && null != cache) {
//            return cache.getAll(keys);
//        }
        return null;
    }

    private String loadByKey(String key) {
//        try {
//           String value = redisCache.get(key);
//            log.info("DataReadyStatusCacheStrategy-loadByKey-key:{}-value:{}", key, value);
//        } catch (CacheParamException e) {
//            log.warn("DataReadyStatusCacheStrategy-load-CacheParamException", e);
//        }
        return null;
    }


    private Map<String, String> loadAllByKeys(Set<String> keySet) {
        Map<String, String> dsStatusMap = Maps.newHashMap();
//        try {
//            dsStatusMap = redisCache.multiGet(keySet);
//            log.info("DataReadyStatusCacheStrategy-loadAllByKeys-keySet:{}-dsStatusMap:{}", JSON.toJSONString(keySet), JSON.toJSONString(dsStatusMap));
//        } catch (CacheParamException e) {
//            log.warn("DataReadyStatusCacheStrategy-loadAllByKeys-CacheParamException", e);
//        }

        return dsStatusMap;
    }

}