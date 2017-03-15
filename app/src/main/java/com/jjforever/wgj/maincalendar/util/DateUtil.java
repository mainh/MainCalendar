package com.jjforever.wgj.maincalendar.util;

import java.util.Calendar;

/**
 * Created by Wgj on 2016/8/9.
 * 日期计算工具类
 */
public final class DateUtil {
    // 当前日期
    private static Calendar mCurrentDate;

    /**
     * 获取指定年月的天数
     * @param year 指定公历年
     * @param month 指定公历月份
     * @return 该年月的天数
     */
    public static int getMonthDays(int year, int month) {
        Calendar tmpCalendar = Calendar.getInstance();
        tmpCalendar.set(Calendar.YEAR, year);
        tmpCalendar.set(Calendar.MONTH, month);
        tmpCalendar.set(Calendar.DATE, 1);
        tmpCalendar.roll(Calendar.DATE, -1);

        return tmpCalendar.get(Calendar.DATE);
    }

    /**
     * 是否为公历闰年
     * @param year 要判断的公历年
     * @return 是否为闰年
     */
    public static boolean isLeapYear(int year) {
        boolean isLeap = false;
        if (year % 4 == 0) {
            isLeap = true;
        }

        if (year % 100 == 0) {
            isLeap = false;
        }

        if (year % 400 == 0) {
            isLeap = true;
        }

        return isLeap;
    }

    /**
     * 重新获取当前日期
     */
    public static void updateCurrent()
    {
        mCurrentDate = Calendar.getInstance();
    }

    /**
     * 获取当前年
     * @return 当前年
     */
    public static int getYear() {
        return mCurrentDate.get(Calendar.YEAR);
    }

    /**
     * 获取当前月
     * @return 当前月份
     */
    public static int getMonth() {
        return mCurrentDate.get(Calendar.MONTH);
    }

    /**
     * 获取当前日
     * @return 当前日
     */
    public static int getCurrentMonthDay() {
        return mCurrentDate.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取当前周几
     * @return 今天是周几
     */
//    public static int getWeekDay() {
//        return mCurrentDate.get(Calendar.DAY_OF_WEEK);
//    }

    /**
     * 获取制定年指定月第一天是周几
     * @param year 指定公历年
     * @param month 指定公历月份
     * @return 第一天是周几，从0开始算而非1 周日是0
     */
    public static int getWeekDayFromDate(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        int tmpIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (tmpIndex < 0){
            tmpIndex = 0;
        }
        return tmpIndex;
    }

    /**
     * 是否为当日
     * @param date 检查输入的日期是否为当日
     * @return 是否为当日
     */
//    public static boolean isToday(LunarCalendar date){
//        return(date.get(Calendar.YEAR) == DateUtil.getYear() &&
//                date.get(Calendar.MONTH) == DateUtil.getMonth()
//                && date.get(Calendar.DAY_OF_MONTH) == DateUtil.getCurrentMonthDay());
//    }

    /**
     * 是否为当月
     * @param date 检查输入日期是否为当月
     * @return 是否为当月
     */
    public static boolean isCurrentMonth(LunarCalendar date){
        return (date.get(Calendar.YEAR) == DateUtil.getYear() &&
                date.get(Calendar.MONTH) == DateUtil.getMonth());
    }

    /**
     * 查看两个日期是否为相同月份内
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否在相同月内
     */
    public static boolean isSameMonth(LunarCalendar date1, LunarCalendar date2){
        return (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH));
    }

    /**
     * 判断两个日期是否为同一天
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(Calendar date1, Calendar date2){
        return (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 精确到分钟数的日期比较
     * @param date1 日期1
     * @param date2 日期2
     * @return 是否相等
     */
//    public static boolean isSameMinute(Calendar date1, Calendar date2){
//        return (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
//                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
//                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH) &&
//                date1.get(Calendar.HOUR_OF_DAY) == date2.get(Calendar.HOUR_OF_DAY) &&
//                date1.get(Calendar.MINUTE) == date2.get(Calendar.MINUTE));
//    }

    /**
     * 指定日期与当前日期进行比较
     * @param date 指定的日期
     * @return > 0 说明日期比当前日期晚，< 0说明日期比当前日期早，= 0说明日期相同
     */
    public static int compareDate(LunarCalendar date){
        int curYear = mCurrentDate.get(Calendar.YEAR);
        int curMonth = mCurrentDate.get(Calendar.MONTH);
        int curDay = mCurrentDate.get(Calendar.DAY_OF_MONTH);
        int tmpYear = date.get(Calendar.YEAR);
        int tmpMonth = date.get(Calendar.MONTH);
        int tmpDay = date.get(Calendar.DAY_OF_MONTH);

        // 先比较年
        if (tmpYear > curYear){
            return 1;
        }
        else if (tmpYear < curYear){
            return -1;
        }
        // 年相等比较月
        if (tmpMonth > curMonth){
            return 1;
        }
        else if (tmpMonth < curMonth){
            return -1;
        }
        // 月相等比较日
        if (tmpDay > curDay){
            return 1;
        }
        else if (tmpDay < curDay){
            return -1;
        }

        return 0;
    }

    /**
     * 根据日历返回应该显示在日期底部的字符串，是农历日期还是节气，或者节日
     * @param date 要检查的日期
     * @return 农历或者节假日信息
     */
    public static String getSubCalendar(LunarCalendar date)
    {
        // 检查节气
        int dayIndex = date.get(LunarCalendar.DAY_OF_MONTH);
        int princeTerm = date.get(LunarCalendar.LUNAR_PRINCIPLE_TERM);
        int secondTerm = date.get(LunarCalendar.LUNAR_SECTIONAL_TERM);
        if (dayIndex == princeTerm){
            return date.getLunar(LunarCalendar.LUNAR_PRINCIPLE_TERM);
        }
        else if (dayIndex == secondTerm){
            return date.getLunar(LunarCalendar.LUNAR_SECTIONAL_TERM);
        }

        int dateIndex = date.get(LunarCalendar.LUNAR_DATE);

        if (dateIndex == 1){
            return date.getLunar(LunarCalendar.LUNAR_MONTH) + "月";
        }
        else {
            return date.getLunar(LunarCalendar.LUNAR_DATE);
        }
    }
}
