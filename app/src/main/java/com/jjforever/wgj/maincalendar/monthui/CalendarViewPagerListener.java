package com.jjforever.wgj.maincalendar.monthui;

import android.support.v4.view.ViewPager;

import com.jjforever.wgj.maincalendar.util.LunarCalendar;

/**
 * Created by Wgj on 2016/8/10.
 * 日历页面滑动监听类
 */
public class CalendarViewPagerListener implements ViewPager.OnPageChangeListener {
    // 默认页面索引
    public static final int DEFAULT_INDEX = 498;

    // 滑动方向定义
    private static final int SLIDE_NO = 0;
    private static final int SLIDE_LEFT = 1;
    private static final int SLIDE_RIGHT = 2;

    // 滑动方向
    private int mDirection = SLIDE_NO;
    // 当前页面索引
    private int mCurrIndex = DEFAULT_INDEX;
    // 显示的日历页面
    private CalendarView[] mShowViews;

    public CalendarViewPagerListener(CalendarViewPagerAdapter viewAdapter) {
        super();
        this.mShowViews = viewAdapter.getAllItems();
    }

    @Override
    public void onPageSelected(int position) {
        measureDirection(position);
        updateCalendarView(position);
    }

    /**
     * 转到今日
     */
    public void backToday(){
        getCurrentView().backToday();
        onPageSelected(mCurrIndex);
    }

    /**
     * 更细当前页面在添加或删除记录后
     */
    public void updateView(){
        getCurrentView().update(true);
        onPageSelected(mCurrIndex);
    }

    /**
     * 转到指定日期
     * @param date 指定日期
     */
    public void locateToDay(LunarCalendar date){
        getCurrentView().locateToDay(date);
        onPageSelected(mCurrIndex);
    }

    /**
     * 根据索引更新页面视图
     * @param index 页面索引
     */
    private void updateCalendarView(int index) {
        LunarCalendar curDate = getCurrentView().getClickCell().CellDate;
        if(mDirection == SLIDE_RIGHT || mDirection == SLIDE_NO){
            mShowViews[(index + 1)% mShowViews.length].rightSlide(new LunarCalendar(curDate));
        }
        if(mDirection == SLIDE_LEFT || mDirection == SLIDE_NO){
            int tmpIndex = (index - 1)% mShowViews.length;
            if (tmpIndex < 0){
                tmpIndex += mShowViews.length;
            }
            mShowViews[tmpIndex].leftSlide(new LunarCalendar(curDate));
        }
        mDirection = SLIDE_NO;
        getCurrentView().callBackDate();
    }

    /**
     * 判断滑动方向
     * @param index 要载入的页面索引
     */
    private void measureDirection(int index) {
        if (index > mCurrIndex) {
            mDirection = SLIDE_RIGHT;
        } else if (index < mCurrIndex) {
            mDirection = SLIDE_LEFT;
        }
        mCurrIndex = index;
    }

    /**
     * 获取当前显示的页面
     * @return 当前显示的日历页面
     */
    public CalendarView getCurrentView()
    {
       return mShowViews[mCurrIndex % mShowViews.length];
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * 获取所有页面
     * @return 所有页面
     */
//    public CalendarView[] getAllItems() {
//        return mShowViews;
//    }
}
