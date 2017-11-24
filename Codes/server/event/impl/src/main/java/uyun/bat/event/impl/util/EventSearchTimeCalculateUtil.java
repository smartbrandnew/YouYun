package uyun.bat.event.impl.util;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件台时间对齐算法
 */
public class EventSearchTimeCalculateUtil {

    public static Map<String,Object> getSearchTime(long beginTime, long endTime, int granularity){
        long beginOrigin=beginTime;
        long endOrign=endTime;
        long begin=beginTime-beginTime % (60*1000);
        long end=endTime-endTime%(60*1000);
        long diff =end-begin;
        long interval=diff/granularity;
        if (beginOrigin!=begin&&endOrign!=end){ //分钟为整则不对齐时间
            if (interval<10*60*1000){//1.2分钟
                end=end+60*1000;
            }else{
                long endNum=Long.parseLong(DateUtil.getNumFmtDate(new Date(end)));
                if (interval<30*60*1000){//10分钟
                    end=end-end%(10*60*1000)+10*60*1000;
                }else if(interval<60*60*1000){//半小时
                	end=end-end%(30*60*1000)+30*60*1000;
                }else if(interval<360*60*1000){//一小时
                	end=end-end%(60*60*1000)+60*60*1000;
                }else if(interval<720*60*1000){//六小时
                	end=end-end%(360*60*1000)+360*60*1000;
                }else if(interval<1440*60*1000){//12小时
                	end=end-end%(720*60*1000)+720*60*1000;
                }else{//每天
                	end=end-end%(1440*60*1000)+1440*60*1000;
                }
            }
        }
        begin=end-diff;
        Map<String,Object> map=new HashMap<>();
        map.put("begin",new Date(begin));
        map.put("end",new Date(end));
        map.put("interval",interval);
        map.put("diffTime",diff);
        return map;
    }


    public static void main(String[] args) throws ParseException {
       /*String beginSch="2016-07-30 15:15:27";
        String endSch="2016-08-29 15:15:27";*/
    	long beginSch = 1469862927000L;
    	long endSch = 1472454927000L;
        int granularity=30;
		Map<String,Object> map=getSearchTime(beginSch,endSch,granularity);
		System.out.println(new Date(beginSch).getTime());
        System.out.println("begin:"+DateUtil.fmt2TimeStr(new Date(beginSch))+",end:"+DateUtil.fmt2TimeStr(new Date(endSch)));
        System.out.println("begin:"+DateUtil.fmt2TimeStr((Date)map.get("begin"))+",end:"+DateUtil.fmt2TimeStr((Date)map.get("end")));
    }


}
