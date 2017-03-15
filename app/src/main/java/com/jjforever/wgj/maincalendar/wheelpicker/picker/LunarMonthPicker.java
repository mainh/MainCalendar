package com.jjforever.wgj.maincalendar.wheelpicker.picker;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.util.LunarCalendar;
import com.jjforever.wgj.maincalendar.wheelpicker.widget.WheelView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Wgj on 2016/9/11.
 * 农历月日选择器
 */
public class LunarMonthPicker extends WheelPicker {

    private int selectedMonth = 0;
    private int selectedDay = 0;
    private OnLunarMonthPickListener onLunarMonthPickListener;

    public LunarMonthPicker(Activity activity) {
        super(activity);
        Calendar tmpCalendar = Calendar.getInstance();
        LunarCalendar tmpLunar = new LunarCalendar(tmpCalendar);
        selectedMonth = tmpLunar.get(LunarCalendar.LUNAR_MONTH) - 1;
        selectedDay = tmpLunar.get(LunarCalendar.LUNAR_DATE) - 1;
    }

    public void setSelectedItem(int month, int day) {
        selectedMonth = month - 1;
        selectedDay = day - 1;
    }

    public void setOnLunarMonthPickListener(OnLunarMonthPickListener listener) {
        this.onLunarMonthPickListener = listener;
    }

    @Override
    @NonNull
    protected View makeCenterView() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        WheelView monthView = new WheelView(activity);
        monthView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        monthView.setTextSize(textSize);
        monthView.setTextColor(textColorNormal, textColorFocus);
        monthView.setLineVisible(lineVisible);
        monthView.setLineColor(lineColor);
        layout.addView(monthView);
        TextView monthTextView = new TextView(activity);
        monthTextView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        monthTextView.setTextSize(textSize);
        monthTextView.setTextColor(textColorFocus);
        monthTextView.setText("月");
        layout.addView(monthTextView);

        WheelView dayView = new WheelView(activity);
        dayView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        dayView.setTextSize(textSize);
        dayView.setTextColor(textColorNormal, textColorFocus);
        dayView.setLineVisible(lineVisible);
        dayView.setLineColor(lineColor);
        dayView.setOffset(offset);
        layout.addView(dayView);

        ArrayList<String> months = new ArrayList<>();
        for (int i = 1; i < LunarCalendar.lunarMonthNames.length; i++){
            months.add(LunarCalendar.lunarMonthNames[i]);
        }
        monthView.setItems(months, selectedMonth);
        ArrayList<String> days = new ArrayList<>();
        for (int i = 1; i < LunarCalendar.lunarDateNames.length; i++) {
            days.add(LunarCalendar.lunarDateNames[i]);
        }
        dayView.setItems(days, selectedDay);
        monthView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                selectedMonth = selectedIndex;
            }
        });
        dayView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                selectedDay = selectedIndex;
            }
        });
        return layout;
    }

    @Override
    public void onSubmit() {
        if (onLunarMonthPickListener != null) {
            onLunarMonthPickListener.onLunarMonthPicked(selectedMonth + 1, selectedDay + 1);
        }
    }

    public interface OnLunarMonthPickListener {
        /**
         * 选中农历月日触发事件
         * @param month 农历月
         * @param day 农历日
         */
        void onLunarMonthPicked(int month, int day);

    }
}
