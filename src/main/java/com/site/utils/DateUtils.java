package com.site.utils;

import com.alibaba.fastjson.JSONObject;
import com.site.core.model.OpenTimeManage;
import com.site.core.model.common.OpenTimeStep;
import com.site.service.VersionDataService;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 时间工具类
 */
public class DateUtils {

    private static final Logger log = Logger.getLogger(VersionDataService.class);

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getNowDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getNowDate(String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date());
    }

    /**
     * 获取昨天前天的日期
     *
     * @param date       当前日期
     * @param difference 差异天数
     * @return
     */
    public static String getDate(String date, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.DATE, difference);
        calendar.getTime();
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取昨天前天的日期
     *
     * @param date       当前日期
     * @param difference 差异天数
     * @return
     */
    public static String getDate(Date date, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, difference);
        calendar.getTime();
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取昨天前天的日期
     *
     * @param date       当前日期
     * @param difference 差异天数
     * @return
     */
    public static String getDate(Date date, String patten, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat(patten);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, difference);
        calendar.getTime();
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取昨天前天的日期
     *
     * @param date       当前日期
     * @param difference 差异天数
     * @return
     */
    public static String getDate(String date, String patten, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat(patten);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.DATE, difference);
        calendar.getTime();
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取昨天前天的日期
     *
     * @param date   当前日期
     * @param patten 格式化参数
     * @return
     */
    public static String getDate(Date date, String patten) {
        SimpleDateFormat sdf = new SimpleDateFormat(patten);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.getTime();
        return sdf.format(calendar.getTime());
    }

    /**
     * 根据差值获取月份
     *
     * @param date       当前日期
     * @param difference 差异月份
     * @return
     */
    public static String getDateByMonth(String date, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.MONTH, difference);
        calendar.getTime();
        return sdf.format(calendar.getTime());
    }

    /**
     * 根据年份差值获取日期
     *
     * @param date       当前日期
     * @param difference 差异月份
     * @return
     */
    public static String getDateByYear(String date, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.YEAR, difference);
        calendar.getTime();
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取某月的最后一天
     *
     * @param date    当前日期
     * @param pattern 初始化SimpleDateFormat时的格式
     * @return
     */
    public static String getLastDayOfMonth(String date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return sdfDate.format(calendar.getTime());
    }

    /**
     * 获取某周的最后一天
     *
     * @param date    当前日期
     * @param pattern 初始化SimpleDateFormat时的格式
     * @return
     */
    public static String getLastDayOfWeek(String date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK) + 1);
        return sdfDate.format(calendar.getTime());
    }

    /**
     * 获取某周的第一天
     *
     * @param date    当前日期
     * @param pattern 初始化SimpleDateFormat时的格式
     * @return
     */
    public static String getFirstDayOfWeek(String date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return sdfDate.format(calendar.getTime());
    }

    /**
     * 取得当月天数
     */
    public static int getNumberOfDays(String date, String patten) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(patten);
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.DATE, 1);//把日期设置为当月第一天
        calendar.roll(Calendar.DATE, -1);//日期回滚一天，也就是最后一天
        int maxDate = calendar.get(Calendar.DATE);
        return maxDate;
    }

    /**
     * 获取当前月
     *
     * @param difference 差值
     * @return 06
     */
    public static int getMonth(int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, difference);
        String yearMonth = sdf.format(calendar.getTime());
        return Integer.parseInt(yearMonth);
    }

    /**
     * 获取当前月
     * <p>
     * /**
     * 获取当前月
     *
     * @param difference 差值
     * @return 06
     */
    public static String getMonth(String date, String pattern, int difference) {
        SimpleDateFormat sdfDate = new SimpleDateFormat(pattern);
        SimpleDateFormat sdf = new SimpleDateFormat("MM");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdfDate.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.MONTH, difference);
        String month = sdf.format(calendar.getTime());
        return month;
    }

    /**
     * 获取当前年月
     *
     * @param difference 差值
     * @return 201806
     */
    public static int getYearAndMonth(int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, difference);
        String yearMonth = sdf.format(calendar.getTime());
        return Integer.parseInt(yearMonth);
    }

    /**
     * 获取当前年月
     *
     * @param pattern    格式
     * @param difference 差值
     * @return 201806
     */
    public static String getYearAndMonth(String pattern, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, difference);
        String yearMonth = sdf.format(calendar.getTime());
        return yearMonth;
    }

    /**
     * 获取当前年月
     *
     * @param difference 差值
     * @return 201806
     */
    public static String getYearAndMonth(String date, String pattern, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.MONTH, difference);
        String yearMonth = sdf.format(calendar.getTime());
        return yearMonth;
    }

    /**
     * 获取当前年月
     *
     * @param difference 差值
     * @return 201806
     */
    public static int getYearAndMonth(OpenTimeStep openTimeStep, int difference) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(openTimeStep.getYearAndMonth() + ""));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.add(Calendar.MONTH, difference);
        String yearMonth = sdf.format(calendar.getTime());
        return Integer.parseInt(yearMonth);
    }

    /**
     * 获取两个时间相差的月份
     *
     * @return 201806
     */
    public static List<String> getDifferenceMonth(String minDate, String maxDate) {
        int result = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");

        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        try {
            c1.setTime(sdf.parse(minDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            c2.setTime(sdf.parse(maxDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int diffYear = c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR);
        result = diffYear * 12 + c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);

        List<String> months = new ArrayList<>();
        //包含本月时间
        months.add(sdf.format(c1.getTime()));
        for (int i = 0; i < result; i++) {
            c1.add(Calendar.MONTH, 1);
            months.add(sdf.format(c1.getTime()));
        }

        return months;
    }

    /**
     * 获取两个时间相差的天数
     *
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDays(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if (year1 != year2)   //同一年
        {
            int timeDistance = 0;
            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0)    //闰年
                {
                    timeDistance += 366;
                } else    //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2 - day1);
        } else    //不同年
        {
            return day2 - day1;
        }
    }

    public static Date formateDate(String data, String patten) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(patten);
        try {
            Date parse = simpleDateFormat.parse(data);
            return parse;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取当前日期是星期几<br>
     *
     * @param date
     * @return 当前日期是星期几
     */
    public static int getWeekOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w == 0)
            w = 7;
        return w;
    }

    public static void main(String[] args) {
        Long a = 1l;
        BigDecimal bd = new BigDecimal(a);

        System.out.println(bd.add(new BigDecimal(1)));
    }

}
