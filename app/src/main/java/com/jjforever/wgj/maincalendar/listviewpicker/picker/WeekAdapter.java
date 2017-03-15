package com.jjforever.wgj.maincalendar.listviewpicker.picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jjforever.wgj.maincalendar.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Wgj on 2016/9/17.
 * 星期适配器
 */
public class WeekAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> list;
    // 用来控制CheckBox的选中状况
    private HashMap<Integer,Boolean> isSelected = new HashMap<>();

    public WeekAdapter(Context context){
        this.context = context;

        this.list = new ArrayList<>();
        String[] weekdays = new DateFormatSymbols().getWeekdays();
        this.list.add(weekdays[Calendar.SUNDAY]);
        this.list.add(weekdays[Calendar.MONDAY]);
        this.list.add(weekdays[Calendar.TUESDAY]);
        this.list.add(weekdays[Calendar.WEDNESDAY]);
        this.list.add(weekdays[Calendar.THURSDAY]);
        this.list.add(weekdays[Calendar.FRIDAY]);
        this.list.add(weekdays[Calendar.SATURDAY]);

        for(int i = 0; i < list.size(); i++) {
            setIsSelected(i, false);
        }
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
        ViewHolder holder;
        if (convertView == null) {
            // 获得ViewHolder对象
            holder = new ViewHolder();
            // 导入布局并赋值给convertView
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.listview_item_check, parent, false);
            holder.tv = (TextView) convertView.findViewById(R.id.item_tv);
            holder.cb = (CheckBox) convertView.findViewById(R.id.item_cb);
            // 为view设置标签
            convertView.setTag(holder);
        } else {
            // 取出holder
            holder = (ViewHolder) convertView.getTag();
        }

        // 设置list中TextView的显示
        holder.tv.setText(list.get(position));
        // 根据isSelected来设置checkbox的选中状况
        holder.cb.setChecked(getIsSelected(position));
        return convertView;
    }

    public boolean getIsSelected(Integer position){
        return isSelected.get(position);
    }

    public void setIsSelected(Integer position, boolean checked) {
        this.isSelected.put(position, checked);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        TextView tv;
        CheckBox cb;
    }
}
