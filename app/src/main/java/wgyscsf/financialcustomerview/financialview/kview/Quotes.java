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

    /**
     * 原始数据
     */
    public long t;
    public double o;
    public double h;
    public double l;
    public double c;


    /**
     * 扩展的数据
     */
    //实际中展示的时间
    public String showTime;
    //在自定义view:FundView中的位置坐标
    public float floatX;
    public float floatY;

    //MA
    public double ma5;
    public double ma10;
    public double ma20;

    //BOLL
    public double mb;//上轨线
    public double up;//中轨线
    public double dn;//下轨线

    //KDJ
    public double k;
    public double d;
    public double j;
    //调试使用，找到k、d、j中的最小值
    public double getMinKDJ(){
       double min;
       if(k<=d&&k<=j){
           min=k;
       }else if(d<=k&&d<=j){
           min=d;
       }else{
           min=j;
        }
        return min;
    }
    //调试使用，找到k、d、j中的最大值
    public double getMaxKDJ(){
        double max;
        if(k>=d&&k>=j){
            max=k;
        }else if(d>=k&&d>=j){
            max=d;
        }else{
            max=j;
        }
        return max;
    }

    //macd
    public double dif;
    public double dea;
    public double macd;

    //调试使用，找到dif、dea、macd中的最小值
    public double getMinMacd(){
        double min;
        if(dif<=dea&&dif<=macd){
            min=dif;
        }else if(dea<=dif&&dea<=macd){
            min=dea;
        }else{
            min=macd;
        }
        return min;
    }
    //调试使用，找到dif、dea、macd中的最大值
    public double getMaxMacd(){
        double max;
        if(dif>=dea&&dif>=macd){
            max=dif;
        }else if(dea>=dif&&dea>=macd){
            max=dea;
        }else{
            max=macd;
        }
        return max;
    }

    //rsi
    public double rsi6;
    public double rsi12;
    public double rsi24;

    //调试使用，找到rsi6、rsi12、rsi24中的最小值
    public double getMinRsi(){
        double min;
        if(rsi6<=rsi12&&rsi6<=rsi24){
            min=rsi6;
        }else if(rsi12<=rsi6&&rsi12<=rsi24){
            min=rsi12;
        }else{
            min=rsi24;
        }
        return min;
    }
    //调试使用，找到rsi6、rsi12、rsi24中的最大值
    public double getMaxRsi(){
        double max;
        if(rsi6>=rsi12&&rsi6>=rsi24){
            max=rsi6;
        }else if(rsi12>=rsi6&&rsi12>=rsi24){
            max=rsi12;
        }else{
            max=rsi24;
        }
        return max;
    }





    @Override
    public String toString() {
        return "Quotes{" +
                "k=" + k +
                ", d=" + d +
                ", j=" + j +
                ", dif=" + dif +
                ", dea=" + dea +
                ", macd=" + macd +
                ", rsi6=" + rsi6 +
                ", rsi12=" + rsi12 +
                ", rsi24=" + rsi24 +
                '}';
    }
}
