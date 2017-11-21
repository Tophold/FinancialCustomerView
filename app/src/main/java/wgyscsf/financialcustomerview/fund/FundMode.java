package wgyscsf.financialcustomerview.fund;

import wgyscsf.financialcustomerview.BaseModel;
import wgyscsf.financialcustomerview.utils.RegxUtils;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/10/25 14:50
 * 描 述 ：
 * ============================================================
 **/
public class FundMode extends BaseModel{
    //x轴原始时间数据，ms
    public long datetime;
    public float dataY;
    public String originDataY;
    //在自定义view:FundView中的位置坐标
    public float floatX;
    public float floatY;

    public FundMode(long timestamp, String actual) {
        this.datetime = timestamp;
        this.originDataY = actual;
        this.dataY = RegxUtils.getPureDouble(originDataY);
    }

    @Override
    public String toString() {
        return "FundMode{" +
                "datetime=" + datetime +
                ", dataY=" + dataY +
                ", originDataY='" + originDataY + '\'' +
                ", floatX=" + floatX +
                ", floatY=" + floatY +
                '}';
    }
}
