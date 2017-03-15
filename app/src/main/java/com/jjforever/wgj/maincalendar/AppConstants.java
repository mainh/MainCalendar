package com.jjforever.wgj.maincalendar;

import android.content.Context;
import android.util.Log;

import com.jjforever.wgj.maincalendar.BLL.DatabaseHelper;
import com.jjforever.wgj.maincalendar.BLL.GlobalSettingMng;
import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.monthui.ThemeStyle;
import com.jjforever.wgj.maincalendar.util.Helper;

/**
 * Created by Wgj on 2016/8/27.
 * 应用中用到的一些常量定义
 */
public final class AppConstants {

    // 调试标识
    public static final String LOG_TAG = "MainCalendar";
    // 是否为调试模式
    public static final boolean DEBUG = BuildConfig.DEBUG;
    // 主界面选中的日期
    static final String MAIN_ACTIVITY_CLICK_DATE = "main_click_date";
    // 服务线程调用页面
    public static final String SERVICE_CALL_ACTIVITY = "service_call_activity";
    // 服务传递动作
    public static final String MAIN_CALENDAR_SERVICE = "MAIN_CALENDAR_SERVICE";
    // 本程序用于SharedPreferences设置项标识
    public final static String SETTING_PREFERENCE = "MainCalendar";

    // 删除记录代码
    static int RESULT_DELETE = 7;
    // 预载日历页面数量
    final static int LOAD_CALENDAR_VIEW_COUNT = 3;

    // 闹钟类型对应下拉列表的索引
    static final int[] AlarmTypeIndexs = new int[]{
            AlarmRecord.ONCE, AlarmRecord.BY_LUNAR_ONCE,
            AlarmRecord.BY_DAY, AlarmRecord.BY_WEEK,
            AlarmRecord.BY_MONTH, AlarmRecord.BY_YEAR,
            AlarmRecord.BY_LUNAR_MONTH, AlarmRecord.BY_LUNAR_YEAR};

    // 闹钟类型名称集合
    public static String[] AlarmTypeNames;

    /**
     * 根据闹钟类型获取索引
     * @param type 闹钟类型
     * @return 在数组中的索引
     */
    public static int getActionTypeIndex(int type){
        for (int i = 0; i < AlarmTypeIndexs.length; i++){
            if (AlarmTypeIndexs[i] == type){
                return i;
            }
        }

        return -1;
    }

    /**
     * 载入全局性服务，Activity跟Service都需要用到的设置
     * @param context 上下文内容提供器
     */
    public static void loadGlobalService(Context context){
        // 初始化数据库
        DatabaseHelper.initDatabase(context);

        // 读取全局设置
        GlobalSettingMng.ReadSetting(context);

        // 载入默认主题
        ThemeStyle.LoadThemeResource(context);
        ThemeStyle.LoadGlobalTheme();
    }

    /**
     * 输出调试信息
     * @param str 调试信息
     * @return 输出结果
     */
    public static int DLog(String str){
        Helper.RecordLog('D', str);
        return Log.d(LOG_TAG, str);
    }

//    public static int ILog(String str){
//        return Log.i(LOG_TAG, str);
//    }

    public static int WLog(String str){
        Helper.RecordLog('W', str);
        return Log.w(LOG_TAG, str);
    }

    public static int ELog(String str){
        Helper.RecordLog('E', str);
        return Log.e(LOG_TAG, str);
    }

    public static int XLog(String str){
        return Log.e(LOG_TAG, str);
    }
}
