package com.tophold.trade.view.kview;

import java.io.Serializable;
import java.util.Date;

import com.tophold.trade.utils.TimeUtils;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/11/21 11:12
 * 描 述 ：包装后的Quotes，实际使用的Quotes
 * ============================================================
 **/
public class Quotes implements Serializable {

    /**
     * 原始数据
     */
    public long t;//开始时间
    public double o;
    public double h;
    public double l;
    public double c;

    //扩展一个结束时间
    public long e;

    //vol 量，可选
    public double vol;

    /**
     * 适配数据的构造方法，包括五个参数，全部必须。价格格式为String类型
     *
     * @param o 闭盘价
     * @param h 最高价
     * @param l 最低价
     * @param c 收盘价
     * @param t 时间
     */
    public Quotes(String o, String h, String l, String c, String t) {
        this(Double.parseDouble(o), Double.parseDouble(h), Double.parseDouble(l), Double.parseDouble(c), TimeUtils.date2Millis(new Date(t)));
    }

    /**
     * 价格格式是double类型
     *
     * @param o
     * @param h
     * @param l
     * @param c
     * @param t
     */
    public Quotes(double o, double h, double l, double c, long t) {
        this(o, h, l, c, t, -1, -1);
    }

    public Quotes(double o, double h, double l, double c, long t, double vol) {
        this(o, h, l, c, t, -1, vol);
    }

    /**
     * 最原始构造方法
     *
     * @param o
     * @param h
     * @param l
     * @param c
     * @param t
     * @param e
     */
    public Quotes(double o, double h, double l, double c, long t, long e, double vol) {
        this.o = o;
        this.h = h;
        this.l = l;
        this.c = c;
        this.t = t;
        this.e = e;
        this.vol = vol;
    }

    /**
     * 多添加一个时间，包括两个格式的时间，一个开始时间，一个结束时间，标准以开始时间为准。
     *
     * @param o
     * @param h
     * @param l
     * @param c
     * @param s
     * @param e
     */
    public Quotes(String o, String h, String l, String c, long s, long e) {
        this(Double.parseDouble(o), Double.parseDouble(h), Double.parseDouble(l), Double.parseDouble(c), s, e, -1);

    }

    /**
     * 扩展的数据
     */
    //实际中展示的时间
    private String showTime;

    public String getShowTime() {
        showTime = TimeUtils.millis2String(t);
        return showTime;
    }

    //在自定义view:FundView中的位置坐标
    public float floatX;
    public float floatY;

    //MA
    public double ma5;
    public double ma10;
    public double ma20;

    //BOLL
    public double up;//上轨线
    public double mb;//中轨线
    public double dn;//下轨线

    //KDJ
    public double k;
    public double d;
    public double j;

    //macd
    public double dif;
    public double dea;
    public double macd;

    //rsi
    public double rsi6;
    public double rsi12;
    public double rsi24;

    //vol
    public double volMa5;
    public double volMa10;

    @Override
    public String toString() {
        return "Quotes{" +
                "t=" + t +
                ", o=" + o +
                ", h=" + h +
                ", l=" + l +
                ", c=" + c +
                ", e=" + e +
                ", vol=" + vol +
                ", showTime='" + showTime + '\'' +
                ", floatX=" + floatX +
                ", floatY=" + floatY +
                ", ma5=" + ma5 +
                ", ma10=" + ma10 +
                ", ma20=" + ma20 +
                ", up=" + up +
                ", mb=" + mb +
                ", dn=" + dn +
                ", k=" + k +
                ", d=" + d +
                ", j=" + j +
                ", dif=" + dif +
                ", dea=" + dea +
                ", macd=" + macd +
                ", rsi6=" + rsi6 +
                ", rsi12=" + rsi12 +
                ", rsi24=" + rsi24 +
                ", volMa5=" + volMa5 +
                ", volMa10=" + volMa10 +
                '}';
    }
}
