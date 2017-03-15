package com.jjforever.wgj.maincalendar.wheelpicker.picker;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.weather.util.WeatherConstants;
import com.jjforever.wgj.maincalendar.weather.util.WeatherIconUtil;
import com.jjforever.wgj.maincalendar.wheelpicker.widget.WheelView;

import java.util.ArrayList;

/**
 * Created by Wgj on 2016/8/24.
 * 天气情况选择控件
 */
public class WeatherPicker extends WheelPicker {

    // 选择的天气索引
    private int mSelectedWeatherIndex = 0;
    private ArrayList<String> weathers = new ArrayList<>();
    // 天气选中触发事件
    private OnWeatherPickListener onWeatherPickListener;
    // 图片显示
    private TextView mImageView;

    /**
     * 控件初始化
     * @param activity 所在活动
     */
    public WeatherPicker(Activity activity){
        super(activity);

        for (int i = WeatherConstants.START_CODE; i <= WeatherConstants.END_CODE; i++){
            weathers.add(WeatherIconUtil.getWeatherName(i));
        }
    }

    /**
     * 设置触发事件
     * @param listener 监听事件
     */
    public void setOnWeatherPickListener(OnWeatherPickListener listener) {
        this.onWeatherPickListener = listener;
    }

    /**
     * 获取选中的天气代码
     * @return 天气代码
     */
    public int getSelectedWeather()
    {
        return mSelectedWeatherIndex;
    }

    /**
     * 设置默认显示天气
     * @param code 天气代码
     */
    public void setSelectedItem(int code)
    {
        mSelectedWeatherIndex = code;
    }

    // 获取图片
    Html.ImageGetter imageGetter = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            int id = Integer.parseInt(source);
            Drawable drawable = activity.getResources().getDrawable(id);
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight());
            }
            return drawable;
        }
    };

    @Override
    @NonNull
    protected View makeCenterView() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);

        WheelView weatherView = new WheelView(activity);
        weatherView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        weatherView.setTextSize(textSize);
        weatherView.setTextColor(textColorNormal, textColorFocus);
        weatherView.setLineVisible(lineVisible);
        weatherView.setLineColor(lineColor);
        layout.addView(weatherView);

        mImageView = new TextView(activity);
        mImageView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        mImageView.setText(Html.fromHtml("<img src='" + WeatherIconUtil.getWeatherIcon(mSelectedWeatherIndex) + "'/>", imageGetter, null));
        layout.addView(mImageView);

        weatherView.setItems(weathers, mSelectedWeatherIndex);
        weatherView.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(boolean isUserScroll, int selectedIndex, String item) {
                mSelectedWeatherIndex = selectedIndex;
                mImageView.setText(Html.fromHtml("<img src='" + WeatherIconUtil.getWeatherIcon(mSelectedWeatherIndex) + "'/>", imageGetter, null));
            }
        });

        return layout;
    }

    @Override
    public void onSubmit() {
        if (onWeatherPickListener != null) {
            onWeatherPickListener.onWeatherPicked(getSelectedWeather());
        }
    }

    public interface OnWeatherPickListener {
        /**
         * 天气选中触发事件
         * @param code 天气代码
         */
        void onWeatherPicked(int code);
    }
}
