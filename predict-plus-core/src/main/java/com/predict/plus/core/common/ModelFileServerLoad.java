package com.predict.plus.core.common;


import com.alibaba.fastjson.JSON;
import com.predict.plus.algo.common.model.GetAccessUriModel;
import com.predict.plus.common.utils.HttpClient;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;  
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * <p></p>
 * 
 * @Date: 2020/11/13 16:22
 */
@Component
@Slf4j
public class ModelFileServerLoad {

 
	public String parentPath="/data/webroot";
 
    private String fileServerUrl="http://wcf.tuhu.work:9010/Utility/RemoteUpload/GetAccessUri";

    /**
     * 获取模型文件流
     * @param getAccessUriModel
     * @return
     */
    public File downloadFile(GetAccessUriModel getAccessUriModel, String modelPath) {
        try {
            if (null == getAccessUriModel) {
                return null;
            }

            List<String> urlList = getAccessUriModel.getResult();
            if (CollectionUtils.isNotEmpty(urlList)) {
                // 下载模型zip文件
                String localFilePath = parentPath + modelPath;
                String fileUrl = urlList.get(0);
                log.info("ModelFileServerLoad-downloadFile-localFilePath:{}-fileUrl:{}", localFilePath, fileUrl);
                HttpClient.DownloadFileRequest request = new HttpClient.DownloadFileRequest();
                request.setUrl(fileUrl);
                request.setFilePath(localFilePath);
                File file = HttpClient.downloadFile(request);
                return file;
            }
        } catch (Exception e) {
            log.info("ModelFileServerLoad-downloadFile-filePath:{}-Exception", e);
        }
        return null;
    }


    /**
     * 获取文件服务器中文件地址信息
     * @param filePath
     * @return
     */
    public GetAccessUriModel getAccessUriModel(String filePath) {
        try {
            HttpClient.HttpGetRequest httpGetRequest = new HttpClient.HttpGetRequest();
            httpGetRequest.setUrl(fileServerUrl);
            httpGetRequest.addHeader("requestID", generateUniqueId());
            httpGetRequest.addQueryParameter("paths", filePath);

            HttpClient.HttpResponse<String> response = HttpClient.get(httpGetRequest);
            if (null != response && null != response.getBody()) {
                // 解析
                log.info("ModelFileServerLoad-getAccessUriModel-body:{}", response.getBody());
                GetAccessUriModel getAccessUriModel = JSON.parseObject(response.getBody(), GetAccessUriModel.class);
                return getAccessUriModel;
            }
        } catch (Exception e) {
            log.error("ModelFileServerLoad-getAccessUriModel-HttpException", e);
        }

        return null;
    }
    
    public String generateUniqueId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }


}