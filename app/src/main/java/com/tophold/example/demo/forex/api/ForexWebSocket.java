package com.tophold.example.demo.forex.api;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocketListener;
import okhttp3.logging.HttpLoggingInterceptor;
import com.tophold.example.BuildConfig;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 21:15
 * 描 述 ：
 * ============================================================
 **/
public class ForexWebSocket {
    public static final String TAG = ForexWebSocket.class.getSimpleName();
    private static final int DEFAULT_TIME_OUT = 5;//超时时间 5s
    private static final int DEFAULT_READ_TIME_OUT = 10;
    //temp quotes
    private static final String BASE_URL = "http://***:8990/websocket";
    private static final String TOKEN="eyJ0eXAiOiJKV1QiLCJhbGc***Y2NvdW50SWQiOiJFVVI2MTQxLjAwMSIsImlhdCI6MTUyMjgxOTE0NiwiZXhwIjoxNTIzNDIzOTQ2fQ.je5e76RSNh07ZYtu3_yOA8BsBl_wH0_tkqxlgX5hhww";

    Request mRequest;
    okhttp3.WebSocket mWebSocket;
    OkHttpClient client;
    boolean isConnctted = false;

    private ForexWebSocket() {
        // 创建 OKHttpClient
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);//连接超时时间
        builder.writeTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//写操作 超时时间
        builder.readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS);//读操作超时时间
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
        builder.addInterceptor(commonInterceptor);


        //HuobiWebSocket
        client = builder.build();
    }

    public void init() {
        mRequest = new Request.Builder().header("Sec-WebSocket-Protocol", TOKEN).url(BASE_URL).build();
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
                WebSocketParser.fix2Object(text);
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
                //Toast.makeText(MyApplication.mAppContext, "websocket异常：" + response.toString(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: " + (response != null ? response.toString() : "response==null"));
                t.printStackTrace();
            }
        });
    }

    private static class SingletonHolder {
        private static final ForexWebSocket INSTANCE = new ForexWebSocket();
    }

    /**
     * 获取RetrofitServiceManager
     *
     * @return
     */
    public static ForexWebSocket getInstance() {
        return ForexWebSocket.SingletonHolder.INSTANCE;
    }

}
