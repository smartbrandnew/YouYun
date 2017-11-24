package uyun.bat.event.impl.util;

import org.junit.Test;

public class DateUtilTest {

    @Test
    public void teststr2Time(){
        String str="2016-12-20 17:11:40";
        DateUtil.str2Time(str);
    }

    @Test
    public void teststr2TimeHm(){
        String str="2016-12-20 17:11";
        DateUtil.str2TimeH(str);
    }

    @Test
    public void teststr2TimeH(){
        String str="2016-12-20 17";
        DateUtil.str2TimeH(str);
    }


}
