package com.jjforever.wgj.maincalendar.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.BLL.GlobalSettingMng;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Wgj on 2016/8/25.
 * 帮助类
 */
public final class Helper {

    /**
     * 判断字符串是否为空
     * @param str 要判断的字符串
     * @return 是否为空
     */
    public static boolean isNullOrEmpty(String str)
    {
        return  (str == null || str.length() <= 0);
    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";

        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (isNullOrEmpty(versionName)) {
                return "";
            }
        } catch (Exception e) {
            AppConstants.ELog(e.toString());
        }
        return versionName;
    }

    /**
     * 根据全路径获取文件名
     * @param path 路径
     * @return 文件名
     */
    public static String getFileName(String path){
        int start = path.lastIndexOf("/");
        int end = path.lastIndexOf(".");
        if (start != -1 && end != -1){
            return path.substring(start + 1, end);
        }else{
            return null;
        }
    }

    /**
     * 根据全路径获取路径名
     * @param path 路径
     * @return 路径
     */
    public static String getFilePath(String path){
        int end = path.lastIndexOf("/");
        if (end != -1){
            return path.substring(0, end);
        }
        else{
            return null;
        }
    }

    /**
     * 获取状态栏高度
     * @return 状态栏高度
     */
    public static int getStatusHeight(Context context){
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            return context.getResources().getDimensionPixelSize(resourceId) - 5;
        }

        AppConstants.WLog("Do not find status height...");
        return 35;
    }

    /**
     * 获取日志文件路径
     * @return 文件句柄
     */
    private static File getLogFile(){
        File file = null;

        String logPath = Environment.getExternalStorageDirectory().getPath() + "/MainLog.txt";
        try {
            file = new File(logPath);
            if (!file.exists()) {
                if (!file.createNewFile()){
                    AppConstants.XLog("create New file fail...");
                    return null;
                }
            }
        } catch (Exception e) {
            AppConstants.XLog("get log file fail: " + e.toString());
        }

        return file;
    }

    /**
     * 将日志记录到文件中
     * @param content 记录内容
     */
    public static void RecordLog(char level, String content){
        if (level == 'X'){
            return;
        }
        if (!AppConstants.DEBUG && !GlobalSettingMng.getSetting().getIsRecordLog()){
            return;
        }

        File tmpFile = getLogFile();
        if (tmpFile == null){
            return;
        }

        try {
            RandomAccessFile raf = new RandomAccessFile(tmpFile, "rwd");
            raf.seek(tmpFile.length());
            DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
            String tmpStr = String.format("%s %c %s\r\n",
                                dateFormat.format(new java.util.Date()), level,
                                content);
            raf.write(tmpStr.getBytes());
            raf.close();
        }
        catch (Exception ex){
            AppConstants.XLog("Record log ex: " + ex.toString());
        }
    }

    /**
     * 是否为KitKat以后的系统
     * @return true of false
     */
    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * 查看指定服务是否正在运行
     * @param context 内容提供器
     * @param serviceName 服务全名
     * @return 是否在运行
     */
//    public static boolean isServiceRunning(Context context,String serviceName){
//        // 校验服务是否还存在
//        ActivityManager am = (ActivityManager) context
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
//        for (ActivityManager.RunningServiceInfo info : services) {
//            // 得到所有正在运行的服务的名称
//            String name = info.service.getClassName();
//            if (serviceName.equals(name)) {
//                return true;
//            }
//        }
//        return false;
//    }
}
