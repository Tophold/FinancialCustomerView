package com.tophold.example.demo.forex.model;

import java.io.Serializable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 11:52
 * 描 述 ：
 * ============================================================
 **/
public class XcfdQuotes implements Serializable{
    public String t; //201802080610
    public String o;
    public String h;
    public String l;
    public String c;

    public long s;//数据开始时间
    public long e;//结束时间

    @Override
    public String toString() {
        return "Quotes{" +
                "t=" + t +
                ", o='" + o + '\'' +
                ", h='" + h + '\'' +
                ", l='" + l + '\'' +
                ", c='" + c + '\'' +
                ", s=" + s +
                ", e=" + e +
                '}';
    }
}
