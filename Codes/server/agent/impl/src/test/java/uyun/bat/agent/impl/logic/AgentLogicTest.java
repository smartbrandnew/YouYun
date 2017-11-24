package uyun.bat.agent.impl.logic;


import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import uyun.bat.agent.api.entity.Agent;
import uyun.bat.agent.api.entity.AgentSource;
import uyun.bat.agent.api.entity.AgentStatus;
import uyun.bat.agent.api.entity.PageAgent;
import uyun.bat.agent.impl.Startup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AgentLogicTest {
	private static final String TENANT_ID = "12345678910111213141516171819202";
	private static final String AGENT_ID="0b6df4263c19432594e09c655c807311";
	private static final String[] tags={"host:winpc","业务系统:A系统","机房1"};
	AgentLogic logic=Startup.getInstance().getBean(AgentLogic.class);

	@Test
	public void testASaveAgent() {
		List<String> agTags=Arrays.asList(tags);
		List<String> apps=new ArrayList<>(Arrays.asList(new String[]{"cassandra.yaml"}));
		Agent agent=new Agent(AGENT_ID, "winpc", "127.0.0.1", agTags, apps, AgentSource.agentless, new Date());
		agent.setOnlineStatus(AgentStatus.online.getName());
		agent.setTenantId(TENANT_ID);
		logic.saveAgent(agent);

		agent=new Agent(AGENT_ID, "winpc", "127.0.0.2", agTags, apps, AgentSource.agentless, new Date());
		agent.setOnlineStatus(AgentStatus.online.getName());
		agent.setTenantId("33333333333333333333333333333333");
		logic.saveAgent(agent);

		agent=new Agent(AGENT_ID, "winpc", "127.0.0.3", agTags, apps, AgentSource.agentless, new Date());
		agent.setOnlineStatus(AgentStatus.online.getName());
		agent.setTenantId(TENANT_ID);
		logic.saveAgent(agent);

		agent=new Agent(AGENT_ID, "winpc", "127.0.0.4", agTags, apps, AgentSource.agentless, new Date());
		agent.setOnlineStatus(AgentStatus.online.getName());
		agent.setTenantId(TENANT_ID);
		logic.saveAgent(agent);
	}

	@Test
	public void testBQueryTags(){
		List<String> agTags=logic.queryTags(TENANT_ID,AgentSource.agentless.name());
		assertTrue(agTags.size()==3);
	}

	@Test
	public void testCQueryByTags(){
		String searchValue="winpc";
		PageAgent pageAgent =logic.queryByTags(TENANT_ID,tags,AgentSource.agentless.name(),searchValue,1,10);
		assertTrue(pageAgent.getCount()==1);
		assertTrue(pageAgent.getAgents().size()==1);
	}

	@Test
	public void testDGetAgent(){
		Agent rs=logic.getAgentById(AGENT_ID,TENANT_ID);
		assertTrue(rs.getId().equals(AGENT_ID));
	}

	@Test
	public void testEClearData(){
		logic.deleteAgent(AGENT_ID);
		logic.deleteAgentTagById(AGENT_ID);
	}
}

