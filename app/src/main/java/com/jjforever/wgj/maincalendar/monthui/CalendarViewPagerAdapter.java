package com.jjforever.wgj.maincalendar.monthui;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;


/**
 * Created by Wgj on 2016/8/10.
 * 自定义的页面视图适配器
 */
public class CalendarViewPagerAdapter extends PagerAdapter {

    private CalendarView[] mCalendarViews;

    public CalendarViewPagerAdapter(CalendarView[] views) {
        super();
        this.mCalendarViews = views;
    }

//    @Override
//    public void finishUpdate(View arg0) {
//    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position %= mCalendarViews.length;
        if (position < 0){
            position = mCalendarViews.length + position;
        }

        CalendarView tmpView = mCalendarViews[position];
        ViewParent vp = tmpView.getParent();
        if (vp != null){
            ViewGroup parent = (ViewGroup)vp;
            parent.removeView(tmpView);
        }
        container.addView(tmpView);

        return tmpView;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == (arg1);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // TODO Auto-generated method stub

    }

//    @Override
//    public void startUpdate(View arg0) {
//    }

    /**
     * 获取所有页面
     * @return 所有页面
     */
    CalendarView[] getAllItems() {
        return mCalendarViews;
    }
}
