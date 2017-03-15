package com.jjforever.wgj.maincalendar.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by Wgj on 2016/8/6.
 * 农历类，获取农历日期工具类
 */
public class LunarCalendar extends GregorianCalendar{

    private static final long serialVersionUID = 8L;

    /** 农历年属性 */
    public static final int LUNAR_YEAR = 801;
    /** 农历月属性 */
    public static final int LUNAR_MONTH = 802;
    /** 农历日属性 */
    public static final int LUNAR_DATE = 803;
    /** 当月的节气对应的公历日(前一个节气)属性 */
    static final int LUNAR_SECTIONAL_TERM = 804;
    /** 当月的中气对应的公历日(后一个节气)属性 */
    static final int LUNAR_PRINCIPLE_TERM = 805;
    /** 天干属性 */
    static final int LUNAR_HEAVENLY_STEM = 806;
    /** 地支属性 */
    static final int LUNAR_EARTHLY_BRANCH = 807;
    /** 农历年的属相(生肖)属性 */
    public static final int LUNAR_ANIMAL = 808;

    // 阴历年
    private int lunarYear;
    // 阴历月 1起始，负数表示闰月
    private int lunarMonth;
    // 阴历日
    private int lunarDate;
    // 当月节气的公历日
    private int sectionalTerm;
    // 当月中气的公历日
    private int principleTerm;
    // 农历日期是否已经经过计算确认
    private boolean areLunarFieldsComputed;
    // 节气是否已经经过计算确认
    private boolean areSolarTermsComputed;
    // 最后设置的是不是农历属性
    private boolean lastSetLunar;

    @IntDef(value = {YEAR, MONTH, DAY_OF_MONTH, DAY_OF_WEEK,
    DAY_OF_WEEK_IN_MONTH, DAY_OF_YEAR, HOUR, HOUR_OF_DAY, MINUTE, SECOND,
    LUNAR_ANIMAL, LUNAR_DATE, LUNAR_EARTHLY_BRANCH, LUNAR_HEAVENLY_STEM,
    LUNAR_YEAR, LUNAR_MONTH, LUNAR_PRINCIPLE_TERM, LUNAR_SECTIONAL_TERM,
    MILLISECOND})
    @Retention(RetentionPolicy.SOURCE)
    @interface Field {
    }

    /** 使用当前时间构造一个实例。 */
    public LunarCalendar() {
        super();
    }

    /** 使用指定时间构造一个实例。 */
    public LunarCalendar(Date d) {
        super.setTime(d);
    }

    /** 使用指定时间构造一个实例。 */
    public LunarCalendar(Calendar c) {
        this(c.getTime());
    }

    /** 使用指定公历日期构造一个实例。 */
    public LunarCalendar(int y, int m, int d) {
        super(y, m, d);
    }

    /**
     * 使用指定日期构造一个农历日期。
     * @param y 农历年
     * @param m 农历月
     * @param d 农历日
     * @param leap 是否为闰月
     */
    public LunarCalendar(int y, int m, int d, boolean leap) {
        this.set(LUNAR_YEAR, y);
        this.set(LUNAR_MONTH, leap ? -m : m);
        this.set(LUNAR_DATE, d);
    }

    /**
     * 设置指定日期属性的值，可以为阴历跟阳历
     * @param field 字段 可为Calendar中定义字段，也可以为本类中定义的字段
     * @param value 要设置的值
     */
    @Override
    public void set(@Field int field, int value) {
        computeIfNeed(field);

        if (isLunarField(field)) {
            // 农历属性
            switch (field) {
                case LUNAR_YEAR:
                    lunarYear = value;
                    break;

                case LUNAR_MONTH:
                    lunarMonth = value;
                    break;

                case LUNAR_DATE:
                    lunarDate = value;
                    break;

                default:
                    throw new IllegalArgumentException("Not supported field: " + field);
            }

            lastSetLunar = true;
        } else {
            // 非农历属性
            super.set(field, value);
            lastSetLunar = false;
        }

        areFieldsSet = false;
        areLunarFieldsComputed = false;
        areSolarTermsComputed = false;
    }

    /**
     * 获取指定日期属性的值，可以为阴历跟阳历
     * @param field 字段 可为Calendar中定义字段，也可以为本类中定义的字段
     * @return 获取的值
     */
    @Override
    public int get(@Field int field) {
        computeIfNeed(field);

        if (!isLunarField(field)) {
            return super.get(field);
        }

        if (!areLunarFieldsComputed) {
            // 计算农历属性
            computeLunarFields();
            areLunarFieldsComputed = true;
        }

        switch (field) {
            case LUNAR_YEAR:
                return lunarYear;

            case LUNAR_MONTH:
                return lunarMonth;

            case LUNAR_DATE:
                return lunarDate;

            case LUNAR_SECTIONAL_TERM:
                if (!areSolarTermsComputed) {
                    computeSolarTerms();
                    areSolarTermsComputed = true;
                }
                return sectionalTerm;

            case LUNAR_PRINCIPLE_TERM:
                if (!areSolarTermsComputed) {
                    computeSolarTerms();
                    areSolarTermsComputed = true;
                }
                return principleTerm;

            case LUNAR_HEAVENLY_STEM:
                return (lunarYear - 4) % 10 + 1;

            case LUNAR_EARTHLY_BRANCH:
            case LUNAR_ANIMAL:
                return (lunarYear - 4) % 12 + 1;

            default:
                throw new IllegalArgumentException("Not supported field: " + field);
        }
    }

    /**
     * 日期指定属性增加指定值
     * @param field 字段 可为Calendar中定义字段，也可以为本类中定义的字段
     * @param amount 要增加的值
     */
    public void add(@Field int field, int amount) {
        computeIfNeed(field);

        if (!isLunarField(field)) {
            super.add(field, amount);

            lastSetLunar = false;
            areLunarFieldsComputed = false;
            areSolarTermsComputed = false;
            return;
        }

        switch (field) {
            case LUNAR_YEAR:
                lunarYear += amount;
                break;

            case LUNAR_MONTH:
                for (int i = 0; i < amount; i++) {
                    lunarMonth = nextLunarMonth(lunarYear, lunarMonth);
                    if (lunarMonth == 1) {
                        lunarYear++;
                    }
                }
                break;

            case LUNAR_DATE:
                int maxDate = daysInLunarMonth(lunarYear, lunarMonth);
                for (int i = 0; i < amount; i++) {
                    lunarDate++;
                    if (lunarDate > maxDate) {
                        lunarDate = 1;
                        lunarMonth = nextLunarMonth(lunarYear, lunarMonth);
                        if (lunarMonth == 1) {
                            lunarYear++;
                        }

                        maxDate = daysInLunarMonth(lunarYear, lunarMonth);
                    }
                }

            default:
                throw new IllegalArgumentException("Not supported field: " + field);
        }

        lastSetLunar = true;
        areFieldsSet = false;
        areSolarTermsComputed = false;
    }

    public void roll(@Field int field, int amount) {
        computeIfNeed(field);

        if (!isLunarField(field)) {
            super.roll(field, amount);
            lastSetLunar = false;
            areLunarFieldsComputed = false;
            areSolarTermsComputed = false;
            return;
        }

        switch (field) {
            case LUNAR_YEAR:
                lunarYear += amount;
                break;

            case LUNAR_MONTH:
                for (int i = 0; i < amount; i++) {
                    lunarMonth = nextLunarMonth(lunarYear, lunarMonth);
                }
                break;

            case LUNAR_DATE:
                int maxDate = daysInLunarMonth(lunarYear, lunarMonth);
                for (int i = 0; i < amount; i++) {
                    lunarDate++;
                    if (lunarDate > maxDate) {
                        lunarDate = 1;
                    }
                }

            default:
                throw new IllegalArgumentException("Not supported field: " + field);
        }

        lastSetLunar = true;
        areFieldsSet = false;
        areSolarTermsComputed = false;
    }

    /**
     * 获得属性的中文，可以使用的属性字段为DAY_OF_WEEK以及所有农历属性字段。
     * @param field 本类中定义的字段
     * @return 农历日期中文字符串
     */
    public String getLunar(@Field int field) {
        switch (field) {
            case LUNAR_YEAR:
                return getLunar(LUNAR_HEAVENLY_STEM)
                        + getLunar(LUNAR_EARTHLY_BRANCH);

            case LUNAR_MONTH:
                return getLunarMonth(lunarMonth);

            case LUNAR_DATE:
                return lunarDateNames[lunarDate];

            case LUNAR_SECTIONAL_TERM:
                return SolarTerm.getSectionalTermName(get(Calendar.MONTH));

            case LUNAR_PRINCIPLE_TERM:
                return SolarTerm.getPrincipleTermName(get(Calendar.MONTH));

            case LUNAR_HEAVENLY_STEM:
//                if (!areSolarTermsComputed) {
//                    computeSolarTerms();
//                    areSolarTermsComputed = true;
//                }
                return stemNames[get(field)];

            case LUNAR_EARTHLY_BRANCH:
//                if (!areSolarTermsComputed) {
//                    computeSolarTerms();
//                    areSolarTermsComputed = true;
//                }
                return branchNames[get(field)];

            case LUNAR_ANIMAL:
                return animalNames[get(field)];

            case Calendar.DAY_OF_WEEK:
                return lunarWeekNames[get(field)];

            default:
                throw new IllegalArgumentException("Not supported field: " + field);
        }
    }

    /**
     * 获取阳历的数字日期 20XX-XX-XX
     * @return 20XX-XX-XX样式的日期描述
     */
    public String toShortString() {
        return String.format(Locale.getDefault(), "%d-%02d-%02d", get(YEAR),
                get(MONTH) + 1, get(DAY_OF_MONTH));
    }

    /**
     * 时间线中用到的时间日期字符串
     * @param showDate 是否显示日期部分
     * @return 日期时间
     */
    public String getTimeLineString(boolean showDate){
        String tmpStr = "";
        if (showDate) {
            tmpStr = String.format(Locale.getDefault(), "%d-%02d-%02d ",
                    get(YEAR), get(MONTH) + 1, get(DAY_OF_MONTH));
        }
        return String.format(Locale.getDefault(), "%s%02d:%02d",
                    tmpStr, get(HOUR_OF_DAY), get(MINUTE));
    }

    /**
     * 获取农历的数字日期 XXXX-XX-XX
     * @return 20XX-XX-XX样式的日期描述
     */
//    public String getSimpleLunarDateString() {
//        return String.format(Locale.getDefault(), "%d-%s-%d",
//                get(LUNAR_YEAR), get(LUNAR_MONTH) > 0 ? ""
//                        + get(LUNAR_MONTH) : "*"
//                        + (-get(LUNAR_MONTH)),
//                get(LUNAR_DATE));
//    }

    /**
     * 获取农历日期天干地支描述
     * @return 天干地支日期描述
     */
    public String getLunarDateString() {
        return new StringBuilder().append(getLunar(LUNAR_YEAR))
                .append(getLunar(LUNAR_ANIMAL))
                .append("年")
                .append(getLunar(LUNAR_MONTH))
                .append("月")
                .append(getLunar(LUNAR_DATE))
                .toString();
    }

    /**
     * 获取农历年及月信息
     * @return 农历年月信息
     */
//    public String getLunarMonthString(){
//        return new StringBuilder().append(getLunar(LUNAR_YEAR))
//                .append(getLunar(LUNAR_ANIMAL))
//                .append("年")
//                .append(getLunar(LUNAR_MONTH))
//                .append("月")
//                .toString();
//    }

    /**
     * 输出公历及农历详细字符串
     * @return 公历及农历详细字符串
     */
    public String toString() {
        return new StringBuilder().append(toShortString())
                .append(" | ")
                .append(getLunar(DAY_OF_WEEK))
                .append(" | [农历]")
                .append(getLunarDateString()).toString();
    }

    /**
     * 输出20XX年XX月XX日 XX:XX
     * @return 用于记录的日期字符串
     */
    public String toRecordTime()
    {
        computeIfNeed(LUNAR_MONTH);

        return String.format(Locale.getDefault(), "%4d年%d月%d日 %02d:%02d\r\n%s月%s %s",
                get(YEAR), get(MONTH) + 1, get(DAY_OF_MONTH),
                get(HOUR_OF_DAY), get(MINUTE),
                getLunar(LUNAR_MONTH), getLunar(LUNAR_DATE), getLunar(DAY_OF_WEEK));
    }

    /**
     * 判断是不是农历属性
     * @param field 要判断的字段
     * @return 是否为农历属性字段
     */
    private boolean isLunarField(int field) {
        switch (field) {
            case LUNAR_YEAR:
            case LUNAR_MONTH:
            case LUNAR_DATE:
            case LUNAR_SECTIONAL_TERM:
            case LUNAR_PRINCIPLE_TERM:
            case LUNAR_HEAVENLY_STEM:
            case LUNAR_EARTHLY_BRANCH:
            case LUNAR_ANIMAL:
                return true;

            default:
                return false;
        }
    }

    /**
     * 更新农历信息
     */
    public void updateLunar(){
        computeIfNeed(LUNAR_DATE);
    }

    /**
     * 如果上一次设置的与这次将要设置或获取的属性不是同一类（农历/公历），
     * 例如上一次设置的是农历而现在要设置或获取公历，
     * 则需要先根据之前设置的农历日期计算出公历日期。
     * @param field 根据字段判断是否需要进行阴阳历转换
     */
    private void computeIfNeed(int field) {
        if (isLunarField(field)) {
            if (!lastSetLunar && !areLunarFieldsComputed) {
                super.complete();
                computeLunarFields();
                areFieldsSet = true;
                areLunarFieldsComputed = true;
                areSolarTermsComputed = false;
            }
        } else {
            if (lastSetLunar && !areFieldsSet) {
                computeGregorianFields();
                super.complete();
                areFieldsSet = true;
                areLunarFieldsComputed = true;
                areSolarTermsComputed = false;
            }
        }
    }

    /**
     * 使用农历日期计算出公历日期
     */
    private void computeGregorianFields() {
        int y = lunarYear;
        int m = lunarMonth;
        int d = lunarDate;

        areLunarFieldsComputed = true;
        areFieldsSet = true;
        lastSetLunar = false;

        // 调整日期范围
        if (y < 1900) {
            y = 1899;
        }
        else if (y > 2100) {
            y = 2101;
        }
        
        if (m < -12) {
            m = -12;
        }
        else if (m > 12) {
            m = 12;
        }

        if (d < 1) {
            d = 1;
        }
        else if (d > 30) {
            d = 30;
        }

        int dateInt = y * 10000 + Math.abs(m) * 100 + d;
        if (dateInt < 19001111) {
            // 太小
            set(1901, Calendar.JANUARY, 1);
            super.complete();
        } else if (dateInt > 21001201) {
            // 太大
            set(2100, Calendar.DECEMBER, 31);
            super.complete();
        } else {
            if (Math.abs(m) > 12) {
                m = 12;
            }

            int days = LunarCalendar.daysInLunarMonth(y, m);
            if (days == 0) {
                m = -m;
                days = LunarCalendar.daysInLunarMonth(y, m);
            }

            if (d > days) {
                d = days;
            }

            set(y, Math.abs(m) - 1, d);
            computeLunarFields();

            int amount = 0;
            while (lunarYear != y || lunarMonth != m) {
                amount += daysInLunarMonth(lunarYear, lunarMonth);
                lunarMonth = nextLunarMonth(lunarYear, lunarMonth);
            }

            amount += d - lunarDate;
            super.add(Calendar.DATE, amount);
        }

        computeLunarFields();
    }

    /**
     * 使用公历日期计算出农历日期
     */
    private void computeLunarFields() {
        int gregorianYear = internalGet(Calendar.YEAR);
        int gregorianMonth = internalGet(Calendar.MONTH) + 1;
        int gregorianDate = internalGet(Calendar.DATE);
        if (gregorianYear < 1901 || gregorianYear > 2100) {
            return;
        }

        int startYear = baseYear;
        int startMonth = baseMonth;
        int startDate = baseDate;

        lunarYear = baseLunarYear;
        lunarMonth = baseLunarMonth;
        lunarDate = baseLunarDate;

        // 第二个对应日，用以提高计算效率
        // 公历 2000 年 1 月 1 日，对应农历 4697 年 11 月 25 日
        if (gregorianYear >= 2000) {
            startYear = baseYear + 99;
            startMonth = 1;
            startDate = 1;

            lunarYear = baseLunarYear + 99;
            lunarMonth = 11;
            lunarDate = 25;
        }

        int daysDiff = 0;
        for (int i = startYear; i < gregorianYear; i++) {
            daysDiff += 365;
            if (DateUtil.isLeapYear(i)) {
                daysDiff += 1; // leap year
            }
        }

        for (int i = startMonth; i < gregorianMonth; i++) {
            daysDiff += DateUtil.getMonthDays(gregorianYear, i - 1);
        }

        daysDiff += gregorianDate - startDate;
        lunarDate += daysDiff;

        int lastDate = daysInLunarMonth(lunarYear, lunarMonth);
        int nextMonth = nextLunarMonth(lunarYear, lunarMonth);
        while (lunarDate > lastDate) {
            if (Math.abs(nextMonth) < Math.abs(lunarMonth)) {
                lunarYear++;
            }

            lunarMonth = nextMonth;
            lunarDate -= lastDate;

            lastDate = daysInLunarMonth(lunarYear, lunarMonth);
            nextMonth = nextLunarMonth(lunarYear, lunarMonth);
        }
    }

    /**
     * 计算节气
     */
    private void computeSolarTerms() {
        int gregorianYear = internalGet(Calendar.YEAR);
        int gregorianMonth = internalGet(Calendar.MONTH);
        if (gregorianYear < 1901 || gregorianYear > 2100) {
            return;
        }

        sectionalTerm = SolarTerm.sectionalTerm(gregorianYear, gregorianMonth);
        principleTerm = SolarTerm.principleTerm(gregorianYear, gregorianMonth);
    }

    /**
     * 计算农历年月的天数
     * @param y 农历年
     * @param m 农历月
     * @return 天数
     */
    static int daysInLunarMonth(int y, int m) {
        // 注意：闰月 m < 0
        int index = y - baseLunarYear + baseIndex;
        int v, l;
        int d = 30;

        if (1 <= m && m <= 8) {
            v = lunarMonths[2 * index];
            l = m - 1;
            if (((v >> l) & 0x01) == 1) {
                d = 29;
            }
        } else if (9 <= m && m <= 12) {
            v = lunarMonths[2 * index + 1];
            l = m - 9;
            if (((v >> l) & 0x01) == 1) {
                d = 29;
            }
        } else {
            v = lunarMonths[2 * index + 1];
            v = (v >> 4) & 0x0F;
            if (v != Math.abs(m)) {
                d = 0;
            } else {
                d = 29;
                for (int tmpIndex : bigLeapMonthYears) {
                    if (tmpIndex == index) {
                        d = 30;
                        break;
                    }
                }
            }
        }

        return d;
    }

    /**
     * 计算农历的下个月
     * @param y 农历年
     * @param m 农历月
     * @return 下个月是农历几月
     */
    private static int nextLunarMonth(int y, int m) {
        int n = Math.abs(m) + 1;
        if (m > 0) {
            int index = y - baseLunarYear + baseIndex;
            int v = lunarMonths[2 * index + 1];
            v = (v >> 4) & 0x0F;
            if (v == m) {
                n = -m;
            }
        }

        if (n == 13) {
            n = 1;
        }

        return n;
    }

    /**
     * 根据星期的位组合返回星期字符串集合
     * @param week 星期
     * @return 星期字符串集合
     */
    public static String getWeeksStr(int week){
        StringBuilder tmpSB = new StringBuilder();
        for (int i = 0; i < 7; i++){
            if ((week & (1 << i)) != 0){
                tmpSB.append(lunarWeekNames[i + 1]).append(",");
            }
        }
        if (tmpSB.length() <= 0){
            return null;
        }
        tmpSB.delete(tmpSB.length() - 1, tmpSB.length());

        return tmpSB.toString();
    }

    /**
     * 获取农历月份描述
     * @param month 农历月份
     * @return 月份描述
     */
    public static String getLunarMonth(int month){
        if (month > 0) {
            return lunarMonthNames[month];
        }
        else {
            return "闰" + lunarMonthNames[-month];
        }
    }

    /**
     * 获取农历日描述
     * @param day 农历日
     * @return 农历日描述
     */
    public static String getLunarDay(int day){
        return lunarDateNames[day];
    }

	// 日历第一天的日期
    private static final int baseYear = 1901;
    private static final int baseMonth = 1;
    private static final int baseDate = 1;
    private static final int baseIndex = 0;
    // 农历的第一天日期
    private static final int baseLunarYear = 1900;
    private static final int baseLunarMonth = 11;
    private static final int baseLunarDate = 11;

	// 中文字符串
    private static final String[] lunarWeekNames = { "", "周日", "周一", "周二",
                                                    "周三", "周四", "周五", "周六" };

    // 农历月份表述集合
    public static final String[] lunarMonthNames = { "", "正", "二", "三", "四",
                                                    "五", "六", "七", "八", "九", "十", "冬", "腊" };

    // 农历日期集合
    public static final String[] lunarDateNames = { "", "初一", "初二", "初三",
            "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二", "十三", "十四",
            "十五", "十六", "十七", "十八", "十九", "二十", "廿一", "廿二", "廿三", "廿四", "廿五",
            "廿六", "廿七", "廿八", "廿九", "三十" };

    private static final String[] stemNames = { "", "甲", "乙", "丙", "丁", "戊",
            "己", "庚", "辛", "壬", "癸" };

    private static final String[] branchNames = { "", "子", "丑", "寅", "卯", "辰",
            "巳", "午", "未", "申", "酉", "戌", "亥" };

    private static final String[] animalNames = { "", "鼠", "牛", "虎", "兔", "龙",
            "蛇", "马", "羊", "猴", "鸡", "狗", "猪" };

    // 接下来是数据表
    private static final int[] bigLeapMonthYears = { 6, 14, 19, 25, 33, 36, 38,
            41, 44, 52, 55, 79, 117, 136, 147, 150, 155, 158, 185, 193 };

    private static final char[] lunarMonths = { 0x00, 0x04, 0xad, 0x08, 0x5a,
            0x01, 0xd5, 0x54, 0xb4, 0x09, 0x64, 0x05, 0x59, 0x45, 0x95, 0x0a,
            0xa6, 0x04, 0x55, 0x24, 0xad, 0x08, 0x5a, 0x62, 0xda, 0x04, 0xb4,
            0x05, 0xb4, 0x55, 0x52, 0x0d, 0x94, 0x0a, 0x4a, 0x2a, 0x56, 0x02,
            0x6d, 0x71, 0x6d, 0x01, 0xda, 0x02, 0xd2, 0x52, 0xa9, 0x05, 0x49,
            0x0d, 0x2a, 0x45, 0x2b, 0x09, 0x56, 0x01, 0xb5, 0x20, 0x6d, 0x01,
            0x59, 0x69, 0xd4, 0x0a, 0xa8, 0x05, 0xa9, 0x56, 0xa5, 0x04, 0x2b,
            0x09, 0x9e, 0x38, 0xb6, 0x08, 0xec, 0x74, 0x6c, 0x05, 0xd4, 0x0a,
            0xe4, 0x6a, 0x52, 0x05, 0x95, 0x0a, 0x5a, 0x42, 0x5b, 0x04, 0xb6,
            0x04, 0xb4, 0x22, 0x6a, 0x05, 0x52, 0x75, 0xc9, 0x0a, 0x52, 0x05,
            0x35, 0x55, 0x4d, 0x0a, 0x5a, 0x02, 0x5d, 0x31, 0xb5, 0x02, 0x6a,
            0x8a, 0x68, 0x05, 0xa9, 0x0a, 0x8a, 0x6a, 0x2a, 0x05, 0x2d, 0x09,
            0xaa, 0x48, 0x5a, 0x01, 0xb5, 0x09, 0xb0, 0x39, 0x64, 0x05, 0x25,
            0x75, 0x95, 0x0a, 0x96, 0x04, 0x4d, 0x54, 0xad, 0x04, 0xda, 0x04,
            0xd4, 0x44, 0xb4, 0x05, 0x54, 0x85, 0x52, 0x0d, 0x92, 0x0a, 0x56,
            0x6a, 0x56, 0x02, 0x6d, 0x02, 0x6a, 0x41, 0xda, 0x02, 0xb2, 0xa1,
            0xa9, 0x05, 0x49, 0x0d, 0x0a, 0x6d, 0x2a, 0x09, 0x56, 0x01, 0xad,
            0x50, 0x6d, 0x01, 0xd9, 0x02, 0xd1, 0x3a, 0xa8, 0x05, 0x29, 0x85,
            0xa5, 0x0c, 0x2a, 0x09, 0x96, 0x54, 0xb6, 0x08, 0x6c, 0x09, 0x64,
            0x45, 0xd4, 0x0a, 0xa4, 0x05, 0x51, 0x25, 0x95, 0x0a, 0x2a, 0x72,
            0x5b, 0x04, 0xb6, 0x04, 0xac, 0x52, 0x6a, 0x05, 0xd2, 0x0a, 0xa2,
            0x4a, 0x4a, 0x05, 0x55, 0x94, 0x2d, 0x0a, 0x5a, 0x02, 0x75, 0x61,
            0xb5, 0x02, 0x6a, 0x03, 0x61, 0x45, 0xa9, 0x0a, 0x4a, 0x05, 0x25,
            0x25, 0x2d, 0x09, 0x9a, 0x68, 0xda, 0x08, 0xb4, 0x09, 0xa8, 0x59,
            0x54, 0x03, 0xa5, 0x0a, 0x91, 0x3a, 0x96, 0x04, 0xad, 0xb0, 0xad,
            0x04, 0xda, 0x04, 0xf4, 0x62, 0xb4, 0x05, 0x54, 0x0b, 0x44, 0x5d,
            0x52, 0x0a, 0x95, 0x04, 0x55, 0x22, 0x6d, 0x02, 0x5a, 0x71, 0xda,
            0x02, 0xaa, 0x05, 0xb2, 0x55, 0x49, 0x0b, 0x4a, 0x0a, 0x2d, 0x39,
            0x36, 0x01, 0x6d, 0x80, 0x6d, 0x01, 0xd9, 0x02, 0xe9, 0x6a, 0xa8,
            0x05, 0x29, 0x0b, 0x9a, 0x4c, 0xaa, 0x08, 0xb6, 0x08, 0xb4, 0x38,
            0x6c, 0x09, 0x54, 0x75, 0xd4, 0x0a, 0xa4, 0x05, 0x45, 0x55, 0x95,
            0x0a, 0x9a, 0x04, 0x55, 0x44, 0xb5, 0x04, 0x6a, 0x82, 0x6a, 0x05,
            0xd2, 0x0a, 0x92, 0x6a, 0x4a, 0x05, 0x55, 0x0a, 0x2a, 0x4a, 0x5a,
            0x02, 0xb5, 0x02, 0xb2, 0x31, 0x69, 0x03, 0x31, 0x73, 0xa9, 0x0a,
            0x4a, 0x05, 0x2d, 0x55, 0x2d, 0x09, 0x5a, 0x01, 0xd5, 0x48, 0xb4,
            0x09, 0x68, 0x89, 0x54, 0x0b, 0xa4, 0x0a, 0xa5, 0x6a, 0x95, 0x04,
            0xad, 0x08, 0x6a, 0x44, 0xda, 0x04, 0x74, 0x05, 0xb0, 0x25, 0x54,
            0x03 };
}
