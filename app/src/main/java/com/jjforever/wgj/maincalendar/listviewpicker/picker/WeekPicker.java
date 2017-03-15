package com.jjforever.wgj.maincalendar.listviewpicker.picker;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.jjforever.wgj.maincalendar.R;
import com.jjforever.wgj.maincalendar.common.popup.ConfirmPopup;

import java.util.ArrayList;

/**
 * Created by Wgj on 2016/9/17.
 * 星期选择器
 */
public class WeekPicker extends ConfirmPopup<LinearLayout> {
    // 星期列表
    private ListView mListView;
    // 标题
    private String mTitle;
    // 星期内容提供器
    private WeekAdapter mWeekAdapter;

    // 监听事件
    private OnWeekPickListener onWeekPickListener;

    /**
     * 创建一个对话框
     * @param activity 活动
     * @param title 标题
     */
    public WeekPicker(Activity activity, String title)
    {
        super(activity);
        mWeekAdapter = new WeekAdapter(activity);
        mTitle = title;
    }

    /**
     * 创建一个对话框，标题为提醒字样
     * @param activity 活动
     */
    public WeekPicker(Activity activity)
    {
        this(activity, activity.getResources().getString(R.string.please_chose_week));
    }

    /**
     * 设置选中的星期
     * @param items 选中的星期
     */
    public void setCheckedItems(int items){
        for (int i = 0; i < mWeekAdapter.getCount(); i++){
            if ((items & (1 << i)) != 0){
                mWeekAdapter.setIsSelected(i, true);
            }
            else{
                mWeekAdapter.setIsSelected(i, false);
            }
        }
    }

    /**
     * 设置监听事件
     * @param onWeekPickListener 回调方法
     */
    public void setOnWeekPickListener(OnWeekPickListener onWeekPickListener) {
        this.onWeekPickListener = onWeekPickListener;
    }

    @Override
    protected void onSubmit() {
        if (onWeekPickListener != null) {
            int tmpDay = 0;
            for (int i = 0; i < mWeekAdapter.getCount(); i++){
                if (mWeekAdapter.getIsSelected(i)){
                    tmpDay |= (1 << i);
                }
            }
            onWeekPickListener.onWeekPicked(tmpDay);
        }
    }

    @Override
    @NonNull
    protected LinearLayout makeCenterView() {
        LinearLayout rootLayout = new LinearLayout(activity);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        mListView = new ListView(activity);
        mListView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mListView.getLayoutParams().height = screenHeightPixels/4;
        mListView.setPadding(30, 10, 30, 10);
        // 去掉分割线
        mListView.setDividerHeight(0);
        mListView.setAdapter(mWeekAdapter);
        rootLayout.addView(mListView);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int arg2, long arg3) {
                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
                WeekAdapter.ViewHolder holder = (WeekAdapter.ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                // 将CheckBox的选中状况记录下来
                mWeekAdapter.setIsSelected(arg2, holder.cb.isChecked());
            }
        });
        setTitleText(mTitle);
        return rootLayout;
    }

    /**
     * 星期选择框确认取消接口
     */
    public interface OnWeekPickListener {
        /**
         * 星期选择框确认事件
         * @param selected 选中的星期
         */
        void onWeekPicked(int selected);
    }
}
