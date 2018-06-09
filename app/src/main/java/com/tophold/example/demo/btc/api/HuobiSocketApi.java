package com.tophold.example.demo.btc.api;

import android.util.Log;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 21:31
 * 描 述 ：
 * ============================================================
 **/
public class HuobiSocketApi {
    public static final String TAG = HuobiSocketApi.class.getSimpleName();

    public static void ping(long time) {
        String str = "{\"ping\":" + time + "}";
        sendService(str);
    }

    public static void pong(long time) {
        String str = "{\"pong\":" + time + "}";
        sendService(str);
    }

    /**
     * 订阅KLine和取消KLine
     *
     * @param id     一个唯一值，建议用时间戳
     * @param symbol
     * @param period 周期，不管任何周期，全部转化为时间戳格式
     * @param sub    true:订阅；false:取消订阅
     */
    public static void kLine(long id, String symbol, long period, boolean sub) {
        String periodByMs = getPeriodByMs(period);
        if (periodByMs == null) {
            Log.e(TAG, "subKLine: period 不合法！！！");
            return;
        }
        String kline = "market." + symbol + ".kline." + periodByMs;
        String json = "{" + (sub ? "\"sub\"" : "\"unsub\"") + ":\"" + kline + "\",\"id\":\"" + id + "\"}";

        sendService(json);

    }

    public static void subKLine(String symbol, long period, boolean sub) {
        long id = System.currentTimeMillis();
        kLine(id, symbol, period, sub);
    }

    private static void sendService(String str) {
        Log.d(TAG, "sendService: " + str);
        HuobiWebSocket.getInstance().getWebSocket().send(str);
    }

    private static String getPeriodByMs(long ms) {
        long s = ms / 1000;
        if (s == 1 * 60) return "1min";
        if (s == 5 * 60) return "5min";
        if (s == 15 * 60) return "15min";
        if (s == 30 * 60) return "30min";
        if (s == 60 * 60) return "60min";
        if (s == 24 * 60 * 60) return "1day";
        if (s == 7 * 24 * 60 * 60) return "1week";
        if (s == 30 * 24 * 60 * 60) return "1mon";
        if (s == 365 * 24 * 60 * 60) return "1year";
        Log.d(TAG, "getPeriodByMs: unkonwn ms!!！");
        return null;
    }
}
