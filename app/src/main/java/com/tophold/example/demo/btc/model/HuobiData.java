package com.tophold.example.demo.btc.model;

import java.io.Serializable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/26 13:29
 * 描 述 ：
 * ============================================================
 **/
public class HuobiData<T> implements Serializable{
    public String status;
    public String ch;
    public long ts;
    public T data;

    @Override
    public String toString() {
        return "HuobiData{" +
                "status='" + status + '\'' +
                ", ch='" + ch + '\'' +
                ", ts=" + ts +
                ", data=" + data +
                '}';
    }


}
