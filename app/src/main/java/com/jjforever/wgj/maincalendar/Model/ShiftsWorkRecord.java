package com.jjforever.wgj.maincalendar.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.jjforever.wgj.maincalendar.util.LunarCalendar;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Wgj on 2016/9/19.
 * 倒班/轮班记录实体类
 */
public class ShiftsWorkRecord implements Parcelable {
    // 记录索引
    private long mIndex;
    // 该轮班的标题
    private String mTitle = "";
    // 计算轮班的起始日期
    private LunarCalendar mStartDate;
    // 一个倒班循环周期天数 最多8天，最少2天
    private int mPeriod;
    // 每天记录集合
    private ArrayList<ShiftsWorkItem> mItems;
    // 记录创建时间
    private Calendar mCreateTime;
    // 是否为新记录
    private boolean mIsNew;

    public ShiftsWorkRecord(){
        mIsNew = true;
    }

    public ShiftsWorkRecord(boolean isNew){
        this.mIsNew = isNew;
    }

    public long getIndex(){
        return mIndex;
    }

    public void setIndex(long index){
        mIndex = index;
    }

    public String getTitle(){
        return mTitle;
    }

    public void setTitle(String title){
        this.mTitle = title;
    }

    public LunarCalendar getStartDate(){
        return this.mStartDate;
    }

    public void setStartDate(LunarCalendar date){
        this.mStartDate = date;
    }

    public int getPeriod(){
        return mPeriod;
    }

    public void setPeriod(int period){
        this.mPeriod = period;
    }

    public ArrayList<ShiftsWorkItem> getItems(){
        return mItems;
    }

    public void setItems(ArrayList<ShiftsWorkItem> items){
        this.mItems = items;
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

    /**
     * 计算指定日期是轮班的第几天
     * @param year 年
     * @param month 月
     * @param day 日
     * @return 第几天 第一天为0 小于0表示无效
     */
    public int getDayNo(int year, int month, int day){
        LunarCalendar tmpDate = new LunarCalendar(year, month, day);
        long curMill = tmpDate.getTimeInMillis();
        long startMill = this.mStartDate.getTimeInMillis();
        long diff = Math.abs(curMill - startMill);
        int dayCount = (int)(diff / (24 * 3600000));
        if (curMill >= startMill){
            return dayCount % this.mPeriod;
        }

        int dayNo = this.mPeriod - (dayCount % this.mPeriod);
        return dayNo == this.mPeriod ? 0 : dayNo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mIndex);
        dest.writeString(this.mTitle);
        dest.writeSerializable(this.mStartDate);
        dest.writeInt(this.mPeriod);
        dest.writeTypedList(this.mItems);
        dest.writeSerializable(this.mCreateTime);
        dest.writeByte(this.mIsNew ? (byte) 1 : (byte) 0);
    }

    protected ShiftsWorkRecord(Parcel in) {
        this.mIndex = in.readLong();
        this.mTitle = in.readString();
        this.mStartDate = (LunarCalendar) in.readSerializable();
        this.mPeriod = in.readInt();
        this.mItems = in.createTypedArrayList(ShiftsWorkItem.CREATOR);
        this.mCreateTime = (Calendar) in.readSerializable();
        this.mIsNew = in.readByte() != 0;
    }

    public static final Parcelable.Creator<ShiftsWorkRecord> CREATOR = new Parcelable.Creator<ShiftsWorkRecord>() {
        @Override
        public ShiftsWorkRecord createFromParcel(Parcel source) {
            return new ShiftsWorkRecord(source);
        }

        @Override
        public ShiftsWorkRecord[] newArray(int size) {
            return new ShiftsWorkRecord[size];
        }
    };
}

