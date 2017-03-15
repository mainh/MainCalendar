package com.jjforever.wgj.maincalendar.BLL;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;

import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.Model.GlobalSetting;
import com.jjforever.wgj.maincalendar.R;

/**
 * Created by Wgj on 2016/9/22.
 * 软件设置管理类
 */
public final class GlobalSettingMng {
    // 显示的轮班索引
    private static final String ShiftsWorkIndex = "ShiftsWorkIndex";
    // 主题颜色
    private static final String PrimaryColor = "PrimaryColor";
    // 闹钟铃声路径
    private static final String RingPath = "RingPath";
    // 闹钟响铃时长(s)
    private static final String RingSeconds = "RingSeconds";
    // 闹钟提醒是否在状态栏显示
    private static final String IsNotification = "IsNotification";
    // 是否记录调试信息
    private static final String IsRecordLog = "IsRecordLog";
    // 系统设置
    private static GlobalSetting mSetting = new GlobalSetting();

    /**
     * 获取软件设置
     * @return 全局设置
     */
    public static GlobalSetting getSetting(){
        return mSetting;
    }

    public static void setSetting(GlobalSetting setting){
        mSetting = setting;
    }

    /**
     * 读取配置
     */
    public static void ReadSetting(Context context){
        SharedPreferences preferences = context.getSharedPreferences(AppConstants.SETTING_PREFERENCE, Context.MODE_PRIVATE);
        mSetting.setShiftsWorkIndex(preferences.getLong(ShiftsWorkIndex, 0));
        mSetting.setPrimaryColor(preferences.getInt(PrimaryColor, ContextCompat.getColor(context, R.color.colorPrimary)));
        mSetting.setRingPath(preferences.getString(RingPath, ""));
        mSetting.setRingSeconds(preferences.getInt(RingSeconds, 60));
        mSetting.setIsNotification(preferences.getBoolean(IsNotification, true));
        mSetting.setIsRecordLog(preferences.getBoolean(IsRecordLog, false));
    }

    /**
     * 存储配置
     */
    public static boolean SaveSetting(Activity activity){
        try {
            SharedPreferences preferences = activity.getSharedPreferences(AppConstants.SETTING_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor tmpEditor = preferences.edit();
            tmpEditor.putLong(ShiftsWorkIndex, mSetting.getShiftsWorkIndex());
            tmpEditor.putInt(PrimaryColor, mSetting.getPrimaryColor());
            tmpEditor.putString(RingPath, mSetting.getRingPath());
            tmpEditor.putInt(RingSeconds, mSetting.getRingSeconds());
            tmpEditor.putBoolean(IsNotification, mSetting.getIsNotification());
            tmpEditor.putBoolean(IsRecordLog, mSetting.getIsRecordLog());
            tmpEditor.apply();
            return true;
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
    }
}
