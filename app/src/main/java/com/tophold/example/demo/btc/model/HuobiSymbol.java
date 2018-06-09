package com.tophold.example.demo.btc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/26 13:42
 * 描 述 ：
 * ============================================================
 **/
public class HuobiSymbol implements Serializable {
    @SerializedName("base-currency")
    public String base_currency;
    @SerializedName("quote-currency")
    public String quote_currency;
    @SerializedName("price-precision")
    public int price_precision;
    @SerializedName("amount-precision")
    public int amount_precision;
    @SerializedName("symbol-partition")
    public String symbol_partition;

    public HuobiSymbol(String base_currency, String quote_currency, int price_precision, int amount_precision, String symbol_partition) {
        this.base_currency = base_currency;
        this.quote_currency = quote_currency;
        this.price_precision = price_precision;
        this.amount_precision = amount_precision;
        this.symbol_partition = symbol_partition;
    }

    @Override
    public String toString() {
        return "HuobiSymbol{" +
                "base_currency='" + base_currency + '\'' +
                ", quote_currency='" + quote_currency + '\'' +
                ", price_precision=" + price_precision +
                ", amount_precision=" + amount_precision +
                ", symbol_partition='" + symbol_partition + '\'' +
                '}';
    }
}
