package uyun.bat.agent.impl.logic;



import org.junit.Test;

import uyun.bat.agent.api.entity.AgentSource;
import uyun.bat.agent.api.entity.FileCharSet;
import uyun.bat.agent.api.entity.YamlFile;
import uyun.bat.agent.impl.Startup;

import java.util.Date;
import java.util.List;

public class YamlFileLogicTest {
	private YamlFileLogic yamlFileLogic = Startup.getInstance().getBean(YamlFileLogic.class);
	private static final String TENANT_ID = "12345678910111213141516171819202";
	private static final String AGENT_ID = "12345678910111213141516171819203";
	private static final String FILE_NAME = "snmp";

	@Test
	public void testSave() {
		String content = "snmp协议测试";
		YamlFile file = new YamlFile(AGENT_ID, FILE_NAME, TENANT_ID, new Date(), AGENT_ID,
				content.getBytes(FileCharSet.DEFAULT_CHARSET).length,
				content,AgentSource.agent);
		yamlFileLogic.save(file);
	}

	@Test
	public void testGetYamlFileByNameAndAgentId() {
		YamlFile yamlFile = yamlFileLogic.getYamlFileByNameAndAgentId(TENANT_ID, AGENT_ID, FILE_NAME,AgentSource.agent.getName());
		System.out.println("yamlFile: " + yamlFile);
	}

	@Test
	public void testGetYamlListByAgentId() {
		List<YamlFile> list = yamlFileLogic.getYamlFileListByAgentId(TENANT_ID, AGENT_ID,AgentSource.agent.getName());
		System.out.println("list: " + list);
	}

	@Test
	public void testUpdateEnabled() {
		yamlFileLogic.updateEnabled(TENANT_ID, AGENT_ID, FILE_NAME, AgentSource.agent.getName(), true);
	}
	
	@Test
	public void testUpdateDisabled() {
		yamlFileLogic.updateEnabled(TENANT_ID, AGENT_ID, FILE_NAME, AgentSource.agent.getName(), false);
	}
	
	@Test
	public void testDeleteYaml() {
		yamlFileLogic.deleteYaml(TENANT_ID, AGENT_ID, FILE_NAME, AgentSource.agent.getName());
	}
}
