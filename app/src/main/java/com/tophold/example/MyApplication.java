package com.tophold.example;

import android.app.Application;
import android.content.Context;

import com.tophold.example.demo.btc.api.HuobiWebSocket;
import com.tophold.example.demo.forex.api.ForexWebSocket;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 21:38
 * 描 述 ：
 * ============================================================
 **/
public class MyApplication extends Application {
    public static Context mAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mAppContext = this;
        ForexWebSocket.getInstance().init();
        HuobiWebSocket.getInstance().init();
    }
}