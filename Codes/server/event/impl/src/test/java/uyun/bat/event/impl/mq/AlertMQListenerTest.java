package uyun.bat.event.impl.mq;

import org.junit.Test;
import uyun.bat.event.api.entity.AlertData;
import uyun.bat.event.impl.util.JsonUtil;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by yaoyao on 2017/6/14.
 */
public class AlertMQListenerTest {
    @Test
    public void onMessage() throws Exception {
    }

    @Test
    public void testBody() throws IOException {
        AlertData alertData = new AlertData("system.mem.pct_usage-CPU利用率",
                3, "内存使用率超过80", new Date(), "PC Server", "10.1.10.48", Collections.emptyList());
        Map<String, String> map = new HashMap<>();
        map.put("body", JsonUtil.encode(alertData));
        System.out.println(JsonUtil.encode(Collections.singletonList(map)));
    }




}