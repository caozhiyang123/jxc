package com.site.utils;

import java.math.BigDecimal;

public class BigDecimalUtil {

    /**
     * 小于
     * @param bigDecimal 基准数
     * @param number 需要比较的数
     * @return
     */
    public static boolean lt(BigDecimal bigDecimal, Double number) {
        if (bigDecimal == null) {
            return false;
        }
        BigDecimal bigDecimal1 = new BigDecimal(number);
        int result = bigDecimal.compareTo(bigDecimal1);
        return result == -1 ? true : false;
    }

    /**
     * 大于
     * @param bigDecimal 基准数
     * @param number 需要比较的数
     * @return
     */
    public static boolean gt(BigDecimal bigDecimal, Double number) {
        if (bigDecimal == null) {
            return false;
        }
        BigDecimal bigDecimal1 = new BigDecimal(number);
        int result = bigDecimal.compareTo(bigDecimal1);
        return  result == 1 ? true : false;
    }

    /**
     * 小于等于
     * @param bigDecimal 基准数
     * @param number 需要比较的数
     * @return
     */
    public static boolean lte(BigDecimal bigDecimal, Double number) {
        if (bigDecimal == null) {
            return false;
        }
        BigDecimal bigDecimal1 = new BigDecimal(number);
        int result = bigDecimal.compareTo(bigDecimal1);
        return result == -1 || result == 0 ? true : false;
    }

    /**
     * 大于等于
     * @param bigDecimal 基准数
     * @param number 需要比较的数
     * @return
     */
    public static boolean gte(BigDecimal bigDecimal, Double number) {
        if (bigDecimal == null) {
            return false;
        }
        BigDecimal bigDecimal1 = new BigDecimal(number);
        int result = bigDecimal.compareTo(bigDecimal1);
        return  result == 1 || result == 0 ? true : false;
    }

    /**
     * 相等
     * @param bigDecimal 基准数
     * @param decimal 需要比较的数
     * @return
     */
    public static boolean equal(BigDecimal bigDecimal, BigDecimal decimal) {
        if (bigDecimal == null) {
            return false;
        }
        int result = bigDecimal.compareTo(decimal);
        return  result == 0 ? true : false;
    }

    /**
     * 为空置为0
     * @param bigDecimal 基准数
     * @return
     */
    public static BigDecimal isNull(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return new BigDecimal(0);
        }
        return  bigDecimal;
    }
}
