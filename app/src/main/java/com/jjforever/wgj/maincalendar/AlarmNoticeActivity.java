package com.jjforever.wgj.maincalendar;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.BLL.GlobalSettingMng;
import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.common.util.LogUtils;
import com.jjforever.wgj.maincalendar.util.Helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmNoticeActivity extends Activity {
    //private List<AlarmRecord> mAlarmRecords;
    //private ListView mAlarmListView;
    //private AlarmTipAdapter mAlarmAdapter;
    // 提示栏
    private TextView mTipView;
    // 铃声播放器
    private MediaPlayer mMediaPlayer;
    // 计时秒
    private int mSecond;
    // 振动器
    private Vibrator mVibrator;
    // 当前坐标
    private int mCurrentX;
    // 开始滑动的坐标
    private int mStartX;
    // 定时器
    private Timer mTimer;
    // 闹钟铃声播放器
    private AudioManager mAudioManager;
    // 用于确定界面进入Pause的时间间隔
    private long mOldTime = 0;
    // 强制退出
    private boolean mForceFinished = false;

    // 解锁手机
    private PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        final ArrayList<AlarmRecord> mAlarmRecords = getIntent().getParcelableArrayListExtra(AppConstants.SERVICE_CALL_ACTIVITY);
        if (mAlarmRecords == null){
            // 获取错误
            ForceFinished();
            return;
        }

        setContentView(R.layout.activity_alarm_notice);

        // 滑动到屏幕宽度的三分之一才能解锁
        final int tmpWidth = this.getResources().getDisplayMetrics().widthPixels / 3;
        mTipView = (TextView) findViewById(R.id.alarm_tip);
        mTipView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        mCurrentX = (int) event.getRawX();
                        mStartX = mCurrentX;
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                    {
                        int tmpX = (int) event.getRawX();
                        mTipView.scrollBy(mCurrentX - tmpX , 0);
                        mCurrentX = tmpX;
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    {
                        int endX = (int) event.getRawX();
                        if (Math.abs(endX - mStartX) > tmpWidth){
                            if (GlobalSettingMng.getSetting().getIsNotification()) {
                                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                for (AlarmRecord tmpRecord : mAlarmRecords) {
                                    mNotificationManager.cancel((int)tmpRecord.getIndex());
                                }
                            }
                            ForceFinished();
                        }
                        else{
                            mTipView.scrollBy(mCurrentX - mStartX, 0);
                            mCurrentX = mStartX;
                        }
                        break;
                    }
                }
                return true;
            }
        });

        ListView mAlarmListView = (ListView) findViewById(R.id.alarm_list);
        mAlarmListView.setVerticalScrollBarEnabled(true);
        AlarmTipAdapter mAlarmAdapter = new AlarmTipAdapter(this, mAlarmRecords);
        mAlarmListView.setAdapter(mAlarmAdapter);

        // 定时器为100毫秒一触发
        mSecond = GlobalSettingMng.getSetting().getRingSeconds() * 10;

        // 显示Notification
        if (GlobalSettingMng.getSetting().getIsNotification()) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            for (AlarmRecord tmpRecord : mAlarmRecords) {
                String tmpStr = tmpRecord.getContent();
                if (Helper.isNullOrEmpty(tmpStr)) {
                    tmpStr = tmpRecord.toString();
                }
                Notification mNotification = new NotificationCompat.Builder(this)
                        // 设置小图标
                        .setSmallIcon(R.mipmap.ic_launcher)
                        // 状态栏文本标题
                        .setTicker(tmpRecord.getOnlyTitle())
                        // 设置标题
                        .setContentTitle(tmpRecord.getOnlyTitle())
                        // 设置内容
                        .setContentText(tmpStr)
                        .setAutoCancel(true)
                        .setContentIntent(startEditAlarm(tmpRecord))
                        .build();
                mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
                mNotification.ledARGB = Color.GREEN;
                mNotification.ledOnMS = 700;
                mNotification.ledOffMS = 1000;
                mNotificationManager.notify((int) tmpRecord.getIndex(), mNotification);
            }
        }

        acquireScreenCpuWakeLock();
        StartAlarming();
        mOldTime = Calendar.getInstance().getTimeInMillis();
    }

    /**
     * 强制退出
     */
    private void ForceFinished(){
        this.mForceFinished = true;
        finish();
    }

    /**
     * 状态栏点击记录进入编辑记录状态
     * @param record 要查看的闹钟记录
     */
    private PendingIntent startEditAlarm(AlarmRecord record){
        Intent intent = new Intent(this, AddAlarmActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putParcelable(AppConstants.MAIN_ACTIVITY_CLICK_DATE, record);
        intent.putExtras(mBundle);
        return PendingIntent.getActivity(this, (int)record.getIndex(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * 开始进行闹钟响铃动作
     */
    private void StartAlarming(){
        if (mTimer != null) {
            return;
        }
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null && mVibrator.hasVibrator()) {
            long[] pattern = {700, 1000, 700, 1000};   // 停止 开启 停止 开启
            //重复两次上面的pattern 如果只想震动一次，index设为-1
            mVibrator.vibrate(pattern, 0);
        }

        // 定时任务
        if (mTimer == null) {
            mTimer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (mSecond-- < 0) {
                        ForceFinished();
                    }
                }
            };
            mTimer.schedule(timerTask, 0, 100);
        }

        String ringPath = GlobalSettingMng.getSetting().getRingPath();
        if (!Helper.isNullOrEmpty(ringPath)) {
            startPlayMusic(ringPath);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        AppConstants.DLog("on resume...");
        StartAlarming();
    }

    /**
     * 停止播放音乐
     */
    private void stopPlayMusic(){
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer.release();
            if (this.mAudioManager != null) {
                this.mAudioManager.abandonAudioFocus(this.mAudioFocusChangeListener);
                this.mAudioManager = null;
            }
            mMediaPlayer = null;
        }
    }

    /**
     * 开始播放音乐
     * @param path 音乐绝对路径
     */
    private void startPlayMusic(String path){
        try {
            // 先停止如果可以的话
            stopPlayMusic();
            File tmpFile = new File(path);
            if (!tmpFile.exists()){
                return;
            }

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    AppConstants.ELog("Error occurred while playing audio. Stopping AlarmKlaxon.");
                    stopPlayMusic();
                    return true;
                }
            });

            // 设置为闹钟铃声
            if (mAudioManager == null) {
                mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            }
            mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                    AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            if (mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setOnPreparedListener(preparedListener);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepareAsync();
            }
        }catch (Exception e) {
            LogUtils.error(e.toString());
        }
    }

    private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int paramAnonymousInt) {
            switch (paramAnonymousInt) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    stopPlayMusic();

                default:
                    break;
            }
        }
    };

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }
        }
    };

    /**
     * 屏蔽按键 然而并没什么用。。。
     * @param keyEvent 按键事件
     * @return 是否已经处理过按键
     */
    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_SYM:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_STAR:
                return true;
        }

        return super.dispatchKeyEvent(keyEvent);
    }

    @Override
    public void onPause(){
        super.onPause();
        AppConstants.DLog("Enter in pause...");
        if (!mForceFinished) {
            if (mOldTime == 0) {
                return;
            }
            long curTime = Calendar.getInstance().getTimeInMillis();
            AppConstants.DLog("time span is " + (curTime - mOldTime));
            if (curTime - mOldTime < 1500) {
                // 2S内的数据算无效
                mOldTime = curTime;
            }
            else{
                this.ForceFinished();
            }
            return;
        }
        finish();
    }

    /**
     * 获取解锁权
     */
    private void acquireScreenCpuWakeLock() {
        if (mWakeLock != null) {
            return;
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE,
                AppConstants.LOG_TAG);
        mWakeLock.acquire();
    }

    /**
     * 释放解锁权
     */
    private void releaseCpuLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public void finish(){
        super.finish();

        releaseCpuLock();
        stopPlayMusic();
        if (mVibrator != null){
            mVibrator.cancel();
        }
        if (mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
}
