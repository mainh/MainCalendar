package com.jjforever.wgj.maincalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.Model.ShiftsWorkRecord;

import java.util.List;
import java.util.Locale;

/**
 * Created by Wgj on 2016/9/28.
 * 倒班记录列表适配器
 */
public class ShiftsWorkAdapter extends BaseAdapter {
    private Context context;
    private List<ShiftsWorkRecord> list;

    public ShiftsWorkAdapter(Context context, List<ShiftsWorkRecord> list) {
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
    public boolean removeItem(int position){
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
    public boolean setItem(int position, ShiftsWorkRecord record){
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
        WorkViewHolder holder;
        if (convertView == null) {
            holder = new WorkViewHolder();
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.item_time_line, parent, false);
            holder.title = (TextView) convertView
                    .findViewById(R.id.record_date_time);
            holder.content = (TextView) convertView
                    .findViewById(R.id.txt_date_content);
            holder.line = convertView.findViewById(R.id.record_line);
            holder.layout = (RelativeLayout) convertView
                    .findViewById(R.id.record_title);
            convertView.setTag(holder);
        } else {
            holder = (WorkViewHolder) convertView.getTag();
        }
        //时间轴竖线的layout
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.line.getLayoutParams();
        //第一条数据，肯定显示时间标题
        ShiftsWorkRecord tmpRecord = list.get(position);
        holder.layout.setVisibility(View.VISIBLE);
        holder.title.setText(tmpRecord.getStartDate().toShortString());
        params.addRule(RelativeLayout.ALIGN_TOP, R.id.record_title);
        params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.txt_date_content);
        holder.line.setLayoutParams(params);
        holder.content.setText(String.format(Locale.getDefault(), "%s [周期%d天]",
                                tmpRecord.getTitle(), tmpRecord.getPeriod()));
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class WorkViewHolder {
        RelativeLayout layout;
        View line;
        TextView title;
        TextView content;
    }
}
