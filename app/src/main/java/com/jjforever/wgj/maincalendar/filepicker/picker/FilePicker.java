package com.jjforever.wgj.maincalendar.filepicker.picker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.jjforever.wgj.maincalendar.R;
import com.jjforever.wgj.maincalendar.common.popup.ConfirmPopup;
import com.jjforever.wgj.maincalendar.common.util.ConvertUtils;
import com.jjforever.wgj.maincalendar.common.util.LogUtils;
import com.jjforever.wgj.maincalendar.filepicker.adapter.FileAdapter;
import com.jjforever.wgj.maincalendar.filepicker.entity.FileItem;
import com.jjforever.wgj.maincalendar.filepicker.util.FileUtils;
import com.jjforever.wgj.maincalendar.filepicker.util.StorageUtils;
import com.jjforever.wgj.maincalendar.filepicker.widget.MarqueeTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * 文件目录选择器
 *
 * @author 李玉江[QQ :1032694760]
 * @version 2015 /9/29
 */
public class FilePicker extends ConfirmPopup<LinearLayout> implements AdapterView.OnItemClickListener {
    /**
     * Directory mode.
     */
    public static final int DIRECTORY = 0;
    /**
     * File mode.
     */
    public static final int FILE = 1;

    // 支持的音乐后缀
    private static final String[] MusicSuffix = new String[]{".ogg", ".mp3"};

    private String initPath;
    private FileAdapter adapter;
    private MarqueeTextView textView;
    private OnFilePickListener onFilePickListener;
    // 铃声播放器
    private MediaPlayer mMediaPlayer;
    private int mode;
    // 选中的文件
    private String mSelectedFile = "";
    // 之前选中的项
    private View mOldView;

    @IntDef(value = {DIRECTORY, FILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    /**
     * Instantiates a new File picker.
     *
     * @param activity the activity
     * @param mode     data mode
     * @see #FILE #FILE#FILE
     * @see #DIRECTORY #DIRECTORY#DIRECTORY
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @RequiresPermission(anyOf = {Manifest.permission.READ_EXTERNAL_STORAGE})
    public FilePicker(Activity activity, @Mode int mode) {
        super(activity);
        if (activity.getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            // 竖屏设置为一半，横屏全屏
            setHalfScreen(true);
        }
        else{
            setFillScreen(true);
        }
        this.initPath = StorageUtils.getRootPath(activity);
        this.mode = mode;
        this.adapter = new FileAdapter(activity);
        adapter.setOnlyListDir(mode == DIRECTORY);
        this.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (FilePicker.this.mode == FILE) {
                    // 退出界面时关闭音乐播放
                    stopPlayMusic();
                }
            }
        });
    }

    @Override
    @NonNull
    protected LinearLayout makeCenterView() {
        LinearLayout rootLayout = new LinearLayout(activity);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        rootLayout.setBackgroundColor(Color.WHITE);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        ListView listView = new ListView(activity);
        listView.setBackgroundColor(Color.WHITE);
        listView.setDivider(new ColorDrawable(0xFFDDDDDD));
        listView.setDividerHeight(1);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setSelector(R.color.itemSelected);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        rootLayout.addView(listView);
        return rootLayout;
    }

    @Nullable
    @Override
    protected View makeFooterView() {
        textView = new MarqueeTextView(activity);
        textView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        textView.setTextColor(Color.BLACK);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        int padding = ConvertUtils.toPx(activity, 10);
        textView.setPadding(padding, padding, padding, padding);
        return textView;
    }

    /**
     * Sets root path.
     *
     * @param initPath the init path
     */
    public void setRootPath(String initPath) {
        this.initPath = initPath;
    }

    /**
     * Sets allow extensions.
     *
     * @param allowExtensions the allow extensions
     */
    public void setAllowExtensions(String[] allowExtensions) {
        adapter.setAllowExtensions(allowExtensions);
    }

    /**
     * 设置只显示铃声文件
     */
    public void setRingExtensions(){
        adapter.setAllowExtensions(MusicSuffix);
    }

    /**
     * Sets show up dir.
     *
     * @param showUpDir the show up dir
     */
    public void setShowUpDir(boolean showUpDir) {
        adapter.setShowUpDir(showUpDir);
    }

    /**
     * Sets show home dir.
     *
     * @param showHomeDir the show home dir
     */
    public void setShowHomeDir(boolean showHomeDir) {
        adapter.setShowHomeDir(showHomeDir);
    }

    /**
     * Sets show hide dir.
     *
     * @param showHideDir the show hide dir
     */
    public void setShowHideDir(boolean showHideDir) {
        adapter.setShowHideDir(showHideDir);
    }

    @Override
    protected void setContentViewAfter(View contentView) {
        refreshCurrentDirPath(initPath);
    }

    @Override
    protected void onSubmit() {
        if (mode == FILE) {
            if (onFilePickListener != null) {
                onFilePickListener.onFilePicked(mSelectedFile);
            }
        } else {
            String currentPath = adapter.getCurrentPath();
            LogUtils.debug("已选择目录：" + currentPath);
            if (onFilePickListener != null) {
                onFilePickListener.onFilePicked(currentPath);
            }
        }
    }

    /**
     * Gets current path.
     *
     * @return the current path
     */
    public String getCurrentPath() {
        return adapter.getCurrentPath();
    }

    /**
     * 响应选择器的列表项点击事件
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        FileItem fileItem = adapter.getItem(position);
        stopPlayMusic();
        mSelectedFile = "";
        if (mOldView != null){
            mOldView.setBackgroundColor(Color.WHITE);
            mOldView = null;
        }
        if (fileItem.isDirectory()) {
            refreshCurrentDirPath(fileItem.getPath());
        } else {
            String clickPath = fileItem.getPath();
            if (mode == DIRECTORY) {
                LogUtils.debug("选择的不是有效的目录: " + clickPath);
            } else {
                //dismiss();
                view.setBackgroundResource(R.color.itemSelected);
                mOldView = view;
                LogUtils.debug("已选择文件：" + clickPath);
                mSelectedFile = clickPath;
                String extension = FileUtils.getExtension(clickPath);
                if (ConvertUtils.toString(MusicSuffix).contains(extension)){
                    // 是支持的音乐文件则进行播放
                    startPlayMusic(clickPath);
                }
            }
        }
    }

    /**
     * 停止播放音乐
     */
    private void stopPlayMusic(){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
    }

    /**
     * 开始播放音乐
     * @param path 音乐绝对路径
     */
    private void startPlayMusic(String path){
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(preparedListener);
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
        }catch (Exception e) {
            LogUtils.error(e.toString());
        }
    }

    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
        }
    };

    private void refreshCurrentDirPath(String currentPath) {
        if (currentPath.equals("/")) {
            textView.setText("根目录");
        } else {
            textView.setText(currentPath);
        }
        adapter.loadData(currentPath);
    }

    /**
     * Sets on file pick listener.
     *
     * @param listener the listener
     */
    public void setOnFilePickListener(OnFilePickListener listener) {
        this.onFilePickListener = listener;
    }

    /**
     * The interface On file pick listener.
     */
    public interface OnFilePickListener {

        /**
         * On file picked.
         *
         * @param currentPath the current path
         */
        void onFilePicked(String currentPath);

    }

}
