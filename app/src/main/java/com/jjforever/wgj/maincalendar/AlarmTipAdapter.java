package com.jjforever.wgj.maincalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.Model.AlarmRecord;
import com.jjforever.wgj.maincalendar.monthui.ThemeStyle;
import com.jjforever.wgj.maincalendar.util.Helper;

import java.util.List;

/**
 * Created by Wgj on 2016/10/7.
 * 闹钟提醒适配器
 */
class AlarmTipAdapter extends BaseAdapter {
    private Context context;
    private List<AlarmRecord> list;

    AlarmTipAdapter(Context context, List<AlarmRecord> list) {
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
                    R.layout.item_alarm_notice, parent, false);
            holder.date = (TextView) convertView
                    .findViewById(R.id.alarm_date);
            holder.date.setTextColor(ThemeStyle.Primary);
            holder.content = (TextView) convertView
                    .findViewById(R.id.alarm_content);
            holder.title = (TextView) convertView
                    .findViewById(R.id.alarm_title);
            holder.title.setTextColor(ThemeStyle.Primary);
            convertView.setTag(holder);
        } else {
            holder = (AlarmViewHolder) convertView.getTag();
        }

        AlarmRecord tmpRecord = list.get(position);
        holder.date.setText(tmpRecord.toString());
        holder.title.setText(tmpRecord.getOnlyTitle());
        if (Helper.isNullOrEmpty(tmpRecord.getContent())){
            holder.content.setVisibility(View.GONE);
        }
        else {
            holder.content.setText(tmpRecord.getContent());
            holder.content.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static class AlarmViewHolder {
        TextView title;
        TextView date;
        TextView content;
    }
}
