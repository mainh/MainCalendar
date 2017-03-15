package com.jjforever.wgj.maincalendar.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

import com.jjforever.wgj.maincalendar.AlarmNoticeActivity;
import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.BLL.AlarmRecordMng;
import com.jjforever.wgj.maincalendar.BLL.DatabaseHelper;
import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.Model.AlarmTime;
import com.jjforever.wgj.maincalendar.util.Helper;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Wgj on 2016/10/4.
 * 日历后台服务，闹钟，天气更新
 */
public class CalendarService extends Service {
    // 上次闹钟触发时间项
    private static final String LastAlarmTime = "LastAlarmTime";
    // 一小时的毫秒数
    private static int onHourMs = 60 * 60 * 1000;

    // 是否显示闹钟图标 修改，不显示闹钟图标，不占用系统闹钟工具闹钟图标
//    private boolean isShowIcon = false;

    // 解锁手机
    private PowerManager.WakeLock mWakeLock;

    // 当前日期
//    private LunarCalendar mLunar;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
//        mLunar = new LunarCalendar(Calendar.getInstance());
        if (DatabaseHelper.isNeedInit()){
            // 载入必须服务
            AppConstants.loadGlobalService(getBaseContext());
        }
//        AlarmAlertWakeLock.releaseCpuLock();
        // 提前统一设置下图标状态
//        isShowIcon = false;
//        setStatusIcon(true);
        AppConstants.DLog("Alarm service onCreate.");
        super.onCreate();
    }

    /**
     * 在状态栏显示闹钟图标
     * @param isShow 是否显示
     */
//    private void setStatusIcon(boolean isShow){
//        if (isShowIcon != isShow) {
//            isShowIcon = isShow;
//            Intent localIntent = new Intent("android.intent.action.ALARM_CHANGED");
//            localIntent.putExtra("alarmSet", isShow);
//            getBaseContext().sendBroadcast(localIntent);
//        }
//    }

    /**
     * 设置下次触发时间
     * @param ms 距离下次的毫秒数
     */
    private void setAlarmTime(long ms){
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent tmpIntent = new Intent(this, CalendarReceiver.class);
        tmpIntent.setAction(AppConstants.MAIN_CALENDAR_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, tmpIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Helper.isKitKatOrLater()) {
            manager.setExact(AlarmManager.RTC_WAKEUP, ms, pi);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, ms, pi);
        }
    }

    /**
     * 更新最近一次闹钟触发时间
     * @param time 闹钟时间
     */
    private void setLastTime(int time){
        SharedPreferences.Editor preferences = getSharedPreferences(AppConstants.SETTING_PREFERENCE, Context.MODE_PRIVATE).edit();
        preferences.putInt(LastAlarmTime, time);
        preferences.apply();
    }

    /**
     * 获取最近一次闹钟触发时间
     * @return 闹钟时间
     */
    private int getLastTime(){
        SharedPreferences preferences = getSharedPreferences(AppConstants.SETTING_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getInt(LastAlarmTime, -1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Calendar tmpCalendar = Calendar.getInstance();
                tmpCalendar.set(Calendar.SECOND, 0);
                tmpCalendar.set(Calendar.MILLISECOND, 0);

                // 当前时分
                AlarmTime curTime = new AlarmTime(tmpCalendar.get(Calendar.HOUR_OF_DAY), tmpCalendar.get(Calendar.MINUTE));
                int totalMinute = curTime.getTime();

                // 获取下次闹钟记录时间点
                long curMs = tmpCalendar.getTimeInMillis(); //SystemClock.elapsedRealtime();
                int nextMinute = AlarmRecordMng.getNextAlarmTime(totalMinute);
                long tmpMs = onHourMs;
                if (nextMinute > 0) {
                    // 设置下次触发时间
                    tmpMs = (nextMinute - totalMinute) * 60000;
                    if (tmpMs <= 0) {
                        // 说明非当天，需要加一天的毫秒数
                        tmpMs += AlarmRecord.DAY_MILLISECONDS;
                    }

                    if (tmpMs > onHourMs){
                        // 至少一个小时唤醒一次，防止被深度睡眠掉。。。
                        tmpMs = onHourMs;
                    }
//                    setStatusIcon(true);
                }
//                else{
//                    setStatusIcon(false);
//                }
                // 设置下次响应时间
                curMs += tmpMs;
                AppConstants.DLog(String.format(Locale.getDefault(),
                                    "Next alarm time: %tF %<tT", curMs));
                // 设置下次触发时间
                setAlarmTime(curMs);

                int lastTime = getLastTime();
                if (lastTime >= 0){
                    // 同一天同一分钟则不处理
                    if (lastTime == curTime.getTime()) {
                        AppConstants.DLog("Repeat alarm " + curTime.toString());
                        return;
                    }
                    // 清除上次闹钟触发时间
                    setLastTime(-1);
                }

                LunarCalendar mLunar = new LunarCalendar(tmpCalendar);
                AppConstants.DLog("Start check alarm " + mLunar.toString());
                ArrayList<AlarmRecord> tmpLst = AlarmRecordMng.getAlarmRecordsCurrent(totalMinute, mLunar);
                if (tmpLst != null && tmpLst.size() > 0) {
                    AppConstants.DLog("Find alarm count: " + tmpLst.size()
                            + ", first alarm content: " + tmpLst.get(0).toString());
                    // 更新闹钟提醒时间
                    acquireScreenCpuWakeLock();
                    setLastTime(curTime.getTime());
                    Intent noticeIntent = new Intent(getBaseContext(), AlarmNoticeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(AppConstants.SERVICE_CALL_ACTIVITY, tmpLst);
                    noticeIntent.putExtras(bundle);
                    noticeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 提醒过的一次性闹钟自动设置为暂停
                    for (AlarmRecord tmpRecord : tmpLst){
                        if (tmpRecord.getActionType() == AlarmRecord.ONCE
                                || tmpRecord.getActionType() == AlarmRecord.BY_LUNAR_ONCE) {
                            tmpRecord.setPause(true);
                            AlarmRecordMng.update(tmpRecord);
                        }
                    }
                    releaseCpuLock();
                    getApplication().startActivity(noticeIntent);
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DatabaseHelper.closeDatabase();
//        setStatusIcon(false);
//        releaseCpuLock();
//        AlarmAlertWakeLock.releaseCpuLock();
        AppConstants.DLog("Alarm service onDestroy");
        Intent localIntent = new Intent();
        // 销毁时重新启动Service
        localIntent.setClass(this, CalendarService.class);
        this.startService(localIntent);
    }

    /**
     * 获取解锁权
     */
    private void acquireScreenCpuWakeLock() {
        if (mWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE,
                AppConstants.LOG_TAG);
        mWakeLock.acquire();
    }

    /**
     * 释放解锁权
     */
    private void releaseCpuLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
