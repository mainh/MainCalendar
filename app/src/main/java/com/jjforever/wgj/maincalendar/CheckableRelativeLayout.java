package com.jjforever.wgj.maincalendar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.jjforever.wgj.maincalendar.monthui.ThemeStyle;

/**
 * Created by Wgj on 2016/9/6.
 * 用于列表多选项定制
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    private boolean mChecked;

    public CheckableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        setBackgroundColor(checked ? ThemeStyle.ItemSelected : ThemeStyle.BackColor);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
