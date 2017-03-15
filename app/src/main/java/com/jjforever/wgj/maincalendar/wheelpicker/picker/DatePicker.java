package com.jjforever.wgj.maincalendar.wheelpicker.picker;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import com.jjforever.wgj.maincalendar.common.util.DateUtils;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;
import com.jjforever.wgj.maincalendar.wheelpicker.widget.WheelView;

/**
 * 日期选择器
 *
 * @author 李玉江[QQ :1032694760]
 * @version 2015 /12/14
 */
public class DatePicker extends WheelPicker {
    /**
     * 年月日
     */
    public static final int YEAR_MONTH_DAY = 0;
    /**
     * 年月
     */
    public static final int YEAR_MONTH = 1;
    /**
     * 月日
     */
    public static final int MONTH_DAY = 2;
    /**
     * 年月日 时分
     */
    public static final int DATE_TIME = 3;
    private ArrayList<String> years = new ArrayList<>();
    private ArrayList<String> months = new ArrayList<>();
    private ArrayList<String> days = new ArrayList<>();
    private ArrayList<String> hours = new ArrayList<>();
    private ArrayList<String> minutes = new ArrayList<>();
    private OnDatePickListener onDatePickListener;
//    private String yearLabel = "年", monthLabel = "月", dayLabel = "日";
//    private String hourLabel = ":", minuteLabel = "";
    private String selectedHour = "", selectedMinute = "";
    private int selectedYearIndex = 0, selectedMonthIndex = 0, selectedDayIndex = 0;
    private int mode = YEAR_MONTH_DAY;

    // 农历日期
    private LunarCalendar mLunar = new LunarCalendar();

    /**
     * 安卓开发应避免使用枚举类（enum），因为相比于静态常量enum会花费两倍以上的内存。
     *
     * @link http ://developer.android.com/training/articles/memory.html#Overhead
     */
    @IntDef(value = {YEAR_MONTH_DAY, YEAR_MONTH, MONTH_DAY, DATE_TIME})
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode {
    }

    /**
     * Instantiates a new Date picker.
     *
     * @param activity the activity
     */
    public DatePicker(Activity activity) {
        this(activity, YEAR_MONTH_DAY);
    }

    /**
     * Instantiates a new Date picker.
     *
     * @param activity the activity
     * @param mode     the mode
     * @see #YEAR_MONTH_DAY #YEAR_MONTH_DAY#YEAR_MONTH_DAY
     * @see #YEAR_MONTH #YEAR_MONTH#YEAR_MONTH
     * @see #MONTH_DAY #MONTH_DAY#MONTH_DAY
     */
    public DatePicker(Activity activity, @Mode int mode) {
        super(activity);
        this.mode = mode;
        for (int i = 2000; i <= 2050; i++) {
            years.add(String.valueOf(i));
        }
        for (int i = 1; i <= 12; i++) {
            months.add(DateUtils.fillZero(i));
        }
        for (int i = 1; i <= 31; i++) {
            days.add(DateUtils.fillZero(i));
        }
        for (int i = 0; i < 24; i++) {
            hours.add(DateUtils.fillZero(i));
        }
        for (int i = 0; i < 60; i++) {
            minutes.add(DateUtils.fillZero(i));
        }
    }

    /**
     * 设置年月日的单位字符
     *
     * @param yearLabel  the year label
     * @param monthLabel the month label
     * @param dayLabel   the day label
     */
//    public void setLabel(String yearLabel, String monthLabel, String dayLabel) {
//        this.yearLabel = yearLabel;
//        this.monthLabel = monthLabel;
//        this.dayLabel = dayLabel;
//    }

    /**
     * 设置要显示的年代范围
     *
     * @param startYear the start year
     * @param endYear   the end year
     */
    public void setRange(int startYear, int endYear) {
        years.clear();
        for (int i = startYear; i <= endYear; i++) {
            years.add(String.valueOf(i));
        }
    }

    private int findItemIndex(ArrayList<String> items, int item) {
        //折半查找有序元素的索引
        int index = Collections.binarySearch(items, item, new Comparator<Object>() {
            @Override
            public int compare(Object lhs, Object rhs) {
                String lhsStr = lhs.toString();
                String rhsStr = rhs.toString();
                lhsStr = lhsStr.startsWith("0") ? lhsStr.substring(1) : lhsStr;
                rhsStr = rhsStr.startsWith("0") ? rhsStr.substring(1) : rhsStr;
                return Integer.parseInt(lhsStr) - Integer.parseInt(rhsStr);
            }
        });
        if (index < 0) {
            index = 0;
        }
        return index;
    }

    /**
     * Sets selected item.
     *
     * @param year  the year
     * @param month the month
     * @param day   the day
     */
    public void setSelectedItem(int year, int month, int day) {
        selectedYearIndex = findItemIndex(years, year);
        selectedMonthIndex = findItemIndex(months, month + 1);
        selectedDayIndex = findItemIndex(days, day);
    }

    /**
     * 设置选择项
     * @param year 年
     * @param month 月
     * @param day 日
     * @param hour 时
     * @param minute 分
     */
    public void setSelectedItem(int year, int month, int day, int hour, int minute)
    {
        setSelectedItem(year, month, day);
        selectedHour = DateUtils.fillZero(hour);
        selectedMinute = DateUtils.fillZero(minute);
    }

    /**
     * Sets selected item.
     *
     * @param yearOrMonth the year or month
     * @param monthOrDay  the month or day
     */
    public void setSelectedItem(int yearOrMonth, int monthOrDay) {
        if (mode == MONTH_DAY) {
            selectedMonthIndex = findItemIndex(months, yearOrMonth + 1);
            selectedDayIndex = findItemIndex(days, monthOrDay);
        } else {
            selectedYearIndex = findItemIndex(years, yearOrMonth);
            selectedMonthIndex = findItemIndex(months, monthOrDay + 1);
        }
    }

    /**
     * Sets on CellDate pick listener.
     *
     * @param listener the listener
     */
    public void setOnDatePickListener(OnDatePickListener listener) {
        this.onDatePickListener = listener;
    }

    /**
     * 更新农历日期
     */
    private void updateLunar()
    {
        mLunar.set(Calendar.YEAR, getSelectedYear());
        mLunar.set(Calendar.MONTH, getSelectedMonth());
        mLunar.set(Calendar.DAY_OF_MONTH, getSelectedDay());
        setTitleText(mLunar.getLunarDateString());
    }

    /**
     * 创建一个滑动选择视图
     * @param layout 要添加的父层
     * @param label 单位标签
     * @return 创建的滑动控件
     */
    private WheelView createWheelView(LinearLayout layout, String label, boolean hide)
    {
        if (hide){
            return null;
        }
        WheelView tmpView = new WheelView(activity.getBaseContext());
        tmpView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        tmpView.setTextSize(textSize);
        tmpView.setTextColor(textColorNormal, textColorFocus);
        tmpView.setLineVisible(lineVisible);
        tmpView.setLineColor(lineColor);
        tmpView.setOffset(offset);
        layout.addView(tmpView);
        TextView tmpTextView = new TextView(activity);
        tmpTextView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        tmpTextView.setTextSize(textSize);
        tmpTextView.setTextColor(textColorFocus);
        if (!TextUtils.isEmpty(label)) {
            tmpTextView.setText(label);
        }
        layout.addView(tmpTextView);

//        if (hide){
//            tmpView.setVisibility(View.GONE);
//            tmpTextView.setVisibility(View.GONE);
//        }

        return tmpView;
    }

    @Override
    @NonNull
    protected View makeCenterView() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        WheelView yearView = createWheelView(layout, "-", mode == MONTH_DAY);
        WheelView monthView = createWheelView(layout, "-", false);
        final WheelView dayView = createWheelView(layout, "", mode == YEAR_MONTH);

        WheelView hourView = createWheelView(layout, ":", mode != DATE_TIME);
        WheelView minuteView = createWheelView(layout, "", mode != DATE_TIME);

        if (mode == DATE_TIME){
            hourView.setItems(hours, selectedHour);
            minuteView.setItems(minutes, selectedMinute);
            hourView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                    selectedHour = item;
                }
            });
            minuteView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                    selectedMinute = item;
                }
            });
        }

        if (mode != MONTH_DAY) {
            if (selectedYearIndex == 0) {
                yearView.setItems(years);
            } else {
                yearView.setItems(years, selectedYearIndex);
            }
            yearView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                    selectedYearIndex = selectedIndex;
                    //需要根据年份及月份动态计算天数
                    days.clear();
                    int maxDays = DateUtils.calculateDaysInMonth(stringToYearMonthDay(item), stringToYearMonthDay(months.get(selectedMonthIndex)));
                    for (int i = 1; i <= maxDays; i++) {
                        days.add(DateUtils.fillZero(i));
                    }
                    if (selectedDayIndex >= maxDays) {
                        //年或月变动时，保持之前选择的日不动：如果之前选择的日是之前年月的最大日，则日自动为该年月的最大日
                        selectedDayIndex = days.size() - 1;
                    }
                    dayView.setItems(days, selectedDayIndex);
                    updateLunar();
                }
            });
        }

        if (selectedMonthIndex == 0) {
            monthView.setItems(months);
        } else {
            monthView.setItems(months, selectedMonthIndex);
        }
        monthView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                selectedMonthIndex = selectedIndex;
                if (mode != YEAR_MONTH) {
                    //年月日或年月模式下，需要根据年份及月份动态计算天数
                    days.clear();
                    int maxDays = DateUtils.calculateDaysInMonth(stringToYearMonthDay(years.get(selectedYearIndex)), stringToYearMonthDay(item));
                    for (int i = 1; i <= maxDays; i++) {
                        days.add(DateUtils.fillZero(i));
                    }
                    if (selectedDayIndex >= maxDays) {
                        //年或月变动时，保持之前选择的日不动：如果之前选择的日是之前年月的最大日，则日自动为该年月的最大日
                        selectedDayIndex = days.size() - 1;
                    }
                    dayView.setItems(days, selectedDayIndex);
                    updateLunar();
                }
            }
        });
        if (mode != YEAR_MONTH) {
            if (selectedDayIndex == 0) {
                dayView.setItems(days);
            } else {
                dayView.setItems(days, selectedDayIndex);
            }
            dayView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
                @Override
                public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                    selectedDayIndex = selectedIndex;
                    updateLunar();
                }
            });
        }
        return layout;
    }

    private int stringToYearMonthDay(String text) {
        if (text.startsWith("0") && text.length() > 1) {
            //截取掉前缀0以便转换为整数
            text = text.substring(1);
        }
        return Integer.parseInt(text);
    }

    @Override
    protected void onSubmit() {
        if (onDatePickListener != null) {
            int year = getSelectedYear();
            int month = getSelectedMonth();
            int day = getSelectedDay();
            switch (mode) {
                case YEAR_MONTH:
                    ((OnYearMonthPickListener) onDatePickListener).onDatePicked(year, month);
                    break;
                case MONTH_DAY:
                    ((OnMonthDayPickListener) onDatePickListener).onDatePicked(month, day);
                    break;
                case DATE_TIME:
                    ((OnAllPickListener) onDatePickListener).onDatePicked(year, month, day,
                                                        getSelectedHour(), getSelectedMinute());
                    break;
                default:
                    ((OnYearMonthDayPickListener) onDatePickListener).onDatePicked(year, month, day);
                    break;
            }
        }
    }

    /**
     * Gets selected year.
     *
     * @return the selected year
     */
    public int getSelectedYear() {
        return stringToYearMonthDay(years.get(selectedYearIndex));
    }

    /**
     * Gets selected month.
     *
     * @return the selected month
     */
    public int getSelectedMonth() {
        return stringToYearMonthDay(months.get(selectedMonthIndex)) - 1;
    }

    /**
     * Gets selected day.
     *
     * @return the selected day
     */
    public int getSelectedDay() {
        return stringToYearMonthDay(days.get(selectedDayIndex));
    }
    /**
     * Gets selected hour.
     *
     * @return the selected hour
     */
    public int getSelectedHour() {
        return stringToYearMonthDay(selectedHour);
    }

    /**
     * Gets selected minute.
     *
     * @return the selected minute
     */
    public int getSelectedMinute() {
        return stringToYearMonthDay(selectedMinute);
    }

    /**
     * The interface On CellDate pick listener.
     */
    interface OnDatePickListener {

    }

    /**
     * The interface On year month day pick listener.
     */
    public interface OnYearMonthDayPickListener extends OnDatePickListener {

        /**
         * On CellDate picked.
         *
         * @param year  the year
         * @param month the month
         * @param day   the day
         */
        void onDatePicked(int year, int month, int day);

    }

    /**
     * 设置最详细的监听接口
     */
    public interface OnAllPickListener extends OnDatePickListener {

        /**
         * On CellDate picked.
         *
         * @param year  the year
         * @param month the month
         * @param day   the day
         * @param hour 时
         * @param minute 分
         */
        void onDatePicked(int year, int month, int day, int hour, int minute);
    }

    /**
     * The interface On year month pick listener.
     */
    interface OnYearMonthPickListener extends OnDatePickListener {

        /**
         * On CellDate picked.
         *
         * @param year  the year
         * @param month the month
         */
        void onDatePicked(int year, int month);

    }

    /**
     * The interface On month day pick listener.
     */
    public interface OnMonthDayPickListener extends OnDatePickListener {

        /**
         * On CellDate picked.
         *
         * @param month the month
         * @param day   the day
         */
        void onDatePicked(int month, int day);

    }

}
