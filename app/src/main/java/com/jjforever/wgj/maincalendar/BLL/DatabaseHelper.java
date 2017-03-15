package com.jjforever.wgj.maincalendar.BLL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jjforever.wgj.maincalendar.AppConstants;

/**
 * Created by Wgj on 2016/8/27.
 * 数据库帮助类
 * 本数据库中存储的时间都精确到秒，保存为integer
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    static SQLiteDatabase SQLiteDb;

    // 数据库版本号
    private static final int DATABASE_VERSION = 1;
    // 数据库名
    private static final String DATABASE_NAME = "MainCalendar.db";

    /**
     * 构造函数，调用父类SQLiteOpenHelper的构造函数
      */
//    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
//                          int version, DatabaseErrorHandler errorHandler)
//    {
//        super(context, name, factory, version, errorHandler);
//    }

    /**
     * SQLiteOpenHelper的构造函数参数
     * @param context 上下文环境
     * @param name 数据库名字
     * @param factory 游标工厂（可选）
     * @param version 数据库模型版本号
     */
//    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
//                          int version)
//    {
//        super(context, name, factory, version);
//    }

    /**
     * 数据库帮助类
     * @param context 上下文提供器
     */
    private DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // 数据库实际被创建是在getWritableDatabase()或getReadableDatabase()方法调用时
    }

    // 继承SQLiteOpenHelper类,必须要覆写的三个方法：onCreate(),onUpgrade(),onOpen()

    /**
     * 即便程序修改重新运行，只要数据库已经创建过，就不会再进入这个onCreate方法
     * @param db 数据库操作对象
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // 调用时间：数据库第一次创建时onCreate()方法会被调用

        // onCreate方法有一个 SQLiteDatabase对象作为参数，根据需要对这个对象填充表和初始化数据
        // 这个方法中主要完成创建数据库后对数据库的操作
        AppConstants.DLog("DatabaseHelper onCreate");

        //创建日常记录数据表
        createDailyRecord(db);
        // 创建闹钟记录数据库
        createAlarmRecord(db);
        // 创建轮班记录数据库
        createShiftsWorkRecord(db);
    }

    /**
     * 创建日常记录数据库
     */
    private void createDailyRecord(SQLiteDatabase db){
        // 构建创建表的SQL语句
        // 当都为固定字符串常量时使用String要快速，编译器会进行优化
        String createSQL = "CREATE TABLE [" + DailyRecordMng.TABLE_NAME + "] (";
        createSQL += "[index] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ";
        createSQL += "[record_time] INTEGER,";
        createSQL += "[weather] INTEGER,";
        createSQL += "[title] TEXT,";
        createSQL += "[content] TEXT,";
        createSQL += "[display] INTEGER,";
        createSQL += "[create_time] INTEGER)";

        // 执行创建表的SQL语句
        db.execSQL(createSQL);
    }

    /**
     * 创建闹钟记录数据表
     * @param db 数据库
     */
    private void createAlarmRecord(SQLiteDatabase db){
        String createSQL = "CREATE TABLE [" + AlarmRecordMng.TABLE_NAME + "] (";
        createSQL += "[alarm_index] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ";
        createSQL += "[action_type] INTEGER,";
        createSQL += "[alarm_time] INTEGER,";
        createSQL += "[date_year] INTEGER,";
        createSQL += "[date_month] INTEGER,";
        createSQL += "[date_day] INTEGER,";
        createSQL += "[title] TEXT,";
        createSQL += "[content] TEXT,";
        createSQL += "[display] INTEGER,";
        createSQL += "[pause] INTEGER,";
        createSQL += "[create_time] INTEGER)";

        // 执行创建表的SQL语句
        db.execSQL(createSQL);
    }

    /**
     * 创建轮班记录数据库
     * @param db 数据库
     */
    private void createShiftsWorkRecord(SQLiteDatabase db){
        // 倒班记录表
        String createSQL = "CREATE TABLE [" + ShiftsWorkRecordMng.TABLE_NAME + "] (";
        createSQL += "[work_index] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ";
        createSQL += "[work_title] TEXT,";
        createSQL += "[start_date] INTEGER,";
        createSQL += "[work_period] INTEGER,";
        createSQL += "[create_time] INTEGER)";

        // 执行创建表的SQL语句
        db.execSQL(createSQL);

        // 倒班记录每天设置项表
        createSQL = "CREATE TABLE [" + ShiftsWorkItemMng.TABLE_NAME + "] (";
        createSQL += "[item_index] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ";
        createSQL += "[work_index] INTEGER,";
        createSQL += "[day_no] INTEGER,";
        createSQL += "[item_title] TEXT,";
        createSQL += "[start_time] INTEGER,";
        createSQL += "[end_time] INTEGER)";

        // 执行创建表的SQL语句
        db.execSQL(createSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // 调用时间：如果DATABASE_VERSION值被改为别的数,系统发现现有数据库版本不同,即会调用onUpgrade

        // onUpgrade方法的三个参数，一个 SQLiteDatabase对象，一个旧的版本号和一个新的版本号
        // 这样就可以把一个数据库从旧的模型转变到新的模型
        // 这个方法中主要完成更改数据库版本的操作

        AppConstants.DLog("DatabaseHelper onUpgrade");
        // 删除旧数据库重建数据库，测试时用，正式版不能这么用
        db.execSQL("DROP TABLE IF EXISTS " + DailyRecordMng.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AlarmRecordMng.TABLE_NAME);
        onCreate(db);
        // 上述做法简单来说就是，通过检查常量值来决定如何，升级时删除旧表，然后调用onCreate来创建新表
        // 一般在实际项目中是不能这么做的，正确的做法是在更新数据表结构时，还要考虑用户存放于数据库中的数据不丢失
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        // 每次打开数据库之后首先被执行
        AppConstants.DLog("DatabaseHelper onOpen");
    }

    /**
     * 初始化SQLite数据库
     * @param context 内容提供器
     */
    public static void initDatabase(Context context){
        if (SQLiteDb == null) {
            SQLiteDb = new DatabaseHelper(context).getWritableDatabase();
        }
    }

    /**
     * 判断是否需要初始化数据库
     * @return 是否需要
     */
    public static boolean isNeedInit(){
        return SQLiteDb == null;
    }

    /**
     * 关闭数据库
     */
    public static void closeDatabase(){
        if (SQLiteDb != null){
            SQLiteDb.close();
            SQLiteDb = null;
        }
    }

}
