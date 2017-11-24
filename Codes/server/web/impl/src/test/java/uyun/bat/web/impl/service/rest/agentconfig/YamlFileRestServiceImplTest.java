package uyun.bat.web.impl.service.rest.agentconfig;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uyun.bat.agent.api.entity.AgentSource;
import uyun.bat.agent.api.entity.YamlFile;
import uyun.bat.web.impl.testservice.StartService;

public class YamlFileRestServiceImplTest extends StartService{

	YamlFileRestServiceImpl y = new YamlFileRestServiceImpl();
	private static final String TENANT_ID = "94baaadca64344d2a748dff88fe7159e";
	private static final String AGENT_ID = "12345678910111213141516171819203";
	private static final String FILE_NAME = "snmp";

	@Test
	public void testUpload() {
		List<YamlFile> yamlFiles = new ArrayList<>();
		YamlFile yamlFile = new YamlFile();
		yamlFile.setTenantId(TENANT_ID);
		yamlFile.setContent("content");
		y.upload(TENANT_ID, yamlFiles);
	}
	
	@Test
	public void testGetYamlContent(){
		y.getYamlContent(TENANT_ID, "123", "fileName", "source");
	}
	
	@Test
	public void testGetAllYamlName(){
		y.getAllYamlName(TENANT_ID, "source");
	}
	
	@Test
	public void testEnable(){
		y.enable(AGENT_ID, AGENT_ID, FILE_NAME, AgentSource.agent.getName());
	}

	@Test
	public void testDisable(){
		
	}
	
	@Test
	public void testDeleteYaml(){
		
	}
}
