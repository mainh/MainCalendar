package com.jjforever.wgj.maincalendar.Model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Wgj on 2016/9/20.
 * 倒班每天记录实体类
 */
public class ShiftsWorkItem implements Parcelable {
    // 什么都不做标识
    final static int DO_NOTHING = 0;
    // 该条需要更新标识
    public final static int UPDATE = 1;
    // 该条需要添加标识
    public final static int INSERT = 2;
    // 该条需要删除标识
    public final static int DELETE = 3;

    @IntDef({DO_NOTHING, UPDATE, INSERT, DELETE})
    @Retention(RetentionPolicy.SOURCE)
    @interface ActionFlag {
    }

    // 记录索引
    private long mIndex;
    // 所属倒班记录索引
    private long mWorkIndex;
    // 第几天
    private int mDayNo;
    // 当天上班或休息标题
    private String mTitle = "";
    // 开始时间
    private AlarmTime mStartTime;
    // 结束时间
    private AlarmTime mEndTime;
    // 需要更新或者删除标识 0表示什么都不做
    @ActionFlag
    private int mFlag;

    public ShiftsWorkItem(){
        this.mStartTime = new AlarmTime(0, 0);
        this.mEndTime = new AlarmTime(23, 59);
        this.mFlag = DO_NOTHING;
    }

    public long getIndex(){
        return mIndex;
    }

    public void setIndex(long index){
        this.mIndex = index;
    }

    public long getWorkIndex(){
        return mWorkIndex;
    }

    public void setWorkIndex(long index){
        this.mWorkIndex = index;
    }

    public int getDayNo(){
        return mDayNo;
    }

    public void setDayNo(int day){
        this.mDayNo = day;
    }

    public String getTitle(){
        return mTitle;
    }

    public void setTitle(String title){
        this.mTitle = title;
    }

    public AlarmTime getStartTime(){
        return mStartTime;
    }

    public void setStartTime(int hour, int minute){
        this.mStartTime.Hour = hour;
        this.mStartTime.Minute = minute;
    }

    public void setStartTime(int time){
        this.mStartTime.setTime(time);
    }

    public AlarmTime getEndTime(){
        return mEndTime;
    }

    public void setEndTime(int hour, int minute){
        this.mEndTime.Hour = hour;
        this.mEndTime.Minute = minute;
    }

    /**
     * 从数据库读取的数据进行时间设置
     * @param time 数据库时间值 分钟数
     */
    public void setEndTime(int time){
        this.mEndTime.setTime(time);
    }

    @ActionFlag
    public int getFlag(){
        return mFlag;
    }

    public void setFlag(@ActionFlag int flag){
        this.mFlag = flag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mIndex);
        dest.writeLong(this.mWorkIndex);
        dest.writeInt(this.mDayNo);
        dest.writeString(this.mTitle);
        dest.writeParcelable(this.mStartTime, flags);
        dest.writeParcelable(this.mEndTime, flags);
        dest.writeInt(this.mFlag);
    }

    @SuppressWarnings("ResourceType")
    protected ShiftsWorkItem(Parcel in) {
        this.mIndex = in.readLong();
        this.mWorkIndex = in.readLong();
        this.mDayNo = in.readInt();
        this.mTitle = in.readString();
        this.mStartTime = in.readParcelable(AlarmTime.class.getClassLoader());
        this.mEndTime = in.readParcelable(AlarmTime.class.getClassLoader());
        this.mFlag = in.readInt();
    }

    public static final Parcelable.Creator<ShiftsWorkItem> CREATOR = new Parcelable.Creator<ShiftsWorkItem>() {
        @Override
        public ShiftsWorkItem createFromParcel(Parcel source) {
            return new ShiftsWorkItem(source);
        }

        @Override
        public ShiftsWorkItem[] newArray(int size) {
            return new ShiftsWorkItem[size];
        }
    };
}
