package com.jjforever.wgj.maincalendar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjforever.wgj.maincalendar.BLL.AlarmRecordMng;
import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.dialogpicker.picker.DialogPicker;
import com.jjforever.wgj.maincalendar.listviewpicker.picker.WeekPicker;
import com.jjforever.wgj.maincalendar.monthui.SwitchButton;
import com.jjforever.wgj.maincalendar.services.CalendarService;
import com.jjforever.wgj.maincalendar.toolbar.ToolBarActivity;
import com.jjforever.wgj.maincalendar.util.Helper;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.DatePicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.LunarMonthPicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.NumberPicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.OptionPicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.TimePicker;

import java.util.Calendar;
import java.util.Locale;

public class AddAlarmActivity extends ToolBarActivity {
    // 闹钟的时间文本控件
    private TextView mTimeText;
    // 闹钟的日期文本控件
    private TextView mDateText;
    // 闹钟类型文本控件
    private TextView mAlarmTypeText;
    // 是否在日历中显示按钮
    private SwitchButton mDisplayButton;
    // 是否停止闹钟按钮
    private SwitchButton mPauseButton;
    // 记录是否更改，以便退出时进行提示
    private boolean mIsChanged = false;
    // 标题栏控件
    private EditText mTitleEdit;
    // 内容编辑控件
    private EditText mContentEdit;
    // 是否确定退出
    private boolean mSureQuit = false;
    // 编辑的记录
    private AlarmRecord mRecord;
    // 闹钟周期层
    private RelativeLayout mAlarmStartLayout;
    // 每几日闹钟开始日期
    private TextView mAlarmStartView;
    // 每几日暂时记录
    private int mAlarmPeriod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        mRecord = getIntent().getParcelableExtra(AppConstants.MAIN_ACTIVITY_CLICK_DATE);
        if (mRecord == null){
            // 获取错误
            mSureQuit = true;
            finish();
            return;
        }

        setTitle(getResources().getString(R.string.add_alarm));
        mAlarmPeriod = mRecord.getHighDay();
        mDisplayButton = (SwitchButton) this.findViewById(R.id.display_in_calendar);
        mDisplayButton.setChecked(mRecord.getDisplay());
        RelativeLayout displayLayout =  (RelativeLayout) this.findViewById(R.id.display_layout);
        if (displayLayout != null) {
            displayLayout.setOnClickListener(this);
        }
        mPauseButton = (SwitchButton) this.findViewById(R.id.alarm_pause);
        mPauseButton.setChecked(mRecord.getPause());
        RelativeLayout pauseLayout = (RelativeLayout) this.findViewById(R.id.pause_layout);
        if (pauseLayout != null) {
            pauseLayout.setOnClickListener(this);
        }

        mAlarmTypeText = (TextView) this.findViewById(R.id.alarm_type);
        if (AppConstants.AlarmTypeNames == null){
            // 载入闹钟类型
            AppConstants.AlarmTypeNames = getResources().getStringArray(R.array.alarm_type_names);
        }
        mAlarmTypeText.setText(AppConstants.AlarmTypeNames[AppConstants.getActionTypeIndex(mRecord.getActionType())]);
//        mAlarmTypeText.setOnClickListener(this);
        RelativeLayout typeLayout = (RelativeLayout) this.findViewById(R.id.type_layout);
        if (typeLayout != null){
            typeLayout.setOnClickListener(this);
        }

        mTimeText = (TextView) this.findViewById(R.id.alarm_notice_time);
        mTimeText.setText(mRecord.getAlarmTime().toString());
//        mTimeText.setOnClickListener(this);
        RelativeLayout timeLayout = (RelativeLayout) this.findViewById(R.id.notice_time_layout);
        if (timeLayout != null){
            timeLayout.setOnClickListener(this);
        }

        mAlarmStartLayout = (RelativeLayout) this.findViewById(R.id.alarm_start_layout);
        mAlarmStartView = (TextView) this.findViewById(R.id.alarm_start);
        mAlarmStartView.setText(new LunarCalendar(mRecord.getYear(), mRecord.getMonth(), mRecord.getDay()).toShortString());
//        mAlarmStartView.setOnClickListener(this);
        mAlarmStartLayout.setOnClickListener(this);

        mDateText = (TextView) this.findViewById(R.id.alarm_date);
        setDateText(mRecord);
//        mDateText.setOnClickListener(this);
        RelativeLayout dateLayout = (RelativeLayout) this.findViewById(R.id.alarm_date_layout);
        if (dateLayout != null){
            dateLayout.setOnClickListener(this);
        }

        mTitleEdit = (EditText) this.findViewById(R.id.text_alarm_title);
        if (mTitleEdit != null && !Helper.isNullOrEmpty(mRecord.getOnlyTitle())) {
            mTitleEdit.setText(mRecord.getOnlyTitle());
        }
        mContentEdit = (EditText) this.findViewById(R.id.alarm_remark);
        if (mContentEdit != null && !Helper.isNullOrEmpty(mRecord.getContent())){
            mContentEdit.setText(mRecord.getContent());
        }
        if (!mRecord.getIsNew()) {
            setTitle(getResources().getString(R.string.edit_alarm));
            // 编辑时才显示删除按钮，新建时不显示
            showDeleteBtn(true);
        }
    }

    /**
     * 根据闹钟类型设置显示的日期文本
     * @param record 闹钟记录
     */
    private void setDateText(AlarmRecord record){
        String typeStr = record.getDateString();
        if (record.getActionType() == AlarmRecord.BY_DAY){
            this.mDateText.setText(String.format(Locale.getDefault(), "每%d日", mAlarmPeriod));
            this.mAlarmStartLayout.setVisibility(View.VISIBLE);
        }
        else{
            this.mAlarmStartLayout.setVisibility(View.GONE);
            this.mDateText.setText(typeStr);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.display_layout:
                mDisplayButton.setChecked(!mDisplayButton.isChecked());
                break;

            case R.id.pause_layout:
                mPauseButton.setChecked(!mPauseButton.isChecked());
                break;

//            case R.id.alarm_date:
            case R.id.alarm_date_layout:
                showDatePicker(mRecord.getActionType());
                break;

//            case R.id.alarm_start:
            case R.id.alarm_start_layout:
                // 闹钟开始日期
                DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH_DAY);
                picker.setSelectedItem(mRecord.getYear(),
                        mRecord.getMonth(), mRecord.getDay());
                picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
                    @Override
                    public void onDatePicked(int year, int month, int day) {
                        // 判断是否更改了时间
                        if (mRecord.getYear() != year || mRecord.getMonth() != month
                                || mRecord.getDay() != day) {
                            mIsChanged = true;
                            mRecord.setYear(year);
                            mRecord.setMonth(month);
                            mRecord.setDay(day);
                            mAlarmStartView.setText(new LunarCalendar(mRecord.getYear(), mRecord.getMonth(), mRecord.getDay()).toShortString());
                        }
                    }
                });
                picker.show();
                break;

//            case R.id.alarm_notice_time:
            case R.id.notice_time_layout:
                // 选择提醒时间
                TimePicker timePicker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
                timePicker.setSelectedItem(mRecord.getAlarmTime().Hour, mRecord.getAlarmTime().Minute);
                timePicker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(int hour, int minute) {
                        if (mRecord.getAlarmTime().Hour != hour
                                || mRecord.getAlarmTime().Minute != minute){
                            mIsChanged = true;
                            mRecord.setAlarmTime(hour, minute);
                            mTimeText.setText(mRecord.getAlarmTime().toString());
                        }
                    }
                });
                timePicker.show();
                break;

//            case R.id.alarm_type:
            case R.id.type_layout:
                // 闹钟类型
                OptionPicker tmpOption = new OptionPicker(this, AppConstants.AlarmTypeNames);
                tmpOption.setSelectedIndex(AppConstants.getActionTypeIndex(mRecord.getActionType()));
                tmpOption.setTextSize(16);
                tmpOption.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(String option) {
                        @AlarmRecord.Flag
                        int tmpType = AlarmRecord.ONCE;
                        for (int i = 0; i < AppConstants.AlarmTypeNames.length; i++){
                            if (option.equals(AppConstants.AlarmTypeNames[i])){
                                tmpType = AppConstants.AlarmTypeIndexs[i];
                                break;
                            }
                        }
                        showDatePicker(tmpType);
                    }
                });
                tmpOption.show();
                break;

            default:
                break;
        }
    }

    /**
     * 根据闹钟类型弹出日期选择框
     * @param actionType 类型
     */
    private void showDatePicker(@AlarmRecord.Flag final int actionType){
        final int tmpIndex = AppConstants.getActionTypeIndex(actionType);

        switch (actionType){
            case AlarmRecord.ONCE:
            case AlarmRecord.BY_LUNAR_ONCE:
                // 指定农历或者阳历日期都以定义到阳历日期以方便定义闹钟
                DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH_DAY);
                picker.setSelectedItem(mRecord.getYear(),
                        mRecord.getMonth(), mRecord.getDay());
                picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
                    @Override
                    public void onDatePicked(int year, int month, int day) {
                        // 判断是否更改了时间
                        if (mRecord.getYear() != year || mRecord.getMonth() != month
                                || mRecord.getDay() != day || mRecord.getActionType() != actionType) {
                            mIsChanged = true;
                            mRecord.setActionType(actionType);
                            mRecord.setYear(year);
                            mRecord.setMonth(month);
                            mRecord.setDay(day);
                            mAlarmTypeText.setText(AppConstants.AlarmTypeNames[tmpIndex]);
                            setDateText(mRecord);
                        }
                    }
                });
                picker.show();
                break;

            case AlarmRecord.BY_DAY:
                NumberPicker dayPicker = new NumberPicker(this);
                dayPicker.setRange(2, 14);
                dayPicker.setSelectedItem(mAlarmPeriod < 2 ? 2 : mAlarmPeriod);
                dayPicker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(String option) {
                        int tmpPeriod = Integer.parseInt(option);
                        if (tmpPeriod != mAlarmPeriod
                                || mRecord.getActionType() != actionType){
                            mIsChanged = true;
                            mAlarmPeriod = tmpPeriod;
                            mRecord.setActionType(actionType);
                            mAlarmTypeText.setText(AppConstants.AlarmTypeNames[tmpIndex]);
                            setDateText(mRecord);
                        }
                    }
                });
                dayPicker.show();
                break;

            case AlarmRecord.BY_WEEK:
                WeekPicker weekPicker = new WeekPicker(this);
                weekPicker.setCheckedItems(mRecord.getDay());
                weekPicker.setOnWeekPickListener(new WeekPicker.OnWeekPickListener() {
                    @Override
                    public void onWeekPicked(int selected) {
                        AppConstants.DLog("selected items is: " + selected);
                        if (selected == 0){
                            return;
                        }
                        if (selected != mRecord.getDay() || mRecord.getActionType() != actionType){
                            mIsChanged = true;
                            mRecord.setActionType(actionType);
                            mRecord.setDay(selected);
                            mAlarmTypeText.setText(AppConstants.AlarmTypeNames[tmpIndex]);
                            setDateText(mRecord);
                        }
                    }
                });
                weekPicker.show();
                break;

            case AlarmRecord.BY_MONTH:
                // 阳历每月指定天
                NumberPicker numPicker = new NumberPicker(this);
                numPicker.setRange(1, 31, 1);
                numPicker.setSelectedItem(mRecord.getDay());
                numPicker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(String option) {
                        int newVal = Integer.parseInt(option);
                        if (newVal != mRecord.getDay() || mRecord.getActionType() != actionType){
                            mIsChanged = true;
                            mRecord.setActionType(actionType);
                            mRecord.setDay(newVal);
                            mAlarmTypeText.setText(AppConstants.AlarmTypeNames[tmpIndex]);
                            setDateText(mRecord);
                        }
                    }
                });
                numPicker.show();
                break;

            case AlarmRecord.BY_YEAR:
                // 阳历每年的指定月的指定天
                picker = new DatePicker(this, DatePicker.MONTH_DAY);
                picker.setSelectedItem(mRecord.getMonth(), mRecord.getDay());
                picker.setOnDatePickListener(new DatePicker.OnMonthDayPickListener() {
                    @Override
                    public void onDatePicked(int month, int day) {
                        // 判断是否更改了时间
                        if (mRecord.getMonth() != month || mRecord.getDay() != day
                                || mRecord.getActionType() != actionType) {
                            mIsChanged = true;
                            mRecord.setActionType(actionType);
                            mRecord.setMonth(month);
                            mRecord.setDay(day);
                            mAlarmTypeText.setText(AppConstants.AlarmTypeNames[tmpIndex]);
                            setDateText(mRecord);
                        }
                    }
                });
                picker.show();
                break;

            case AlarmRecord.BY_LUNAR_MONTH:
                // 阴历每月指定天
                String[] tmpStrings = new String[LunarCalendar.lunarDateNames.length - 1];
                System.arraycopy(LunarCalendar.lunarDateNames, 1, tmpStrings, 0, tmpStrings.length);
                OptionPicker optPicker = new OptionPicker(this, tmpStrings);
                optPicker.setSelectedIndex(mRecord.getDay() - 1);
                optPicker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(String option) {
                        int newVal = 0;
                        for (int i = 0; i < LunarCalendar.lunarDateNames.length; i++){
                            if (option.equals(LunarCalendar.lunarDateNames[i])){
                                newVal = i;
                                break;
                            }
                        }

                        if (newVal != mRecord.getDay() || mRecord.getActionType() != actionType){
                            mIsChanged = true;
                            mRecord.setActionType(actionType);
                            mRecord.setDay(newVal);
                            mAlarmTypeText.setText(AppConstants.AlarmTypeNames[tmpIndex]);
                            setDateText(mRecord);
                        }
                    }
                });
                optPicker.show();
                break;

            case AlarmRecord.BY_LUNAR_YEAR:
                LunarMonthPicker lunarPicker = new LunarMonthPicker(this);
                lunarPicker.setSelectedItem(mRecord.getMonth(), mRecord.getDay());
                lunarPicker.setOnLunarMonthPickListener(new LunarMonthPicker.OnLunarMonthPickListener() {
                    @Override
                    public void onLunarMonthPicked(int month, int day) {
                        // 判断是否更改了时间
                        if (mRecord.getMonth() != month || mRecord.getDay() != day
                                || mRecord.getActionType() != actionType) {
                            mIsChanged = true;
                            mRecord.setActionType(actionType);
                            mRecord.setMonth(month);
                            mRecord.setDay(day);
                            mAlarmTypeText.setText(AppConstants.AlarmTypeNames[tmpIndex]);
                            setDateText(mRecord);
                        }
                    }
                });
                lunarPicker.show();
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
        Toast.makeText(AddAlarmActivity.this,
                getResources().getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 启动闹钟服务
     */
    private void startService(){
//        Calendar tmpCalendar = Calendar.getInstance();
//        if (record.getAlarmTime().compareTime(tmpCalendar.get(Calendar.HOUR_OF_DAY),
//                tmpCalendar.get(Calendar.MINUTE)) <= 0){
            // 说明闹钟时间比当前时间晚，触发一下服务
            startService(new Intent(this, CalendarService.class));
//        }
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
        if (AlarmRecordMng.isExist(title, mRecord.getIndex())){
            // 该标题已存在
            showToastMsg(R.string.existed_record);
            return;
        }
        if (mRecord.getActionType() == AlarmRecord.BY_DAY){
            mRecord.setHighDay(mAlarmPeriod);
        }
        
        if (mRecord.getIsNew()){
            // 新建记录
            mRecord.setTitle(title);
            mRecord.setContent(content);
            mRecord.setDisplay(mDisplayButton.isChecked());
            mRecord.setPause(mPauseButton.isChecked());

            // 获取当前时间
            mRecord.setCreateTime(Calendar.getInstance());
            // 保存
            boolean result = AlarmRecordMng.insert(mRecord);
            if (result) {
                showToastMsg(R.string.add_record_success);
                startService();
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
            mRecord.setDisplay(mDisplayButton.isChecked());
            mRecord.setPause(mPauseButton.isChecked());

            // 保存
            boolean result = AlarmRecordMng.update(mRecord);
            if (result) {
                showToastMsg(R.string.edit_record_success);
                startService();
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
                    if (AlarmRecordMng.delete(mRecord.getIndex())) {
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
        boolean isDisplayed = mDisplayButton.isChecked();
        boolean isPaused = mPauseButton.isChecked();
        if (!title.equals(mRecord.getOnlyTitle())
                || !content.equals(mRecord.getContent())
                || isDisplayed != mRecord.getDisplay()
                || isPaused != mRecord.getPause()){
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
