package com.jjforever.wgj.maincalendar.dialogpicker.picker;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.R;
import com.jjforever.wgj.maincalendar.common.popup.ConfirmPopup;

/**
 * Created by Wgj on 2016/8/25.
 * 自定义对话框
 */
public class DialogPicker extends ConfirmPopup<LinearLayout> {
    private TextView mTextView;
    // 标题
    private String mTitle;
    // 内容
    private String mContent;

    // 监听事件
    private OnDialogPickListener onDialogPickListener;

    /**
     * 创建一个对话框
     * @param activity 活动
     * @param title 标题
     * @param content 内容
     */
    public DialogPicker(Activity activity, String title, String content)
    {
        super(activity);
        mTitle = title;
        mContent = content;
    }

    /**
     * 创建一个对话框，标题为提醒字样
     * @param activity 活动
     * @param content 内容
     */
    public DialogPicker(Activity activity, String content)
    {
        this(activity, activity.getResources().getString(R.string.dialog_title), content);
    }

    /**
     * 设置监听事件
     * @param onDialogPickListener 回调方法
     */
    public void setOnDialogPickListener(OnDialogPickListener onDialogPickListener) {
        this.onDialogPickListener = onDialogPickListener;
    }

    @Override
    protected void onSubmit() {
        if (onDialogPickListener != null) {
            onDialogPickListener.onDialogPicked(true);
        }
    }

    @Override
    protected void onCancel()
    {
        if (onDialogPickListener != null) {
            onDialogPickListener.onDialogPicked(false);
        }
    }

    @Override
    @NonNull
    protected LinearLayout makeCenterView() {
        LinearLayout rootLayout = new LinearLayout(activity);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        mTextView = new TextView(activity);
        mTextView.setHeight(screenHeightPixels/4);
        mTextView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setTextSize(16);
        mTextView.setPadding(10, 10, 10, 10);
        mTextView.setText(mContent);
        rootLayout.addView(mTextView);

        setTitleText(mTitle);
        return rootLayout;
    }

    /**
     * 对话框确认取消接口
     */
    public interface OnDialogPickListener {
        /**
         * 对话框确认事件
         * @param confirm 是否确认
         */
        void onDialogPicked(boolean confirm);

    }
}
