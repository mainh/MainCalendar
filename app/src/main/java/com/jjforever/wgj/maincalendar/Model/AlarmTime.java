package com.jjforever.wgj.maincalendar.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 * Created by Wgj on 2016/9/8.
 * 报警用的时间类
 */
public class AlarmTime implements Parcelable {
    // 小时
    public int Hour;
    // 分钟
    public int Minute;

    public AlarmTime(int hour, int minute){
        this.Hour = hour;
        this.Minute = minute;
    }

    /**
     * 根据时间分钟数创建对象 time=小时*60 + 分钟
     * @param time 设置的时间分钟数
     */
    AlarmTime(int time){
        setTime(time);
    }

    public void setTime(int time){
        this.Hour = time/60;
        this.Minute = time%60;
    }

    /**
     * 获取时间分钟总数
     * @return 分钟总数
     */
    public int getTime(){
        return (this.Hour * 60 + this.Minute);
    }

    /**
     * 比较时间
     * @param hour 小时
     * @param minute 分钟
     * @return 相差时间 > 0 参数时间比该对象时间晚的分钟数
     * = 0说明时间相等
     * < 0 参数时间比该对象时间早的分钟数
     */
    public int compareTime(int hour, int minute){
        int alarmMinute = this.getTime();
        int curMinute = hour * 60 + minute;

        return (curMinute - alarmMinute);
    }

    @Override
    public String toString(){
        return String.format(Locale.getDefault(),
                        "%02d:%02d", this.Hour, this.Minute);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.Hour);
        dest.writeInt(this.Minute);
    }

    protected AlarmTime(Parcel in) {
        this.Hour = in.readInt();
        this.Minute = in.readInt();
    }

    public static final Parcelable.Creator<AlarmTime> CREATOR = new Parcelable.Creator<AlarmTime>() {
        @Override
        public AlarmTime createFromParcel(Parcel source) {
            return new AlarmTime(source);
        }

        @Override
        public AlarmTime[] newArray(int size) {
            return new AlarmTime[size];
        }
    };
}
