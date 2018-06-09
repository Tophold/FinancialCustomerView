package com.tophold.example.demo.btc.model;

import java.io.Serializable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/26 16:07
 * 描 述 ：
 * ============================================================
 **/
public class HuobiTab implements Serializable {
    public HuobiTab.HuobiType mForexType;
    public String tabName;

    public HuobiTab(HuobiTab.HuobiType forexType, String tabName) {
        this.mForexType = forexType;
        this.tabName = tabName;
    }

    /**
     * 获取对应类型的长度
     *
     * @return sencond
     */
    public long getTypeLength() {
        long len = 0;
        switch (mForexType) {
            case _timeShring:
                len = 60;
                break;
            case _1m:
                len = 60;
                break;
            case _5m:
                len = 5 * 60;
                break;
            case _15m:
                len = 15 * 60;
                break;
            case _30m:
                len = 30 * 60;
                break;
            case _60m:
                len = 60 * 60;
                break;
            case _1d:
                len = 24 * 60 * 60;
                break;
            case _1w:
                len = 7 * 24 * 60 * 60;
                break;
            case _1mon:
                len = 30 * 24 * 60 * 60;
                break;
            case _1y:
                len = 365 * 24 * 60 * 60;
                break;
            default:
                len = 60;
                break;
        }
        return len;
    }

    /**
     * 根据mForexType返回接口需要的类型字段
     *
     * @return
     */
    public String getType() {
        String typeMsg = "1min";
        switch (mForexType) {
            case _timeShring:
                typeMsg = "1min";
                break;
            case _1m:
                typeMsg = "1min";
                break;
            case _5m:
                typeMsg = "5min";
                break;
            case _15m:
                typeMsg = "15min";
                break;
            case _30m:
                typeMsg = "30min";
                break;
            case _60m:
                typeMsg = "60min";
                break;
            case _1d:
                typeMsg = "1day";
                break;
            case _1w:
                typeMsg = "1week";
                break;
            case _1mon:
                typeMsg = "1mon";
                break;
            case _1y:
                typeMsg = "1year";
                break;
            default:
                typeMsg = "1min";
                break;
        }
        return typeMsg;
    }

    public enum HuobiType {
        _timeShring,
        _1m,
        _5m,
        _15m,
        _30m,
        _60m,
        _1d,
        _1w,
        _1mon,
        _1y
    }
}