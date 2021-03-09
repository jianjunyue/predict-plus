package com.predict.plus.common.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * <p>
 * HTTP
 * </p>
 *
 * @Author: fc.w
 * @Date: 2020/09/01 16:58
 */
@Slf4j
public class HttpClient {

	private static final OkHttpClient httpClient = new OkHttpClient();

	/**
	 * Get 请求
	 * 
	 * @param httpGetRequest
	 * @return
	 * @throws HttpException
	 */
	public static HttpResponse<String> get(HttpGetRequest httpGetRequest) throws Exception {
		try {
			HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(httpGetRequest.getUrl()).newBuilder();
			httpGetRequest.getQueryParameters().forEach(httpUrlBuilder::addQueryParameter);
			HttpUrl httpUrl = httpUrlBuilder.build();
			Request request = new Request.Builder().get().url(httpUrl).headers(Headers.of(httpGetRequest.getHeaders()))
					.build();
			Response response = httpClient.newCall(request).execute();
			return getResponseBodyString(response);
		} catch (Exception e) {
			throw new Exception("http请求失败", e);
		}
	}

	/**
	 * POST 请求
	 * 
	 * @param httpPostRequest
	 * @return
	 * @throws HttpException
	 */
	public static HttpResponse<String> post(HttpPostRequest httpPostRequest) throws Exception {
		try {
			HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(httpPostRequest.getUrl()).newBuilder();
			httpPostRequest.getQueryParameters().forEach(httpUrlBuilder::addQueryParameter);
			HttpUrl httpUrl = httpUrlBuilder.build();

			RequestBody requestBody = RequestBody.create(MediaType.parse(httpPostRequest.getContentType()),
					httpPostRequest.getBody());
			Request request = new Request.Builder().post(requestBody).url(httpUrl)
					.headers(Headers.of(httpPostRequest.getHeaders())).build();
			Response response = httpClient.newCall(request).execute();
			return getResponseBodyString(response);
		} catch (Exception e) {
			throw new Exception("http请求失败", e);
		}
	}

	/**
	 * POST Form表单格式提交
	 * 
	 * @param httpPostFormRequest
	 * @return
	 * @throws HttpException
	 */
	public static HttpResponse<String> postForm(HttpPostFormRequest httpPostFormRequest) throws Exception {
		try {
			HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(httpPostFormRequest.getUrl()).newBuilder();
			httpPostFormRequest.getQueryParameters().forEach(httpUrlBuilder::addQueryParameter);
			HttpUrl httpUrl = httpUrlBuilder.build();

			FormBody.Builder formBodyBuilder = new FormBody.Builder();
			httpPostFormRequest.getFormData().forEach(formBodyBuilder::add);
			RequestBody requestBody = formBodyBuilder.build();

			Request request = new Request.Builder().post(requestBody).url(httpUrl)
					.headers(Headers.of(httpPostFormRequest.getHeaders())).build();
			Response response = httpClient.newCall(request).execute();
			return getResponseBodyString(response);
		} catch (Exception e) {
			throw new Exception("http请求失败", e);
		}
	}

	public static HttpClient.HttpResponse<String> uploadFile(HttpClient.UploadFileRequest uploadFileRequest)
			throws Exception {
		try {
			HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(uploadFileRequest.getUrl()).newBuilder();
			uploadFileRequest.getQueryParameters().forEach(httpUrlBuilder::addQueryParameter);
			HttpUrl httpUrl = httpUrlBuilder.build();

			MediaType contentType = MediaType.parse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);
			RequestBody fileBody = RequestBody.create(contentType, uploadFileRequest.getFile());

			MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart(uploadFileRequest.getFileFieldName(), uploadFileRequest.getFile().getName(),
							fileBody);
			uploadFileRequest.getFormData().forEach(requestBodyBuilder::addFormDataPart);
			RequestBody requestBody = requestBodyBuilder.build();

			Request request = new Request.Builder().post(requestBody).url(httpUrl)
					.headers(Headers.of(uploadFileRequest.getHeaders())).build();
			Response response = httpClient.newCall(request).execute();
			return getResponseBodyString(response);
		} catch (Exception e) {
			throw new Exception("http请求失败", e);
		}
	}

	/**
	 * 文件下载
	 * 
	 * @param downloadFileRequest
	 * @return
	 * @throws HttpException
	 */
	public static File downloadFile(HttpClient.DownloadFileRequest downloadFileRequest) throws Exception {
		BufferedSink bufferedSink = null;
		try {
			File file = new File(downloadFileRequest.getFilePath());
			File parent = file.getParentFile();
			if (!parent.exists()) {
				log.info("Predict-HttpClient-Create Directory:{}", parent);
				parent.mkdir();
			}
			if (parent != null && !parent.mkdirs() && !parent.isDirectory()) {
				throw new IOException("Predict-HttpClient-Directory '" + parent + "' could not be created");
			}

			// 如果模型压缩文件已存在，则不必重复下载。 因为压缩文件名称是唯一的，一个版本对应一个压缩文件。
			if (file.exists()) {
				return file;
			}

			Request request = new Request.Builder().get().url(downloadFileRequest.getUrl())
					.headers(Headers.of(downloadFileRequest.getHeaders())).build();
			Response response = httpClient.newCall(request).execute();
			Sink sink = Okio.sink(file);
			bufferedSink = Okio.buffer(sink);
			bufferedSink.writeAll(response.body().source());
			bufferedSink.flush();
			return file;
		} catch (Exception e) {
			throw new Exception("http下载文件失败", e);
		} finally {
			if (bufferedSink != null) {
				try {
					bufferedSink.close();
				} catch (IOException e) {
					log.warn("bufferedSink close error", e);
				}
			}
		}
	}

	private static HttpResponse<String> getResponseBodyString(Response response) throws Exception {
		HttpResponse<String> httpResponse = new HttpResponse<>();
		addResponseHeaders(response.headers(), httpResponse);
		ResponseBody body = getResponseBody(response);
		if (body != null)
			httpResponse.setBody(body.string());
		return httpResponse;
	}

	private static HttpResponse<byte[]> getResponseBodyBytes(Response response) throws Exception {
		HttpResponse<byte[]> httpResponse = new HttpResponse<>();
		addResponseHeaders(response.headers(), httpResponse);
		ResponseBody body = getResponseBody(response);
		if (body != null)
			httpResponse.setBody(body.bytes());
		return httpResponse;
	}

	private static <T> void addResponseHeaders(Headers headers, HttpResponse<T> httpResponse) {
		if (headers != null) {
			for (int i = 0, size = headers.size(); i < size; i++) {
				httpResponse.addHeader(headers.name(i), headers.value(i));
			}
		}
	}

	private static ResponseBody getResponseBody(Response response) throws Exception {
		if (response.code() != HttpStatus.OK.value()) {
			throw new Exception("响应状态异常 -> statusCode:" + response.code());
		}
		return response.body();
	}

	@Data
	public static class HttpGetRequest {
		private String url;
		private Map<String, String> queryParameters = new HashMap<>();
		private Map<String, String> headers = new HashMap<>();

		public void addQueryParameter(String name, String value) {
			queryParameters.put(name, value);
		}

		public void addHeader(String name, String value) {
			headers.put(name, value);
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class HttpPostRequest extends HttpGetRequest {
		private String contentType;
		private String body;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class HttpPostFormRequest extends HttpGetRequest {
		private Map<String, String> formData = new HashMap<>();

		public void addFormData(String name, String value) {
			formData.put(name, value);
		}
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class UploadFileRequest extends HttpPostFormRequest {
		private String fileFieldName = "file";
		private File file;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class DownloadFileRequest extends HttpGetRequest {
		private String filePath;
		private String fileName;
	}

	@Data
	@ToString
	public static class HttpResponse<T> {
		private T body;
		private Map<String, String> headers = new HashMap<>();

		public void addHeader(String name, String value) {
			headers.put(name, value);
		}
	}
}
