package wgyscsf.financialcustomerview.fund;

import wgyscsf.financialcustomerview.utils.RegxUtils;

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
    public double dataY;
    public String originDataY;

    public FundMode(long timestamp, String actual) {
        this.datetime = timestamp;
        this.originDataY = actual;
        this.dataY = RegxUtils.getPureDouble(originDataY);
    }
}
