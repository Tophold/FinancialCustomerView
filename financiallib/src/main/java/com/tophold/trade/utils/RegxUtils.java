package com.tophold.trade.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/10/25 16:08
 * 描 述 ：
 * ============================================================
 **/
public class RegxUtils {
    public static float getPureDouble(String str) {
        if (str == null || str.length() == 0) return 0;
        float result = 0;
        try {
            Pattern compile = Pattern.compile("(\\d+\\.\\d+)|(\\d+)");//如何提取带负数d ???
            Matcher matcher = compile.matcher(str);
            matcher.find();
            String string = matcher.group();//提取匹配到的结果
            result = Float.parseFloat(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args){

        test();
    }
    public static void test(){
        System.out.println(getPureDouble("12"));
        System.out.println(getPureDouble("wew3423.36"));
        System.out.println(getPureDouble("wewsf"));
        System.out.println(getPureDouble("000"));
        System.out.println(getPureDouble(null));
    }

}
