package com.jjforever.wgj.maincalendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.BLL.AlarmRecordMng;
import com.jjforever.wgj.maincalendar.BLL.ShiftsWorkRecordMng;
import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.Model.DailyRecord;
import com.jjforever.wgj.maincalendar.Model.ICalendarRecord;
import com.jjforever.wgj.maincalendar.Model.RecordType;
import com.jjforever.wgj.maincalendar.Model.ShiftsWorkItem;
import com.jjforever.wgj.maincalendar.Model.ShiftsWorkRecord;
import com.jjforever.wgj.maincalendar.services.CalendarService;
import com.jjforever.wgj.maincalendar.monthui.CalendarView;
import com.jjforever.wgj.maincalendar.monthui.CalendarViewBuilder;
import com.jjforever.wgj.maincalendar.monthui.CalendarViewPagerListener;
import com.jjforever.wgj.maincalendar.monthui.CalendarViewPagerAdapter;
import com.jjforever.wgj.maincalendar.monthui.ThemeStyle;
import com.jjforever.wgj.maincalendar.util.DateUtil;
import com.jjforever.wgj.maincalendar.util.Helper;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

import com.jjforever.wgj.maincalendar.weather.util.WeatherConstants;
import com.jjforever.wgj.maincalendar.weather.util.WeatherIconUtil;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.DatePicker;
import com.jjforever.wgj.maincalendar.wheelpicker.picker.OptionPicker;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CalendarView.CallBack {

    // 日常记录编辑页面的需求编号
    private static final int DailyRecordRequestCode = 0;
    // 闹钟记录编辑页面的需求编号
    private static final int AlarmRecordRequestCode = 1;
    // 轮班记录编辑页面的需求编号
    private static final int ShiftsWorkRecordRequestCode = 2;
    // 系统设置页面的需求编号
    private static final int GlobalSettingRequestCode = 3;

    // 当前布局方向 1:竖屏   2:横屏
    private int mOrientation;
    // 当日任务列表图层
    private RelativeLayout mEventLstLayout;
    // 日历页面左右滑动监听
    private CalendarViewPagerListener mListener;
    private View mContentPager;
    // 当前点击的日期单元格
    private CalendarView.Cell mClickCell;
    // 定位到今日按钮层，只在非本月时显示
    private LinearLayout mTodayLayout;
    // 日程距离日历的距离
    private int mRecordListTopMargin;
    // 行高
    private int mCellHeight = 0;
    // mContentPager的高度记录
    private int mViewHeight = 0;
    // 之前页面显示的行数
    private int mOldRowCount = 0;
    // 工具栏对象
    private Toolbar mToolbar;
    // 阳历日期标签，点击进行日期选择
    private TextView mSolarTitle;
    // 选中日期当天的安排情况
    private TextView mTodaySchedule;
    // 日期变化通知接收
    private BroadcastReceiver mDateReceiver;
    // 时间轴列表
    private ListView mListView;
    // 当前显示月的记录集合
    private ArrayList<ICalendarRecord> mCurMonthRecords;
    // 记录适配器
    private RecordAdapter mRecordAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置透明状态栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ViewGroup contentFrameLayout = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
        if (contentFrameLayout != null) {
            View parentView = contentFrameLayout.getChildAt(0);
            if (parentView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                parentView.setFitsSystemWindows(true);
            }
        }
        setContentView(R.layout.activity_main);
        // 获取屏幕方向
        mOrientation = getResources().getConfiguration().orientation;

        // 载入闹钟类型
        AppConstants.AlarmTypeNames = getResources().getStringArray(R.array.alarm_type_names);
        // 更新日期
        DateUtil.updateCurrent();
        // 载入必须服务
        AppConstants.loadGlobalService(this);

        // 开启后台服务
//        if (!Helper.isServiceRunning(this, CalendarService.class.getName())) {
//            AppConstants.DLog("Enter in service...");
            startService(new Intent(this, CalendarService.class));
//        }

        // 初始化闹钟记录
        AlarmRecordMng.initAlarmRecords();
        ShiftsWorkRecordMng.loadDisplayWork();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            int statusHeight = Helper.getStatusHeight(this);
            mToolbar.getLayoutParams().height += statusHeight;
            mToolbar.setPadding(mToolbar.getPaddingLeft(), statusHeight, mToolbar.getPaddingRight(), mToolbar.getPaddingBottom());
            mToolbar.setBackgroundColor(ThemeStyle.Primary);
        }
        mSolarTitle = (TextView) findViewById(R.id.solarTitle);
        if (mSolarTitle != null) {
            mSolarTitle.setOnClickListener(this);
        }

        // 设置右上角的填充菜单
        mToolbar.inflateMenu(R.menu.menu_main);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(this);
        }

        // 获取页面资源
        setViewPager();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        mDateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                onReceiveDate(intent);
            }
        };
        registerReceiver(mDateReceiver, intentFilter);
        WeatherIconUtil.initWeatherNames(getResources().getStringArray(R.array.weather_names));
    }

    /**
     * 初始化页面控件
     */
    private void setViewPager()
    {
        mRecordListTopMargin = (int)this.getResources().getDimension(R.dimen.schedule_top_margin);

        mTodayLayout = (LinearLayout) this.findViewById(R.id.today_button_parent);
        mEventLstLayout = (RelativeLayout)this.findViewById(R.id.eventLstLayout);

        TextView todayView = (TextView) this.findViewById(R.id.today_button);
        if (todayView != null) {
            mTodayLayout.setVisibility(View.INVISIBLE);
            todayView.setOnClickListener(this);
        }
        mTodaySchedule = (TextView) this.findViewById(R.id.view_today_schedule);
        mListView = (ListView) findViewById(R.id.record_list);
        if (mListView != null){
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mRecordAdapter == null){
                        return;
                    }

                    Object selItem = mRecordAdapter.getItem(position);
                    if (selItem == null){
                        return;
                    }

                    ICalendarRecord tmpRecord = (ICalendarRecord)selItem;
                    if (tmpRecord.getType() == RecordType.DAILY_RECORD) {
                        startEditDaily((DailyRecord) tmpRecord);
                    }
                    else if (tmpRecord.getType() == RecordType.ALARM_RECORD){
                        startEditAlarm((AlarmRecord)tmpRecord);
                    }
                }
            });
        }

        ViewPager viewPager = (ViewPager) this.findViewById(R.id.viewpager);
        CalendarView[] views = CalendarViewBuilder.createMassCalendarViews(this, AppConstants.LOAD_CALENDAR_VIEW_COUNT, this);
        CalendarViewPagerAdapter viewPagerAdapter = new CalendarViewPagerAdapter(views);
        mListener = new CalendarViewPagerListener(viewPagerAdapter);
        if (viewPager != null) {
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.addOnPageChangeListener(mListener);
            viewPager.setCurrentItem(CalendarViewPagerListener.DEFAULT_INDEX);
        }

        mContentPager = this.findViewById(R.id.contentPager);
        // 竖屏才需要进行检测
        if (mContentPager != null && mOrientation == ORIENTATION_PORTRAIT) {
            // 根据当前日历的行高与页面高度定义抽屉的最小高度与最大高度
            mContentPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mCellHeight == 0) {
                        return;
                    }
                    if (mViewHeight != mContentPager.getHeight()) {
                        // 高度值有变化
                        mViewHeight = mContentPager.getHeight();
                        int tmpHeight = mViewHeight - mCellHeight *  mListener.getCurrentView().getRowCount() - mRecordListTopMargin;
                        mEventLstLayout.setMinimumHeight(tmpHeight);
                        mEventLstLayout.getLayoutParams().height = tmpHeight;
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDateReceiver);
    }

    /**
     * 工具栏显示的当前日期
     * @param date 要显示的日期
     */
    public void setShowDateViewText(LunarCalendar date){
        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        String lunarYear = date.getLunar(LunarCalendar.LUNAR_YEAR)
                            + date.getLunar(LunarCalendar.LUNAR_ANIMAL)
                            + this.getString(R.string.unit_year);
        String lunarMonth = date.getLunar(LunarCalendar.LUNAR_MONTH)
                            + this.getString(R.string.unit_month);
        String solarDate = String.format(Locale.getDefault(), "%4d%s%d%s",
                            year, this.getString(R.string.unit_year),
                            month + 1, this.getString(R.string.unit_month));
        // 设置主标题
        mToolbar.setTitle(lunarMonth);
        // 设置子标题
        mToolbar.setSubtitle(lunarYear);
        // 设置农历信息
        mSolarTitle.setText(solarDate);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.today_button:
                mListener.backToday();
                break;

            case R.id.solarTitle:
                // 默认从2000~2050年
                DatePicker picker = new DatePicker(this);
                Calendar date = mClickCell.CellDate;
                picker.setSelectedItem(date.get(Calendar.YEAR),
                                date.get(Calendar.MONTH),
                                date.get(Calendar.DAY_OF_MONTH));
                picker.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
                    @Override
                    public void onDatePicked(int year, int month, int day) {
                        mListener.locateToDay(new LunarCalendar(year, month, day));
                    }
                });
                picker.show();
                break;

            case R.id.fab:
                OptionPicker tmpOption = new OptionPicker(this,
                        getResources().getStringArray(R.array.record_type));
                tmpOption.setSelectedIndex(1);
                tmpOption.setTextSize(16);
                tmpOption.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
                    @Override
                    public void onOptionPicked(String option) {
                        String[] options = getResources().getStringArray(R.array.record_type);
                        if (option.equals(options[0])){
                            // 闹钟
                            ICalendarRecord tmpRecord = mClickCell.getHoliday();
                            String tmpTitle = "";
                            if (tmpRecord != null){
                                tmpTitle = tmpRecord.getTitle();
                            }
                            startAddAlarm(mClickCell.CellDate, tmpTitle);
                        }
                        else if (option.equals(options[1])){
                            // 日程
                            ICalendarRecord tmpRecord = mClickCell.getHoliday();
                            String tmpTitle = "";
                            if (tmpRecord != null){
                                tmpTitle = tmpRecord.getTitle();
                            }
                            startAddDaily(mClickCell.CellDate, tmpTitle, WeatherConstants.SUNNY);
                        }
                        else if (option.equals(options[2])){
                            // 轮班
                            startAddWork(mClickCell.CellDate);
                        }
                    }
                });
                tmpOption.show();
                break;

            default:
                break;
        }
    }

    /**
     * 打开系统设置页面
     */
    private void startGlobalSetting(){
        Intent intent = new Intent(this, GlobalSettingActivity.class);
        Bundle mBundle = new Bundle();
        intent.putExtras(mBundle);
        startActivityForResult(intent, GlobalSettingRequestCode);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * 开始添加闹钟
     * @param date 默认时间
     */
    private void startAddAlarm(LunarCalendar date, String holiday)
    {
        Intent intent = new Intent(this, AddAlarmActivity.class);
        Bundle bundle = new Bundle();
        Calendar tmpDate = Calendar.getInstance();

        AlarmRecord tmpRecord = new AlarmRecord();
        tmpRecord.setActionType(AlarmRecord.ONCE);
        tmpRecord.setAlarmTime(tmpDate.get(Calendar.HOUR_OF_DAY), tmpDate.get(Calendar.MINUTE));
        tmpRecord.setYear(date.get(Calendar.YEAR));
        tmpRecord.setMonth(date.get(Calendar.MONTH));
        tmpRecord.setDay(date.get(Calendar.DAY_OF_MONTH));
        tmpRecord.setTitle(holiday);
        if (!Helper.isNullOrEmpty(holiday)) {
            tmpRecord.setContent(String.format(getString(R.string.today_is), holiday));
        }
        bundle.putParcelable(AppConstants.MAIN_ACTIVITY_CLICK_DATE, tmpRecord);

        intent.putExtras(bundle);
        startActivityForResult(intent, AlarmRecordRequestCode);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * 开始编辑闹钟记录
     * @param record 闹钟记录
     */
    private void startEditAlarm(AlarmRecord record){
        Intent intent = new Intent(this, AddAlarmActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(AppConstants.MAIN_ACTIVITY_CLICK_DATE, record);
        intent.putExtras(mBundle);
        startActivityForResult(intent, AlarmRecordRequestCode);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * 添加日程
     * @param date 默认时间
     */
    private void startAddDaily(LunarCalendar date, String holiday, int weather)
    {
        Intent intent = new Intent(this, AddDailyActivity.class);
        Bundle mBundle = new Bundle();
        Calendar tmpDate = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, tmpDate.get(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, tmpDate.get(Calendar.MINUTE));

        DailyRecord tmpRecord = new DailyRecord();
        tmpRecord.setRecordTime(date);
        tmpRecord.setWeather(weather);
        tmpRecord.setTitle(holiday);
        if (!Helper.isNullOrEmpty(holiday)) {
            tmpRecord.setContent(String.format(getString(R.string.today_is), holiday));
        }
        mBundle.putParcelable(AppConstants.MAIN_ACTIVITY_CLICK_DATE, tmpRecord);
//        mBundle.putSerializable(AppConstants.MAIN_ACTIVITY_CLICK_DATE, tmpRecord);
        intent.putExtras(mBundle);
        startActivityForResult(intent, DailyRecordRequestCode);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * 编辑日程
     * @param record 要编辑的记录
     */
    private void startEditDaily(DailyRecord record){
        Intent intent = new Intent(this, AddDailyActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(AppConstants.MAIN_ACTIVITY_CLICK_DATE, record);
        intent.putExtras(mBundle);
        startActivityForResult(intent, DailyRecordRequestCode);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * 添加轮班记录
     * @param date 当前点击的日期
     */
    private void startAddWork(LunarCalendar date)
    {
        Intent intent = new Intent(this, AddShiftsWorkActivity.class);
        Bundle mBundle = new Bundle();
        ShiftsWorkRecord tmpRecord = new ShiftsWorkRecord();
        tmpRecord.setStartDate(date);
        tmpRecord.setPeriod(4);
        String[] itemTypes = getResources().getStringArray(R.array.work_type);
        ArrayList<ShiftsWorkItem> tmpLst = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            ShiftsWorkItem tmpItem = new ShiftsWorkItem();
            tmpItem.setDayNo(i + 1);
            tmpItem.setTitle(i < itemTypes.length ? itemTypes[i] : itemTypes[itemTypes.length - 1]);
            tmpItem.setStartTime(0, 0);
            tmpItem.setEndTime(23, 59);
            tmpLst.add(tmpItem);
        }
        tmpRecord.setItems(tmpLst);
        mBundle.putParcelable(AppConstants.MAIN_ACTIVITY_CLICK_DATE, tmpRecord);
        intent.putExtras(mBundle);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DailyRecordRequestCode
                || requestCode == AlarmRecordRequestCode
                || requestCode == GlobalSettingRequestCode
                || requestCode == ShiftsWorkRecordRequestCode) {
            if (requestCode == GlobalSettingRequestCode
                    || requestCode == ShiftsWorkRecordRequestCode) {
                // 重载轮班记录
                ShiftsWorkRecordMng.loadDisplayWork();
                if (requestCode == GlobalSettingRequestCode){
                    ThemeStyle.LoadGlobalTheme();
                    mToolbar.setBackgroundColor(ThemeStyle.Primary);
                }
            }
            // 日常/闹钟记录编辑
            if (resultCode == RESULT_OK || resultCode == AppConstants.RESULT_DELETE) {
                updateCalendarView();
                mCurMonthRecords = mListener.getCurrentView().getCurMonthRecords();
                RecordDateComparator comparator = new RecordDateComparator();
                Collections.sort(mCurMonthRecords, comparator);
                clickDate(mListener.getCurrentView(), mClickCell);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_daily){
            // 查看日常记录列表
            Intent intent = new Intent(this, DailyRecordList.class);
            startActivityForResult(intent, DailyRecordRequestCode);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        else if (id == R.id.action_alarm_clock){
            // 查看闹钟记录列表
            Intent intent = new Intent(this, AlarmRecordList.class);
            startActivityForResult(intent, AlarmRecordRequestCode);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        else if (id == R.id.action_shifts_work){
            // 查看轮班记录列表
            Intent intent = new Intent(this, ShiftsWorkRecordList.class);
            startActivityForResult(intent, ShiftsWorkRecordRequestCode);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }
        else if (id == R.id.action_settings) {
            startGlobalSetting();
            return true;
        }
        else if (id == R.id.action_about){
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMeasureCellHeight(CalendarView view, int cellSpace) {
        // 记录下行高
        if (cellSpace == 0) {
            return;
        }

        mCellHeight = cellSpace;

        if (mOrientation == ORIENTATION_LANDSCAPE){
            // 横向不理会
            return;
        }

        // mContentPager的高度
        if (view != mListener.getCurrentView()){
            return;
        }
        int tmpCount = view.getRowCount();
        if (mOldRowCount != tmpCount) {
            mOldRowCount = tmpCount;
            int viewHeight = mContentPager.getHeight();
            int tmpHeight = viewHeight - cellSpace * mOldRowCount - mRecordListTopMargin;
            mEventLstLayout.setMinimumHeight(tmpHeight);
            mEventLstLayout.getLayoutParams().height = tmpHeight;
        }
    }

    @Override
    public void clickDate(CalendarView view, CalendarView.Cell cell) {
        if (mListener == null || view != mListener.getCurrentView()){
            return;
        }
        if (mClickCell == null || !DateUtil.isSameMonth(mClickCell.CellDate, cell.CellDate)){
            mCurMonthRecords = cell.ParentView.getCurMonthRecords();
            RecordDateComparator comparator = new RecordDateComparator();
            Collections.sort(mCurMonthRecords, comparator);
        }
        mClickCell = cell;
        if (mTodayLayout != null) {
            if (DateUtil.isCurrentMonth(mClickCell.CellDate)) {
                mTodayLayout.setVisibility(View.INVISIBLE);
            } else {
                // 非当前月则显示今天按钮
                mTodayLayout.setVisibility(View.VISIBLE);
            }
        }

        setShowDateViewText(cell.CellDate);
        showClickCellRecords(mClickCell);
    }

    /**
     * 显示点击的日期当天的记录
     * @param cell 点击的单元格
     */
    private void showClickCellRecords(CalendarView.Cell cell) {
        if (cell.Records == null || cell.Records.isEmpty()) {
            mRecordAdapter = null;
            if (mCurMonthRecords != null && !mCurMonthRecords.isEmpty()){
                mRecordAdapter = new RecordAdapter(this, true, mCurMonthRecords);
                mTodaySchedule.setText(String.format(getString(R.string.month_has_things), mCurMonthRecords.size()));
            }
            else{
                mTodaySchedule.setText(getString(R.string.today_no_things));
            }
            mListView.setAdapter(mRecordAdapter);
            return;
        }

        ArrayList<ICalendarRecord> tmpLst = new ArrayList<>();
        String holidayStr = "";
        for (ICalendarRecord tmpRecord : cell.Records) {
            if (tmpRecord.getType() == RecordType.DAILY_RECORD
                    || tmpRecord.getType() == RecordType.ALARM_RECORD) {
                tmpLst.add(tmpRecord);
            }
            else if (tmpRecord.getType() == RecordType.SOLAR_HOLIDAY
                    || tmpRecord.getType() == RecordType.LUNAR_HOLIDAY){
                holidayStr += tmpRecord.getTitle() + " | ";
            }
        }
        if (!Helper.isNullOrEmpty(holidayStr)) {
            holidayStr = holidayStr.substring(0, holidayStr.length() - 3);
            holidayStr = String.format(getString(R.string.today_is), holidayStr);
        }
        if (tmpLst.isEmpty()) {
            mTodaySchedule.setText(getString(R.string.today_no_things));
            mRecordAdapter = null;
            if (mCurMonthRecords != null && !mCurMonthRecords.isEmpty()){
                mRecordAdapter = new RecordAdapter(this, true, mCurMonthRecords);
                mTodaySchedule.setText(String.format(getString(R.string.month_has_things), mCurMonthRecords.size()));
            }
            mListView.setAdapter(mRecordAdapter);

            if (!Helper.isNullOrEmpty(holidayStr)){
                mTodaySchedule.setText(holidayStr);
            }
            return;
        }

        if (!Helper.isNullOrEmpty(holidayStr)){
            mTodaySchedule.setText(holidayStr);
        }
        else {
            mTodaySchedule.setText(String.format(getString(R.string.today_has_things), tmpLst.size()));
        }
        RecordDateComparator comparator = new RecordDateComparator();
        Collections.sort(tmpLst, comparator);
        // ListView绑定适配器
        mRecordAdapter = new RecordAdapter(this, false, tmpLst);
        mListView.setAdapter(mRecordAdapter);
    }

    @Override
    public void changeDate(CalendarView view, LunarCalendar date) {
//        if (mListener != null && view == mListener.getCurrentView()){
//            setShowDateViewText(date);
//        }
    }

    /**
     * 更新当前日历页面
     */
    public void updateCalendarView(){
        mListener.updateView();
    }

    /**
     * 日期更新后当前日期更改
     * @param intent 广播内容
     */
    public void onReceiveDate(Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_DATE_CHANGED)) {
            // 该广播在已经广播过的日期再次设置该日期是不会再广播的
            // 所以接收该广播只是正常的日期变化才有效
            DateUtil.updateCurrent();
            if (DateUtil.isCurrentMonth(mClickCell.CellDate)) {
                updateCalendarView();
            }
        }
    }
}
