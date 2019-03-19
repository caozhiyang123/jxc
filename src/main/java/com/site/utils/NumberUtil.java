package com.site.utils;

import java.util.regex.Pattern;

public class NumberUtil {

    /**
     * 判断是否为自然数
     * @param str
     * @return
     */
    public static boolean isNaturalNumber(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^\\d+$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断是否为小数
     * @param str
     * @return
     */
    public static boolean isDecimal(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-]?\\d+\\.\\d+$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断是否为数字（小数或整数）
     * @param str
     * @return
     */
    public static boolean isNumber(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-]?\\d+(\\.\\d+)?$");
        return pattern.matcher(str).matches();
    }

    public static void main(String[] args) {
        System.out.println(NumberUtil.isDecimal("0"));
    }
}