package uyun.bat.agent.impl.util;

import org.junit.Assert;
import org.junit.Test;
import uyun.bat.agent.api.entity.YamlBean;

/**
 * Created by lilm on 17-5-8.
 */
public class YamlUtilTest {

    @Test
    public void testCustomYamlDump() {
        String text =
                "---\n" +
                "collect_methods:\n" +
                "- name: test\n" +
                "  type: ProtocolWSAgent\n" +
                "  interval: 60\n" +
                "  port: 9080\n" +
                "  connectorHost: 10.1.50.118\n" +
                "  connectorType: SOAP\n" +
                "  username: admin\n" +
                "  connectorPort: 8880\n" +
                "  password: admin\n" +
                "hosts:\n" +
                "- collect_method: test\n" +
                "  host: HHHH\n" +
                "  ip: 10.1.50.118\n" +
                "  os: linux\n" +
                "  tags: app:websphere\n";

        YamlBean bean = YamlUtil.customYamlLoad(text);
        Assert.assertNotNull(bean);
        String str = YamlUtil.customYamlDump(bean);
        Assert.assertEquals(str, text);
    }
}
