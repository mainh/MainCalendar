package com.jjforever.wgj.maincalendar.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jjforever.wgj.maincalendar.AppConstants;

/**
 * Created by Wgj on 2016/10/5.
 * AlarmManager发送广播接收器
 */
public class CalendarReceiver extends BroadcastReceiver {
    /**
     * 每分钟检测启动一次后台服务
     * @param context 内容提供器
     * @param intent 传输数据
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        AppConstants.DLog("Receive start alarm service: " + intent.getAction());
//        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//            // 载入必须服务
//            AppConstants.loadGlobalService(context);
//        }
        context.startService(new Intent(context, CalendarService.class));
    }
}
