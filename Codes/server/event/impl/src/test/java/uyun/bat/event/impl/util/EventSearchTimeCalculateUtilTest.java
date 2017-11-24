package uyun.bat.event.impl.util;

import org.junit.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class EventSearchTimeCalculateUtilTest {

    /**
     *时间跨度30分钟
     * 粒度30
     * 如输入为2016-09-12 15:15:27  2016-09-12 15:45:27
     * 则输出为2016-09-12 15:16:00  2016-09-12 15:46:00
     * interval 1分钟
     */
    @Test
    public void test30Min(){
        /*String beginSch="2016-09-12 15:15:27";
        String endSch="2016-09-12 15:45:27";*/
        long beginSch = 1473664527000L;
        long endSch = 1473666327000L;
        int granularity=30;
        long alignValue=60*1000;
        long baseInterval=alignValue;

        Map<String,Object> map= EventSearchTimeCalculateUtil.getSearchTime(beginSch,endSch,granularity);
        Date begin=(Date)map.get("begin");
        Date end=(Date)map.get("end");
        long interval=(long)map.get("interval");
        /*assertTrue(begin.getTime()==(new Date(beginSch).getTime()+alignValue));
        assertTrue(end.getTime()==(new Date(endSch).getTime())+alignValue);*/
        assertTrue(interval==baseInterval);
    }

    /**
     *时间跨度1小时
     * 粒度30
     * 如输入为2016-09-12 15:15:27  2016-09-12 15:45:27
     * 则输出为2016-09-12 15:16:00  2016-09-12 15:46:00
     *  interval 2分钟
     */
    @Test
    public void test1Hour(){
        /*String beginSch="2016-09-12 15:15:27";
        String endSch="2016-09-12 16:15:27";*/
        long beginSch = 1473664527000L;
        long endSch = 1473668127000L;
        int granularity=30;
        long alignValue=60*1000;
        long baseInterval=2*alignValue;

        Map<String,Object> map= EventSearchTimeCalculateUtil.getSearchTime(beginSch,endSch,granularity);
        Date begin=(Date)map.get("begin");
        Date end=(Date)map.get("end");
        long interval=(long)map.get("interval");
        /*assertTrue(begin.getTime()==(new Date(beginSch).getTime()+alignValue));
        assertTrue(end.getTime()==(new Date(endSch).getTime())+alignValue);*/
        assertTrue(interval==baseInterval);
    }

    /**
     *时间跨度6小时
     * 粒度36
     * 如输入为2016-09-12 14:15:27  2016-09-12 20:15:27
     * 则输出为2016-09-12 14:20:00  2016-09-12 20:20:00
     *  interval 10分钟
     */
    @Test
    public void test6Hour(){
        /*String beginSch="2016-09-12 14:15:27";
        String endSch="2016-09-12 20:15:27";*/
        long beginSch = 1473660927000L;
        long endSch = 1473682527000L;
        int granularity=36;
        long alignValue=5*60*1000;
        long baseInterval=2*alignValue;

        Map<String,Object> map= EventSearchTimeCalculateUtil.getSearchTime(beginSch,endSch,granularity);
        Date begin=(Date)map.get("begin");
        Date end=(Date)map.get("end");
        long interval=(long)map.get("interval");
        /*assertTrue(begin.getTime()==(new Date(beginSch).getTime()+alignValue));
        assertTrue(end.getTime()==(new Date(endSch).getTime())+alignValue);*/
        assertTrue(interval==baseInterval);
    }

    /**
     *时间跨度12小时
     * 粒度24
     * 如输入为2016-09-09 02:15:27  2016-09-09 14:15:27
     * 则输出为2016-09-09 02:30:00  2016-09-12 14:30:00
     *  interval 30分钟
     */
    @Test
    public void test12Hour(){
        /*String beginSch="2016-09-09 02:15:27";
        String endSch="2016-09-09 14:15:27";*/
        long beginSch = 1473358527000L;
        long endSch = 1473401727000L;
        int granularity=24;
        long alignValue=30*60*1000;
        long baseInterval=alignValue;

        Map<String,Object> map= EventSearchTimeCalculateUtil.getSearchTime(beginSch,endSch,granularity);
        Date begin=(Date)map.get("begin");
        Date end=(Date)map.get("end");
        long interval=(long)map.get("interval");
        /*assertTrue(begin.getTime()==(new Date(beginSch).getTime()+alignValue));
        assertTrue(end.getTime()==(new Date(endSch).getTime())+alignValue);*/
        assertTrue(interval==baseInterval);
    }

    /**
     *时间跨度1天
     * 粒度24
     * 如输入为2016-09-09 02:15:27  2016-09-09 21:15:27
     * 则输出为2016-09-09 03:00:00  2016-09-12 22:00:00
     *  interval 1小时
     */
    @Test
    public void test1Day(){
        /*String beginSch="2016-09-09 02:15:27";
        String endSch="2016-09-10 02:15:27";*/
        long beginSch = 1473358527000L;
        long endSch = 1473444927000L;
        int granularity=24;
        long alignValue=60*60*1000;
        long baseInterval=alignValue;

        Map<String,Object> map= EventSearchTimeCalculateUtil.getSearchTime(beginSch,endSch,granularity);
        Date begin=(Date)map.get("begin");
        Date end=(Date)map.get("end");
        long interval=(long)map.get("interval");
        /*assertTrue(begin.getTime()==(new Date(beginSch).getTime()+alignValue));
        assertTrue(end.getTime()==(new Date(endSch).getTime())+alignValue);*/
        assertTrue(interval==baseInterval);
    }

    /**
     *时间跨度3天
     * 粒度36
     * 如输入为2016-09-09 14:15:27  2016-09-12 14:15:27
     * 则输出为2016-09-09 15:00:00  2016-09-12 15:00:00
     *  interval 2小时
     */
    @Test
    public void test3Day(){
        /*String beginSch="2016-09-09 14:15:27";
        String endSch="2016-09-12 14:15:27";*/
        long beginSch = 1473401727000L;
        long endSch = 1473660927000L;
        int granularity=36;
        long alignValue=60*60*1000;
        long baseInterval=2*alignValue;

        Map<String,Object> map= EventSearchTimeCalculateUtil.getSearchTime(beginSch,endSch,granularity);
        Date begin=(Date)map.get("begin");
        Date end=(Date)map.get("end");
        long interval=(long)map.get("interval");
        /*assertTrue(begin.getTime()==(new Date(beginSch).getTime()+alignValue));
        assertTrue(end.getTime()==(new Date(endSch).getTime())+alignValue);*/
        assertTrue(interval==baseInterval);
    }

    /**
     *时间跨度7天
     * 粒度28
     * 如输入为2016-09-05 14:15:27  2016-09-12 14:15:27
     * 则输出为2016-09-05 18:00:00  2016-09-12 18:00:00
     *  interval 6小时
     */
    @Test
    public void test7Day(){
        /*String beginSch="2016-09-05 14:15:27";
        String endSch="2016-09-12 14:15:27";*/
        long beginSch = 1473056127000L;
        long endSch = 1473660927000L;
        int granularity=28;
        long alignValue=4*60*60*1000;
        long baseInterval=6*60*60*1000;

        Map<String,Object> map= EventSearchTimeCalculateUtil.getSearchTime(beginSch,endSch,granularity);
        Date begin=(Date)map.get("begin");
        Date end=(Date)map.get("end");
        long interval=(long)map.get("interval");
        /*assertTrue(begin.getTime()==(new Date(beginSch).getTime()+alignValue));
        assertTrue(end.getTime()==(new Date(endSch).getTime())+alignValue);*/
        assertTrue(interval==baseInterval);
    }

    /**
     *时间跨度1个月
     * 粒度30
     * 如输入为2016-08-13 10:15:27 2016-09-12 10:15:27
     * 则输出为2016-08-14 00:00:00 2016-09-13 00:00:00
     *  interval 1天
     */
    @Test
    public void test1Month(){
        /*String beginSch="2016-08-13 10:15:27";
        String endSch="2016-09-12 10:15:27";*/
        long beginSch = 1471054527000L;
        long endSch = 1473646527000L;
        int granularity=30;
        long alignValue=24*60*60*1000;
        long baseInterval=alignValue;

        Map<String,Object> map= EventSearchTimeCalculateUtil.getSearchTime(beginSch,endSch,granularity);
        Date begin=(Date)map.get("begin");
        Date end=(Date)map.get("end");
        long interval=(long)map.get("interval");
        /*assertTrue(begin.getTime()==(new Date(beginSch).getTime()+alignValue));
        assertTrue(end.getTime()==(new Date(endSch).getTime())+alignValue);*/
        assertTrue(interval==baseInterval);
    }

}
