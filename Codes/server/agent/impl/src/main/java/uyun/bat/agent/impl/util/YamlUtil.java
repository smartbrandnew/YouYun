package uyun.bat.agent.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import uyun.bat.agent.api.entity.YamlBean;
import uyun.bat.agent.api.entity.YamlHost;
import uyun.bat.common.utils.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lilm on 17-5-8.
 */
public class YamlUtil {

    private static Logger log = LoggerFactory.getLogger(YamlUtil.class);

    public static String customYamlDump(YamlBean bean) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setExplicitStart(true);
        Yaml yaml = new Yaml(new Constructor(), new SkipNullRepresenter(), options);
        List<Map<String, Object>> methods = bean.getCollect_methods();
        List<YamlHost> hosts = bean.getHosts();
        if (methods == null && hosts == null) {
            return null;
        }
        if ((methods == null || methods.size() <= 0)
                && (hosts == null || hosts.size() <= 0)) {
            return null;
        }
        if (methods != null && methods.size() > 0) {
            for (Map<String, Object> method : methods) {
                removeNullFromMap(method);
            }
        }
        try {
            return yaml.dumpAsMap(bean);
        } catch (Exception e) {
            log.error("agent yaml setting dump error: ", e);
        }
        return null;
    }

    public static YamlBean customYamlLoad(String str) {
        if (StringUtils.isBlank(str)) {
            return new YamlBean();
        }
        try {
            Yaml yaml = new Yaml();
            return yaml.loadAs(str, YamlBean.class);
        } catch (Exception e) {
            log.error("agent yaml setting load error: ", e);
        }
        return new YamlBean();
    }

    /**
     * 移除空值配置项
     * @param map
     */
    private static void removeNullFromMap(Map<String, Object> map) {
        if (map != null) {
            Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                if (entry == null || entry.getValue() == null || entry.getKey() == null) {
                    it.remove();
                }
            }
        }
    }

    private static class SkipNullRepresenter extends Representer {
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property,
                                                      Object propertyValue, Tag customTag) {
            if (propertyValue == null) {
                return null;
            }
            NodeTuple tuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            Node valueNode = tuple.getValueNode();
            if (Tag.NULL.equals(valueNode.getTag())) {
                return null;// skip 'null' values
            }
            if (valueNode instanceof CollectionNode) {
                if (Tag.SEQ.equals(valueNode.getTag()) && valueNode instanceof SequenceNode) {
                    SequenceNode seq = (SequenceNode) valueNode;
                    if (seq.getValue().isEmpty()) {
                        return null;// skip empty lists
                    }
                }
                if (Tag.MAP.equals(valueNode.getTag()) && valueNode instanceof MappingNode) {
                    MappingNode seq = (MappingNode) valueNode;
                    if (seq.getValue().isEmpty()) {
                        return null;// skip empty maps
                    }
                }
            }
            return tuple;
        }
    }
}
