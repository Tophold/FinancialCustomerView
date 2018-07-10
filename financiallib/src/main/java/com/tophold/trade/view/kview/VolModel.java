package com.tophold.trade.view.kview;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/07/08 21:24
 * 描 述 ：量
 * ============================================================
 */
public class VolModel {
    public boolean cUp;//当前收盘价是否大于等于前一天收盘价
    public double vol;//量
    public double ma5;
    public double ma10;

    public VolModel(boolean cUp, double vol, double ma5, double ma10) {
        this.cUp = cUp;
        this.vol = vol;
        this.ma5 = ma5;
        this.ma10 = ma10;
    }
}
