package com.jjforever.wgj.maincalendar.monthui;

import android.content.Context;

/**
 * Created by Wgj on 2016/8/10.
 * 生成日历视图集合
 */
public class CalendarViewBuilder {
    /**
     * 创建多个CalendarView，默认视图为月视图样式
     * @param context 内容提供器
     * @param count 生成个数
     * @param callBack 回调函数
     * @return 日历视图集合
     */
    public static CalendarView[] createMassCalendarViews(Context context, int count, CalendarView.CallBack callBack){
        CalendarView[] calendarViews = new CalendarView[count];
        for(int i = 0; i < count;i++){
            calendarViews[i] = new CalendarView(context, callBack);
        }
        return calendarViews;
    }
}
