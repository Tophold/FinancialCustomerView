package com.tophold.example.demo.forex.api;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import com.tophold.example.demo.forex.model.WsPrice;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 21:26
 * 描 述 ：
 * ============================================================
 **/
public class WebSocketParser {
    public static final String TAG = WebSocketParser.class.getSimpleName();

    public static void fix2Object(String str) {
        try {
            if (str == null || str.length() == 0) return;
            Log.d(TAG, "fix2Object: " + str);
            String[] arrs = str.split("\\|");
            Map<String, String> map = new HashMap<>();
            for (String arr : arrs) {
                String[] items = arr.split("=");
                map.put(items[0], items[1]);
            }
            //maping
            map2Obj(map);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void map2Obj(Map<String, String> map) {

        String type = map.get("35");
        if (type == null) {
            Log.e(TAG, "map2Obj: type == null");
            return;
        }
        if (type.equals("V")) {
            map2WsPrice(map);
        }
    }

    private static void map2WsPrice(Map<String, String> map) {
        WsPrice wsPrice = new WsPrice();
        wsPrice.productName = map.get("55");
        wsPrice.lastUpdateTime = Long.parseLong(map.get("20068"));
        wsPrice.bidPrice = Double.parseDouble(map.get("132"));
        wsPrice.askPrice = Double.parseDouble(map.get("133"));

        EventBus.getDefault().post(wsPrice);
    }
}
