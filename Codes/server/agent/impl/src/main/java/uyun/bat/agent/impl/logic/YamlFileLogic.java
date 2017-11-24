package uyun.bat.agent.impl.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.agent.api.entity.AgentSource;
import uyun.bat.agent.api.entity.YamlFile;
import uyun.bat.agent.impl.dao.YamlFileDao;
import uyun.bat.common.config.Config;
import uyun.bat.common.utils.StringUtils;

public class YamlFileLogic {
	private static final Logger logger = LoggerFactory.getLogger(YamlFileLogic.class);
	@Resource
	private YamlFileDao yamlFileDao;

	public static Map<String,String> deletedCache = new ConcurrentHashMap<String,String>();

	{
		String dir = Config.getInstance().get("work.dir", System.getProperty("user.dir"));
		String[] searchAgentLocalPaths = new String[] { "/linux/conf.d/", "/../linux/conf.d/",
				"/src/main/resources/linux/conf.d/" };
		for (String path : searchAgentLocalPaths) {
			File file = new File(dir, path);
			if (file.exists()) {
				AGENT_LOCAL_DIR = file.getAbsolutePath();
			}
		}
		String[] searchAgentlessLocalPaths = new String[] { "/agentless/conf.d/", "/../agentless/conf.d/",
				"/src/main/resources/agentless/conf.d/" };
		for (String path : searchAgentlessLocalPaths) {
			File file = new File(dir, path);
			if (file.exists()) {
				AGENTLESS_LOCAL_DIR = file.getAbsolutePath();
			}
		}
	}

	private String AGENT_LOCAL_DIR;
	private String AGENTLESS_LOCAL_DIR;

	public boolean save(YamlFile yamlFile) {
		yamlFileDao.save(yamlFile);
		return true;
	}

	/**
	 * 
	 * @param tenantId
	 * @param agentId
	 * @param fileName 不带后缀,cassandra/disk
	 * @return
	 */
	public YamlFile getYamlFileByNameAndAgentId(String tenantId, String agentId, String fileName, String source) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tenantId", tenantId);
		map.put("agentId", agentId);
		map.put("fileName", fileName);
		map.put("source", source);
		return yamlFileDao.getYamlFileByNameAndAgentId(map);
	}

	/**
	 * 获取yaml配置文件
	 * 
	 * @param tenantId
	 * @param agentId
	 * @param fileName 带后缀,cassandra.yaml/cassandra.yaml.example
	 * @return
	 */

	public String getYamlContent(String tenantId, String agentId, String fileName, String source) {
		if (StringUtils.isNullOrBlank(agentId))
			return getYamlContentFromLocal(tenantId, agentId, fileName, source);
		if (fileName.endsWith(".yaml")) {
			String content = getYamlContentFromDB(tenantId, agentId, fileName, source);
			if (content != null && content.length() > 0)
				return content;
			else
				return getYamlContentFromLocal(tenantId, agentId, fileName + ".example", source);
		} else if (fileName.endsWith(".yaml.example")) {
			return getYamlContentFromLocal(tenantId, agentId, fileName, source);
		} else {
			throw new RuntimeException("FileName format isn't supported,unless it ends with \".yaml\" and \".yaml.example\"");
		}
	}

	/**
	 * 从DB获取yaml文件
	 * 
	 * @param tenantId
	 * @param agentId
	 * @param fileName 带后缀,以.yaml结尾:cassandra.yaml
	 * @return
	 */
	private String getYamlContentFromDB(String tenantId, String agentId, String fileName, String source) {
		fileName = fileName.replace(".yaml", "");
		YamlFile file = getYamlFileByNameAndAgentId(tenantId, agentId, fileName, source);
		if (file != null) {
			return file.getContent();
		}
		return null;
	}

	/**
	 * 从本地获取yaml模板文件(.example)
	 * 
	 * @param tenantId
	 * @param agentId
	 * @param fileName 带后缀,以.yaml.example结尾:cassandra.yaml.example
	 * @return
	 */
	private String getYamlContentFromLocal(String tenantId, String agentId, String fileName, String source) {
		if (AgentSource.agent.getName().equalsIgnoreCase(source)) {
			File file = new File(AGENT_LOCAL_DIR, fileName);
			return YamlContentFromLocal(tenantId, agentId, fileName, file);
		} else if (AgentSource.agentless.getName().equalsIgnoreCase(source)) {
			File file = new File(AGENTLESS_LOCAL_DIR, fileName);
			return YamlContentFromLocal(tenantId, agentId, fileName, file);
		}
		return null;
	}

	private String YamlContentFromLocal(String tenantId, String agentId, String fileName, File file) {
		if (!file.exists())
			throw new RuntimeException("yaml config file lost: " + file.getAbsolutePath());

		try {
			FileReader fReader = new FileReader(file);
			BufferedReader bufferReader = new BufferedReader(fReader);
			StringBuilder builder = new StringBuilder();
			String line = bufferReader.readLine();
			while (line != null) {
				builder.append(line);
				builder.append("\n");
				line = bufferReader.readLine();
			}
			bufferReader.close();
			fReader.close();
			String json = builder.toString();
			return json;
		} catch (FileNotFoundException e) {
			logger.warn("yaml config file{}not found:{}", file.getAbsolutePath(), e);
		} catch (Exception e) {
			logger.warn("yaml config file load exception: ", e);
		}
		return null;

	}

	public List<YamlFile> getYamlFileListByAgentId(String tenantId, String agentId, String source) {
		return yamlFileDao.getYamlFileListByAgentId(tenantId, agentId, source);
	}

	public List<String> getAllYamlName(String tenantId, String source) {
		List<String> list = new ArrayList<String>();
		if (AgentSource.agent.getName().equalsIgnoreCase(source)) {
			File file = new File(AGENT_LOCAL_DIR);
			list = getAllYamlName(file);
		} else if (AgentSource.agentless.getName().equalsIgnoreCase(source)) {
			File file = new File(AGENTLESS_LOCAL_DIR);
			list = getAllYamlName(file);
		}
		return list;
	}

	private List<String> getAllYamlName(File file) {
		List<String> list = new ArrayList<String>();
		if (!file.exists())
			throw new RuntimeException("yaml config folder lost: " + file.getAbsolutePath());
		if (file.isDirectory()) {
			File[] files = file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.getName().endsWith(".yaml.example") || pathname.getName().endsWith(".yaml"))
						return true;
					return false;
				}
			});
			for (File f : files) {
				String name = f.getName();
				if (name.endsWith(".yaml.example"))
					name = name.replace(".yaml.example", "");
				else if (name.endsWith(".yaml")) {
					name = name.replace(".yaml", "");
				}
				list.add(name);
			}
		}
		Collections.sort(list);
		return list;
	}

	public boolean delete(String id) {
		yamlFileDao.delete(id);
		return true;
	}

	public boolean updateEnabled(String tenantId, String agentId, String fileName, String source, boolean enabled) {
		int flag = yamlFileDao.updateEnabled(tenantId, agentId, fileName, source, enabled, new Date());
		return flag > 0;
	}

	public boolean deleteYaml(String tenantId, String agentId, String fileName, String source) {
		deletedCache.put(tenantId + agentId + fileName + source, fileName);
		int flag = yamlFileDao.deleteYaml(tenantId, agentId, fileName, source);
		return flag > 0;
	}

	public List<String> getDisabledYamlNames(String agentId){
		return yamlFileDao.getYamlNamesByEnabled(agentId, false);
	}

	public List<String> getEnabledYamlNames(String agentId){
		return yamlFileDao.getYamlNamesByEnabled(agentId, true);
	}
	
	public static void main(String[] args) {
		System.out.println("cassandra.yaml.example".endsWith(".yaml.example"));
	}

}
