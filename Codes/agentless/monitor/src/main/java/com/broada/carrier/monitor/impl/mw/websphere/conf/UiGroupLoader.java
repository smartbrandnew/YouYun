package com.broada.carrier.monitor.impl.mw.websphere.conf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;

import com.broada.carrier.monitor.impl.mw.websphere.entity.ui.PerfItem;
import com.broada.carrier.monitor.impl.mw.websphere.entity.ui.UiGroup;

/**
 * @author lixy Sep 17, 2008 10:41:18 AM
 */
public class UiGroupLoader extends AbstractXmlLoader {
  private static Map<String, UiGroup> uiMap = new HashMap<String, UiGroup>();
  private static final String fileName = "/com/broada/srvmonitor/impl/websphere/conf/ui-group.xml";

  static {
    Document doc = buildDoc(fileName, UiGroupLoader.class);
    Element root = doc.getRootElement();
    List<Element> groups = root.getChildren("ui-group");
    for (int i = 0; i < groups.size(); i++) {
      buildUiGroup(groups.get(i));
    }
  }

  public static UiGroup getUiGroupByGroupId(String groupId) {
    UiGroup uiGroup = uiMap.get(groupId);
    if (uiGroup == null) {
      throw new RuntimeException("[" + groupId + "]UI组不存在");
    }
    return uiGroup;
  }

  private static void buildUiGroup(Element group) {
    UiGroup uiGroup = new UiGroup();
    uiGroup.setGoupId(group.getAttributeValue("goupId", ""));
    uiGroup.setName(group.getAttributeValue("name", ""));
    uiGroup.setDesc(group.getAttributeValue("desc", ""));
    try {
      uiGroup.setHasMonitorCol(group.getAttribute("hasMonitorCol").getBooleanValue());
    } catch (DataConversionException e) {
      throw new RuntimeException(fileName + "文件的hasMonitorCol属性类型错误：" + e.getMessage());
    }

    List<Element> items = group.getChildren("perf-item");
    for (int i = 0; i < items.size(); i++) {
      buildPerfItem(uiGroup, items.get(i));
    }
    uiMap.put(uiGroup.getGoupId(), uiGroup);
  }

  private static void buildPerfItem(UiGroup group, Element eleItem) {
    PerfItem item = new PerfItem();
    try {
      item.setItemCode(eleItem.getAttributeValue("itemCode",""));
      item.setName(eleItem.getAttributeValue("name", ""));
      item.setShowPerf(new Boolean(eleItem.getAttributeValue("showPerf", "false")).booleanValue());
      item.setShowCondition(new Boolean(eleItem.getAttributeValue("showCondition", "false")).booleanValue());
      if (item.isShowCondition()) {
        item.setConditionName(eleItem.getAttributeValue("conditionName", ""));
        item.setType(eleItem.getAttribute("type").getIntValue());
        item.setDefaultCondValue(eleItem.getAttribute("defaultCondValue").getIntValue());
      }
    } catch (DataConversionException e) {
      throw new RuntimeException(fileName + "中的perf-item配置错误：" + e.getMessage());
    }
    group.addPerfItem(item);
  }

}
