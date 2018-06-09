package com.tophold.example.demo.forex.model;

import com.google.gson.annotations.SerializedName;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/12/26 11:38
 * 描 述 ：
 * ============================================================
 **/
public class WsPrice  {

    //TAG:55,COMMENT:产品
    @SerializedName("55")
    public String productName;
    //TAG:20068,COMMENT:系统最后价格时间
    @SerializedName("20068")
    public long lastUpdateTime;
    //TAG:132,COMMENT:--->对应老系统的卖价
    @SerializedName("132")
    public double bidPrice;
    //TAG:133,COMMENT:--->对应老系统的买价
    @SerializedName("133")
    public double askPrice;

    @Override
    public String toString() {
        return "WsPrice{" +
                ", productName='" + productName + '\'' +
                ", bidPrice='" + bidPrice + '\'' +
                ", askPrice='" + askPrice + '\'' +
                "现价：" + ((bidPrice + askPrice) / 2) +
                '}';
    }
}
