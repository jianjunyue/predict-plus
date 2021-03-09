//package com.predict.plus.algo.common;
//
//
//
//import com.alibaba.fastjson.JSON;
//import com.google.gson.GsonBuilder;
//import com.tuhu.predict.common.constant.CommonConstant;
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang.StringUtils;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.Enumeration;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * 1.多线程情况下ServletRequestAttributes丢失问题
// * https://my.oschina.net/xiaominmin/blog/3052358
// * 2.feignClient Hystrix=THREAD情况下ServletRequestAttributes丢失问题
// * http://www.cppcns.com/ruanjian/java/252537.html
// *
// * @Author: fc.w
// * @Date: 2020/3/7 19:32
// */
//@Slf4j
//@Component
//public class RequestIdInterceptor implements RequestInterceptor {
//    /**
//     * 服务请求的requestId
//     */
//    public static final String REQUEST_ID = "requestid";
//
//    @Bean
//    public FeignHystrixConcurrencyStrategy feignHystrixConcurrencyStrategy() {
//        return new FeignHystrixConcurrencyStrategy();
//    }
//
//    @Override
//    public void apply(RequestTemplate requestTemplate) {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
//                .getRequestAttributes();
//        HttpServletRequest request = attributes.getRequest();
//        Enumeration<String> headerNames = request.getHeaderNames();
//        if (null != attributes) {
//            if (headerNames != null) {
//                while (headerNames.hasMoreElements()) {
//                    String name = headerNames.nextElement();
//                    if (CommonConstant.REQUEST_ID.equalsIgnoreCase(name)) {
//                        String values = request.getHeader(name);
//                        requestTemplate.header(name, values);
//                    }
//                }
//            }
//            if (StringUtils.isBlank(request.getHeader(REQUEST_ID))) {
//                requestTemplate.header(REQUEST_ID, UUID.randomUUID().toString().replaceAll("-", ""));
//            }
//        }
//        log.info("RequestInterceptor-Header-url:{}-heads:{}", requestTemplate.url(), new GsonBuilder().disableHtmlEscaping().create().toJson(requestTemplate.headers()));
//    }
//
//    /**
//     * 根据key获取header中对应的值
//     *
//     * @param key
//     * @return
//     */
//    public static String getHeaderValue(String key) {
//        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
//                .getRequestAttributes();
//        // 将RequestAttributes对象设置为子线程共享 解决多线程情况下ServletRequestAttributes丢失问题
//        // 无多线程外部接口调用可以去掉
//        RequestContextHolder.setRequestAttributes(attributes, true);
//        if (null != attributes) {
//            HttpServletRequest request = attributes.getRequest();
//            if (null != request) {
//                return request.getHeader(key);
//            }
//        }
//        return "";
//    }
//
//    /**
//     * 生产RequestId/rankId
//     *
//     * @return
//     */
//    public static String generateUniqueId() {
//        return UUID.randomUUID().toString().replaceAll("-", "");
//    }
//
//}
