package com.jjforever.wgj.maincalendar.Model;

import com.jjforever.wgj.maincalendar.util.LunarCalendar;

/**
 * Created by Wgj on 2016/8/28.
 * 阳历节日实体类定义
 */
public class SolarHoliday implements ICalendarRecord {
    // 节日标题
    private String mTitle;

    public SolarHoliday(String title){
        mTitle = title;
    }

    public int getType(){
        return RecordType.SOLAR_HOLIDAY;
    }

    public String getTitle(){
        return mTitle;
    }

    /**
     * 显示方式
     * @return RecordShowType.TEXT
     */
    public int showType() {
        return RecordShowType.TEXT | RecordShowType.DOT;
    }

    /**
     * 该类是不包含记录时间的
     * @return null
     */
    public LunarCalendar getRecordTime(){
        return null;
    }
}
