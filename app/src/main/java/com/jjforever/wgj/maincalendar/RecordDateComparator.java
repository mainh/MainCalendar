package com.jjforever.wgj.maincalendar;

import com.jjforever.wgj.maincalendar.Model.ICalendarRecord;

import java.util.Comparator;

/**
 * Created by Wgj on 2016/8/29.
 * 日志记录时间比较器
 */
class RecordDateComparator implements Comparator<ICalendarRecord> {
    @Override
    public int compare(ICalendarRecord lhs, ICalendarRecord rhs) {
        return rhs.getRecordTime().compareTo(lhs.getRecordTime());
    }
}
