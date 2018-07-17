package com.tophold.trade.view.pie;

import android.support.annotation.ColorInt;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 更新时间 ：2018/07/10 20:36
 * 描 述 ：
 * ============================================================
 */
public class PieEntrys {
    public float value;
    public String label;
    @ColorInt
    public int colorBg;
    public boolean highLight;
    public String symbol;

    public PieEntrys(float value, String label, int colorBg, boolean highLight, String symbol) {
        this.value = value;
        this.label = label;
        this.colorBg = colorBg;
        this.highLight = highLight;
        this.symbol = symbol;
    }

    //一些额外的字段辅助画图
    //单个item需要扫过的角度
    public float mSweepAngle;

}
