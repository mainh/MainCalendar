package com.jjforever.wgj.maincalendar.monthui;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.jjforever.wgj.maincalendar.BLL.GlobalSettingMng;
import com.jjforever.wgj.maincalendar.Model.GlobalSetting;
import com.jjforever.wgj.maincalendar.R;

/**
 * Created by Wgj on 2016/8/15.
 * 界面颜色主题样式类
 */
public class ThemeStyle {
    // 主色调
    public static int Primary;
    // 主暗色调
    public static int PrimaryDark;
    // 悬浮按钮等的颜色
    public static int Accent;
    // 当前显示月份的字体颜色
    public static int CurrentMonth;
    public static int CurrentLunar;
    // 日历背景色
    public static int BackColor;
    // 当日字体颜色
    public static int Today;
    public static int TodayLunar;
    // 节日颜色
    public static int Holiday;
    // 日常记录颜色
    public static int Daily;
    // 列表项被选中的颜色
    public static int ItemSelected;
    // 白班背景色
    public static int WorkDay;
    // 夜班背景色
    public static int WorkNight;

    /**
     * 从资源中载入默认主题
     * @param context 内容提供器
     */
    public static void LoadThemeResource(Context context)
    {
        PrimaryDark = ContextCompat.getColor(context, R.color.colorPrimaryDark);
        Accent = ContextCompat.getColor(context, R.color.colorAccent);
        CurrentMonth = ContextCompat.getColor(context, R.color.currentMonth);
        CurrentLunar = ContextCompat.getColor(context, R.color.currentLunar);
        BackColor = ContextCompat.getColor(context, R.color.backCalendar);
        Holiday = ContextCompat.getColor(context, R.color.holiday);
        Today = ContextCompat.getColor(context, R.color.today);
        TodayLunar = ContextCompat.getColor(context, R.color.todayLunar);
        Daily = ContextCompat.getColor(context, R.color.dailyRecord);
        ItemSelected = ContextCompat.getColor(context, R.color.itemSelected);
        WorkDay = ContextCompat.getColor(context, R.color.workDay);
        WorkNight = ContextCompat.getColor(context, R.color.workNight);
    }

    /**
     * 根据用户设置载入主题颜色
     */
    public static void LoadGlobalTheme(){
        GlobalSetting tmpSetting = GlobalSettingMng.getSetting();
        Primary = tmpSetting.getPrimaryColor();
    }
}
