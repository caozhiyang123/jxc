package com.site.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by lihan on 2017-11-15.
 */
public class HttpClientUtil {

    private static final String CHARSET = "UTF-8";

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19";

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

    public static String get(String url,Map<String,String> parameters,Map<String,String> headers){
        String responseBody = null;
        CloseableHttpClient httpClient = createDefaultHttpClient();
        try {
            if(StringUtils.isBlank(url))
                return null;
            if(null != parameters && !parameters.isEmpty()){
                List<NameValuePair> pairs = new ArrayList<NameValuePair>(parameters.size());
                for(Map.Entry<String,String> entry : parameters.entrySet()){
                    String value = entry.getValue();
                    if(value != null){
                        pairs.add(new BasicNameValuePair(entry.getKey(),value));
                    }
                }
                url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(pairs, CHARSET));
            }
            HttpGet httpGet = new HttpGet(url);
            if(null != headers){
                for(Map.Entry entry : headers.entrySet())
                    httpGet.addHeader(entry.getKey().toString(),entry.getValue().toString());
            }
            responseBody = httpClient.execute(httpGet,createDefaultResponseHandler());
        } catch (Exception e) {
            LOG.error("Get URL : {} occur error : {}",url,e);
        }
        return responseBody;
    }

    public static String post(String url, List<BasicNameValuePair> params, Map<String,String> heads){
        String responseBody = "";
        try {
            CloseableHttpClient httpClient = createDefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            if(null != params)
                httpPost.setEntity(new UrlEncodedFormEntity(params));
            if (null != heads) {
                for (Map.Entry e : heads.entrySet()) {
                    httpPost.addHeader(e.getKey().toString(), e.getValue().toString());
                }
            }
            responseBody = httpClient.execute(httpPost,createDefaultResponseHandler());
        } catch (Exception e) {
            LOG.error("http post to {} throw Exception : {}", url,e);
        }
        return responseBody;
    }

    public static String postJson(String url,String json,Map<String,String> heads){
        String responseBody = "";
        try {
            CloseableHttpClient httpClient = createDefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(json, "utf-8"));
            if (heads != null) {
                for (Map.Entry e : heads.entrySet()) {
                    httpPost.addHeader(e.getKey().toString(), e.getValue().toString());
                }
            }
            responseBody = httpClient.execute(httpPost,createDefaultResponseHandler());
        } catch (Exception e) {
            LOG.error("http post to {} throw Exception : {}", url,e);
        }
        return responseBody;
    }

    /**
     * Response Handler
     * @return
     */
    public static ResponseHandler<String> createDefaultResponseHandler(){
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if(status == HttpStatus.SC_OK){
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity,CHARSET) : null;
                }
                else {
                    LOG.error("Unexpected response status: " + status);
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };
        return responseHandler;
    }

    /**
     * Request RetryHandler
     * @return
     */
    public static HttpRequestRetryHandler createDefaultRequestRetryHandler(){
        HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(
                    IOException exception,
                    int executionCount,
                    HttpContext context) {
                if (executionCount >= 5) {
                    // Do not retry if over max retry count
                    return false;
                }
                if (exception instanceof InterruptedIOException) {
                    // Timeout
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    // Unknown host
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {
                    // Connection refused
                    return false;
                }
                if (exception instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // Retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };
        return myRetryHandler;
    }


    public static CloseableHttpClient createDefaultHttpClient(){
        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(10000)
                .setConnectTimeout(10000)
                .setSocketTimeout(10000).build();
        return HttpClientBuilder.create()
                .setUserAgent(USER_AGENT)
                .setRetryHandler(createDefaultRequestRetryHandler())
                        //.setDefaultCookieSpecRegistry()
                .setDefaultRequestConfig(config)
                        //.setDefaultCookieStore()
                .build();
    }

    /**
     * HTTPS
     * @return
     */
    public static CloseableHttpClient createSSLClient(){
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }

    /**
     * 根据url获取重定向地址
     * @param url
     * @return
     * @throws IOException
     */
    public static List<URI> getRedirectLocations(String url) throws IOException {
        List<URI> redirectLocations = null;
        CloseableHttpResponse httpResponse = null;
        try {
            CloseableHttpClient httpClient = createDefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpClientContext context = HttpClientContext.create();
            httpResponse = httpClient.execute(httpGet,context);
            //获取重定向的地址
            redirectLocations = context.getRedirectLocations();
        } finally {
            if(httpResponse != null){
                httpResponse.close();
            }
        }
        return redirectLocations;
    }

    public static void main(String[] args) throws IOException {
//        List<BasicNameValuePair> data = new ArrayList<BasicNameValuePair>();
//        data.add(new BasicNameValuePair("username", "shandian"));
//        data.add(new BasicNameValuePair("password", "666666"));
//        data.add(new BasicNameValuePair("token", "5B6F6380E7BFD10EDC0786A4FB85E797170B58DA45CDE7FF83C9895A9FB7C7AE355A62D1754D3AB85F0C645A66364880"));
//        String url = "http://www.ovopark.com/service/mobileLogin.action";
//        String xx = post(url,data,null);
//        System.out.println("sss" + JSONObject.parse(xx));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar ca = Calendar.getInstance();
        try {
            ca.setTime(sdf.parse("2018-01-01 00:00:00"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = sdf.format(ca.getTime());
        System.out.println(last);
    }
}
