package com.tophold.trade.utils;

import android.util.Log;

import java.math.BigDecimal;
import java.util.Locale;


public class FormatUtil {

    /**
     * 浮点数格式化
     *
     * @param isPercentage 是否为分数
     * @param needSign     是否需要正号
     * @param isMoney      是否使用金钱格式(每3位用","分隔)
     * @param digit        需要保留的位数
     * @param num          需要格式化的值(int、float、double、String、byte均可)
     */
    public static String format(boolean isPercentage, boolean needSign, boolean isMoney, int digit, Object num) {
        digit = digit < 0 ? 0 : digit;
        if (num == null) num = 0;

        double converted;//需求大多四舍五入  float保留位数会舍去后面的
        try {
            converted = Double.parseDouble(num.toString());
        } catch (Throwable e) {
            Log.e("FormatErr", "Input number is not kind number");
            converted = 0d;
        }
        StringBuilder sb = new StringBuilder("%");

        if (needSign && (converted > 0)) {
            sb.append("+");
        }

        if (isMoney) {
            sb.append(",");
        }

        sb.append(".").append(digit).append("f");

        if (isPercentage) {
            sb.append("%%");
        }

        return String.format(Locale.getDefault(), sb.toString(), converted);
    }

    public static String numFormat(Object num, int digit) {
        return numFormat(false, digit, num);
    }

    public static String numFormat(boolean needSign, int digit, Object num) {
        return format(false, needSign, false, digit, num);
    }

    /**
     * 带%格式化
     */
    public static String percentageFormat(Object num) {
        return percentageFormat(true, num);
    }

    public static String percentageFormat(boolean needSign, Object num) {
        return percentageFormat(needSign, 2, num);
    }

    public static String percentageFormat(int digit, Object num) {
        return percentageFormat(false, digit, num);
    }

    public static String percentageFormat(boolean needSign, int digit, Object num) {
        return format(true, needSign, false, digit, num);
    }

    /**
     * 以金钱格式表示的数字
     */
    public static String moneyFormat(Object num) {
        return moneyFormat(true, num);
    }

    public static String moneyFormat(boolean isPercentage, Object num) {
        return moneyFormat(isPercentage, 2, num);
    }

    public static String moneyFormat(boolean isPercentage, int digit, Object num) {
        return format(isPercentage, false, true, digit, num);
    }

    public static String stringAppend(Object object) {
        if (object != null)
            return String.format(Locale.getDefault(), "%s", object);
        else
            return "- -";
    }

    /**
     * 不四舍五入取n位小数
     */
    public static String formatBySubString(Object obj, int digit) {
        if (obj == null) return "0";
        String num = String.valueOf(obj);
        digit = digit < 0 ? 0 : digit;
        int i = num.indexOf(".");
        if (i >= 0) {
            if (num.length() - ++i > digit) {
                num = num.substring(0, i + digit);
            }
        }
        return num;
    }

    /**
     * 进位处理
     *
     * @param scale 保留几位
     */
    public static String roundUp(int scale, Object num) {
        BigDecimal decimal = new BigDecimal(num.toString());
        return decimal.setScale(scale, BigDecimal.ROUND_UP).toString();
    }

    /**
     * 直接舍弃多余小数
     */
    public static String roundDown(int scale, Object num) {
        BigDecimal decimal = new BigDecimal(num.toString());
        return decimal.setScale(scale, BigDecimal.ROUND_DOWN).toString();
    }

    public static void main(String args[]) {
        double num1 = -123456.789000;
        double num2 = 0;
        double num3 = 1.00;
        double num4 = 1444444444;
        double num5 = 123.123;
        double num6 = 123.123;
        double num7 = 123.123;
        double num8 = 123.123;
        double num9 = 123.123;
        double num10 = 123.123;
        String numStr1 = format(false, false, false, 5, num1);
        String numStr2 = format(true, false, false, 5, num1);
        String numStr3 = format(false, true, false, 5, num1);
        String numStr4 = format(false, false, true, 5, num1);
        System.out.println(numStr1);
        System.out.println(numStr2);
        System.out.println(numStr3);
        System.out.println(numStr4);
    }
}
