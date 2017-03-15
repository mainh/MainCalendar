package com.jjforever.wgj.maincalendar.BLL;

import android.content.ContentValues;
import android.database.Cursor;

import com.jjforever.wgj.maincalendar.AppConstants;
import com.jjforever.wgj.maincalendar.Model.ShiftsWorkItem;

import java.util.ArrayList;

/**
 * Created by Wgj on 2016/9/22.
 * 轮班记录每天设置项
 */
public final class ShiftsWorkItemMng {
    // 管理的表名
    public final static String TABLE_NAME = "ShiftsWorkItem";

    /**
     * 禁止外部实例化
     */
    private ShiftsWorkItemMng(){

    }

    /**
     * 根据记录获取ContentValues
     * @param record 轮班记录
     * @return ContentValues
     */
    private static ContentValues getValues(ShiftsWorkItem record){
        // 实例化常量值
        ContentValues cValue = new ContentValues();
        // 所属轮班索引
        cValue.put("work_index", record.getWorkIndex());
        // 该项是第几天
        cValue.put("day_no", record.getDayNo());
        // 该项上班类型或休息
        cValue.put("item_title", record.getTitle());
        // 该项起始时间
        cValue.put("start_time", record.getStartTime().getTime());
        // 该项结束时间
        cValue.put("end_time", record.getEndTime().getTime());

        return cValue;
    }

    /**
     * 往数据库中添加一条记录
     * @param record 记录
     * @return 是否成功
     */
    public static long insert(ShiftsWorkItem record){
        try
        {
            // 实例化常量值
            ContentValues cValue = getValues(record);
            //调用insert()方法插入数据
            return DatabaseHelper.SQLiteDb.insert(TABLE_NAME, null, cValue);
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return -1;
        }
    }

    /**
     * 更新记录
     * @param record 要更新的记录
     * @return 是否更新成功
     */
    public static boolean update(ShiftsWorkItem record){
        try{
            ContentValues cValue = getValues(record);
            return DatabaseHelper.SQLiteDb.update(TABLE_NAME, cValue, "item_index=?",
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
            return DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "item_index=?",
                    new String[] { String.valueOf(index) }) >= 0;
        }
        catch (Exception ex){
            AppConstants.WLog(ex.toString());
            return false;
        }
    }

    /**
     * 删除指定轮班记录下的设置项
     * @param index 轮班索引
     * @return 是否成功
     */
    public static boolean deleteInWork(long index){
        try{
            return DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "work_index=?",
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
                DatabaseHelper.SQLiteDb.delete(TABLE_NAME, "item_index=?",
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
     * 根据条件查询所有的日常记录
     * @param where where条件
     * @param args where条件的参数
     * @param orderBy 排序语句
     * @param limit 限制语句
     * @return 日常记录集合
     */
    public static ArrayList<ShiftsWorkItem> select(String where, String[] args, String orderBy, String limit){
        ArrayList<ShiftsWorkItem> tmpLst = new ArrayList<>();

        Cursor cursor = DatabaseHelper.SQLiteDb.query(TABLE_NAME, null, where, args, null, null, orderBy, limit);
        if (cursor == null){
            return tmpLst;
        }

        // 遍历读取数据
        try {
            while (cursor.moveToNext()) {
                ShiftsWorkItem tmpRecord = new ShiftsWorkItem();
                tmpRecord.setIndex(cursor.getLong(cursor.getColumnIndex("item_index")));
                tmpRecord.setWorkIndex(cursor.getLong(cursor.getColumnIndex("work_index")));
                tmpRecord.setDayNo(cursor.getInt(cursor.getColumnIndex("day_no")));
                tmpRecord.setTitle(cursor.getString(cursor.getColumnIndex("item_title")));
                tmpRecord.setStartTime(cursor.getInt(cursor.getColumnIndex("start_time")));
                tmpRecord.setEndTime(cursor.getInt(cursor.getColumnIndex("end_time")));

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
     * 获取指定轮班记录下的设置项
     * @param index 轮班记录索引
     * @return 设置项
     */
    public static ArrayList<ShiftsWorkItem> selectInWork(long index){
        return select("work_index=?", new String[]{String.valueOf(index)},
                    "day_no ASC", null);
    }

    /**
     * 根据索引获取指定日常记录
     * @param index 索引
     * @return 指定索引的日常记录
     */
//    public static ShiftsWorkItem select(long index){
//        ArrayList<ShiftsWorkItem> tmpLst = select("item_index=?", new String[]{String.valueOf(index)}, null, null);
//        if (tmpLst == null || tmpLst.size() <= 0){
//            return null;
//        }
//
//        return tmpLst.get(0);
//    }
}
