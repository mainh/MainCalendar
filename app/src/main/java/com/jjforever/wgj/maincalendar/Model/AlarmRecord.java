package com.jjforever.wgj.maincalendar.Model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Wgj on 2016/9/8.
 * 闹钟记录
 */
public class AlarmRecord implements ICalendarRecord, Parcelable {
    // 一天的毫秒数
    public static final int DAY_MILLISECONDS = 24 * 3600000;

    // 根据农历确定日期标识
    private static final int BY_LUNAR = 0x8000;
    // 不循环，只响应一次
    public static final int ONCE = 0x0001;
    // 不循环，只响应农历一次
    public static final int BY_LUNAR_ONCE = BY_LUNAR | ONCE;
    // 按天循环，每几天动作
    public static final int BY_DAY = 0x0002;
    // 根据星期自定义循环
    public static final int BY_WEEK = 0x0004;
    // 按月循环
    public static final int BY_MONTH = 0x0008;
    // 按年循环
    public static final int BY_YEAR = 0x0010;
    // 按农历月循环，农历每月几号
    public static final int BY_LUNAR_MONTH = BY_LUNAR | BY_MONTH;
    // 按农历年循环，农历每年几月几号
    public static final int BY_LUNAR_YEAR = BY_LUNAR | BY_YEAR;

    // 记录索引
    private long mIndex = 0;
    // 闹钟动作类型标识
    private int mActionType;
    // 闹钟响应时间
    private AlarmTime mAlarmTime;
    // 记录的日期 非数据库中结构
    private LunarCalendar mRecordTime;
    // 记录的日期年 根据闹钟类型标识判断年月日中的内容
    private int mYear;
    // 记录的日期月
    private int mMonth;
    // 记录的日期日 每几日闹钟中高8位表示周期日 无符号16位型
    private int mDay;
    // 记录标题
    private String mTitle = "";
    // 记录内容
    private String mContent = "";
    // 是否在日历中显示
    private boolean mDisplay = true;
    // 是否停用
    private boolean mPause = false;
    // 记录创建时间
    private Calendar mCreateTime;
    // 是否为新记录
    private boolean mIsNew;

    @IntDef({ONCE, BY_LUNAR_ONCE, BY_DAY, BY_WEEK,
            BY_MONTH, BY_YEAR, BY_LUNAR_MONTH, BY_LUNAR_YEAR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Flag {
    }

    /**
     * 创建一条新的记录
     */
    public AlarmRecord() {
        mIsNew = true;
    }

    /**
     * 创建一条记录
     * @param isNew 是否为新记录
     */
    public AlarmRecord(boolean isNew){
        mIsNew = isNew;
    }

    /**
     * 获取日常记录类型
     * @return 日常记录类型 RecordType.DAILY_RECORD
     */
    public int getType(){
        return RecordType.ALARM_RECORD;
    }

    /**
     * 显示方式
     * @return RecordShowType类型
     */
    public int showType(){
        return mDisplay ? RecordShowType.DOT : RecordShowType.HIDE;
    }

    public long getIndex(){
        return mIndex;
    }

    public void setIndex(long index){
        mIndex = index;
    }

    public AlarmTime getAlarmTime(){
        return this.mAlarmTime;
    }

    public void setAlarmTime(int time){
        if (this.mAlarmTime == null) {
            this.mAlarmTime = new AlarmTime(time);
        }
        else{
            this.mAlarmTime.setTime(time);
        }
        //mRecordTime.set(Calendar.HOUR_OF_DAY, this.mAlarmTime.Hour);
        //mRecordTime.set(Calendar.MINUTE, this.mAlarmTime.Minute);
    }

    public void setAlarmTime(int hour, int minute){
        if (this.mAlarmTime == null){
            this.mAlarmTime = new AlarmTime(hour, minute);
        }
        else{
            this.mAlarmTime.Hour = hour;
            this.mAlarmTime.Minute = minute;
        }
    }

    /**
     * 获取记录的日期
     * @return 记录的日期
     */
    public LunarCalendar getRecordTime(){
        return mRecordTime;
    }

    /**
     * 判断指定日期是否有本对象闹钟
     * @param calendar 要检测的日期
     * @return 是否有闹钟
     */
    public boolean isRecordDate(LunarCalendar calendar){
        if (this.mActionType == BY_DAY){
            Calendar tmpDate = new GregorianCalendar(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            long curMill = tmpDate.getTimeInMillis();
            tmpDate.set(this.mYear, this.mMonth, this.getDay());
            long startMill = tmpDate.getTimeInMillis();
            if (startMill > curMill){
                return false;
            }
            long diff = curMill - startMill;
            int dayCount = (int)(diff / DAY_MILLISECONDS);

            // 每几天的闹钟
            return (getHighDay() != 0) && (dayCount % getHighDay() == 0);
        }
        if (this.mActionType == BY_WEEK){
            // 每周动作
            // 周日为1...
            return ((mDay & (1<<(calendar.get(Calendar.DAY_OF_WEEK) - 1))) != 0);
        }

        boolean sameYear = (mYear == calendar.get(Calendar.YEAR));
        boolean sameMonth = (mMonth == calendar.get(Calendar.MONTH));
        boolean sameDay = (mDay == calendar.get(Calendar.DAY_OF_MONTH));
        if ((this.mActionType & ONCE) == ONCE){
            // 只一次的动作，年月日都为阳历
            return (sameYear && sameMonth && sameDay);
        }

        if ((this.mActionType & BY_LUNAR) != 0){
            // 根据农历确定日期
            sameMonth = (mMonth == calendar.get(LunarCalendar.LUNAR_MONTH));
            sameDay = (mDay == calendar.get(LunarCalendar.LUNAR_DATE));
        }

        if ((this.mActionType & BY_YEAR) == BY_YEAR){
            // 每年动作
            return (sameMonth && sameDay);
        }
        else if ((this.mActionType & BY_MONTH) == BY_MONTH){
            // 每月的动作
            return sameDay;
        }

        return false;
    }

    public int getYear(){
        return mYear;
    }

    public void setYear(int year){
        mYear = year;
    }

    public int getMonth(){
        return mMonth;
    }

    public void setMonth(int month){
        mMonth = month;
    }

    /**
     * 获取日期的低8位
     * @return 日期的低8位
     */
    public int getDay(){
        return mDay & 0xFF;
    }

    /**
     * 获取日期所有位
     * @return 日期的所有位
     */
    public int getAllDay(){
        return mDay;
    }

    /**
     * 获取天的高8位
     * @return 天的高8位，用于特殊用途等
     */
    public int getHighDay(){
        return (mDay >> 8) & 0xFF;
    }

    public void setDay(int day){
        mDay = day;
    }

    public void setHighDay(int highDay){
        mDay &= 0x00FF;
        mDay |= (highDay << 8) & 0xFF00;
    }

    /**
     * 设置闹钟日期
     * @param calendar 日期
     */
    public void setRecordTime(LunarCalendar calendar){
        mRecordTime = new LunarCalendar(calendar);
        mRecordTime.set(Calendar.HOUR_OF_DAY, this.mAlarmTime.Hour);
        mRecordTime.set(Calendar.MINUTE, this.mAlarmTime.Minute);
    }

    /**
     * 获取闹钟动作类型
     * @return 闹钟动作类型
     */
    @Flag
    public int getActionType(){
        return mActionType;
    }

    /**
     * 设置闹钟动作类型
     * @param type 闹钟动作类型
     */
    public void setActionType(@Flag int type){
        mActionType = type;
    }

    public String getTitle(){
        if (this.mActionType == BY_DAY){
            return String.format(Locale.getDefault(), "%s/每%d天", mTitle, getHighDay());
        }
        return mTitle + "/" + AppConstants.AlarmTypeNames[AppConstants.getActionTypeIndex(this.mActionType)];
    }

    public String getOnlyTitle(){
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

    public boolean getPause(){
        return  mPause;
    }

    public void setPause(boolean pause){
        mPause = pause;
    }

    public boolean getIsNew(){
        return  mIsNew;
    }

    public void setIsNew(boolean isNew){
        mIsNew = isNew;
    }

    /**
     * 获取日期描述
     * @return 无返回null
     */
    public String getDateString(){
        String typeStr = null;
        switch (this.mActionType){
            case AlarmRecord.ONCE:
                typeStr = String.format(Locale.getDefault(),
                        "%d年%02d月%02d日", this.mYear,
                        this.mMonth + 1, this.mDay);
                break;

            case AlarmRecord.BY_LUNAR_ONCE:
                LunarCalendar tmpCalendar = new LunarCalendar(this.mYear, this.mMonth, this.mDay);
                typeStr = tmpCalendar.getLunarDateString();
                break;

            case AlarmRecord.BY_DAY:
                typeStr = String.format(Locale.getDefault(), "每%d日", getHighDay());
                break;

            case AlarmRecord.BY_WEEK:
                typeStr = LunarCalendar.getWeeksStr(this.mDay);
                break;

            case AlarmRecord.BY_MONTH:
                typeStr = String.format(Locale.getDefault(),
                        "每月%02d日", this.mDay);
                break;

            case AlarmRecord.BY_YEAR:
                typeStr = String.format(Locale.getDefault(),
                        "每年%02d月%02d日", this.mMonth + 1, this.mDay);
                break;

            case AlarmRecord.BY_LUNAR_MONTH:
                typeStr = String.format(Locale.getDefault(),
                        "每月%s", LunarCalendar.getLunarDay(this.mDay));
                break;

            case AlarmRecord.BY_LUNAR_YEAR:
                typeStr = String.format(Locale.getDefault(),
                        "每年%s月%s",
                        LunarCalendar.getLunarMonth(this.mMonth),
                        LunarCalendar.getLunarDay(this.mDay));
                break;

            default:
                break;
        }

        return typeStr;
    }

    @Override
    public String toString(){
        return getDateString() + " " + this.mAlarmTime.toString();
    }

    /**
     * 深度复制
     * @return 闹钟记录
     */
    public AlarmRecord depthClone(){
        AlarmRecord tmpRecord = new AlarmRecord(this.mIsNew);
        tmpRecord.mIndex = this.mIndex;
        tmpRecord.mActionType = this.mActionType;
        tmpRecord.setAlarmTime(this.mAlarmTime.getTime());
        tmpRecord.mYear = this.mYear;
        tmpRecord.mMonth = this.mMonth;
        tmpRecord.mDay = this.mDay;
        tmpRecord.mTitle = this.mTitle;
        tmpRecord.mContent = this.mContent;
        tmpRecord.mDisplay = this.mDisplay;
        tmpRecord.mPause = this.mPause;
        tmpRecord.mCreateTime = new LunarCalendar(this.mCreateTime);

        return tmpRecord;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mIndex);
        dest.writeInt(this.mActionType);
        dest.writeParcelable(this.mAlarmTime, flags);
        dest.writeSerializable(this.mRecordTime);
        dest.writeInt(this.mYear);
        dest.writeInt(this.mMonth);
        dest.writeInt(this.mDay);
        dest.writeString(this.mTitle);
        dest.writeString(this.mContent);
        dest.writeByte(this.mDisplay ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mPause ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.mCreateTime);
        dest.writeByte(this.mIsNew ? (byte) 1 : (byte) 0);
    }

    protected AlarmRecord(Parcel in) {
        this.mIndex = in.readLong();
        this.mActionType = in.readInt();
        this.mAlarmTime = in.readParcelable(AlarmTime.class.getClassLoader());
        this.mRecordTime = (LunarCalendar) in.readSerializable();
        this.mYear = in.readInt();
        this.mMonth = in.readInt();
        this.mDay = in.readInt();
        this.mTitle = in.readString();
        this.mContent = in.readString();
        this.mDisplay = in.readByte() != 0;
        this.mPause = in.readByte() != 0;
        this.mCreateTime = (Calendar) in.readSerializable();
        this.mIsNew = in.readByte() != 0;
    }

    public static final Parcelable.Creator<AlarmRecord> CREATOR = new Parcelable.Creator<AlarmRecord>() {
        @Override
        public AlarmRecord createFromParcel(Parcel source) {
            return new AlarmRecord(source);
        }

        @Override
        public AlarmRecord[] newArray(int size) {
            return new AlarmRecord[size];
        }
    };
}
