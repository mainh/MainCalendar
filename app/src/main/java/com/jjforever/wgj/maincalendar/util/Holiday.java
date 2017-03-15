package com.jjforever.wgj.maincalendar.util;

import com.jjforever.wgj.maincalendar.Model.ICalendarRecord;
import com.jjforever.wgj.maincalendar.Model.LunarHoliday;
import com.jjforever.wgj.maincalendar.Model.SolarHoliday;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Wgj on 2016/8/16.
 * 本类定义了一些常用的公历及农历节日
 * 不常用的就不做定义了。。。
 */
public final class Holiday {

    // 公历节日定义
    private static final String[] solarHolidays = {
            "元旦", "情人节", "妇女节", "植树节", "消费者权益日", "愚人节",
            "劳动节", "青年节", "儿童节", "建党节", "建军节", "教师节",
            "国庆节", "平安夜", "圣诞节",
            };
    // 农历节日定义
    private static final String[] lunarHolidays = {
            "春节", "元宵节", "龙头节", "端午节", "七夕", "中元节",
            "中秋节", "重阳节", "腊八节", "北方小年", "南方小年", "除夕",
            };
    private static final String[] weekHolidays = {
            "母亲节", "父亲节", "感恩节",
            };

    // 阳历节日日期定义，第一个字节为月份 + 1，第二个字节为日期
    private static final byte[][] solarDate = {
            {1, 1}, {2, 14}, {3, 8}, {3, 12}, {3, 15}, {4, 1},
            {5, 1}, {5, 4}, {6, 1}, {7, 1}, {8, 1}, {9, 10},
            {10, 1}, {12, 24}, {12, 25}
            };
    // 农历节日日期定义，第一个字节为农历月份，第二个为农历日期 除夕另外计算
    private static final byte[][] lunarDate = {
            {1, 1}, {1, 15}, {2, 2}, {5, 5}, {7, 7}, {7, 15},
            {8, 15}, {9, 9}, {12, 8}, {12, 23}, {12, 24}
            };
    // 根据星期定义的节日日期，第一个字节为月份+1，第二个为第几周，
    // 第三个为周几，Calendar中获取的数据
    private static final byte[][] weekDate = {
            // 周日为1 母亲节 5月的第二个星期日
            {5, 2, 1}, {6, 3, 1}, {11, 4, 5}
            };

    /**
     * 根据日期获取阳历假日信息
     * @param date 日历信息
     * @return 如果有阳历假日信息返回对应字符串，没有返回null
     */
    public static SolarHoliday getSolarHoliday(LunarCalendar date)
    {
        int month = date.get(Calendar.MONTH) + 1;
        int day = date.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < solarDate.length; i++){
            if (solarDate[i][0] == month && solarDate[i][1] == day){
                return new SolarHoliday(solarHolidays[i]);
            }
        }

        return null;
    }

    /**
     * 根据日期获取农历假日信息
     * @param date 日历信息
     * @return 如果有农历假日信息则返回对应字符串，没有返回null
     */
    public static LunarHoliday getLunarHoliday(LunarCalendar date)
    {
        int month = date.get(LunarCalendar.LUNAR_MONTH);
        int day = date.get(LunarCalendar.LUNAR_DATE);
        if (month < 0){
            // 小于0为闰月，无节日
            return null;
        }

        for (int i = 0; i < lunarDate.length; i++){
            if (lunarDate[i][0] == month && lunarDate[i][1] == day){
                return new LunarHoliday(lunarHolidays[i]);
            }
        }

        // 计算除夕
        if (month == 12){
            if (day == LunarCalendar.daysInLunarMonth(date.get(LunarCalendar.LUNAR_YEAR), month)){
                // 12月的最后一天为除夕
                return new LunarHoliday(lunarHolidays[lunarHolidays.length - 1]);
            }
        }

        return null;
    }

    /**
     * 根据日期返回按星期计算的阳历节日
     * @param date 日历信息
     * @return 节日字符串
     */
    public static SolarHoliday getWeekHoliday(LunarCalendar date)
    {
        int month = date.get(Calendar.MONTH) + 1;
        int week = date.get(Calendar.DAY_OF_WEEK);
        //  不能用WEEK_IN_MONTH获取
        int weekCount = date.get(Calendar.DAY_OF_WEEK_IN_MONTH);

        for (int i = 0; i < weekDate.length; i++){
            if (month == weekDate[i][0] && weekCount == weekDate[i][1]
                    && week == weekDate[i][2]){
                return new SolarHoliday(weekHolidays[i]);
            }
        }

        return null;
    }

    /**
     * 根据日历信息获取节假日信息
     * @param date 日历信息
     * @return 节假日信息，多个使用/分割
     */
    public static ArrayList<ICalendarRecord> getHolidays(LunarCalendar date)
    {
        ArrayList<ICalendarRecord> tmpLst = new ArrayList<>();

        // 农历节日
        ICalendarRecord tmpRecord = getLunarHoliday(date);
        if (tmpRecord != null){
            tmpLst.add(tmpRecord);
        }
        // 阳历节日
        tmpRecord = getSolarHoliday(date);
        if (tmpRecord != null){
            tmpLst.add(tmpRecord);
        }
        // 星期节日
        tmpRecord = getWeekHoliday(date);
        if (tmpRecord != null){
            tmpLst.add(tmpRecord);
        }

        // 没有则返回空
        return tmpLst.isEmpty() ? null : tmpLst;
    }
}
