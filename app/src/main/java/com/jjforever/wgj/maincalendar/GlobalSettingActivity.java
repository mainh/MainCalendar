package com.jjforever.wgj.maincalendar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjforever.wgj.maincalendar.BLL.GlobalSettingMng;
import com.jjforever.wgj.maincalendar.BLL.ShiftsWorkRecordMng;
import com.jjforever.wgj.maincalendar.Model.GlobalSetting;
import com.jjforever.wgj.maincalendar.Model.KeyValue;
import com.jjforever.wgj.maincalendar.colorpicker.picker.ColorPicker;
import com.jjforever.wgj.maincalendar.dialogpicker.picker.DialogPicker;
import com.jjforever.wgj.maincalendar.filepicker.picker.FilePicker;
import com.jjforever.wgj.maincalendar.monthui.SwitchButton;
import com.jjforever.wgj.maincalendar.toolbar.ToolBarActivity;
import com.jjforever.wgj.maincalendar.util.Helper;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.NumberPicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.OptionPicker;

import java.util.ArrayList;


public class GlobalSettingActivity extends ToolBarActivity {

    // 设置参数
    private GlobalSetting mParams;
    // 主题颜色View
    private TextView mThemeView;
    // 轮班显示View
    private TextView mWorkView;
    // 闹钟铃声View
    private TextView mRingView;
    // 响铃时间View
    private TextView mRingTimeView;
    // 是否在状态栏提醒闹钟
    private SwitchButton mNotificationButton;
    // 是否在日历中显示按钮
    private SwitchButton mDebugButton;
    // 记录是否更改，以便退出时进行提示
    private boolean mIsChanged = false;
    // 是否确定退出
    private boolean mSureQuit = false;
    // 轮班记录集合
    private ArrayList<KeyValue> mShiftsWorks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_setting);
        setTitle(getResources().getString(R.string.action_settings));

        mParams = GlobalSettingMng.getSetting().depthClone();
        mShiftsWorks = ShiftsWorkRecordMng.getAllKeyValue();
        mShiftsWorks.add(0, new KeyValue(0, getString(R.string.not_display)));

        mThemeView = (TextView) findViewById(R.id.theme_color);
        mThemeView.setBackgroundColor(mParams.getPrimaryColor());
//        mThemeView.setOnClickListener(this);
        RelativeLayout colorLayout = (RelativeLayout) findViewById(R.id.color_layout);
        if (colorLayout != null){
            colorLayout.setOnClickListener(this);
        }

        mWorkView = (TextView) findViewById(R.id.display_work);
        Long workIndex = mParams.getShiftsWorkIndex();
        if (workIndex > 0){
            String tmpTitle = getWorkTitle(workIndex);
            if (!Helper.isNullOrEmpty(tmpTitle)){
                mWorkView.setText(tmpTitle);
            }
        }
//        mWorkView.setOnClickListener(this);
        RelativeLayout workLayout = (RelativeLayout) findViewById(R.id.work_layout);
        if (workLayout != null){
            workLayout.setOnClickListener(this);
        }

        mRingView = (TextView) findViewById(R.id.ring_file);
        if (!Helper.isNullOrEmpty(mParams.getRingPath())){
            mRingView.setText(Helper.getFileName(mParams.getRingPath()));
        }
//        mRingView.setOnClickListener(this);
        RelativeLayout ringLayout = (RelativeLayout) findViewById(R.id.ring_layout);
        if (ringLayout != null){
            ringLayout.setOnClickListener(this);
        }

        mRingTimeView = (TextView) findViewById(R.id.ring_time);
        mRingTimeView.setText(String.valueOf(mParams.getRingSeconds()));
//        mRingTimeView.setOnClickListener(this);
        RelativeLayout timeLayout = (RelativeLayout) findViewById(R.id.time_layout);
        if (timeLayout != null){
            timeLayout.setOnClickListener(this);
        }

        mNotificationButton = (SwitchButton) findViewById(R.id.notification_switch_button);
        mNotificationButton.setChecked(mParams.getIsNotification());
        RelativeLayout notiLayout = (RelativeLayout) findViewById(R.id.notification_layout);
        if (notiLayout != null){
            notiLayout.setOnClickListener(this);
        }

        mDebugButton = (SwitchButton) findViewById(R.id.debug_switch_button);
        mDebugButton.setChecked(mParams.getIsRecordLog());
        RelativeLayout debugLayout = (RelativeLayout) findViewById(R.id.debug_layout);
        if (debugLayout != null){
            debugLayout.setOnClickListener(this);
        }
    }

    /**
     * 获取轮班记录标题
     * @param index 索引
     * @return 如果有返回标题
     */
    private String getWorkTitle(long index){
        for (KeyValue tmpValue : mShiftsWorks){
            if (tmpValue.Index == index){
                return tmpValue.Title;
            }
        }

        return null;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.color_layout:
                ColorPicker colorPicker = new ColorPicker(GlobalSettingActivity.this);
                colorPicker.setInitColor(mParams.getPrimaryColor());
                colorPicker.setOnColorPickListener(new ColorPicker.OnColorPickListener() {
                    @Override
                    public void onColorPicked(@ColorInt int pickedColor) {
                        if (pickedColor != mParams.getPrimaryColor()){
                            mIsChanged = true;
                            mParams.setPrimaryColor(pickedColor);
                            mThemeView.setBackgroundColor(pickedColor);
                            GlobalSettingActivity.super.setToolbarBack(pickedColor);
                        }
                    }
                });
                colorPicker.show();
                break;

            case R.id.work_layout:
                final long[] indexBuf = new long[mShiftsWorks.size()];
                final String[] strBuf = new String[mShiftsWorks.size()];
                int i = 0;
                for (KeyValue tmpValue : mShiftsWorks){
                    indexBuf[i] = tmpValue.Index;
                    strBuf[i] = tmpValue.Title;
                    i++;
                }
                OptionPicker workPicker = new OptionPicker(GlobalSettingActivity.this, strBuf);
                for (i = 0; i < strBuf.length; i++){
                    if (mParams.getShiftsWorkIndex() == indexBuf[i]){
                        break;
                    }
                }
                workPicker.setSelectedIndex(i);
                workPicker.setTextSize(16);
                workPicker.setOnOptionIndexPickListener(new OptionPicker.OnOptionIndexPickListener() {
                    @Override
                    public void onOptionPicked(int index) {
                        long tmpIndex = indexBuf[index];
                        if (tmpIndex != mParams.getShiftsWorkIndex()){
                            mIsChanged = true;
                            mParams.setShiftsWorkIndex(tmpIndex);
                            mWorkView.setText(strBuf[index]);
                        }
                    }
                });
                workPicker.show();
                break;

            case R.id.ring_layout:
                FilePicker filePicker;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        filePicker = new FilePicker(this, FilePicker.FILE);
                    }
                    else{
                        filePicker = new FilePicker(this, FilePicker.FILE);
                    }
                }
                else{
                    filePicker = new FilePicker(this, FilePicker.FILE);
                }

                filePicker.setShowHideDir(false);
                filePicker.setShowUpDir(true);
                filePicker.setRingExtensions();
                if (!Helper.isNullOrEmpty(mParams.getRingPath())) {
                    filePicker.setRootPath(Helper.getFilePath(mParams.getRingPath()));
                }
                filePicker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
                    @Override
                    public void onFilePicked(String currentPath) {
                        if (!mParams.getRingPath().equals(currentPath)) {
                            mIsChanged = true;
                            mParams.setRingPath(currentPath);
                            if (Helper.isNullOrEmpty(currentPath)){
                                mRingView.setText(getString(R.string.has_no));
                            }
                            else {
                                mRingView.setText(Helper.getFileName(currentPath));
                            }
                        }
                    }
                });
                filePicker.show();
                break;

            case R.id.time_layout:
                NumberPicker numberPicker = new NumberPicker(GlobalSettingActivity.this);
                numberPicker.setRange(15, 180, 15);
                numberPicker.setSelectedItem(mParams.getRingSeconds());
                numberPicker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(String option) {
                        int tmpValue = Integer.parseInt(option);
                        if (tmpValue != mParams.getRingSeconds()){
                            mIsChanged = true;
                            mParams.setRingSeconds(tmpValue);
                            mRingTimeView.setText(option);
                        }
                    }
                });
                numberPicker.show();
                break;

            case R.id.notification_layout:
                mNotificationButton.setChecked(!mNotificationButton.isChecked());
                break;

            case R.id.debug_layout:
                mDebugButton.setChecked(!mDebugButton.isChecked());
                break;

            default:
                break;
        }
    }

    /**
     * 显示提示消息
     * @param msgId 提示消息字符串ID
     */
    private void showToastMsg(int msgId){
        Toast.makeText(GlobalSettingActivity.this,
                getResources().getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOKButtonClick()
    {
        mParams.setIsNotification(mNotificationButton.isChecked());
        mParams.setIsRecordLog(mDebugButton.isChecked());
        GlobalSettingMng.setSetting(mParams);
        GlobalSettingMng.SaveSetting(this);
        showToastMsg(R.string.set_success);
        mIsChanged = false;
        mSureQuit = true;
        setResult(RESULT_OK, null);
        finish();
    }

    @Override
    public void finish()
    {
        if (mSureQuit){
            super.finish();
            return;
        }

        if (mIsChanged) {
            // 新建记录
            mSureQuit = false;
            DialogPicker picker = new DialogPicker(this, getResources().getString(R.string.is_cancel_set));
            picker.setOnDialogPickListener(new DialogPicker.OnDialogPickListener() {
                @Override
                public void onDialogPicked(boolean confirm) {
                    if (confirm) {
                        mSureQuit = true;
                        finish();
                    }
                }
            });
            picker.show();
            return;
        }
        super.finish();
    }
}
