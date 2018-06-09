package com.tophold.trade.view.fund;


import com.tophold.trade.utils.RegxUtils;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/10/25 14:50
 * 描 述 ：
 * ============================================================
 **/
public class FundMode {
    //x轴原始时间数据，ms
    public long datetime;
    //y轴的原始数据
    public String originDataY;
    //y轴的转换后的数据
    public float dataY;

    //在自定义view:FundView中的位置坐标
    public float floatX;
    public float floatY;

    public FundMode(long timestamp, String actual) {
        this.datetime = timestamp;
        this.originDataY = actual;
        this.dataY = RegxUtils.getPureDouble(originDataY);//提取后的Y周的值
    }
}
