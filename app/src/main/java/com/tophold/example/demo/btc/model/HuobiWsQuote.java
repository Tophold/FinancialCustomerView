package com.tophold.example.demo.btc.model;

import java.io.Serializable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/28 18:39
 * 描 述 ：
 * ============================================================
 **/
public class HuobiWsQuote implements Serializable{

    /**
     * ch : market.eosusdt.kline.1min
     * ts : 1522233524438
     * tick : {"id":1522233480,"open":6.317,"close":6.3216,"low":6.3091,"high":6.3216,"amount":2024.6227,"vol":12789.80758224,"count":21}
     */

    public String ch;
    public long ts;
    public TickBean tick;

    public static class TickBean {
        /**
         * id : 1522233480
         * open : 6.317
         * close : 6.3216
         * low : 6.3091
         * high : 6.3216
         * amount : 2024.6227
         * vol : 12789.80758224
         * count : 21
         */

        public int id;
        public double open;
        public double close;
        public double low;
        public double high;
        public double amount;
        public double vol;
        public int count;
    }
}
