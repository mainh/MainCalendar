package com.jjforever.wgj.maincalendar.toolbar;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.R;
import com.jjforever.wgj.maincalendar.monthui.ThemeStyle;
import com.jjforever.wgj.maincalendar.util.Helper;

/**
 * Created by Wgj on 2016/8/20.
 * 带工具栏的活动
 */
public abstract class ToolBarActivity extends AppCompatActivity implements View.OnClickListener {
    // 工具栏
    public Toolbar mToolbar;
    // 标题控件
    private TextView mTitleView;
    // 确定按钮控件
    private ImageView mOkView;
    // 删除按钮
    private ImageView mDeleteView;
    // 标题字符串
    //private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        ViewGroup contentFrameLayout = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        if (contentFrameLayout != null) {
            View parentView = contentFrameLayout.getChildAt(0);
            if (parentView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                parentView.setFitsSystemWindows(true);
            }
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        int statusHeight = Helper.getStatusHeight(this);
        ToolBarHelper mToolBarHelper = new ToolBarHelper(this,layoutResID);
        mToolbar = mToolBarHelper.getToolBar();
        mToolbar.getLayoutParams().height += statusHeight;
        mToolbar.setPadding(mToolbar.getPaddingLeft(), statusHeight, mToolbar.getPaddingRight(), mToolbar.getPaddingBottom());
        setToolbarBack(ThemeStyle.Primary);
        setContentView(mToolBarHelper.getContentView());
        /*把 mToolbar 设置到Activity 中*/
        setSupportActionBar(mToolbar);
        /*自定义的一些操作*/
        onCreateCustomToolBar(mToolbar);
        mTitleView = (TextView) this.findViewById(R.id.toolbar_title);
        mOkView = (ImageView) this.findViewById(R.id.toolbar_ok_btn);
        if (mOkView != null){
            mOkView.getLayoutParams().height = mOkView.getLayoutParams().width / 3;
            mOkView.setOnClickListener(this);
        }
        mDeleteView = (ImageView) this.findViewById(R.id.toolbar_delete_btn);
        if (mDeleteView != null){
            mDeleteView.getLayoutParams().height = mDeleteView.getLayoutParams().width / 3;
            mDeleteView.setOnClickListener(this);
        }
    }

    public void onCreateCustomToolBar(Toolbar toolbar){
        toolbar.setContentInsetsRelative(0,0);
        getLayoutInflater().inflate(R.layout.toolbar_button, toolbar);
    }

    /**
     * 设置标题
     * @param title 标题字符串
     */
    public void setTitle(CharSequence title)
    {
        //this.mTitle = title;
        if (mTitleView != null) {
            mTitleView.setText(title);
        }
    }

    /**
     * 设置工具栏背景色
     * @param color 背景色
     */
    public void setToolbarBack(int color){
        mToolbar.setBackgroundColor(color);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 显示或者隐藏确定按钮
     * @param show 是否显示
     */
    protected void showOkBtn(boolean show){
        if (mOkView != null){
            mOkView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 显示或隐藏删除按钮
     * @param show 是否显示
     */
    protected void showDeleteBtn(boolean show){
        if (mDeleteView != null){
            mDeleteView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 设置OK按钮图标
     * @param resId 要设置的按钮图标资源
     */
    protected void setOkBtnImage(int resId){
        if (mOkView != null){
            mOkView.setImageResource(resId);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_ok_btn:
                // 确定按钮
                onOKButtonClick();
                break;

            case R.id.toolbar_delete_btn:
                // 删除按钮
                onDeleteButtonClick();
                break;

            default:
                break;
        }
    }

    /**
     * 按下确定按钮触发事件，需在子类中重写
     */
    public void onOKButtonClick(){

    }

    /**
     * 按下删除按钮触发事件，需在子类中重写
     */
    public void onDeleteButtonClick(){

    }

    @Override
    public void finish()
    {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
