package com.jjforever.wgj.maincalendar.Model;

import com.jjforever.wgj.maincalendar.util.LunarCalendar;

/**
 * Created by Wgj on 2016/8/28.
 * 日历记录接口
 */
public interface ICalendarRecord {
    /**
     * 获取该记录的类型
     * @return RecordType中定义的类型
     */
    int getType();

    /**
     * 获取记录的标题
     * @return 该记录的标题
     */
    String getTitle();

    /**
     * 获取记录时间
     * @return 记录时间
     */
    LunarCalendar getRecordTime();

    /**
     * 获取在日历中的显示方式
     * @return 是否显示在日历中 RecordShowType中定义方式
     */
    int showType();
}
