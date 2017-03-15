package com.jjforever.wgj.maincalendar;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jjforever.wgj.maincalendar.BLL.AlarmRecordMng;
import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.monthui.SwitchButton;
import com.jjforever.wgj.maincalendar.services.CalendarService;

import java.util.List;

/**
 * Created by Wgj on 2016/9/19.
 * 闹钟记录适配器
 */
public class AlarmAdapter extends BaseAdapter {
    private Context context;
    private List<AlarmRecord> list;

    public AlarmAdapter(Context context, List<AlarmRecord> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        if (list == null) {
            return null;
        }
        return list.get(position);
    }

    /**
     * 删除指定位置的条目
     * @param position 指定的位置
     * @return 删除成功与否
     */
//    public boolean removeItem(int position){
//        if (list == null){
//            return false;
//        }
//        if (position < 0 || position >= list.size()){
//            return false;
//        }
//
//        return list.remove(position) != null;
//    }

    /**
     * 在指定位置更新记录信息
     * @param position 指定位置
     * @param record 记录信息
     * @return 是否成功
     */
//    public boolean setItem(int position, AlarmRecord record){
//        if (list == null){
//            return false;
//        }
//
//        return list.set(position, record) != null;
//    }

    /**
     * 获取Item的索引
     * @param position 位置
     * @return 索引
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlarmViewHolder holder;
        if (convertView == null) {
            holder = new AlarmViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.alarm_record_item, parent, false);
            holder.title = (TextView) convertView
                    .findViewById(R.id.record_date_time);
            holder.content = (TextView) convertView
                    .findViewById(R.id.txt_date_content);
            holder.button = (ToggleButton) convertView.findViewById(R.id.pause_button);
            holder.layout = (LinearLayout) convertView
                    .findViewById(R.id.record_title);
            convertView.setTag(holder);
        } else {
            holder = (AlarmViewHolder) convertView.getTag();
        }

        // 根据位置获取闹钟记录
        final AlarmRecord tmpRecord = list.get(position);
        holder.title.setText(String.format("%s %s", tmpRecord.getDateString(), tmpRecord.getAlarmTime().toString()));
        holder.content.setText(list.get(position).getOnlyTitle());
        holder.button.setChecked(!tmpRecord.getPause());

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((ToggleButton)v).isChecked();
                if (isChecked != tmpRecord.getPause()){
                    return;
                }
                tmpRecord.setPause(!isChecked);
                if (AlarmRecordMng.update(tmpRecord)){
                    context.startService(new Intent(context, CalendarService.class));
                    Toast.makeText(context,
                            tmpRecord.getOnlyTitle() + context.getResources().getString(isChecked ? R.string.no_pause : R.string.is_pause),
                            Toast.LENGTH_SHORT).show();
                }
                else{
                    tmpRecord.setPause(isChecked);
                }
            }
        });

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static class AlarmViewHolder {
        LinearLayout layout;
        ToggleButton button;
        TextView title;
        TextView content;
    }
}
