package uyun.bat.event.impl.util;

import org.junit.Test;

import java.io.IOException;

public class JsonUtilTest {

    @Test
    public void testDecode() {
        try {
            JsonUtil.decode("ssss", String.class);
        } catch (IOException e) {
        }
    }

    @Test
    public void testGetList() {try {
        JsonUtil.getList(null, String.class);
    }catch (Exception e) {}}

    @Test
    public void testEncode() {
        try {
            JsonUtil.encode("ssssss");
        }catch (Exception e) {}

    }
}
