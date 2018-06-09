package com.tophold.example.demo.forex.model;

import java.io.Serializable;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2018/03/22 13:03
 * 描 述 ：
 * ============================================================
 **/
public class Forex implements Serializable{
    public String id;
    public String enName;
    public String cnName;

    public Forex(String id, String enName, String cnName) {
        this.id=id;
        this.enName=enName;
        this.cnName=cnName;
    }

    @Override
    public String toString() {
        return "Forex{" +
                "id='" + id + '\'' +
                ", enName='" + enName + '\'' +
                ", cnName='" + cnName + '\'' +
                '}';
    }
}
