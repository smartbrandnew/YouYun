package com.broada.carrier.monitor;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;

import com.broada.carrier.monitor.impl.mw.weblogic.WeblogicMonitorPackage;
import com.broada.carrier.monitor.impl.virtual.vmware.VSphereMonitorPackage;
import com.broada.carrier.monitor.method.common.BaseMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

import edu.emory.mathcs.backport.java.util.Arrays;

public class GenerateYaml {
	/*

	public void generateYaml() {
		String path = System.getProperty("user.dir") + "/conf1/yaml/";
		Map<String, Set<String>> monitorTypeMap = new HashMap<String, Set<String>>();
		ServiceLoader<MonitorPackage> loader = ServiceLoader
				.load(MonitorPackage.class);

		for (MonitorPackage pack : loader) {
			if (pack.getTypes() != null) {
				Set<String> set = new HashSet<String>();
				for (MonitorType type : pack.getTypes()) {
					String[] typeIds = type.getMethodTypeIds();
					if (typeIds != null)
						set.addAll(Arrays.asList(typeIds));
				}
				monitorTypeMap.put(pack.getTypes()[0].getGroupId(), set);
			}
			MonitorPackage p=new WeblogicMonitorPackage();
			Set<String> set = new HashSet<String>();
			for (MonitorType type : p.getTypes()) {
				String[] typeIds = type.getMethodTypeIds();
				if (typeIds != null)
					set.addAll(Arrays.asList(typeIds));
			}
			monitorTypeMap.put(p.getTypes()[0].getGroupId(), set);
		
			MonitorPackage p1=new VSphereMonitorPackage();
			Set<String> set1 = new HashSet<String>();
			for (MonitorType type : p1.getTypes()) {
				String[] typeIds = type.getMethodTypeIds();
				if (typeIds != null)
					set1.addAll(Arrays.asList(typeIds));
			}
			monitorTypeMap.put(p1.getTypes()[0].getGroupId(), set1);
		}

		List<MonitorMethod> list = getAllMethods(
				"com.broada.carrier.monitor.method", MonitorMethod.class);
		System.out.println("list: "+list);
		Map<String, Set<String>> map = generateYaml(list);
		for (Entry<String, Set<String>> entry : monitorTypeMap.entrySet()) {
			String name = entry.getKey();
			File file = new File(path + name.toLowerCase() + ".yaml");
			try {
				if (!file.exists())
					file.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			YamlBean bean = new YamlBean();
			List<Map<String, Object>> methods = new ArrayList<Map<String, Object>>();
			for (String type : entry.getValue()) {
				Set<String> props = map.get(type);
				Map<String, Object> propMap = new LinkedHashMap<String, Object>();
				propMap.put("name", "test");
				propMap.put("interval", "10");
				for (String s : props) {
					propMap.put(s, "");
				}
				methods.add(propMap);
			}
			bean.setCollect_methods(methods);
			List<YamlHost> hostList = new ArrayList<YamlHost>();
			YamlHost host = new YamlHost();
			host.setCollect_method("test");
			host.setIp("127.0.0.1");
			hostList.add(host);
			bean.setHosts(hostList);
			try {
				org.ho.yaml.Yaml.dump(bean, file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public List<MonitorMethod> getAllMethods(String packageName, Class<?> clss) {
		List<MonitorMethod> list = new ArrayList<MonitorMethod>();
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
						List<MonitorMethod> clsList = generate(f.getPath(),
								clss, new ArrayList<MonitorMethod>());
						list.addAll(clsList);
					}
				} else {
					if (file.getName().endsWith(".class")) {
						String pathName = file.toString();
						pathName = pathName.replace("\\", ".");
						pathName = pathName.replace("/", ".");
						String className = pathName.substring(pathName
								.indexOf("classes."));
						className = className.replace("classes.", "").trim();
						if (className.endsWith("Option")
								|| className.endsWith("Method")) {
							Class<?> cls = Class.forName(className);
							list.add((MonitorMethod) cls.newInstance());
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

	private List<MonitorMethod> generate(String path, Class<?> clss,
			List<MonitorMethod> list) {
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
				String className = pathName.substring(pathName
						.indexOf("classes."));
				className = className.replace("classes.", "");
				className = className.replace(".class", "").trim();
				if (true) {
					Class<?> cls;
					try {
						cls = Class.forName(className);
						if (MonitorMethod.class.isAssignableFrom(cls))
							list.add((MonitorMethod) cls.newInstance());
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

	public Map<String, Set<String>> generateYaml(List<MonitorMethod> methods) {
		Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		for (MonitorMethod method : methods) {
			List<Method> msList = new ArrayList<Method>();
			Method[] ms = method.getClass().getDeclaredMethods();
			msList.addAll(Arrays.asList(ms));
			Class<?> cls = method.getClass().getSuperclass();
			while (!cls.getSimpleName().equals(
					MonitorMethod.class.getSimpleName())
					&& !cls.getSimpleName().equals(
							BaseMethod.class.getSimpleName())) {
				msList.addAll(Arrays.asList(cls.getDeclaredMethods()));
				cls = cls.getSuperclass();
			}
			String typeId = null;
			try {
				typeId = method.getClass().getDeclaredField("TYPE_ID")
						.get(method).toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (typeId == null)
				continue;
			Set<String> set = map.get(typeId);
			if (set == null) {
				set = new HashSet<String>();
				map.put(typeId, set);
			}
			for (Method m : msList) {
				if (m.getName() != null && m.getName().startsWith("set")) {
					String properties = m.getName().replace("set", "");
					properties = properties.replaceFirst(new String(
							new char[] { properties.charAt(0) }), new String(
							new char[] { (char) (properties.charAt(0) + 32) }));
					set.add(properties);
				}
			}
		}
		return map;
	}

	public void correctedYaml() {
		String path = System.getProperty("user.dir") + "/conf1/yaml/";
		File file = new File(path);
		File[] files = file.listFiles();

		for (File f : files) {
			try {
				String pth=f.getPath();
				pth=pth.replace("conf1", "conf");
				File f1 = new File(pth);
				if(!f1.exists())
					f1.createNewFile();
				FileWriter fwriter = new FileWriter(f1);
				FileReader reader = new FileReader(f);
				BufferedReader breader = new BufferedReader(reader);
				String str=breader.readLine();
				while (str != null
						&& str.trim().length() > 0) {
					if(str!=null){
					if (str.contains("--- !com.broada.carrier.monitor.probe.impl.yaml.YamlBean"))
						str = "---";
					else if (str.contains("- !java.util.LinkedHashMap")){
						str=breader.readLine();
						str="  - "+str.trim();
					}
					else if (str
							.contains("- !com.broada.carrier.monitor.probe.impl.yaml.YamlHost")){
						str=breader.readLine();
						str="  - "+str.trim();
					}
					if(str.contains("\"10\"")){
						str=str.replace("\"10\"", "10");
					}
					str=str.replace("\"\"", "");
					fwriter.write(str);
					fwriter.write("\n");
					str=breader.readLine();
				}
				}
				fwriter.flush();
				fwriter.close();
				reader.close();
				breader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public static void main(String[] args) {

		GenerateYaml yaml = new GenerateYaml();
		yaml.generateYaml();
		yaml.correctedYaml();
	}
*/
}
