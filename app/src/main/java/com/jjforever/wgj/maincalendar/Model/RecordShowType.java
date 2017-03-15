package com.jjforever.wgj.maincalendar.Model;

/**
 * Created by Wgj on 2016/8/28.
 * 特殊日期的显示方式
 */
public final class RecordShowType {
    // 不在日历中显示
    public static int HIDE = 0;
    // 文字显示
    public static int TEXT = 1;
    // 特殊点显示
    public static int DOT = 2;
    // 文字显示+特殊颜色点显示
    public static int TEXT_DOT = TEXT | DOT;
}
