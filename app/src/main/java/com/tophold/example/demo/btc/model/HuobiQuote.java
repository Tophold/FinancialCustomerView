package com.tophold.example.demo.btc.model;

import java.io.Serializable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/26 13:31
 * 描 述 ：
 * ============================================================
 **/
public class HuobiQuote implements Serializable{
    public long id;
    public double open;
    public double close;
    public double low;
    public double high;
    public double amount;
    public double vol;
    public double count;

    @Override
    public String toString() {
        return "HuobiQuote{" +
                "id='" + id + '\'' +
                ", open=" + open +
                ", close=" + close +
                ", low=" + low +
                ", high=" + high +
                ", amount=" + amount +
                ", vol=" + vol +
                ", count=" + count +
                '}';
    }
}
