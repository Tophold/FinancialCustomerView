package wgyscsf.financialcustomerview.timesharing;

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

    Quotes(String o, String h, String l, String c, String t) {
        this.o = o;
        this.h = h;
        this.l = l;
        this.c = c;
        this.t = TimeUtils.date2Millis(new Date(t));
        this.showTime = TimeUtils.millis2String(this.t);
    }

    //实际中展示的时间
    public String showTime;
    //标准时间戳（ms）
    public long t;
    public String o;
    public String h;
    public String l;
    public String c;
}
