package com.tophold.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * <p>GSON工具类</p>
 *
 * @author
 * @version $Id: GsonUtil.java
 */
public class GsonUtil {

    private static Gson gson = null;
    private static Gson prettyGson = null;

    static {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        prettyGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
                .setPrettyPrinting()
                .create();
    }

    /**
     * 小写下划线的格式解析JSON字符串到对象
     * <p>例如 is_success->isSuccess</p>
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T fromJsonUnderScoreStyle(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * JSON字符串转为Map<String,String>
     *
     * @param json
     * @return
     */
    @SuppressWarnings("all")
    public static <T> T fronJson2Map(String json) {
        return gson.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    /**
     * 小写下划线的格式将对象转换成JSON字符串
     *
     * @param src
     * @return
     */
    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    public static String toPrettyString(Object src) {
        return prettyGson.toJson(src);
    }

    public static <T> T fromJson2Object(String src, Class<T> t) {
        return gson.fromJson(src, t);
    }

    public static <T> T fromJson2Object(String src, Type typeOfT) {
        return gson.fromJson(src, typeOfT);
    }

}
