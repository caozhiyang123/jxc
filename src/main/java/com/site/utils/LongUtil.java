package com.site.utils;

public class LongUtil {

    /**
     * 小于
     *
     * @param number  基准数
     * @param number1 需要比较的数
     * @return
     */
    public static boolean lt(Long number, Long number1) {
        if (number == null) {
            return false;
        }
        return number < number1;
    }

    /**
     * 大于
     *
     * @param number  基准数
     * @param number1 需要比较的数
     * @return
     */
    public static boolean gt(Long number, Long number1) {
        if (number == null) {
            return false;
        }
        return number > number1 ? true : false;
    }

    /**
     * 小于等于
     *
     * @param number  基准数
     * @param number1 需要比较的数
     * @return
     */
    public static boolean lte(Long number, Long number1) {
        if (number == null) {
            return false;
        }
        return number <= number1 ? true : false;
    }

    /**
     * 大于等于
     *
     * @param number  基准数
     * @param number1 需要比较的数
     * @return
     */
    public static boolean gte(Long number, Long number1) {
        if (number == null) {
            return false;
        }
        return number >= number1 ? true : false;
    }

    /**
     * 相等
     *
     * @param number  基准数
     * @param number1 需要比较的数
     * @return
     */
    public static boolean equal(Long number, Long number1) {
        if (number == null && number1 == null) {
            return true;
        }
        if (number == null) {
            return false;
        }
        return number.equals(number1) ? true : false;
    }

    /**
     * 为空置为0
     *
     * @param number 基准数
     * @return
     */
    public static Long isNull(Long number) {
        if (number == null) {
            return 0L;
        }
        return number;
    }

    public static Boolean isEmpty(Long num) {
        if (num == null) {
            return true;
        }
        return num.equals(0L);
    }

    public static Boolean isNotEmpty(Long num) {
        return !isEmpty(num);
    }
}
