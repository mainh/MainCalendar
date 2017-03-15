package com.jjforever.wgj.maincalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.Model.ICalendarRecord;

import java.util.List;

/**
 * Created by Wgj on 2016/8/29.
 * 记录适配器
 */
class RecordAdapter extends BaseAdapter {
    private Context context;
    private List<ICalendarRecord> list;
    // 是否显示日期，否则只显示时间
    private boolean mShowDate;

    RecordAdapter(Context context, boolean showDate, List<ICalendarRecord> list) {
        this.context = context;
        this.list = list;
        mShowDate = showDate;
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
    boolean removeItem(int position){
        if (list == null){
            return false;
        }
        if (position < 0 || position >= list.size()){
            return false;
        }

        return list.remove(position) != null;
    }

    /**
     * 在指定位置更新记录信息
     * @param position 指定位置
     * @param record 记录信息
     * @return 是否成功
     */
    boolean setItem(int position, ICalendarRecord record){
        if (list == null){
            return false;
        }

        return list.set(position, record) != null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_time_line, parent, false);
            holder.date = (TextView) convertView
                    .findViewById(R.id.record_date_time);
            holder.content = (TextView) convertView
                    .findViewById(R.id.txt_date_content);
            holder.line = convertView.findViewById(R.id.record_line);
            holder.title = (RelativeLayout) convertView
                    .findViewById(R.id.record_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //时间轴竖线的layout
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.line.getLayoutParams();
        //第一条数据，肯定显示时间标题
        if (position == 0) {
            holder.title.setVisibility(View.VISIBLE);
            holder.date.setText(
                    list.get(position).getRecordTime().getTimeLineString(mShowDate));
            params.addRule(RelativeLayout.ALIGN_TOP, R.id.record_title);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.txt_date_content);
        } else { // 不是第一条数据
            // 本条数据和上一条数据的时间戳相同，时间标题不显示
            if (list.get(position).getRecordTime()
                    .equals(list.get(position - 1).getRecordTime())) {
                holder.title.setVisibility(View.GONE);
                params.addRule(RelativeLayout.ALIGN_TOP, R.id.txt_date_content);
                params.addRule(RelativeLayout.ALIGN_BOTTOM,
                        R.id.txt_date_content);
            } else {
                //本条数据和上一条的数据的时间戳不同的时候，显示数据
                holder.title.setVisibility(View.VISIBLE);
                holder.date.setText(
                        list.get(position).getRecordTime().getTimeLineString(mShowDate));
                params.addRule(RelativeLayout.ALIGN_TOP, R.id.record_title);
                params.addRule(RelativeLayout.ALIGN_BOTTOM,
                        R.id.txt_date_content);
            }
        }
        holder.line.setLayoutParams(params);
        holder.content.setText(list.get(position).getTitle());
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static class ViewHolder {
        RelativeLayout title;
        View line;
        TextView date;
        TextView content;
    }
}
