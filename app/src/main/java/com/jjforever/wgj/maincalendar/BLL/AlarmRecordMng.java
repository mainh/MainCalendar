package com.jjforever.wgj.maincalendar.BLL;

import android.content.ContentValues;
import android.database.Cursor;

import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.util.DateUtil;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Wgj on 2016/9/10.
 * 闹钟记录管理类
 */
public final class AlarmRecordMng {
    // 管理的表名
    final static String TABLE_NAME = "AlarmRecord";
    // 闹钟记录集合
    private static ArrayList<AlarmRecord> mAlarmRecords;

    /**
     * 构造方法，私有化，禁止实例化
     */
    private AlarmRecordMng(){}

    /**
     * 根据记录获取ContentValues
     * @param record 闹钟记录
     * @param isNew 是否为需要插入的新记录
     * @return ContentValues
     */
    private static ContentValues getValues(AlarmRecord record, boolean isNew){
        // 实例化常量值
        ContentValues cValue = new ContentValues();
        // 记录时间
        cValue.put("action_type", record.getActionType());
        // 报警时间
        cValue.put("alarm_time", record.getAlarmTime().getTime());
        // 年月日
        cValue.put("date_year", record.getYear());
        cValue.put("date_month", record.getMonth());
        cValue.put("date_day", record.getAllDay());
        // 标题
        cValue.put("title", record.getOnlyTitle());
        // 内容
        cValue.put("content", record.getContent());
        // 是否显示
        cValue.put("display", record.getDisplay() ? 1 : 0);
        // 是否停用
        cValue.put("pause", record.getPause() ? 1 : 0);
        if (isNew) {
            // 本条记录创建时间
            cValue.put("create_time", record.getCreateTime().getTimeInMillis());
        }

        return cValue;
    }

    /**
     * 往数据库中添加一条闹钟记录
     * @param record 闹钟记录
     * @return 成功返回索引，失败返回-1
     */
    private static long insertSQL(AlarmRecord record){
        AppConstants.DLog("DBManager --> add alarm");
        try
        {
            // 实例化常量值
            ContentValues cValue = getValues(record, true);

            //调用insert()方法插入数据
            return DatabaseHelper.SQLiteDb.insert(TABLE_NAME, null, cValue);
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return -1;
        }
    }

    /**
     * 插入一条闹钟记录
     * @param record 要插入的闹钟记录
     * @return 插入成功与否
     */
    public static boolean insert(AlarmRecord record){
        long tmpIndex = insertSQL(record);
        if (tmpIndex <= 0){
            return false;
        }
        record.setIndex(tmpIndex);
        if (mAlarmRecords == null){
            mAlarmRecords = new ArrayList<>();
        }
        mAlarmRecords.add(0, record);
        return true;
    }

    /**
     * 更新记录
     * @param record 要更新的记录
     * @return 是否更新成功
     */
    private static boolean updateSQL(AlarmRecord record){
        try{
            // 实例化常量值
            ContentValues cValue = getValues(record, false);

            return DatabaseHelper.SQLiteDb.update(TABLE_NAME, cValue, "alarm_index=?",
                    new String[] { String.valueOf(record.getIndex()) }) > 0;
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
    }

    /**
     * 在数据库与内存中更新一条闹钟记录
     * @param record 要更新的记录
     * @return 成功与否
     */
    public static boolean update(AlarmRecord record){
        if (updateSQL(record)){
            if (mAlarmRecords != null){
                for (int i = 0; i < mAlarmRecords.size(); i++){
                    if (mAlarmRecords.get(i).getIndex() == record.getIndex()){
                        mAlarmRecords.set(i, record);
                        break;
                    }
                }
            }
            return true;
        }

        return false;
    }

    /**
     * 根据索引从集合中删除一条记录
     * @param index 索引
     */
    private static void deleteFromList(long index){
        if (mAlarmRecords == null){
            return;
        }
        for (AlarmRecord tmpRecord : mAlarmRecords){
            if (tmpRecord.getIndex() == index){
                mAlarmRecords.remove(tmpRecord);
                return;
            }
        }
    }

    /**
     * 根据索引删除一条闹钟记录
     * @param index 记录索引
     * @return 是否成功
     */
    private static boolean deleteSQL(long index){
        try{
            return DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "alarm_index=?",
                    new String[] { String.valueOf(index) }) >= 0;
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
    }

    /**
     * 根据索引从数据库及内存中删除一条闹钟记录
     * @param index 索引
     * @return 删除成功与否
     */
    public static boolean delete(long index){
        if (deleteSQL(index)){
            deleteFromList(index);
            return true;
        }

        return false;
    }

    /**
     * 根据索引集合批量删除记录
     * @param indexs 索引集合
     * @return 删除成功与否
     */
    private static boolean deleteSQL(ArrayList<Long> indexs){
        // 采用事务处理，确保数据完整性
        DatabaseHelper.SQLiteDb.beginTransaction();
        try
        {
            for (long index : indexs)
            {
                DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "alarm_index=?",
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
     * 从数据库与内存列表中删除一条闹钟记录
     * @param indexs 删除的闹钟记录索引集合
     * @return 删除成功与否
     */
    public static boolean delete(ArrayList<Long> indexs){
        if (deleteSQL(indexs)){
            if (mAlarmRecords != null){
                // 从集合中删除
                for (long index : indexs){
                    deleteFromList(index);
                }
            }
            return true;
        }

        return false;
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
                            " and alarm_index<>'%d'", index);
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
     * 根据条件查询所有的闹钟记录
     * @param where where条件
     * @param args where条件的参数
     * @param orderBy 排序语句
     * @param limit 限制语句
     * @return 闹钟记录集合
     */
    @SuppressWarnings("WrongConstant")
    private static ArrayList<AlarmRecord> select(String where, String[] args, String orderBy, String limit){
        ArrayList<AlarmRecord> tmpLst = new ArrayList<>();

        Cursor cursor = DatabaseHelper.SQLiteDb.query(TABLE_NAME, null, where, args, null, null, orderBy, limit);
        if (cursor == null){
            return tmpLst;
        }

        // 遍历读取数据
        try {
            while (cursor.moveToNext()) {
                AlarmRecord tmpRecord = new AlarmRecord(false);
                tmpRecord.setIndex(cursor.getLong(cursor.getColumnIndex("alarm_index")));
                tmpRecord.setActionType(cursor.getInt(cursor.getColumnIndex("action_type")));
                tmpRecord.setAlarmTime(cursor.getInt(cursor.getColumnIndex("alarm_time")));
                tmpRecord.setYear(cursor.getInt(cursor.getColumnIndex("date_year")));
                tmpRecord.setMonth(cursor.getInt(cursor.getColumnIndex("date_month")));
                tmpRecord.setDay(cursor.getInt(cursor.getColumnIndex("date_day")));
                tmpRecord.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                tmpRecord.setContent(cursor.getString(cursor.getColumnIndex("content")));
                tmpRecord.setDisplay(cursor.getInt(cursor.getColumnIndex("display")) == 1);
                tmpRecord.setPause(cursor.getInt(cursor.getColumnIndex("pause")) == 1);
                long tmpMill = cursor.getLong(cursor.getColumnIndex("create_time"));
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
     * 根据索引降序排列取出所有闹钟记录
     * @return 查询到的闹钟记录
     */
    private static ArrayList<AlarmRecord> selectAll(){
        return select(null, null, "alarm_index DESC", null);
    }

    /**
     * 根据索引获取指定闹钟记录
     * @param index 索引
     * @return 指定索引的闹钟记录
     */
//    private static AlarmRecord select(long index){
//        ArrayList<AlarmRecord> tmpLst = select("alarm_index=?", new String[]{String.valueOf(index)}, null, null);
//        if (tmpLst == null || tmpLst.size() <= 0){
//            return null;
//        }
//
//        return tmpLst.get(0);
//    }

    /**
     * 根据索引获取闹钟记录
     * @param index 索引
     * @return 闹钟记录
     */
//    public static AlarmRecord getRecord(long index){
//        for (AlarmRecord tmpRecord : mAlarmRecords){
//            if (tmpRecord.getIndex() == index){
//                return tmpRecord;
//            }
//        }
//
//        return null;
//    }

    /**
     * 载入所有闹钟记录
     */
    public static void initAlarmRecords(){
        mAlarmRecords = AlarmRecordMng.selectAll();
    }

    /**
     * 获取所有的闹钟记录
     * @return 闹钟记录
     */
    public static ArrayList<AlarmRecord> getAllRecords(){
        return mAlarmRecords;
    }

    /**
     * 获取指定日期的闹钟记录
     * @param calendar 指定日期
     * @return 闹钟记录
     */
    public static ArrayList<AlarmRecord> getRecords(LunarCalendar calendar){
        if (mAlarmRecords == null){
            return null;
        }
        if (DateUtil.compareDate(calendar) < 0){
            return null;
        }

        ArrayList<AlarmRecord> tmpLst = new ArrayList<>();
        for (AlarmRecord tmpRecord : mAlarmRecords){
            if (tmpRecord.getActionType() != AlarmRecord.BY_WEEK
                    && !tmpRecord.getPause() && tmpRecord.getDisplay()) {
                if (tmpRecord.isRecordDate(calendar)) {
                    tmpLst.add(tmpRecord);
                }
            }
        }

        return tmpLst;
    }

    /**
     * 根据当前时间获取下次闹钟时间点
     * @param curTime 当前时间
     * @return 下次时间点
     */
    private static int getNextLargerAlarmTime(int curTime) {
        Cursor cursor = DatabaseHelper.SQLiteDb.query(TABLE_NAME, null,
                "pause<>? and alarm_time>?", new String[]{"1", String.valueOf(curTime)}, null, null,
                "alarm_time ASC", "0,1");
        if (cursor == null) {
            return -1;
        }

        try {
            if (cursor.moveToNext()){
                return cursor.getInt(cursor.getColumnIndex("alarm_time"));
            }
        } catch (Exception ex) {
            AppConstants.WLog(ex.toString());
        } finally {
            cursor.close();
        }

        return -1;
    }

    /**
     * 获取下次闹钟响应时间
     * @param curTime 当前时间
     * @return 下次时间 小于0说明未取到
     */
    public static int getNextAlarmTime(int curTime){
        int nextTime = getNextLargerAlarmTime(curTime);
        if (nextTime >= 0){
            return nextTime;
        }

        // 说明当天没有可触发的闹钟了，获取第一条的时间闹钟
        Cursor cursor = DatabaseHelper.SQLiteDb.query(TABLE_NAME, null,
                "pause<>?", new String[]{"1"}, null, null,
                "alarm_time ASC", "0,1");
        if (cursor == null) {
            return -1;
        }

        try {
            if (cursor.moveToNext()){
                return cursor.getInt(cursor.getColumnIndex("alarm_time"));
            }
        } catch (Exception ex) {
            AppConstants.WLog(ex.toString());
        } finally {
            cursor.close();
        }

        return -1;
    }

    /**
     * 获取指定目前日期时间的闹钟记录，用于提示用户
     * @return 符合条件的闹钟记录
     */
    public static ArrayList<AlarmRecord> getAlarmRecordsCurrent(int totalMinute, LunarCalendar calendar){
        // 先根据时间查找当前时间前三分钟到目前的记录，并且非暂停记录
        ArrayList<AlarmRecord> selectLst = select("pause<>? and alarm_time=?",
                                    new String[]{"1", String.valueOf(totalMinute)},
                                    "alarm_index DESC", null);
        if (selectLst == null){
            return null;
        }

        // 获取当前时间
        ArrayList<AlarmRecord> tmpLst = new ArrayList<>();
        for (AlarmRecord tmpRecord : selectLst){
            if (tmpRecord.isRecordDate(calendar)) {
                // 是当天的
                tmpLst.add(tmpRecord);
            }
        }

        return tmpLst;
    }
}
