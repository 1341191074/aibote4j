package net.aibote.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类 httpclient5
 */
@Slf4j
public class HttpClientUtils {

    private static CloseableHttpClient httpClient;

    /**
     * post请求 json参数
     *
     * @param url
     * @param bodyJsonParams
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doPost(String url, String bodyJsonParams, Map<String, String> headers) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        //httpPost.setProtocolVersion(new ProtocolVersion("HTTP", 1, 0));
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(bodyJsonParams, StandardCharsets.UTF_8));

        addHeader(httpPost, headers);
        return execute(httpPost);
    }

    /**
     * post请求 json参数
     *
     * @param url
     * @param httpEntity
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doPost(String url, HttpEntity httpEntity, Map<String, String> headers) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        //httpPost.setProtocolVersion(new ProtocolVersion("HTTP", 1, 1));
        //httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(httpEntity);

        addHeader(httpPost, headers);
        return execute(httpPost);
    }

    /**
     * post k-v参数
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doPost(String url, Map<String, Object> params, Map<String, String> headers) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        //httpPost.setProtocolVersion(new ProtocolVersion("HTTP", 1, 1));
        if (params != null && !params.keySet().isEmpty()) {
            httpPost.setEntity(getUrlEncodedFormEntity(params));
        }
        addHeader(httpPost, headers);
        return execute(httpPost);
    }

    /**
     * patch json参数
     *
     * @param url
     * @param bodyJsonParams
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doPatch(String url, String bodyJsonParams, Map<String, String> headers) throws Exception {
        HttpPatch httpPatch = new HttpPatch(url);
        httpPatch.setEntity(new StringEntity(bodyJsonParams));
        addHeader(httpPatch, headers);
        return execute(httpPatch);
    }

    /**
     * patch k-v参数
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doPatch(String url, Map<String, Object> params, Map<String, String> headers) throws Exception {
        HttpPatch httpPatch = new HttpPatch(url);
        if (params != null && !params.isEmpty()) {
            httpPatch.setEntity(getUrlEncodedFormEntity(params));
        }
        addHeader(httpPatch, headers);
        return execute(httpPatch);
    }

    /**
     * PUT JSON参数
     *
     * @param url
     * @param bodyJsonParams
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doPut(String url, String bodyJsonParams, Map<String, String> headers) throws Exception {
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", "application/json");
        httpPut.setEntity(new StringEntity(bodyJsonParams, StandardCharsets.UTF_8));

        addHeader(httpPut, headers);
        return execute(httpPut);
    }

    /**
     * put k-v参数
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doPut(String url, Map<String, Object> params, Map<String, String> headers) throws Exception {
        HttpPut httpPut = new HttpPut(url);
        if (params != null && params.keySet().isEmpty()) {
            httpPut.setEntity(getUrlEncodedFormEntity(params));
        }
        addHeader(httpPut, headers);
        return execute(httpPut);
    }

    /**
     * delete k-v参数
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doDelete(String url, Map<String, Object> params, Map<String, String> headers) throws Exception {

        StringBuilder paramsBuilder = new StringBuilder(url);
        if (params != null && !params.keySet().isEmpty()) {
            if (url.indexOf("?") == -1) {
                paramsBuilder.append("?");
            }
            String paramsStr = EntityUtils.toString(Objects.requireNonNull(getUrlEncodedFormEntity(params)));
            paramsBuilder.append(paramsStr);
        }

        HttpDelete httpDelete = new HttpDelete(paramsBuilder.toString());
        addHeader(httpDelete, headers);

        return execute(httpDelete);
    }

    /**
     * head请求
     *
     * @param url
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doHeader(String url, Map<String, String> headers) throws Exception {
        HttpHead httpHead = new HttpHead(url);
        addHeader(httpHead, headers);
        return execute(httpHead);

    }

    /**
     * get请求
     *
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws IOException
     */
    public static String doGet(String url, Map<String, Object> params, Map<String, String> headers) throws Exception {
        // 参数
        StringBuilder paramsBuilder = new StringBuilder(url);
        if (params != null && !params.keySet().isEmpty()) {
            if (url.indexOf("?") == -1) {
                paramsBuilder.append("?");
            }
            String paramsStr = EntityUtils.toString(getUrlEncodedFormEntity(params));
            paramsBuilder.append(paramsStr);
        }
        HttpGet httpGet = new HttpGet(paramsBuilder.toString());
        addHeader(httpGet, headers);
        return execute(httpGet);
    }

    /**
     * 执行请求并返回string值
     *
     * @param httpUriRequest
     * @return
     * @throws IOException
     */
    private static String execute(HttpUriRequest httpUriRequest) throws IOException, ParseException {
        if (null == httpClient) {
            synchronized (httpUriRequest) {
                httpClient = HttpClients.createDefault();
                log.info("加锁，创建httpClient");
            }
        }
        CloseableHttpResponse response = httpClient.execute(httpUriRequest);
        String defaultCharset = "UTF-8";
        if (null != response.getEntity().getContentType()) {
            String charset = getCharSet(response.getEntity().getContentType());
            if (!StringUtils.isEmpty(charset)) {
                defaultCharset = charset;
            }
        }

        return EntityUtils.toString(response.getEntity(), defaultCharset);

    }

    /**
     * 添加请求头部
     *
     * @param httpUriRequest
     * @param headers
     */
    private static void addHeader(HttpUriRequest httpUriRequest, Map<String, String> headers) {
        if (httpUriRequest != null) {
            if (headers != null && !headers.keySet().isEmpty()) {
                Set<String> keySet = headers.keySet();
                for (String key : keySet) {
                    String value = headers.get(key);
                    httpUriRequest.addHeader(key, value);
                }
            }
        }
    }

    /**
     * 获取 UrlEncodedFormEntity 参数实体
     *
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    private static UrlEncodedFormEntity getUrlEncodedFormEntity(Map<String, Object> params) throws UnsupportedEncodingException {
        if (params != null && !params.keySet().isEmpty()) {
            List<NameValuePair> list = new ArrayList<>();
            Set<String> keySet = params.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                log.info("key :" + key);
                Object value = params.get(key);
                log.info("value:" + value);
                if (value == null) {
                    continue;
                }
                String valueStr = value.toString();
                list.add(new BasicNameValuePair(key, valueStr));
            }
            return new UrlEncodedFormEntity(list, Charset.defaultCharset());
        }
        return null;
    }

    /**
     * 根据HTTP 响应头部的content type抓取响应的字符集编码
     *
     * @param content
     * @return
     */
    private static String getCharSet(String content) {
        String regex = ".*charset=([^;]*).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) return matcher.group(1);
        else return null;
    }

}
