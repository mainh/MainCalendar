package com.jjforever.wgj.maincalendar.Model;

/**
 * Created by Wgj on 2016/9/21.
 * 软件全局设置项
 */
public class GlobalSetting {
    // 如果要显示轮班记录则此值大于0
    private long mShiftsWorkIndex;
    // 主题颜色
    private int mPrimaryColor;
    // 铃声路径
    private String mRingPath;
    // 响铃时间长度，单位秒，默认60秒
    private int mRingSeconds;
    // 闹钟是否显示到状态栏中
    private boolean mNotification;
    // 是否将调试信息写入到文件中
    private boolean mRecordLog;

    public GlobalSetting(){
        this.mRingSeconds = 60;
        this.mShiftsWorkIndex = 0;
        this.mNotification = true;
        this.mRecordLog = false;
    }

    public long getShiftsWorkIndex(){
        return mShiftsWorkIndex;
    }

    public void setShiftsWorkIndex(long index){
        this.mShiftsWorkIndex = index;
    }

    public int getPrimaryColor(){
        return mPrimaryColor;
    }

    public void setPrimaryColor(int color){
        this.mPrimaryColor = color;
    }

    public String getRingPath(){
        return mRingPath;
    }

    public void setRingPath(String path){
        this.mRingPath = path;
    }

    public int getRingSeconds(){
        return mRingSeconds;
    }

    public void setRingSeconds(int seconds){
        this.mRingSeconds = seconds;
    }

    public boolean getIsNotification(){
        return mNotification;
    }

    public void setIsNotification(boolean isNotification){
        this.mNotification = isNotification;
    }

    public boolean getIsRecordLog(){
        return mRecordLog;
    }

    public void setIsRecordLog(boolean isRecord){
        this.mRecordLog = isRecord;
    }

    /**
     * 复制本地对象
     * @return 复制的对象
     */
    public GlobalSetting depthClone(){
        GlobalSetting copyObj = new GlobalSetting();

        copyObj.setPrimaryColor(this.mPrimaryColor);
        copyObj.setRingSeconds(this.mRingSeconds);
        copyObj.setRingPath(this.mRingPath);
        copyObj.setShiftsWorkIndex(this.mShiftsWorkIndex);
        copyObj.setIsNotification(this.mNotification);
        copyObj.setIsRecordLog(this.mRecordLog);

        return copyObj;
    }
}
