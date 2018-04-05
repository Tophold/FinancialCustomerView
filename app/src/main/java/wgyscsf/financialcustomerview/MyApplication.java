package wgyscsf.financialcustomerview;

import android.app.Application;
import android.content.Context;

import wgyscsf.financialcustomerview.demo.btc.api.HuobiWebSocket;
import wgyscsf.financialcustomerview.demo.forex.api.ForexWebSocket;

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