package com.jjforever.wgj.maincalendar.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.jjforever.wgj.maincalendar.util.LunarCalendar;
import java.util.Calendar;

/**
 * Created by Wgj on 2016/8/26.
 * 日程记录实体类 实现Serializable接口方便传递
 */
public class DailyRecord implements ICalendarRecord, Parcelable {

    // 记录索引
    private long mIndex = 0;
    // 记录的日期
    private LunarCalendar mRecordTime;
    // 天气情况代码
    private int mWeather;
    // 记录标题
    private String mTitle = "";
    // 记录内容
    private String mContent = "";
    // 是否在日历中显示
    private boolean mDisplay = true;
    // 记录创建时间
    private Calendar mCreateTime;
    // 是否为新记录
    private boolean mIsNew;

    /**
     * 创建一条新的记录
     */
    public DailyRecord() {
        mIsNew = true;
    }

    /**
     * 创建一条记录
     * @param isNew 是否为新记录
     */
    public DailyRecord(boolean isNew){
        mIsNew = isNew;
    }

    /**
     * 获取日常记录类型
     * @return 日常记录类型 RecordType.DAILY_RECORD
     */
    public int getType(){
        return RecordType.DAILY_RECORD;
    }

    /**
     * 显示方式
     * @return RecordShowType类型
     */
    public int showType(){
        return mDisplay ? RecordShowType.TEXT_DOT : RecordShowType.HIDE;
    }

    public long getIndex(){
        return mIndex;
    }

    public void setIndex(long index){
        mIndex = index;
    }

    /**
     * 获取记录的日期
     * @return 记录的日期
     */
    public LunarCalendar getRecordTime(){
        return mRecordTime;
    }

    public void setRecordTime(LunarCalendar calendar){
        mRecordTime = calendar;
    }

    public int getWeather(){
        return mWeather;
    }

    public void setWeather(int weather){
        mWeather = weather;
    }

    public String getTitle(){
        return mTitle;
    }

    public void setTitle(String title){
        mTitle = title;
    }

    public String getContent(){
        return mContent;
    }

    public void setContent(String content){
        mContent = content;
    }

    public boolean getDisplay(){
        return  mDisplay;
    }

    public void setDisplay(boolean display){
        mDisplay = display;
    }

    public Calendar getCreateTime(){
        return mCreateTime;
    }

    public void setCreateTime(Calendar calendar){
        mCreateTime = calendar;
    }

    public boolean getIsNew(){
        return  mIsNew;
    }

    public void setIsNew(boolean isNew){
        mIsNew = isNew;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mIndex);
        dest.writeSerializable(this.mRecordTime);
        dest.writeInt(this.mWeather);
        dest.writeString(this.mTitle);
        dest.writeString(this.mContent);
        dest.writeByte(this.mDisplay ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.mCreateTime);
        dest.writeByte(this.mIsNew ? (byte) 1 : (byte) 0);
    }

    protected DailyRecord(Parcel in) {
        this.mIndex = in.readLong();
        this.mRecordTime = (LunarCalendar) in.readSerializable();
        this.mWeather = in.readInt();
        this.mTitle = in.readString();
        this.mContent = in.readString();
        this.mDisplay = in.readByte() != 0;
        this.mCreateTime = (Calendar) in.readSerializable();
        this.mIsNew = in.readByte() != 0;
    }

    public static final Parcelable.Creator<DailyRecord> CREATOR = new Parcelable.Creator<DailyRecord>() {
        @Override
        public DailyRecord createFromParcel(Parcel source) {
            return new DailyRecord(source);
        }

        @Override
        public DailyRecord[] newArray(int size) {
            return new DailyRecord[size];
        }
    };
}
