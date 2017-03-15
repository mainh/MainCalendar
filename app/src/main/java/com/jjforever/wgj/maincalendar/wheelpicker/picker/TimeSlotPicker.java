package com.jjforever.wgj.maincalendar.wheelpicker.picker;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.common.util.DateUtils;
import com.jjforever.wgj.maincalendar.wheelpicker.widget.WheelView;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Wgj on 2016/9/21.
 * 时间段选择器
 */
public class TimeSlotPicker extends WheelPicker {

    private String mStartHour = "", mStartMinute = "";
    private String mEndHour = "", mEndMinute = "";

    private OnTimeSlotPickListener onTimeSlotPickListener;

    /**
     * Instantiates a new Time picker.
     *
     * @param activity the activity
     */
    public TimeSlotPicker(Activity activity) {
        super(activity);

        mStartHour = DateUtils.fillZero(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        mStartMinute = DateUtils.fillZero(Calendar.getInstance().get(Calendar.MINUTE));
        mEndHour = DateUtils.fillZero(23);
        mEndMinute = DateUtils.fillZero(59);
    }

    /**
     * 设置显示的时间段
     * @param startHour 开始时
     * @param startMinute 开始分
     * @param endHour 结束时
     * @param endMinute 结束分
     */
    public void setTimeSlot(int startHour, int startMinute, int endHour, int endMinute) {
        this.mStartHour = DateUtils.fillZero(startHour);
        this.mStartMinute = DateUtils.fillZero(startMinute);
        this.mEndHour = DateUtils.fillZero(endHour);
        this.mEndMinute = DateUtils.fillZero(endMinute);
    }

    /**
     * 设置监听方法
     * @param listener 监听触发事件
     */
    public void setOnTimeSlotPickListener(OnTimeSlotPickListener listener) {
        this.onTimeSlotPickListener = listener;
    }

    @Override
    @NonNull
    protected View makeCenterView() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);

        WheelView sHourView = new WheelView(activity);
        sHourView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        sHourView.setTextSize(textSize);
        sHourView.setTextColor(textColorNormal, textColorFocus);
        sHourView.setLineVisible(lineVisible);
        sHourView.setLineColor(lineColor);
        layout.addView(sHourView);
        TextView hourTextView = new TextView(activity);
        hourTextView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        hourTextView.setTextSize(textSize);
        hourTextView.setTextColor(textColorFocus);
        hourTextView.setText(":");
        layout.addView(hourTextView);
        WheelView sMinuteView = new WheelView(activity);
        sMinuteView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        sMinuteView.setTextSize(textSize);
        sMinuteView.setTextColor(textColorNormal, textColorFocus);
        sMinuteView.setLineVisible(lineVisible);
        sMinuteView.setLineColor(lineColor);
        sMinuteView.setOffset(offset);
        layout.addView(sMinuteView);

        TextView sepTextView = new TextView(activity);
        sepTextView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        sepTextView.setTextSize(textSize);
        sepTextView.setTextColor(textColorFocus);
        sepTextView.setText(" ~ ");
        layout.addView(sepTextView);

        WheelView eHourView = new WheelView(activity);
        eHourView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        eHourView.setTextSize(textSize);
        eHourView.setTextColor(textColorNormal, textColorFocus);
        eHourView.setLineVisible(lineVisible);
        eHourView.setLineColor(lineColor);
        layout.addView(eHourView);
        TextView hourTextView1 = new TextView(activity);
        hourTextView1.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        hourTextView1.setTextSize(textSize);
        hourTextView1.setTextColor(textColorFocus);
        hourTextView1.setText(":");
        layout.addView(hourTextView1);
        WheelView eMinuteView = new WheelView(activity);
        eMinuteView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        eMinuteView.setTextSize(textSize);
        eMinuteView.setTextColor(textColorNormal, textColorFocus);
        eMinuteView.setLineVisible(lineVisible);
        eMinuteView.setLineColor(lineColor);
        eMinuteView.setOffset(offset);
        layout.addView(eMinuteView);

        ArrayList<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(DateUtils.fillZero(i));
        }
        sHourView.setItems(hours, mStartHour);
        eHourView.setItems(hours, mEndHour);

        ArrayList<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(DateUtils.fillZero(i));
        }
        sMinuteView.setItems(minutes, mStartMinute);
        eMinuteView.setItems(minutes, mEndMinute);

        sHourView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                mStartHour = item;
            }
        });
        eHourView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                mEndHour = item;
            }
        });
        sMinuteView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                mStartMinute = item;
            }
        });
        eMinuteView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                mEndMinute = item;
            }
        });
        return layout;
    }

    @Override
    public void onSubmit() {
        if (onTimeSlotPickListener != null) {
            onTimeSlotPickListener.onTimeSlotPicked(
                    stringToInt(this.mStartHour), stringToInt(this.mStartMinute),
                    stringToInt(this.mEndHour), stringToInt(this.mEndMinute));
        }
    }

    /**
     * 字符串转整形
     * @param text 数字字符串
     * @return 整形数据
     */
    private int stringToInt(String text) {
        if (text.startsWith("0") && text.length() > 1) {
            //截取掉前缀0以便转换为整数
            text = text.substring(1);
        }
        return Integer.parseInt(text);
    }

    /**
     * The interface On time pick listener.
     */
    public interface OnTimeSlotPickListener {

        /**
         * 时间段确定触发事件
         * @param startHour 起始时
         * @param startMinute 起始分
         * @param endHour 结束时
         * @param endMinute 结束分
         */
        void onTimeSlotPicked(int startHour, int startMinute, int endHour, int endMinute);

    }
}
