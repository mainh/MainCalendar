package com.jjforever.wgj.maincalendar.BLL;

import android.content.ContentValues;
import android.database.Cursor;

import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.Model.DailyRecord;
import com.jjforever.wgj.maincalendar.Model.ICalendarRecord;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Wgj on 2016/8/26.
 * 日常记录管理类
 */
public final class DailyRecordMng {
    // 管理的表名
    final static String TABLE_NAME = "DailyRecord";

    private DailyRecordMng(){}

    private static ContentValues getValues(DailyRecord record, boolean isNew){
        // 实例化常量值
        ContentValues cValue = new ContentValues();
        // 记录时间
        cValue.put("record_time", record.getRecordTime().getTimeInMillis());
        // 天气代码
        cValue.put("weather", record.getWeather());
        // 标题
        cValue.put("title", record.getTitle());
        // 内容
        cValue.put("content", record.getContent());
        // 是否显示
        cValue.put("display", record.getDisplay() ? 1 : 0);
        if (isNew) {
            // 本条记录创建时间
            cValue.put("create_time", record.getCreateTime().getTimeInMillis());
        }

        return cValue;
    }

    /**
     * 往数据库中添加一条日常记录
     * @param record 日常记录
     * @return 是否成功
     */
    public static boolean insert(DailyRecord record){
        AppConstants.DLog("DBManager --> add");
        try
        {
            // 实例化常量值
            ContentValues cValue = getValues(record, true);

            //调用insert()方法插入数据
            return DatabaseHelper.SQLiteDb.insert(TABLE_NAME, null, cValue) > 0;
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
    }

    /**
     * 更新记录
     * @param record 要更新的记录
     * @return 是否更新成功
     */
    public static boolean update(DailyRecord record){
        try{
            ContentValues cValue = getValues(record, false);
            return DatabaseHelper.SQLiteDb.update(TABLE_NAME, cValue, "[index]=?",
                    new String[] { String.valueOf(record.getIndex()) }) > 0;
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
    }

    /**
     * 根据索引删除一条日常记录
     * @param index 记录索引
     * @return 是否成功
     */
    public static boolean delete(long index){
        try{
            return DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "[index]=?",
                    new String[] { String.valueOf(index) }) >= 0;
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
    }

    /**
     * 根据索引集合批量删除记录
     * @param indexs 索引集合
     * @return 删除成功与否
     */
    public static boolean delete(ArrayList<Long> indexs){
        // 采用事务处理，确保数据完整性
        DatabaseHelper.SQLiteDb.beginTransaction();
        try
        {
            for (long index : indexs)
            {
                DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "[index]=?",
                        new String[] { String.valueOf(index) });
            }
            // 设置事务成功完成
            DatabaseHelper.SQLiteDb.setTransactionSuccessful();
            return true;
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
        finally
        {
            DatabaseHelper.SQLiteDb.endTransaction(); // 结束事务
        }
    }

    /**
     * 检查数据库是否存在此标题的记录
     * @param title 要检查的标题
     * @param index 不为0则排除该索引
     * @return 是否存在
     */
    public static boolean isExist(String title, long index){
        String sqlStr = "select count(*) from " + TABLE_NAME;
        sqlStr += String.format(" where title='%s'", title);
        if (index > 0){
            sqlStr += String.format(Locale.getDefault(),
                    " and [index]<>'%d'", index);
        }
        Cursor cursor = DatabaseHelper.SQLiteDb.rawQuery(sqlStr, null);
        if (cursor == null){
            return false;
        }
        try {
            return (cursor.moveToNext() && cursor.getLong(0) > 0);
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
        finally {
            cursor.close();
        }
    }

    /**
     * 根据条件查询所有的日常记录
     * @param where where条件
     * @param args where条件的参数
     * @param orderBy 排序语句
     * @param limit 限制语句
     * @return 日常记录集合
     */
    private static ArrayList<DailyRecord> select(String where, String[] args, String orderBy, String limit){
        ArrayList<DailyRecord> tmpLst = new ArrayList<>();

        Cursor cursor = DatabaseHelper.SQLiteDb.query(TABLE_NAME, null, where, args, null, null, orderBy, limit);
        if (cursor == null){
            return tmpLst;
        }

        // 遍历读取数据
        try {
            while (cursor.moveToNext()) {
                DailyRecord tmpRecord = new DailyRecord(false);
                tmpRecord.setIndex(cursor.getLong(cursor.getColumnIndex("index")));
                long tmpMill = cursor.getLong(cursor.getColumnIndex("record_time"));
                LunarCalendar recordTime = new LunarCalendar();
                recordTime.setTimeInMillis(tmpMill);
                recordTime.updateLunar();
                tmpRecord.setRecordTime(recordTime);
                tmpRecord.setWeather(cursor.getInt(cursor.getColumnIndex("weather")));
                tmpRecord.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                tmpRecord.setContent(cursor.getString(cursor.getColumnIndex("content")));
                tmpRecord.setDisplay(cursor.getInt(cursor.getColumnIndex("display")) == 1);
                tmpMill = cursor.getLong(cursor.getColumnIndex("create_time"));
                LunarCalendar createTime = new LunarCalendar();
                createTime.setTimeInMillis(tmpMill);
                createTime.updateLunar();
                tmpRecord.setCreateTime(createTime);

                tmpLst.add(tmpRecord);
            }
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
        }
        finally {
            cursor.close();
        }

        return tmpLst;
    }

    /**
     * 根据索引降序排列取出所有日常记录
     * @return 查询到的日常记录
     */
    public static ArrayList<DailyRecord> selectAll(){
        return select(null, null, "[index] DESC", null);
    }

    /**
     * 根据索引获取指定日常记录
     * @param index 索引
     * @return 指定索引的日常记录
     */
    public static DailyRecord select(long index){
        ArrayList<DailyRecord> tmpLst = select("[index]=?", new String[]{String.valueOf(index)}, null, null);
        if (tmpLst == null || tmpLst.size() <= 0){
            return null;
        }

        return tmpLst.get(0);
    }

    /**
     * 获取指定年月的日常记录
     * @param year 指定年
     * @param month 指定月
     * @return 记录集合
     */
    public static ArrayList<ICalendarRecord> selectByMonth(int year, int month){
        LunarCalendar tmpDate = new LunarCalendar(year, month, 1);
        // 月初时间
        long startTime = tmpDate.getTimeInMillis();
        tmpDate.add(Calendar.MONTH, 1);
        tmpDate.add(Calendar.MILLISECOND, -1);
        // 月末时间
        long endTime = tmpDate.getTimeInMillis();

        return selectBySlot(startTime, endTime);
    }

    /**
     * 或者指定日期的日常记录
     * @param date 指定日期
     * @return 记录集合
     */
//    public static ArrayList<ICalendarRecord> selectByDate(LunarCalendar date){
//        // 指定日期的0点
//        LunarCalendar tmpDate = new LunarCalendar(date.get(Calendar.YEAR),
//                                                date.get(Calendar.MONTH),
//                                                date.get(Calendar.DAY_OF_MONTH));
//        long startTime = tmpDate.getTimeInMillis();
//        // 定位到指定日期的23点59分59秒
//        tmpDate.add(Calendar.DAY_OF_MONTH, 1);
//        tmpDate.add(Calendar.MILLISECOND, -1);
//        long endTime = tmpDate.getTimeInMillis();
//
//        return selectBySlot(startTime, endTime);
//    }

    /**
     * 查询指定时间段的记录数
     * @param startTime 开始的毫秒数
     * @param endTime 结束时间的毫秒数
     * @return 记录数
     */
    private static ArrayList<ICalendarRecord> selectBySlot(long startTime, long endTime){
        ArrayList<DailyRecord> tmpLst = select("record_time >= ? and record_time <= ? and display <> ?",
                new String[]{String.valueOf(startTime), String.valueOf(endTime), "0"},
                "[index] DESC", null);
        if (tmpLst == null || tmpLst.isEmpty()){
            return null;
        }

        ArrayList<ICalendarRecord> refLst = new ArrayList<>(tmpLst.size());
        refLst.addAll(tmpLst);
        return  refLst;
    }
}
