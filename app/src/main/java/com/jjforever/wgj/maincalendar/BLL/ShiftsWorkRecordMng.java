package com.jjforever.wgj.maincalendar.BLL;

import android.content.ContentValues;
import android.database.Cursor;

import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.Model.KeyValue;
import com.jjforever.wgj.maincalendar.Model.ShiftsWorkItem;
import com.jjforever.wgj.maincalendar.Model.ShiftsWorkRecord;
import com.jjforever.wgj.maincalendar.util.LunarCalendar;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Wgj on 2016/9/22.
 * 轮班记录数据库管理类
 */
public final class ShiftsWorkRecordMng {
    // 管理的表名
    final static String TABLE_NAME = "ShiftsWorkRecord";
    // 当前显示的轮班记录
    private static ShiftsWorkRecord mDisplayWork;

    /**
     * 禁止外部实例化
     */
    private ShiftsWorkRecordMng(){

    }

    /**
     * 载入要显示的轮班记录
     */
    public static void loadDisplayWork(){
        long tmpIndex = GlobalSettingMng.getSetting().getShiftsWorkIndex();
        if (tmpIndex <= 0){
            setDisplayWork(null);
        }
        else{
            setDisplayWork(select(tmpIndex));
        }
    }

    /**
     * 设置当前要显示的轮班记录
     * @param record 轮班记录
     */
    private static void setDisplayWork(ShiftsWorkRecord record){
        mDisplayWork = record;
    }

    /**
     * 获取当前显示的轮班记录
     * @return 轮班记录
     */
    public static ShiftsWorkRecord getDisplayWork(){
        return mDisplayWork;
    }

    /**
     * 根据记录获取ContentValues
     * @param record 轮班记录
     * @param isNew 是否为需要插入的新记录
     * @return ContentValues
     */
    private static ContentValues getValues(ShiftsWorkRecord record, boolean isNew){
        // 实例化常量值
        ContentValues cValue = new ContentValues();
        // 轮班标题
        cValue.put("work_title", record.getTitle());
        // 轮班开始时间
        cValue.put("start_date", record.getStartDate().getTimeInMillis());
        // 轮班周期
        cValue.put("work_period", record.getPeriod());

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
    public static boolean insert(ShiftsWorkRecord record){
        // 采用事务处理，确保数据完整性
        DatabaseHelper.SQLiteDb.beginTransaction();
        try
        {
            // 实例化常量值
            ContentValues cValue = getValues(record, true);
            //调用insert()方法插入数据
            long index = DatabaseHelper.SQLiteDb.insert(TABLE_NAME, null, cValue);
            if (index > 0) {
                // 设置事务成功完成
                for (ShiftsWorkItem tmpItem: record.getItems()) {
                    tmpItem.setWorkIndex(index);
                    if (ShiftsWorkItemMng.insert(tmpItem) <= 0){
                        // 插入失败
                        return false;
                    }
                }
                DatabaseHelper.SQLiteDb.setTransactionSuccessful();
            }
            return index > 0;
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
     * 更新记录
     * @param record 要更新的记录
     * @return 是否更新成功
     */
    public static boolean update(ShiftsWorkRecord record){
        // 采用事务处理，确保数据完整性
        DatabaseHelper.SQLiteDb.beginTransaction();
        try{
            for (ShiftsWorkItem tmpItem : record.getItems()){
                if (tmpItem.getIndex() > 0){
                    if (tmpItem.getFlag() == ShiftsWorkItem.UPDATE) {
                        // 需要更新的
                        if (!ShiftsWorkItemMng.update(tmpItem)) {
                            return false;
                        }
                    }
                    else if (tmpItem.getFlag() == ShiftsWorkItem.DELETE){
                        // 需要删除的
                        if (!ShiftsWorkItemMng.delete(tmpItem.getIndex())){
                            return false;
                        }
                    }
                }
                else{
                    if (tmpItem.getFlag() == ShiftsWorkItem.INSERT) {
                        // 需要添加的
                        if (ShiftsWorkItemMng.insert(tmpItem) <= 0) {
                            return false;
                        }
                    }
                }
            }
            ContentValues cValue = getValues(record, false);
            int tmpCnt = DatabaseHelper.SQLiteDb.update(TABLE_NAME, cValue, "work_index=?",
                    new String[] { String.valueOf(record.getIndex()) });
            if (tmpCnt > 0){
                DatabaseHelper.SQLiteDb.setTransactionSuccessful();
            }
            return tmpCnt > 0;
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
     * 根据索引删除一条日常记录
     * @param index 记录索引
     * @return 是否成功
     */
    public static boolean delete(long index){
        // 采用事务处理，确保数据完整性
        DatabaseHelper.SQLiteDb.beginTransaction();
        try{
            if (!ShiftsWorkItemMng.deleteInWork(index)){
                return false;
            }
            int tmpCnt = DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "work_index=?",
                    new String[] { String.valueOf(index) });

            // 设置事务成功完成
            if (tmpCnt >= 0) {
                DatabaseHelper.SQLiteDb.setTransactionSuccessful();
            }
            return tmpCnt >= 0;
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
                if (!ShiftsWorkItemMng.deleteInWork(index)){
                    return false;
                }
                DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "work_index=?",
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
        sqlStr += String.format(" where work_title='%s'", title);
        if (index > 0){
            sqlStr += String.format(Locale.getDefault(),
                    " and work_index<>'%d'", index);
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
     * 只获取所有的轮班记录索引及标题键值对
     * @return 集合
     */
    public static ArrayList<KeyValue> getAllKeyValue(){
        ArrayList<KeyValue> tmpLst = new ArrayList<>();
        Cursor cursor = DatabaseHelper.SQLiteDb.query(TABLE_NAME, null, null, null, null, null, "work_index DESC", null);
        if (cursor == null){
            return tmpLst;
        }
        // 遍历读取数据
        try {
            while (cursor.moveToNext()) {
                KeyValue tmpValue = new KeyValue();
                tmpValue.Index = cursor.getLong(cursor.getColumnIndex("work_index"));
                tmpValue.Title = cursor.getString(cursor.getColumnIndex("work_title"));
                tmpLst.add(tmpValue);
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
     * 根据条件查询所有的日常记录
     * @param where where条件
     * @param args where条件的参数
     * @param orderBy 排序语句
     * @param limit 限制语句
     * @return 日常记录集合
     */
    private static ArrayList<ShiftsWorkRecord> select(String where, String[] args, String orderBy, String limit){
        ArrayList<ShiftsWorkRecord> tmpLst = new ArrayList<>();

        Cursor cursor = DatabaseHelper.SQLiteDb.query(TABLE_NAME, null, where, args, null, null, orderBy, limit);
        if (cursor == null){
            return tmpLst;
        }

        // 遍历读取数据
        try {
            while (cursor.moveToNext()) {
                ShiftsWorkRecord tmpRecord = new ShiftsWorkRecord(false);
                tmpRecord.setIndex(cursor.getLong(cursor.getColumnIndex("work_index")));
                long tmpMill = cursor.getLong(cursor.getColumnIndex("start_date"));
                LunarCalendar startDate = new LunarCalendar();
                startDate.setTimeInMillis(tmpMill);
                startDate.updateLunar();
                tmpRecord.setStartDate(startDate);
                tmpRecord.setTitle(cursor.getString(cursor.getColumnIndex("work_title")));
                tmpRecord.setPeriod(cursor.getInt(cursor.getColumnIndex("work_period")));
                tmpMill = cursor.getLong(cursor.getColumnIndex("create_time"));
                LunarCalendar createTime = new LunarCalendar();
                createTime.setTimeInMillis(tmpMill);
                createTime.updateLunar();
                tmpRecord.setCreateTime(createTime);
                tmpRecord.setItems(ShiftsWorkItemMng.selectInWork(tmpRecord.getIndex()));

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
    public static ArrayList<ShiftsWorkRecord> selectAll(){
        return select(null, null, "work_index DESC", null);
    }

    /**
     * 根据索引获取指定日常记录
     * @param index 索引
     * @return 指定索引的日常记录
     */
    public static ShiftsWorkRecord select(long index){
        ArrayList<ShiftsWorkRecord> tmpLst = select("work_index=?", new String[]{String.valueOf(index)}, null, null);
        if (tmpLst == null || tmpLst.size() <= 0){
            return null;
        }

        return tmpLst.get(0);
    }
}
