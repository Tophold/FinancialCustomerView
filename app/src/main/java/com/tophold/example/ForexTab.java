package com.tophold.example;

import java.io.Serializable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 14:16
 * 描 述 ：
 * ============================================================
 **/
public class ForexTab implements Serializable {
    public ForexType mForexType;
    public String tabName;

    public ForexTab(ForexType forexType, String tabName) {
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
            case _1h:
                len = 60 * 60;
                break;
            case _4h:
                len = 4 * 60 * 60;
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
        String typeMsg = "1m";
        switch (mForexType) {
            case _timeShring:
                typeMsg = "1m";
                break;
            case _1m:
                typeMsg = "1m";
                break;
            case _5m:
                typeMsg = "5m";
                break;
            case _15m:
                typeMsg = "15m";
                break;
            case _30m:
                typeMsg = "30m";
                break;
            case _1h:
                typeMsg = "1h";
                break;
            case _4h:
                typeMsg = "4h";
                break;
            case _1d:
                typeMsg = "1d";
                break;
            case _1w:
                typeMsg = "1w";
                break;
            case _1mon:
                typeMsg = "1mon";
                break;
            default:
                typeMsg = "1m";
                break;
        }
        return typeMsg;
    }

    public enum ForexType {
        _timeShring,
        _1m,
        _5m,
        _15m,
        _30m,
        _1h,
        _4h,
        _1d,
        _1w,
        _1mon
    }
}