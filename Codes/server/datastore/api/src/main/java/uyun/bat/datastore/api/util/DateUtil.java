package uyun.bat.datastore.api.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 日期工具类
 */
public class DateUtil {

    public static final String FORMAT_TO_D = "yyyy-MM-dd";
    public static final String FORMAT_TO_S = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取过去的天数
     * @param date
     * @return
     */
    public static long pastDays(Date date) {
        long t = new Date().getTime()-date.getTime();
        return t/(24*60*60*1000);
    }

    /**
     * 获取过去的小时
     * @param date
     * @return
     */
    public static long pastHour(Date date) {
        long t = new Date().getTime()-date.getTime();
        return t/(60*60*1000);
    }

    /**
     * 获取过去的分钟
     * @param date
     * @return
     */
    public static long pastMinutes(Date date) {
        long t = new Date().getTime()-date.getTime();
        return t/(60*1000);
    }

    /**
     * 转换为时间（天,时:分:秒.毫秒）
     * @param timeMillis
     * @return
     */
    public static String formatDateTime(long timeMillis){
        long day = timeMillis/(24*60*60*1000);
        long hour = (timeMillis/(60*60*1000)-day*24);
        long min = ((timeMillis/(60*1000))-day*24*60-hour*60);
        long s = (timeMillis/1000-day*24*60*60-hour*60*60-min*60);
        long sss = (timeMillis-day*24*60*60*1000-hour*60*60*1000-min*60*1000-s*1000);
        return (day>0?day+",":"")+hour+":"+min+":"+s+"."+sss;
    }

    /**
     * 获取两个日期之间的天数
     *
     * @param before
     * @param after
     * @return
     */
    public static double getDistanceOfTwoDate(Date before, Date after) {
        long beforeTime = before.getTime();
        long afterTime = after.getTime();
        return (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
    }

    public static Date parseDate(String str){
        if(str==null||str.equalsIgnoreCase(""))
            return null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date rt = null;
        try {
            rt = simpleDateFormat.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rt;
    }

    public static String formatDate(Date date, String param){
        if (date == null) {
            return null;
        }
        if (param == null || "".equals(param)) {
            param = FORMAT_TO_S;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(param);
        return simpleDateFormat.format(date);
    }

    public static Date GMT2CSTDate(Date GMTDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(GMTDate);
        calendar.add(Calendar.HOUR,8);
        return calendar.getTime();
    }

    public static Long GMT2CSTLong(Date GMTDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(GMTDate);
        calendar.add(Calendar.HOUR,8);
        return calendar.getTime().getTime();
    }

    /**
     * utc时间转换为Date时间
     * @param utcTime
     * @return
     */
    public static Date convertToDate(String utcTime) {
        try {

            String timeZone;
            if (!utcTime.endsWith("Z")) {
                //末尾不包含Z,则不需要减去8小时
                utcTime += "Z";
                timeZone = "+0800";
            } else {
                timeZone = "+0000";
            }
            Date date = (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")).parse(utcTime.replaceAll("Z$", timeZone));
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * Date时间转utc时间
     *
     * @param date
     * @return
     */
    public static String convertToUtcTime(Date date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 根据开始时间和结束时间返回时间段内的时间集合
     *
     * @param beginDate
     * @param endDate
     * @return List
     */
    public static List<Date> getDatesBetweenTwoDate(Date beginDate, Date endDate) {
        List<Date> lDate = new ArrayList<Date>();
        lDate.add(endDate);// 把终止加入集合
        Calendar cal = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        cal.setTime(endDate);
        boolean bContinue = true;
        while (bContinue) {
            // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
            cal.add(Calendar.DAY_OF_MONTH, -1);
            // 测试此日期是否在指定日期之前
            if (beginDate.before(cal.getTime())) {
                lDate.add(cal.getTime());
            } else {
                break;
            }
        }
        lDate.add(beginDate);// 把开始时间加入集合
        return lDate;
    }

    /**
     * 获取指定时间
     */
    public static Date getAnyDate(int num){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, num);//0 今天; -1 昨天; +1 明天
        return calendar.getTime();
    }

    /**
     * 加天
     * @param day
     * @param base
     * @return
     */
    public static Date addDay(int day, Date base){
        Calendar cal=Calendar.getInstance();
        if(base==null) base = cal.getTime();
        cal.setTime(base);
        cal.add(Calendar.DAY_OF_YEAR, day);
        return cal.getTime();
    }

    /**
     * 获取指本周第一天
     *
     * @return
     */
    public static Date getFirstDayofWeek() {
        Calendar calendar = Calendar.getInstance();
        Calendar ca = (Calendar) calendar.clone();
        ca.add(Calendar.DATE, 1 - calendar.get(Calendar.DAY_OF_WEEK));
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    /**
     * 本周最后一天
     *
     * @return
     */
    public static Date getLastDayofWeek() {
        Calendar calendar = Calendar.getInstance();
        Calendar ca = (Calendar) calendar.clone();
        ca.add(Calendar.DATE, 8 - calendar.get(Calendar.DAY_OF_WEEK));
        ca.set(Calendar.HOUR_OF_DAY, 24);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    /**
     * 获取本月第一天
     *
     * @return
     */
    public static Date getFirstDayofMonth() {
        Calendar calendar = Calendar.getInstance();
        Calendar ca = (Calendar) calendar.clone();
        ca.set(Calendar.DAY_OF_MONTH, 1);
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    /**
     * 本月最后一天
     *
     * @return
     */
    public static Date getLastDayofMonth() {
        Calendar calendar = Calendar.getInstance();
        Calendar ca = (Calendar) calendar.clone();
        ca.add(Calendar.MONTH, 1);
        ca.set(Calendar.DAY_OF_MONTH, 1);
        ca.add(Calendar.DAY_OF_MONTH, -1);
        ca.set(Calendar.HOUR_OF_DAY, 24);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    /**
     * 下周
     *
     * @return
     */
    public static Date getNextWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 7);
        return calendar.getTime();
    }

    /**
     * 一周第一天
     *
     * @param date
     * @return
     */
    public static Date getFirstDayofWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar ca = (Calendar) calendar.clone();
        ca.add(Calendar.DATE, 2 - calendar.get(Calendar.DAY_OF_WEEK));
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    /**
     * 一周最后一天
     *
     * @param date
     * @return
     */
    public static Date getLastDayofWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar ca = (Calendar) calendar.clone();
        ca.add(Calendar.DATE, 8 - calendar.get(Calendar.DAY_OF_WEEK));
        ca.set(Calendar.HOUR_OF_DAY, 23);
        ca.set(Calendar.MINUTE, 59);
        ca.set(Calendar.SECOND, 59);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    /**
     * 获取某年第一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getFirstDayofYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    /**
     * 获取某年最后一天日期
     *
     * @param year 年份
     * @return Date
     */
    public static Date getLastDayofYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }


    /**
     * 获取月份的第一天
     * month 0 : 一月
     * @param month
     * @param year
     * @return
     */
    public static Date getFirstDayofMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取最后一天
     * month 0 : 一月
     * @param month
     * @param year
     * @return
     */
    public static Date getLastDayofMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month + 1);
        calendar.set(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 24);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static void main(String[] args) {
        //获取昨天时间
        Date lastDate = DateUtil.getAnyDate(-1);
        Date date = DateUtil.getMorning(lastDate);
        Date date1 = DateUtil.getNight(lastDate);

        Date lastWeekDate = DateUtil.getAnyDate(-7);
        Date date2 = DateUtil.getFirstDayofWeek(lastWeekDate);
        Date date3 = DateUtil.getLastDayofWeek(lastWeekDate);

        Calendar c = Calendar.getInstance();
        //获取上月时间
        int lastMonth = c.get(Calendar.MONTH) - 1;
        int year = c.get(Calendar.YEAR);
        Date date4 = DateUtil.getFirstDayofMonth(lastMonth, year);
        Date date5 = DateUtil.getLastDayofMonth(lastMonth, year);
        System.out.println("");
    }

    /**
     * this year
     *
     * @return
     */
    public static int getThisYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * 获取当前月份
     *
     * @return
     */
    public static int getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1;
    }


    /**
     * 获取当前年份
     *
     * @return
     */
    public static int getCurrentYear() {
        return getThisYear();
    }

    /**
     * 当天凌晨
     *
     * @return
     */
    public static Date getCurrentMor() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 当天24点
     *
     * @return
     */
    public static Date getCurrentNig() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 24);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取日期的凌晨
     * @param date
     * @return
     */
    public static Date getMorning(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar ca = (Calendar) cal.clone();
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.MILLISECOND, 0);
        return ca.getTime();
    }

    public static Date getNight(Date date){
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        Calendar cal = (Calendar) ca.clone();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    /**
     * 获取上半年或是下半年的第一天
     *
     * @return
     */
    public static Date getSemiyearlyFirstDay(int month, int year) {
        if (month != -1) {
            return getFirstDayofMonth(month, year);
        } else {//当前月份
            int currentMonth = getCurrentMonth();
            int currentYear = getCurrentYear();
            if (currentMonth > 6) {
                return getFirstDayofMonth(7, currentYear);
            } else {
                return getFirstDayofMonth(1, currentYear);
            }
        }
    }

    /**
     *
     * @return
     */
    public static Date getSemiyearlyLastDay(int month, int year){
        if (month != -1) {
            if(month>6){
                return getLastDayofMonth(12, year);
            }else {
                return getLastDayofMonth(6, year);
            }
        } else {//当前月份
            int currentMonth = getCurrentMonth();
            int currentYear = getCurrentYear();
            if (currentMonth > 6) {
                return getLastDayofMonth(12, currentYear);
            } else {
                return getLastDayofMonth(6, currentYear);
            }
        }
    }

    /**
     * 昨天
     * @return
     */
    public static Date getYesterday(){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,-1);
        return cal.getTime();
    }


    /**
     *获取年份
     * @param date
     * @return
     */
    public static int getYear(Date date){
        if (date!=null){
            Calendar cal=Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.YEAR);
        }
        return 0;
    }

    /**
     * 获取月份
     * @param date
     * @return
     */
    public  static int getMonth(Date date){
        if(date!=null){
            Calendar cal=Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.MONTH);
        }
        return 1;
    }

    public static int getWeek(Date date){
        if(date!=null){
            Calendar cal=Calendar.getInstance();
            cal.setTime(date);
            return cal.get(Calendar.DAY_OF_WEEK);
        }

        return 1;
    }


}
