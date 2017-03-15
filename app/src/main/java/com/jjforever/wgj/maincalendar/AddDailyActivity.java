package com.jjforever.wgj.maincalendar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjforever.wgj.maincalendar.BLL.DailyRecordMng;
import com.jjforever.wgj.maincalendar.Model.DailyRecord;
import com.jjforever.wgj.maincalendar.dialogpicker.picker.DialogPicker;
import com.jjforever.wgj.maincalendar.monthui.SwitchButton;
import com.jjforever.wgj.maincalendar.toolbar.ToolBarActivity;
import com.jjforever.wgj.maincalendar.util.Helper;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;
import com.jjforever.wgj.maincalendar.weather.util.WeatherIconUtil;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.DatePicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.WeatherPicker;

import java.util.Calendar;

public class AddDailyActivity extends ToolBarActivity {

    // 记录的时间文本控件
    private TextView mTimeText;
    // 天气情况文本控件
    private ImageView mWeatherView;
    // 是否在日历中显示按钮
    private SwitchButton mSwitchButton;
    // 记录是否更改，以便退出时进行提示
    private boolean mIsChanged = false;
    // 标题栏控件
    private EditText mTitleEdit;
    // 内容编辑控件
    private EditText mContentEdit;
    // 是否确定退出
    private boolean mSureQuit = false;
    // 编辑的记录
    private DailyRecord mRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily);

        mRecord = getIntent().getParcelableExtra(AppConstants.MAIN_ACTIVITY_CLICK_DATE);
        if (mRecord == null){
            // 获取错误
            mSureQuit = true;
            finish();
            return;
        }

        setTitle(getResources().getString(R.string.add_daily));
        mTimeText = (TextView) this.findViewById(R.id.record_time);
        mTimeText.setText(mRecord.getRecordTime().toRecordTime());
        mTimeText.setOnClickListener(this);
        mWeatherView = (ImageView) this.findViewById(R.id.record_weather);
        if (mWeatherView != null) {
            mWeatherView.setImageResource(WeatherIconUtil.getWeatherIcon(mRecord.getWeather()));
            mWeatherView.setOnClickListener(this);
        }

        mSwitchButton = (SwitchButton) this.findViewById(R.id.switch_notice);
        mSwitchButton.setChecked(mRecord.getDisplay());
        RelativeLayout displayLayout = (RelativeLayout) this.findViewById(R.id.display_layout);
        if (displayLayout != null){
            displayLayout.setOnClickListener(this);
        }

        mTitleEdit = (EditText) this.findViewById(R.id.text_record_title);
        if (mTitleEdit != null && !Helper.isNullOrEmpty(mRecord.getTitle())) {
            mTitleEdit.setText(mRecord.getTitle());
        }
        mContentEdit = (EditText) this.findViewById(R.id.text_record_content);
        if (mContentEdit != null && !Helper.isNullOrEmpty(mRecord.getContent())){
            mContentEdit.setText(mRecord.getContent());
        }
        if (!mRecord.getIsNew()) {
            // 编辑时才显示删除按钮，新建时不显示
            setTitle(getResources().getString(R.string.edit_daily));
            showDeleteBtn(true);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.display_layout:
                mSwitchButton.setChecked(!mSwitchButton.isChecked());
                break;

            case R.id.record_time:
                // 选择时间 默认从2000~2050年
                DatePicker picker = new DatePicker(this, DatePicker.DATE_TIME);
                LunarCalendar tmpCalendar = mRecord.getRecordTime();
                picker.setSelectedItem(tmpCalendar.get(Calendar.YEAR),
                        tmpCalendar.get(Calendar.MONTH),
                        tmpCalendar.get(Calendar.DAY_OF_MONTH),
                        tmpCalendar.get(Calendar.HOUR_OF_DAY),
                        tmpCalendar.get(Calendar.MINUTE));
                picker.setOnDatePickListener(new DatePicker.OnAllPickListener() {
                    @Override
                    public void onDatePicked(int year, int month, int day, int hour, int minute) {
                        // 判断是否更改了时间
                        LunarCalendar tmpCalendar = mRecord.getRecordTime();
                        if (tmpCalendar.get(Calendar.YEAR) != year
                            || tmpCalendar.get(Calendar.MONTH) != month
                            || tmpCalendar.get(Calendar.DAY_OF_MONTH) != day
                            || tmpCalendar.get(Calendar.HOUR_OF_DAY) != hour
                            || tmpCalendar.get(Calendar.MINUTE) != minute) {
                            mIsChanged = true;
                            tmpCalendar.set(Calendar.YEAR, year);
                            tmpCalendar.set(Calendar.MONTH, month);
                            tmpCalendar.set(Calendar.DAY_OF_MONTH, day);
                            tmpCalendar.set(Calendar.HOUR_OF_DAY, hour);
                            tmpCalendar.set(Calendar.MINUTE, minute);
                            mTimeText.setText(tmpCalendar.toRecordTime());
                        }
                    }
                });
                picker.show();
                break;

            case R.id.record_weather:
                // 选择天气
                WeatherPicker weatherPicker = new WeatherPicker(this);
                weatherPicker.setSelectedItem(this.mRecord.getWeather());
                weatherPicker.setOnWeatherPickListener(new WeatherPicker.OnWeatherPickListener(){
                    @Override
                    public void onWeatherPicked(int code){
                        if (mRecord.getWeather() != code) {
                            mIsChanged = true;
                            mRecord.setWeather(code);
                            mWeatherView.setImageResource(WeatherIconUtil.getWeatherIcon(code));
                        }
                    }
                });
                weatherPicker.show();
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
        Toast.makeText(AddDailyActivity.this,
                getResources().getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOKButtonClick()
    {
        String title = mTitleEdit.getText().toString();
        if (Helper.isNullOrEmpty(title)){
            new DialogPicker(this, getResources().getString(R.string.must_input_title)).show();
            return;
        }
        String content = mContentEdit.getText().toString();
        if (DailyRecordMng.isExist(title, mRecord.getIndex())){
            // 该标题已存在
            showToastMsg(R.string.existed_record);
            return;
        }

        if (mRecord.getIsNew()){
            // 新建记录
            mRecord.setTitle(title);
            mRecord.setContent(content);
            mRecord.setDisplay(mSwitchButton.isChecked());
            // 获取当前时间
            mRecord.setCreateTime(Calendar.getInstance());
            // 保存
            boolean result = DailyRecordMng.insert(mRecord);
            if (result) {
                showToastMsg(R.string.add_record_success);
                mIsChanged = false;
                mRecord.setIsNew(false);
                mSureQuit = true;
                setResult(RESULT_OK, null);
                finish();
                return;
            }
        }
        else{
            // 编辑记录
            mRecord.setTitle(title);
            mRecord.setContent(content);
            mRecord.setDisplay(mSwitchButton.isChecked());

            // 保存
            boolean result = DailyRecordMng.update(mRecord);
            if (result) {
                showToastMsg(R.string.edit_record_success);
                mIsChanged = false;
                mSureQuit = true;
                setResult(RESULT_OK, null);
                finish();
                return;
            }
        }
        showToastMsg(R.string.add_fail);
    }

    @Override
    public void onDeleteButtonClick(){
        if (mRecord.getIsNew()){
            return;
        }
        // 删除当前记录
        DialogPicker picker = new DialogPicker(this, getResources().getString(R.string.confirm_delete));
        picker.setOnDialogPickListener(new DialogPicker.OnDialogPickListener() {
            @Override
            public void onDialogPicked(boolean confirm) {
                if (confirm) {
                    // 开始删除
                    if (DailyRecordMng.delete(mRecord.getIndex())) {
                        showToastMsg(R.string.delete_success);
                        setResult(AppConstants.RESULT_DELETE, null);
                        mSureQuit = true;
                        finish();
                    }
                    else{
                        showToastMsg(R.string.delete_fail);
                    }
                }
            }
        });
        picker.show();
    }

    @Override
    public void finish()
    {
        if (mSureQuit){
            super.finish();
            return;
        }

        String title = mTitleEdit.getText().toString();
        String content = mContentEdit.getText().toString();
        boolean isChecked = mSwitchButton.isChecked();
        if (!title.equals(mRecord.getTitle())
                || !content.equals(mRecord.getContent())
                || isChecked != mRecord.getDisplay()){
            mIsChanged = true;
        }

        if (mIsChanged) {
            // 新建记录
            mSureQuit = false;
            DialogPicker picker = new DialogPicker(this, getResources().getString(R.string.is_cancel_edit));
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
