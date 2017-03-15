package com.jjforever.wgj.maincalendar.monthui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.BLL.AlarmRecordMng;
import com.jjforever.wgj.maincalendar.BLL.DailyRecordMng;
import com.jjforever.wgj.maincalendar.BLL.ShiftsWorkRecordMng;
import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.Model.ICalendarRecord;
import com.jjforever.wgj.maincalendar.Model.RecordShowType;
import com.jjforever.wgj.maincalendar.Model.RecordType;
import com.jjforever.wgj.maincalendar.Model.ShiftsWorkRecord;
import com.jjforever.wgj.maincalendar.util.DateUtil;
import com.jjforever.wgj.maincalendar.util.Helper;
import com.jjforever.wgj.maincalendar.util.Holiday;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Wgj on 2016/8/9.
 * 日历页面类显示周或者月视图
 */
public class CalendarView extends View {

    /**
     * 视图表格列数及行数定义
     */
    public static final int TOTAL_COL = 7;
    public static final int TOTAL_ROW = 6;

    // 当前月的日期
    private static final int CURRENT_MONTH_DAY = 0x01;
//    // 上个月的日期
//    private static final int LAST_MONTH_DAY = 0x02;
//    // 下个月的日期
//    private static final int NEXT_MONTH_DAY = 0x04;
    // 当天日期
    private static final int TODAY = 0x08;
    // 点击的日期
    private static final int CLICK_DAY = 0x10;

    // 点击标志绘制工具
    private static Paint mClickPaint;

    // 日期绘制工具
    private Paint mTextPaint;
    // 农历及节假日绘制工具
    private TextPaint mLunarPaint;
    // 圆点绘制工具
    private TextPaint mCirclePaint;
    // 轮班记录绘制工具
    private Paint mWorkPaint;
    // 每个单元格的大小
    private int mCellSpace;
    // 日期行集合
    private Row mRows[] = new Row[TOTAL_ROW];
    // 当前显示的日期
    private LunarCalendar mShowDate;
    // 回调的方法
    private CallBack mCallBack;
    // 滑动阻尼系数
    private int touchSlop;
    // 被点击的单元格
    private Cell mClickCell;
    // 手指按下时点击的坐标记录
    private float mDownX;
    private float mDownY;
    private Handler mHandler = new Handler();

    // 日常记录
    ArrayList<ICalendarRecord> mDailyRecords = null;

    // 回调接口函数定义
    public interface CallBack {
        // 回调点击的日期
        void clickDate(CalendarView view, Cell cell);
        // 回调cell的高度确定列表层高度
        void onMeasureCellHeight(CalendarView view, int cellSpace);
        // 回调滑动viewPager改变的日期
        void changeDate(CalendarView view, LunarCalendar date);
    }

    static {
        // 点击绘制工具载入一次即可
        mClickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mClickPaint.setStyle(Paint.Style.FILL);
        mClickPaint.setStrokeWidth(4.0f);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarView(Context context) {
        super(context);
        init(context);
    }

    public CalendarView(Context context, CallBack mCallBack) {
        super(context);
        this.mCallBack = mCallBack;
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Row tmpRow : mRows) {
            if (tmpRow != null) {
                tmpRow.drawCells(canvas);
            }
        }
    }

    /**
     * 获取当前显示的行数
     * @return 该月显示的行数
     */
    public int getRowCount(){
        int count = 0;
        for (Row tmpRow : mRows) {
            if (tmpRow != null) {
                count++;
            }
        }

        return count;
    }

    /**
     * 获取当前显示月份所有记录集合 闹钟与日常
     * @return 当前月所有记录集合
     */
    public ArrayList<ICalendarRecord> getCurMonthRecords(){
        ArrayList<ICalendarRecord> tmpLst = new ArrayList<>();
        for (Row tmpRow : mRows) {
            if (tmpRow == null) {
                continue;
            }

            for (Cell tmpCell : tmpRow.cells) {
                if (tmpCell == null) {
                    continue;
                }

                if (!DateUtil.isSameMonth(mShowDate, tmpCell.CellDate)) {
                    continue;
                }

                if (tmpCell.Records == null) {
                    continue;
                }
                for (ICalendarRecord tmpRecord : tmpCell.Records) {
                    if (tmpRecord.getType() == RecordType.DAILY_RECORD
                            || tmpRecord.getType() == RecordType.ALARM_RECORD) {
                        tmpLst.add(tmpRecord);
                    }
                }
            }
        }

        return tmpLst;
    }

    /**
     * 初始化各参数
     *
     * @param context XX
     */
    private void init(Context context) {
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLunarPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.RED);
        mWorkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWorkPaint.setStyle(Paint.Style.FILL);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initDate();
    }

    /**
     * 根据当前格式初始化当前日期
     */
    private void initDate() {
        mClickCell = null;
        mShowDate = new LunarCalendar();
        fillDate();
    }

    /**
     * 填充日期
     */
    private void fillDate() {
        mClickPaint.setColor(ThemeStyle.Primary);
        mCallBack.changeDate(this, mShowDate);
        fillMonthDate();
        mCallBack.onMeasureCellHeight(this, mCellSpace);
    }

    /**
     * 切换到当前页面触发一系列方法
     */
    public void callBackDate(){
        mCallBack.changeDate(this, mShowDate);
        mCallBack.clickDate(this, mClickCell);
        mCallBack.onMeasureCellHeight(this, mCellSpace);
    }

    /**
     * 从集合中获取指定日期的记录集合
     * @param records 记录集合
     * @param date 指定日期
     * @return 指定日期的记录集合
     */
    private ArrayList<ICalendarRecord> getDateRecords(ArrayList<ICalendarRecord> records, LunarCalendar date){
        if (records == null || records.size() <= 0){
            return null;
        }

        ArrayList<ICalendarRecord> tmpLst = new ArrayList<>();
        for (ICalendarRecord tmpRecord : records){
            if (DateUtil.isSameDay(tmpRecord.getRecordTime(), date)){
                tmpLst.add(tmpRecord);
            }
        }

        return tmpLst.size() < 0 ? null : tmpLst;
    }

    /**
     * 填充月视图
     */
    private void fillMonthDate() {
        int monthDay = DateUtil.getCurrentMonthDay();
        int curYear = mShowDate.get(Calendar.YEAR);
        int curMonth = mShowDate.get(Calendar.MONTH);
        int currentMonthDays = DateUtil.getMonthDays(curYear, curMonth);
        int firstDayWeek = DateUtil.getWeekDayFromDate(curYear, curMonth);
        boolean isCurrentMonth = false;
        if (DateUtil.isCurrentMonth(mShowDate)) {
            isCurrentMonth = true;
        }

        ShiftsWorkRecord workRecord = ShiftsWorkRecordMng.getDisplayWork();
        int dayNo = -1;
        if (workRecord != null){
            dayNo = workRecord.getDayNo(curYear, curMonth, 1);
        }
        mDailyRecords = DailyRecordMng.selectByMonth(curYear, curMonth);

        int day = 0;
        for (int j = 0; j < TOTAL_ROW; j++) {
            mRows[j] = new Row(j);
            for (int i = 0; i < TOTAL_COL; i++) {
                int position = i + j * TOTAL_COL;
                LunarCalendar tmpDate;
                if (position >= firstDayWeek
                        && position < firstDayWeek + currentMonthDays) {
                    day++;
                    // 处于本月的日期
                    tmpDate = new LunarCalendar(curYear, curMonth, day);
                    Cell newCell = new Cell(tmpDate,
                            CURRENT_MONTH_DAY, i, j);
                    if (isCurrentMonth && day == monthDay) {
                        newCell.State |= TODAY;
                    }
                    newCell.ParentView = this;
                    mRows[j].cells[i] = newCell;
                    if (dayNo < 0){
                        // 小于0说明没有轮班记录
                        continue;
                    }
                    newCell.WorkType = workRecord.getItems().get(dayNo).getTitle();
                    dayNo++;
                    if (dayNo >= workRecord.getPeriod()){
                        dayNo = 0;
                    }
                }
            }
            if (mRows[j].getCellCount() == 0){
                // 一行没有数据则置为null
                mRows[j] = null;
            }
        }
        if (mClickCell != null){
            mRows[mClickCell.RowIndex].cells[mClickCell.ColIndex].State |= CLICK_DAY;
            mClickCell = mRows[mClickCell.RowIndex].cells[mClickCell.ColIndex];
        }
        else {
            // 定位到显示的日期
            Point tmpPoint = getDateCell(firstDayWeek, mShowDate);
            mRows[tmpPoint.y].cells[tmpPoint.x].State |= CLICK_DAY;
            mClickCell = mRows[tmpPoint.y].cells[tmpPoint.x];
        }
        mCallBack.clickDate(this, mClickCell);
    }

    /**
     * 更新日期
     */
    public void update(boolean clear) {
        if (clear && mClickCell != null){
            mClickCell.State &= ~TODAY;
        }
        fillDate();
        invalidate();
    }

    /**
     * 获取当前点击的单元格
     * @return 选中的单元格
     */
    public Cell getClickCell(){
        return mClickCell;
    }

    /**
     * 更新日期
     */
    public void update()
    {
        update(false);
    }

    /**
     * 返回到今天
     */
    public void backToday() {
        initDate();
        invalidate();
    }

    /**
     * 定位到指定日期
     * @param date 要定位的日期
     */
    public void locateToDay(LunarCalendar date){
        mClickCell = null;
        AppConstants.DLog("locate to " + date.toRecordTime());
        mShowDate = date;
        update();
    }

    /**
     * 根据月首日为星期几和当前日期获取当前日期在本月的位置
     * @param weekDay 首日为星期几
     * @param date 当前日期
     * @return 当前日期在本月的位置
     */
    private Point getDateCell(int weekDay, LunarCalendar date)
    {
        int tmpDate = date.get(Calendar.DAY_OF_MONTH);
        int tmpRow = tmpDate / TOTAL_COL;
        // 从0开始计数
        int tmpCol = tmpDate % TOTAL_COL + weekDay - 1;
        if (tmpCol >= TOTAL_COL){
            // 往下挪一行
            tmpCol -= TOTAL_COL;
            tmpRow += 1;
        }
        else if (tmpCol < 0){
            tmpCol += TOTAL_COL;
            tmpRow -= 1;
        }

        return new Point(tmpCol, tmpRow);
    }

    /**
     * 异步更新
     */
    private void AsyncUpdate(){
        mHandler.postDelayed(new Runnable() {
            @Override public void run() {
                update();
            }
        }, 200);
    }

    /**
     * 以指定日期为基点向后一个月
     * @param date 基点日期
     */
    public void rightSlide(LunarCalendar date){
        this.mShowDate = date;
        rightSlide();
    }

    /**
     * 以指定日期为基点向前一个月
     * @param date 基点日期
     */
    public void leftSlide(LunarCalendar date){
        this.mShowDate = date;
        leftSlide();
    }

    /**
     * 向右滑动
     */
    private void rightSlide() {
        // 新页面清除点击的单元格
        mClickCell = null;
        int curYear = mShowDate.get(Calendar.YEAR);
        int curMonth = mShowDate.get(Calendar.MONTH);
        if (curMonth == 11) {
            mShowDate.set(Calendar.MONTH, 0);
            mShowDate.set(Calendar.YEAR, curYear + 1);
        } else {
            mShowDate.set(Calendar.MONTH, curMonth + 1);
        }
        if (DateUtil.isCurrentMonth(mShowDate)) {
            mShowDate.set(Calendar.DAY_OF_MONTH, DateUtil.getCurrentMonthDay());
        }
        else {
            mShowDate.set(Calendar.DAY_OF_MONTH, 1);
        }
        //update();
        AsyncUpdate();
    }

    /**
     * 向左滑动
     */
    private void leftSlide() {
        // 新页面清除点击的单元格
        mClickCell = null;
        int curYear = mShowDate.get(Calendar.YEAR);
        int curMonth = mShowDate.get(Calendar.MONTH);
        if (curMonth == 0) {
            mShowDate.set(Calendar.MONTH, 11);
            mShowDate.set(Calendar.YEAR, curYear - 1);
        } else {
            mShowDate.set(Calendar.MONTH, curMonth - 1);
        }
        if (DateUtil.isCurrentMonth(mShowDate)) {
            mShowDate.set(Calendar.DAY_OF_MONTH, DateUtil.getCurrentMonthDay());
        }
        else {
            mShowDate.set(Calendar.DAY_OF_MONTH, 1);
        }
        //update();
        AsyncUpdate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        // 计算行高，设置行高
        mCellSpace = Math.min(h / TOTAL_ROW, w / TOTAL_COL);
        mTextPaint.setTextSize(mCellSpace / 3);
        mLunarPaint.setTextSize(mCellSpace / 5);
        mCirclePaint.setTextSize(mCellSpace / 2.5f);
        mCallBack.onMeasureCellHeight(this, mCellSpace);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                float disX = Math.abs(event.getX() - mDownX);
                float disY = Math.abs(event.getY() - mDownY);
                if (disX < touchSlop && disY < touchSlop) {
                    // 低于滑动系数则认为是点击事件
                    int col = (int) (mDownX / mCellSpace);
                    int row = (int) (mDownY / mCellSpace);
                    measureClickCell(col, row);
                }
                break;
        }
        return true;
    }

    /**
     * 绘制点击的单元格
     * @param col 点击单元格所在列
     * @param row 点击单元格所在行
     */
    private void measureClickCell(int col, int row) {
        if (col >= TOTAL_COL || row >= TOTAL_ROW) {
            return;
        }
        if (mRows[row] == null || mRows[row].cells[col] == null){
            return;
        }
        if (mClickCell != null) {
            // 取消之前的点击状态
            mClickCell.State &= ~CLICK_DAY;
            mRows[mClickCell.RowIndex].cells[mClickCell.ColIndex] = mClickCell;
        }
        if (mRows[row] != null) {
            mRows[row].cells[col].State |= CLICK_DAY;
            mClickCell = mRows[row].cells[col];
            mCallBack.clickDate(this, mClickCell);
            invalidate();
        }
    }

        // 日期行定义
        class Row {
            // 行所在索引
            int rowIndex;

        Row(int index) {
            this.rowIndex = index;
        }

        // 每行的单元格集合
        Cell[] cells = new Cell[TOTAL_COL];

        /**
         * 绘制行单元格
         * @param canvas 要绘制的样式
         */
        void drawCells(Canvas canvas) {
            for (Cell tmpCell : cells) {
                if (tmpCell != null) {
                    tmpCell.drawSelf(canvas);
                }
            }
        }

        /**
         * 获取该行有效单元格个数
         * @return 有效单元格个数
         */
        int getCellCount(){
            int count = 0;
            for (Cell tmpCell : cells) {
                if (tmpCell != null) {
                    count++;
                }
            }

            return count;
        }
    }

    // 单元格定义
    public class Cell {
        // 单元格的日期
        public LunarCalendar CellDate;
        // 单元格相对于当前日期的状态,可能出现组合状态
        int State;
        // 单元格所在列索引
        int ColIndex;
        // 单元格所在行索引
        int RowIndex;
        // 节假日记录等集合
        public ArrayList<ICalendarRecord> Records;
        // 该单元所在页面
        public CalendarView ParentView;
        // 轮班类型，白班，夜班，休息
        String WorkType;

        Cell(LunarCalendar date, int state, int colIndex, int rowIndex) {
            super();
            this.CellDate = date;
            this.State = state;
            this.ColIndex = colIndex;
            this.RowIndex = rowIndex;

            // 日常记录
            this.Records = getDateRecords(mDailyRecords, date);
            if (this.Records == null){
                this.Records = new ArrayList<>();
            }

            // 闹钟记录
            ArrayList<AlarmRecord> alarmLst = AlarmRecordMng.getRecords(date);
            //AppConstants.DLog("alarm size is " + alarmLst.size());
            if (alarmLst != null && alarmLst.size() > 0){
                for (AlarmRecord tmpAlarm : alarmLst){
                    // 设置本日期
                    AlarmRecord addAlarm = tmpAlarm;
                    if (tmpAlarm.getActionType() == AlarmRecord.BY_DAY){
                        addAlarm = tmpAlarm.depthClone();
                    }
                    addAlarm.setRecordTime(date);
                    this.Records.add(addAlarm);
                }
            }

            ArrayList<ICalendarRecord> tmpLst = Holiday.getHolidays(date);
            if (tmpLst != null && tmpLst.size() > 0){
                this.Records.addAll(tmpLst);
            }
            if (this.Records.isEmpty()){
                this.Records = null;
            }
        }

        /**
         * 如果有节假日则返回节假日信息
         * @return 节假日信息，没有返回null
         */
        public ICalendarRecord getHoliday(){
            if (this.Records == null){
                return null;
            }

            for (ICalendarRecord tmpRecord : this.Records){
                if (tmpRecord.getType() == RecordType.LUNAR_HOLIDAY
                        || tmpRecord.getType() == RecordType.SOLAR_HOLIDAY){
                    return tmpRecord;
                }
            }

            return null;
        }

        // 绘制一个单元格 如果颜色需要自定义可以修改
        void drawSelf(Canvas canvas) {
            // 是否绘制圆点
            boolean drawCircle = false;
            if ((State & CURRENT_MONTH_DAY) != 0){
                mTextPaint.setColor(ThemeStyle.CurrentMonth);
                mLunarPaint.setColor(ThemeStyle.CurrentLunar);
            }

            if ((State & TODAY) != 0){
                mTextPaint.setColor(ThemeStyle.Today);
                mLunarPaint.setColor(ThemeStyle.TodayLunar);
            }

            if ((State & CLICK_DAY) != 0){
                // 绘制点击效果
                canvas.drawLine((float) (mCellSpace * (ColIndex + 0.1)),
                        (float) ((RowIndex + 0.85) * mCellSpace),
                        (float) (mCellSpace * (ColIndex + 0.9)),
                        (float) ((RowIndex + 0.85) * mCellSpace),
                        mClickPaint);
            }

            // 绘制农历或节假日信息
            String content = null;
            if (this.Records != null && !this.Records.isEmpty()){
                for (ICalendarRecord tmpRecord : this.Records){
                    // 寻找闹钟
                    if (tmpRecord.getType() == RecordType.ALARM_RECORD){
                        drawCircle = true;
                        break;
                    }
                }
                for (ICalendarRecord tmpRecord : this.Records){
                    if ((tmpRecord.showType() & RecordShowType.TEXT) != 0){
                        content = tmpRecord.getTitle();
                        if ((tmpRecord.showType() & RecordShowType.DOT) != 0)
                        {
                            int recordType = tmpRecord.getType();
                            if (recordType == RecordType.LUNAR_HOLIDAY
                                    || recordType == RecordType.SOLAR_HOLIDAY){
                                mTextPaint.setColor(ThemeStyle.Holiday);
                            }
                            else if (recordType == RecordType.DAILY_RECORD){
                                mTextPaint.setColor(ThemeStyle.Daily);
                            }
                        }
                        break;
                    }
                }
            }
            if (content == null) {
                // 节假日为空则填写农历信息
                content = DateUtil.getSubCalendar(CellDate);
            }

            if (drawCircle){
                String circleChar = "•";
                canvas.drawText(circleChar, (float) (mCellSpace * (ColIndex + 0.85)) - mCirclePaint.measureText(circleChar),
                        (float) ((RowIndex + 0.25) * mCellSpace), mCirclePaint);
            }

            if (!Helper.isNullOrEmpty(this.WorkType)){
                boolean isPaint = false;
                if (WorkType.equals("白班")){
                    isPaint = true;
                    mWorkPaint.setColor(ThemeStyle.WorkDay);
                }
                else if (WorkType.equals("夜班")){
                    isPaint = true;
                    mWorkPaint.setColor(ThemeStyle.WorkNight);
                }

                if (isPaint){
                    canvas.drawRect((float) (mCellSpace * ColIndex), (float)((RowIndex) * mCellSpace),
                            (float) (mCellSpace * (ColIndex + 1)), (float) ((RowIndex + 1) * mCellSpace), mWorkPaint);
                }
            }

            // 绘制日期
            String number = String.valueOf(CellDate.get(Calendar.DAY_OF_MONTH));
            canvas.drawText(number,
                    (float) ((ColIndex + 0.5) * mCellSpace - mTextPaint.measureText(number)/2),
                    (float) ((RowIndex + 0.5) * mCellSpace - mTextPaint.measureText(number, 0, 1) / 2), mTextPaint);

            // 字符串过长进行截取
            content = TextUtils.ellipsize(content, new TextPaint(mLunarPaint), mCellSpace * 0.8f,
                    TextUtils.TruncateAt.END).toString();
            canvas.drawText(content,
                    (float) ((ColIndex + 0.5) * mCellSpace - mLunarPaint.measureText(content)/2),
                    (float) ((RowIndex + 0.8) * mCellSpace - mLunarPaint.measureText(content, 0, 1) / 2), mLunarPaint);
        }
    }
}
