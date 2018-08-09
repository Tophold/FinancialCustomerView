package com.tophold.trade.utils;

import java.util.List;
import java.util.Random;

/**
 * ============================================================
 * 作 者 :    wgyscsf@163.com
 * 创建日期 ：2017/11/23 10:25
 * 描 述 ：
 * ============================================================
 **/
public class StringUtils {

    public static boolean isEmptyString(String str) {
        if (str == null || str.isEmpty()) return true;
        return false;
    }

    public static boolean isNotEmptyString(String str) {
        return !isEmptyString(str);
    }

    public static boolean isEmptyList(List list) {
        if (list == null || list.isEmpty()) return true;
        return false;
    }

    public static boolean isNotEmptyList(List list) {
        return !isEmptyList(list);
    }


    public static boolean isBlank(String... strs) {
        for (String str : strs) {
            if (str == null || str.equals("")) return true;
        }
        return false;
    }

    public static boolean isTrimBlank(String... strs) {
        for (String str : strs) {
            if (str == null || str.trim().equals("")) return true;
        }
        return false;
    }

    public static boolean isEmpty(List list) {
        if (list == null || list.size() == 0) return true;
        return false;
    }

    /**
     * 随机获取[m,n]之间的一个数字
     *
     * @param min
     * @param max
     * @return
     */
    public static int getRadomNum(int min, int max) {
        Random rdm = new Random();
        return rdm.nextInt(max - min + 1) + min;
    }
    public static String getString(){
        return getRadomNum(0,1)==0?null:"sdaas";
    }
}
