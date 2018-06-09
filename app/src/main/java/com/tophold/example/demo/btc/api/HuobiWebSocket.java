package com.tophold.example.demo.btc.api;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.ByteString;
import com.tophold.example.BuildConfig;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 21:15
 * 描 述 ：
 * ============================================================
 **/
public class HuobiWebSocket {
    public static final String TAG = HuobiWebSocket.class.getSimpleName();
    private static final int DEFAULT_TIME_OUT = 5;//超时时间 5s
    private static final int DEFAULT_READ_TIME_OUT = 10;
    //temp quotes
    private static final String BASE_URL = "https://api.huobi.br.com:443/ws";

    Request mRequest;
    private okhttp3.WebSocket mWebSocket;
    OkHttpClient client;
    boolean isConnctted = false;

    public WebSocket getWebSocket() {
        return mWebSocket;
    }

    private HuobiWebSocket() {
        // 创建 OKHttpClient
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);//连接超时时间
        builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//写操作 超时时间
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//读操作超时时间
        builder.pingInterval(5000, TimeUnit.MILLISECONDS);
        builder.retryOnConnectionFailure(true);//失败重试

        //DEBUG模式下 添加日志拦截器
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            builder.addInterceptor(interceptor);
        }

        // 添加公共参数拦截器
        HttpCommonInterceptor commonInterceptor = new HttpCommonInterceptor.Builder()
                .addHeaderParams("paltform", "android")
                .addHeaderParams("userToken", "1234343434dfdfd3434")
                .addHeaderParams("userId", "123445")
                .build();
        //builder.addInterceptor(commonInterceptor);


        //HuobiWebSocket
        client = builder.build();
    }

    public void init() {
        mRequest = new Request.Builder().url(BASE_URL).build();
        mWebSocket = client.newWebSocket(mRequest, new WebSocketListener() {
            @Override
            public void onOpen(okhttp3.WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                isConnctted = true;
                Log.d(TAG, "onOpen: ");
                //Toast.makeText(MyApplication.mAppContext, "websocket连接成功...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMessage(okhttp3.WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Log.d(TAG, "onMessage: " + text);
                HuobiSocketParser.fix2Object(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                String s = null;
                try {
                    s = GZipUtil.uncompressBytes(bytes.toByteArray());
                    HuobiSocketParser.fix2Object(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClosed(okhttp3.WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                isConnctted = false;
                Log.d(TAG, "onClosed,code: " + code + ",reason:" + reason);
                //Toast.makeText(MyApplication.mAppContext, "websocket关闭...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(okhttp3.WebSocket webSocket, Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                isConnctted = false;
                Log.e(TAG, "onFailure: " + (response != null ? response.toString() : "response==null"));
                t.printStackTrace();
            }
        });
    }

    private static class SingletonHolder {
        private static final HuobiWebSocket INSTANCE = new HuobiWebSocket();
    }

    /**
     * 获取RetrofitServiceManager
     *
     * @return
     */
    public static HuobiWebSocket getInstance() {
        return HuobiWebSocket.SingletonHolder.INSTANCE;
    }

}
