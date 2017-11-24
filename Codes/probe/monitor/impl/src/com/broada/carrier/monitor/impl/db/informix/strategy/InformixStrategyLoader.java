package com.broada.carrier.monitor.impl.db.informix.strategy;

import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategy;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategyGroup;
import com.broada.carrier.monitor.impl.db.informix.strategy.interceptor.StrategyResultIntercetor;
import com.broada.utils.ListUtil;
import com.broada.utils.StringUtil;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lixy Sep 8, 2008 11:53:04 AM
 */
@SuppressWarnings("rawtypes")
public class InformixStrategyLoader {
	private static Map<String, InformixStrategyGroup> strategiesMap = new HashMap<String, InformixStrategyGroup>();
	private static Map<String,StrategyResultIntercetor> intercetorMap = new HashMap<String,StrategyResultIntercetor>();
	private static final String fileName = "/com/broada/carrier/monitor/impl/db/informix/strategy/strategy-informix.xml";

	static {
		init();
	}

	public static InformixStrategyGroup getStrategyGroup(String groupId) {
		InformixStrategyGroup group = strategiesMap.get(groupId);
		if (group == null) {
			throw new RuntimeException("策略组[" + groupId + "]不存在");
		}
		return group;
	}

	private static void init() {
		Document doc = buildDoc();
		Element rootEle = doc.getRootElement();
		initIntercetorMap(rootEle);
		List groups = getChildrenFromParent(rootEle,"strategy-group");
		for (int i = 0; i < groups.size(); i++) {
			buildStrategyGroup((Element) groups.get(i));
		}
	}

	private static void initIntercetorMap(Element rootEle) {
		Element intercetorsEle = rootEle.getChild("intercetors");
		if (intercetorsEle == null) {
			return;
		}
		List intercetors = getChildrenFromParent(intercetorsEle, "intercetor");
		for (int i = 0; i < intercetors.size(); i++) {
			Element intercetor = (Element) intercetors.get(i);
			try {
				intercetorMap.put(intercetor.getAttributeValue("id"), (StrategyResultIntercetor) Class.forName(
						intercetor.getAttributeValue("class")).newInstance());
			} catch (Exception e) {
				throw new RuntimeException("加载" + intercetor.getAttributeValue("class") + "类失败", e);
			}
		}
	}


	private static List getChildrenFromParent(Element parent, String childrenName) {
		List groups = parent.getChildren(childrenName);
		if (ListUtil.isNullOrEmpty(groups)) {
			return new ArrayList();
		}
		return groups;
	}

	private static Document buildDoc() {
		SAXBuilder sb = new SAXBuilder();
		Document doc = null;
		try {
			doc = sb.build(InformixStrategyLoader.class.getResourceAsStream(fileName));
		} catch (JDOMException e) {
			throw new RuntimeException("解析XML文件[" + fileName + "]出错", e);
		} catch (IOException e) {
			throw new RuntimeException("XML文件[" + fileName + "]不存在", e);
		}
		return doc;
	}

	private static void buildStrategyGroup(Element group) {
		InformixStrategyGroup infStyGroup = new InformixStrategyGroup();
		infStyGroup.setGoupId(getAttributeValue(group,"goupId"));
		infStyGroup.setName(getAttributeValue(group,"name"));
		infStyGroup.setSql(getAttributeValue(group,"sql"));
		infStyGroup.setDesc(getAttributeValue(group,"desc"));
		strategiesMap.put(infStyGroup.getGoupId(), infStyGroup);
		List strategies = getChildrenFromParent(group,"strategy");
		for (int i = 0; i < strategies.size(); i++) {
			buildStrategies(infStyGroup, (Element) strategies.get(i));
		}
	}

	private static void buildStrategies(InformixStrategyGroup infStyGroup, Element strategy) {
		InformixStrategy infSty = new InformixStrategy();
		try {
			infSty.setItemCode(getAttributeValue(strategy,"itemCode"));
			infSty.setName(getAttributeValue(strategy,"name"));
			infSty.setField(getAttributeValue(strategy,"field"));
			infSty.setSql(getAttributeValue(strategy,"sql"));
			Element bsh = strategy.getChild("bsh");
			if (bsh != null) {
				infSty.setBsh(bsh.getText().trim());
			}
			Attribute isCondition = strategy.getAttribute("isCondition");
			if (isCondition != null && isCondition.getBooleanValue()) {
				infSty.setCondition(true);
				infSty.setType(strategy.getAttribute("type").getIntValue());
				infSty.setThreshold(strategy.getAttribute("threshold").getDoubleValue());
				infSty.setUnit(getAttributeValue(strategy,"unit"));
			}
			setStrategyIntercetors(strategy, infSty);
		} catch (DataConversionException e) {
			throw new RuntimeException(fileName + "中组 " + infStyGroup.getGoupId() + " 配置错误。", e);
		}

		infStyGroup.setStrategy(infSty.getItemCode(), infSty);
	}

	private static void setStrategyIntercetors(Element strategy, InformixStrategy infSty) {
		String intercetors = strategy.getAttributeValue("intercetor");
		if (StringUtil.isNullOrBlank(intercetors)) {
			return;
		}
		String[] intercetorArr = intercetors.split(";");
		for (int i = 0; i < intercetorArr.length; i++) {
			infSty.addIntercetor(intercetorMap.get(intercetorArr[i]));
		}
	}

	private static String getAttributeValue(Element ele, String attrName){
		String attr = ele.getAttributeValue(attrName);
		if(attr == null){
			return "";
		}else{
			return attr.trim();
		}
	}
}
