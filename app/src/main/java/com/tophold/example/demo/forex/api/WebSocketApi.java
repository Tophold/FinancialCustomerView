package com.tophold.example.demo.forex.api;

import android.util.Log;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 21:31
 * 描 述 ：
 * ============================================================
 **/
public class WebSocketApi {
    public static final String TAG = WebSocketApi.class.getSimpleName();

    /**
     * 订阅价格和取消订阅
     *
     * @param isSub true:订阅，false:取消订阅
     */
    public static void subPrice(String symbol, boolean isSub) {
        String str = "35=V|55=" + symbol + "|263=" + (isSub ? 1 : 2);
        Log.d(TAG, "subPrice: " + str);
        if (ForexWebSocket.getInstance().isConnctted) {
            ForexWebSocket.getInstance().mWebSocket.send(str);
        } else {
            Log.e(TAG, "subPrice: websocket未连接");
        }
    }
}
