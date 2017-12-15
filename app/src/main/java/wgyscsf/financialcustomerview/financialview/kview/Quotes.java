package wgyscsf.financialcustomerview.financialview.kview;

import java.util.Date;

import wgyscsf.financialcustomerview.BaseModel;
import wgyscsf.financialcustomerview.utils.TimeUtils;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/11/21 11:12
 * 描 述 ：实际使用的Quotes
 * ============================================================
 **/
public class Quotes extends BaseModel {

    public Quotes(String o, String h, String l, String c, String t) {
        this.o = Double.parseDouble(o);
        this.h = Double.parseDouble(h);
        this.l = Double.parseDouble(l);
        this.c = Double.parseDouble(c);
        this.t = TimeUtils.date2Millis(new Date(t));
        this.showTime = TimeUtils.millis2String(this.t);
    }
    //在自定义view:FundView中的位置坐标
    public float floatX;
    public float floatY;
    //实际中展示的时间
    public String showTime;
    //标准时间戳（ms）
    public long t;
    public double o;
    public double h;
    public double l;
    public double c;

    @Override
    public String toString() {
        return "Quotes{" +
                "showTime='" + showTime + '\'' +
                ", t=" + t +
                ", o=" + o +
                ", h=" + h +
                ", l=" + l +
                ", c=" + c +
                '}';
    }
}
