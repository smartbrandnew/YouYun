package com.broada.carrier.monitor.impl.mw.websphere.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.broada.carrier.monitor.impl.common.convertor.ConvertUtil;
import com.broada.carrier.monitor.impl.common.convertor.MonResultConvertor;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.Item;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.Type;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.Version;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.WebSphere;

/**
 * xml文件装载
 * 
 * @author lixy Sep 16, 2008 2:28:31 PM
 */
public class WebSphereGroupLoader extends AbstractXmlLoader {
  private static Map<String, WebSphere> websphereMap = new HashMap<String, WebSphere>();
  private static Map<String, WebSphere> websphereNDMap = new HashMap<String, WebSphere>();
  private static List<Version> versions = new ArrayList<Version>();
  private static List<Version> versionsND = new ArrayList<Version>();
  private static final String fileName = "websphere-group.xml";
  private static final String WSNDFileName = "websphereND-group.xml";
  
  static {
  	initXML(false);
  	initXML(true);
  }
  
  private static void initXML(boolean isGroup){
  	Document doc = buildDoc(isGroup ? WSNDFileName : fileName, WebSphereGroupLoader.class);
    Element root = doc.getRootElement();
    initVersions(root, isGroup);
    List<Element> webspheres = root.getChildren("websphere");
    for (int i = 0; i < webspheres.size(); i++) {
      buildWebSphere((Element) webspheres.get(i), isGroup);
    }
  }

  public static WebSphere getWebSphereByVersion(String version) throws Exception {
  	return getWebSphereByVersion(version, false);
  }
  
  public static WebSphere getWebSphereByVersion(String version, boolean isGroup) throws Exception {
  	for (String key : (isGroup ? websphereNDMap : websphereMap).keySet()) {
      if (version.startsWith(key)) {
        return (isGroup ? websphereNDMap : websphereMap).get(key);
      }
    }
    throw new Exception("对[" + version + "]版本的监测不支持。");
  }

  public static List<Version> getVersions(boolean isGroup) {
  	if(isGroup)
  		return versionsND;
  	else
  		return versions;
  }

  private static void buildWebSphere(Element websphere, boolean isGroup) {
    WebSphere was = new WebSphere();
    was.setVersion(websphere.getAttributeValue("version", ""));
    List<Element> types = websphere.getChildren("type");
    for (int i = 0; i < types.size(); i++) {
      buildType(was, (Element) types.get(i));
    }
    (isGroup ? websphereNDMap : websphereMap).put(was.getVersion(), was);
  }

  private static void initVersions(Element root, boolean isGroup) {
    List<Element> list = root.getChild("versions").getChildren("version");
    for (int i = 0; i < list.size(); i++) {
      Element ele = list.get(i);
      Version version = new Version();
      version.setUrl(ele.getAttributeValue("url", ""));
      version.setEleId(ele.getAttributeValue("eleId", ""));
      version.setValue(ele.getAttributeValue("value", ""));
      version.setDefUrl(ele.getAttributeValue("default",""));
      (isGroup ? versionsND : versions).add(version);
    }
  }

  private static void buildType(WebSphere was, Element eleType) {
    Type type = new Type();
    type.setId(eleType.getAttributeValue("id", ""));
    type.setUrl(eleType.getAttributeValue("url", ""));
    type.setAttr(eleType.getAttributeValue("attr", ""));
    type.setParsePath(eleType.getAttributeValue("parsePath", ""));
    type.setItemAttr(eleType.getAttributeValue("itemAttr", ""));
    type.setInstAttr(eleType.getAttributeValue("instAttr", ""));
    type.setChildName(eleType.getAttributeValue("childName", ""));

    List<Element> items = eleType.getChildren("item");
    for (int i = 0; i < items.size(); i++) {
      buildItem(type, items.get(i));
    }

    was.putType(type);
  }

  private static void buildItem(Type type, Element eleItem) {
    Item item = new Item();
    item.setId(eleItem.getAttributeValue("id", ""));
//    try {
    item.setCode(eleItem.getAttributeValue("code",""));
//    } catch (DataConversionException e) {
//      throw new RuntimeException("获取Item id失败：" + e.getMessage());
//    }
    item.setAttr(eleItem.getAttributeValue("attr", ""));
    item.setName(eleItem.getAttributeValue("name", ""));
    item.setEleId(eleItem.getAttributeValue("eleId", ""));
    item.setValue(eleItem.getAttributeValue("value", ""));
    item.setBsh(eleItem.getAttributeValue("bsh", ""));
    String convertors = eleItem.getAttributeValue("convertors", "");
    if (convertors.length() > 0) {
      String[] convertorArr = convertors.split(";");
      for (int i = 0; i < convertorArr.length; i++) {
        item.addConvertor((MonResultConvertor) ConvertUtil.buildUnitConvertor(convertorArr[i]));
      }
    }
    type.addItem(item);
  }

}
