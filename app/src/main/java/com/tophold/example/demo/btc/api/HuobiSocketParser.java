package com.tophold.example.demo.btc.api;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import com.tophold.example.demo.btc.model.HuobiWsQuote;
import com.tophold.example.GsonUtil;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 21:26
 * 描 述 ：
 * ============================================================
 **/
public class HuobiSocketParser {
    public static final String TAG = HuobiSocketParser.class.getSimpleName();

    public static void fix2Object(String str) {
        Log.d(TAG, "fix2Object: " + str);
        try {
            formatJson(str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void formatJson(String str) throws JSONException {
        JSONObject json = new JSONObject(str);
        if (json.has("ping")) {
            long ping = json.getLong("ping");
            HuobiSocketApi.pong(ping);
            return;
        }

        //判断不严谨
        if (json.has("ch")) {
            HuobiWsQuote huobiWsQuote = GsonUtil.fromJson2Object(str, HuobiWsQuote.class);
            if (huobiWsQuote != null) {
                obj2EventBus(huobiWsQuote);
            }
            return;
        }

    }

    private static void obj2EventBus(Object object) {
        EventBus.getDefault().post(object);
    }

}
