package com.broada;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class GenerateXml {

	private static GenerateXml instance = new GenerateXml();
	private static Document document = new Document();
	private static Element metrics = new Element("metrics");

	public static GenerateXml getInstance() {
		return instance;
	}

	static {
		Element element = new Element("mapping");
		document.addContent(element);
		element.addContent(metrics);
	}

	public List<MonitorPackage> generateXml(String packageName, Class<?> clss) {
		List<MonitorPackage> list = new ArrayList<MonitorPackage>();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		packageName = packageName.replace(".", "/");
		try {
			Enumeration<URL> enumera = loader.getResources(packageName);
			while (enumera.hasMoreElements()) {
				URL url = enumera.nextElement();
				File file = new File(url.getFile());
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (File f : files) {
						List<MonitorPackage> clsList = generate(f.getPath(), clss, new ArrayList<MonitorPackage>());
						list.addAll(clsList);
					}
				} else {
					if (file.getName().endsWith(".class")) {
						String pathName = file.toString();
						pathName = pathName.replace("\\", ".");
						pathName = pathName.replace("/", ".");
						String className = pathName.substring(pathName.indexOf("classes."));
						className = className.replace("classes.", "").trim();
						if (className.endsWith("package") || className.endsWith("Package")) {
							Class<?> cls = Class.forName(className);
							list.add((MonitorPackage) cls.newInstance());
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return list;
	}

	private List<MonitorPackage> generate(String path, Class<?> clss, List<MonitorPackage> list) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				generate(f.getPath(), clss, list);
			}
		} else {
			if (file.getName().endsWith(".class")) {
				String pathName = file.toString();
				pathName = pathName.replace("\\", ".");
				pathName = pathName.replace("/", ".");
				String className = pathName.substring(pathName.indexOf("classes."));
				className = className.replace("classes.", "");
				className = className.replace(".class", "").trim();
				if (className.endsWith("package") || className.endsWith("Package")) {
					Class<?> cls;
					try {
						cls = Class.forName(className);
						list.add((MonitorPackage) cls.newInstance());
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}

			}
		}
		return list;
	}

	public Map<String, List<MonitorItem>> generate2Map(List<MonitorPackage> list) {
		Map<String, List<MonitorItem>> map = new HashMap<String, List<MonitorItem>>();
		for (MonitorPackage pg : list) {
			MonitorType[] types = pg.getTypes();
			for (MonitorType type : types) {
				List<MonitorItem> typeList = map.get(type.getId());
				if (typeList == null) {
					map.put(type.getId(), new ArrayList<MonitorItem>());
				}
			}
			MonitorItem[] items = pg.getItems();
			for (MonitorItem item : items) {
				for (String key : map.keySet()) {
					if (item.getCode().startsWith(key)) {
						map.get(key).add(item);
					}
				}
			}
		}

		return map;
	}

	public void generate2Xml(Map<String, List<MonitorItem>> map, String path) {
		for (Entry<String, List<MonitorItem>> entry : map.entrySet()) {
			String key = entry.getKey();
			List<MonitorItem> localMetrics = entry.getValue();
			Element ele = new Element("itemcode");
			metrics.addContent(ele);
			ele.setAttribute("name", key);
			for (MonitorItem item : localMetrics) {
				Element el = new Element("properties");
				el.setAttribute("code", item.getCode());
				el.setAttribute("remote_code", "");
				el.setAttribute("name", item.getName());
				el.setAttribute("describtion", item.getDescr());
				el.setAttribute("unit", item.getUnit());
				el.setAttribute("value_type", Integer.toString(item.getType().ordinal()));
				ele.addContent(el);
			}
		}
		createXml(document, path);

	}

	private void createXml(Document doc, String path) {
		Format format = Format.getCompactFormat();
		format.setEncoding("UTF-8");
		format.setIndent("    ");
		XMLOutputter out = new XMLOutputter(format);
		try {
			FileOutputStream outStream = new FileOutputStream(path);
			out.output(doc, outStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void create() {
		List<MonitorPackage> list = generateXml("com.broada.carrier.monitor.impl", MonitorPackage.class);
		Map<String, List<MonitorItem>> map = generate2Map(list);
		String path = "d:/mapper.xml";
		generate2Xml(map, path);
	}
}
