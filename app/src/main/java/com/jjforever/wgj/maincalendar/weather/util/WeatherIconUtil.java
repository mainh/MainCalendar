package com.jjforever.wgj.maincalendar.weather.util;

import com.jjforever.wgj.maincalendar.R;

/**
 * Created by Wgj on 2016/8/24.
 * 根据天气情况获取图标
 */
public final class WeatherIconUtil {
    // 天气图标集合，与天气代码一一对应
    private static final int[] mWeatherIcons = new int[]{
            R.drawable.ic_sunny_big, R.drawable.ic_cloudy_big,
            R.drawable.ic_overcast_big, R.drawable.ic_fog_big,
            R.drawable.hurricane_day_night, R.drawable.ic_heavyrain_big,
            R.drawable.ic_heavyrain_big, R.drawable.ic_thundeshower_big,
            R.drawable.ic_shower_big, R.drawable.ic_heavyrain_big,
            R.drawable.ic_moderraterain_big, R.drawable.ic_lightrain_big,
            R.drawable.ic_sleet_big, R.drawable.ic_snow_big,
            R.drawable.ic_snow_big, R.drawable.ic_heavysnow_big,
            R.drawable.ic_snow_big, R.drawable.ic_snow_big,
            R.drawable.ic_sandstorm_big, R.drawable.ic_sandstorm_big,
            R.drawable.ic_sandstorm_big, R.drawable.ic_sandstorm_big,
            R.drawable.freezing_rain_day_night, R.drawable.ic_dust_big,
            R.drawable.ic_haze_big
    };

    // 天气描述集合
    private static String[] mWeatherNames;

    /**
     * 初始化天气描述
     * @param names 天气描述集合
     */
    public static void initWeatherNames(String[] names)
    {
        mWeatherNames = names;
    }

    /**
     * 返回所有的天气描述
     * @return 所有的天气描述
     */
    public static String[] getWeatherNames(){
        return mWeatherNames;
    }

    /**
     * 根据天气代码获取天气图标
     * @param type 天气情况代码
     * @return 天气图标
     */
    public static int getWeatherIcon(int type) {
        if (type >= WeatherConstants.START_CODE && type <= WeatherConstants.END_CODE){
            return mWeatherIcons[type];
        }
        return R.drawable.ic_default_big;
    }

    /**
     * 根据天气代码获取天气描述
     * @param type 天气代码
     * @return 天气描述
     */
    public static String getWeatherName(int type)
    {
        if (type >= WeatherConstants.START_CODE && type <= WeatherConstants.END_CODE){
            return mWeatherNames[type];
        }

        // 最后一个存放无效天气描述
        return mWeatherNames[mWeatherNames.length - 1];
    }
}
