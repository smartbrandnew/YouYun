package uyun.bat.agent.impl.service;


import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import uyun.bat.agent.impl.Startup;
import uyun.bat.agent.impl.httpservletutil.HttpServletTest;
import uyun.bat.agent.impl.logic.AgentLogic;


public class AgentConfigServiceImplTest {

	private static final String AGENT_ID = "12345678910111213141516171819203";
	static{
		Startup.getInstance();
	}
	
	@Test
	public void testGetFileListById() {
		AgentConfigServiceImpl agentConfigServiceImpl = new AgentConfigServiceImpl();
		HttpServletRequest request = new HttpServletTest();
		agentConfigServiceImpl.getFileListById(request, AGENT_ID, "source");
		try{
			agentConfigServiceImpl.getFileContentByNameAndId(request, AGENT_ID, "test.yaml", "source");
		}catch(IllegalArgumentException e){
			System.out.println("参数错误");
		}
		
	}

}
