package com.jjforever.wgj.maincalendar;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjforever.wgj.maincalendar.BLL.ShiftsWorkRecordMng;
import com.jjforever.wgj.maincalendar.Model.ShiftsWorkItem;
import com.jjforever.wgj.maincalendar.Model.ShiftsWorkRecord;
import com.jjforever.wgj.maincalendar.dialogpicker.picker.DialogPicker;
import com.jjforever.wgj.maincalendar.toolbar.ToolBarActivity;
import com.jjforever.wgj.maincalendar.util.Helper;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.DatePicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.NumberPicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.OptionPicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.TimeSlotPicker;

import java.util.ArrayList;
import java.util.Calendar;

public class AddShiftsWorkActivity extends ToolBarActivity {

    // 标题栏控件
    private EditText mTitleEdit;
    // 周期设置项
    private TextView mPeriodView;
    // 开始日期控件
    private TextView mStartDateView;
    // 轮班设置项集合
    private ArrayList<RelativeLayout> mItemLayouts;
    // 设置项层ID集合
    private final int[] mLayoutIds = new int[]{R.id.work_day_layout_1,R.id.work_day_layout_2,
            R.id.work_day_layout_3,R.id.work_day_layout_4,R.id.work_day_layout_5,
            R.id.work_day_layout_6,R.id.work_day_layout_7,R.id.work_day_layout_8};
    // 轮班类型
    private String[] mWorkTypes;

    // 是否更改记录
    private boolean mIsChanged;

    // 是否确定退出
    private boolean mSureQuit = false;
    // 编辑的记录
    private ShiftsWorkRecord mRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shifts_work);
        mRecord = getIntent().getParcelableExtra(AppConstants.MAIN_ACTIVITY_CLICK_DATE);
        if (mRecord == null){
            // 获取错误
            mSureQuit = true;
            finish();
            return;
        }

        setTitle(getResources().getString(R.string.add_work));
        mWorkTypes = getResources().getStringArray(R.array.work_type);

        setView();
        if (!mRecord.getIsNew()) {
            // 编辑时才显示删除按钮，新建时不显示
            setTitle(getResources().getString(R.string.edit_work));
            showDeleteBtn(true);
        }
    }

    // 设置界面
    private void setView(){
        mTitleEdit = (EditText) findViewById(R.id.text_shifts_title);
        if (mTitleEdit != null && !Helper.isNullOrEmpty(mRecord.getTitle())) {
            mTitleEdit.setText(mRecord.getTitle());
        }

        // 设置项
        mItemLayouts = new ArrayList<>(mLayoutIds.length);
        final ArrayList<ShiftsWorkItem> tmpItems = mRecord.getItems();
        for (int i = 0; i < mLayoutIds.length; i++){
            RelativeLayout tmpLayout = (RelativeLayout) findViewById(mLayoutIds[i]);
            if (tmpLayout == null){
                continue;
            }
            mItemLayouts.add(tmpLayout);
            TextView tmpDay = (TextView) tmpLayout.findViewById(R.id.work_day_text);
            tmpDay.setText(String.format(getString(R.string.work_day_no), i + 1));
            final TextView tmpTitle = (TextView) tmpLayout.findViewById(R.id.work_day_title);
            if (i < tmpItems.size()){
                tmpTitle.setText(tmpItems.get(i).getTitle());
                tmpDay.setText(String.format(getString(R.string.work_day_no), tmpItems.get(i).getDayNo()));
            }
            final int clickIndex = i;
            tmpTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OptionPicker tmpOption = new OptionPicker(AddShiftsWorkActivity.this, mWorkTypes);
                    final ShiftsWorkItem clickItem = tmpItems.get(clickIndex);
                    tmpOption.setSelectedItem(clickItem.getTitle());
                    tmpOption.setTextSize(16);
                    tmpOption.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                        @Override
                        public void onOptionPicked(String option) {
                            if (!option.equals(clickItem.getTitle())) {
                                mIsChanged = true;
                                tmpTitle.setText(option);
                                clickItem.setTitle(option);
                                if (clickItem.getIndex() > 0) {
                                    clickItem.setFlag(ShiftsWorkItem.UPDATE);
                                }
                            }
                        }
                    });
                    tmpOption.show();
                }
            });
            final TextView timeSlot = (TextView) tmpLayout.findViewById(R.id.work_time_slot);
            timeSlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ShiftsWorkItem clickItem = tmpItems.get(clickIndex);
                    TimeSlotPicker tmpPicker = new TimeSlotPicker(AddShiftsWorkActivity.this);
                    tmpPicker.setTimeSlot(clickItem.getStartTime().Hour, clickItem.getStartTime().Minute,
                                    clickItem.getEndTime().Hour, clickItem.getEndTime().Minute);
                    tmpPicker.setOnTimeSlotPickListener(new TimeSlotPicker.OnTimeSlotPickListener() {
                        @Override
                        public void onTimeSlotPicked(int startHour, int startMinute, int endHour, int endMinute) {
                            mIsChanged = true;
                            clickItem.setStartTime(startHour, startMinute);
                            clickItem.setEndTime(endHour, endMinute);
                            if (clickItem.getIndex() > 0){
                                clickItem.setFlag(ShiftsWorkItem.UPDATE);
                            }
                            String tmpStr = clickItem.getStartTime().toString() + "~" + clickItem.getEndTime().toString();
                            timeSlot.setText(tmpStr);
                        }
                    });
                    tmpPicker.show();
                }
            });
            if (i < tmpItems.size()){
                ShiftsWorkItem tmpItem = tmpItems.get(i);
                String tmpStr = tmpItem.getStartTime().toString() + "~" + tmpItem.getEndTime().toString();
                timeSlot.setText(tmpStr);
            }
        }

        mPeriodView = (TextView) findViewById(R.id.work_period);
        setPeriod(mRecord.getPeriod());
        RelativeLayout periodLayout = (RelativeLayout) this.findViewById(R.id.period_layout);
        if (periodLayout != null){
            periodLayout.setOnClickListener(this);
        }

        mStartDateView = (TextView) findViewById(R.id.work_start_date);
        mStartDateView.setText(mRecord.getStartDate().toShortString());
//        mStartDateView.setOnClickListener(this);
        RelativeLayout startLayout = (RelativeLayout) this.findViewById(R.id.start_date_layout);
        if (startLayout != null){
            startLayout.setOnClickListener(this);
        }
    }

    /**
     * 设置周期页面
     * @param period 周期数
     */
    private void setPeriod(int period){
        mPeriodView.setText(String.valueOf(period));
        ArrayList<ShiftsWorkItem> tmpLst = mRecord.getItems();
        for (int i = 0; i < mItemLayouts.size(); i++){
            if (i < period){
                mItemLayouts.get(i).setVisibility(View.VISIBLE);
                if (tmpLst.size() <= i){
                    // 集合中没有进行添加
                    ShiftsWorkItem tmpItem = new ShiftsWorkItem();
                    tmpItem.setDayNo(i + 1);
                    tmpItem.setTitle(getString(R.string.work_default_type));
                    tmpItem.setStartTime(0, 0);
                    tmpItem.setEndTime(23, 59);
                    tmpLst.add(tmpItem);
                }
            }
            else{
                mItemLayouts.get(i).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.period_layout:
                // 最大周期设置
                NumberPicker numPicker = new NumberPicker(this);
                numPicker.setRange(2, this.mLayoutIds.length);
                numPicker.setSelectedItem(mRecord.getPeriod());
                numPicker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(String option) {
                        int tmpPeriod = Integer.parseInt(option);
                        if (tmpPeriod != mRecord.getPeriod()){
                            mIsChanged = true;
                            mRecord.setPeriod(tmpPeriod);
                        }
                        setPeriod(tmpPeriod);
                    }
                });
                numPicker.show();
                break;

            case R.id.start_date_layout:
                DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH_DAY);
                final LunarCalendar tmpCalendar = mRecord.getStartDate();
                picker.setSelectedItem(tmpCalendar.get(Calendar.YEAR),
                        tmpCalendar.get(Calendar.MONTH),
                        tmpCalendar.get(Calendar.DAY_OF_MONTH));
                picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
                    @Override
                    public void onDatePicked(int year, int month, int day) {
                        // 判断是否更改了时间
                        if (tmpCalendar.get(Calendar.YEAR) != year
                                || tmpCalendar.get(Calendar.MONTH) != month
                                || tmpCalendar.get(Calendar.DAY_OF_MONTH) != day) {
                            mRecord.setStartDate(new LunarCalendar(year, month, day));
                            mIsChanged = true;
                            mStartDateView.setText(mRecord.getStartDate().toShortString());
                        }
                    }
                });
                picker.show();
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
        Toast.makeText(AddShiftsWorkActivity.this,
                getResources().getString(msgId),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * 整理每班记录项
     */
    private void CleanUpItems(){
        ArrayList<ShiftsWorkItem> tmpLst = mRecord.getItems();
        for (int i = tmpLst.size() - 1; i >= 0; i--) {
            RelativeLayout tmpLayout = mItemLayouts.get(i);
            ShiftsWorkItem tmpItem = tmpLst.get(i);
            if (tmpLayout.getVisibility() != View.VISIBLE){
                // 不显示则进行删除
                if (tmpItem.getIndex() > 0){
                    // 之前存在的项，需要数据库中进行删除
                    tmpItem.setFlag(ShiftsWorkItem.DELETE);
                }
                else{
                    // 不存在于数据库则直接删除
                    tmpLst.remove(i);
                }
            }
            else{
                if (tmpItem.getIndex() <= 0){
                    // 新建项，插入
                    tmpItem.setFlag(ShiftsWorkItem.INSERT);
                    if (mRecord.getIndex() > 0) {
                        tmpItem.setWorkIndex(mRecord.getIndex());
                    }
                }
            }
        }
    }

    @Override
    public void onOKButtonClick()
    {
        String title = mTitleEdit.getText().toString();
        if (Helper.isNullOrEmpty(title)){
            new DialogPicker(this, getResources().getString(R.string.must_input_title)).show();
            return;
        }
        if (ShiftsWorkRecordMng.isExist(title, mRecord.getIndex())){
            // 该标题已存在
            showToastMsg(R.string.existed_record);
            return;
        }
        mRecord.getStartDate().set(Calendar.HOUR, 0);
        mRecord.getStartDate().set(Calendar.MINUTE, 0);
        mRecord.getStartDate().set(Calendar.SECOND, 0);
        mRecord.getStartDate().set(Calendar.MILLISECOND, 0);

        if (mRecord.getIsNew()){
            // 新建记录
            mRecord.setTitle(title);
            CleanUpItems();

            // 获取当前时间
            mRecord.setCreateTime(Calendar.getInstance());
            // 保存
            boolean result = ShiftsWorkRecordMng.insert(mRecord);
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
            CleanUpItems();

            // 保存
            boolean result = ShiftsWorkRecordMng.update(mRecord);
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
                    if (ShiftsWorkRecordMng.delete(mRecord.getIndex())) {
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
        if (!title.equals(mRecord.getTitle())){
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
