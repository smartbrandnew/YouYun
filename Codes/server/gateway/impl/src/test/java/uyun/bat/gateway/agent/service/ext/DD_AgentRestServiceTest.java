package uyun.bat.gateway.agent.service.ext;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import uyun.bat.gateway.dd_agent.entity.DDAgentData;
import uyun.bat.gateway.dd_agent.service.rest.DD_AgentRESTService;
import uyun.bat.gateway.impl.Startup;

import javax.servlet.http.*;
import java.io.IOException;

public class DD_AgentRestServiceTest {
	DD_AgentRESTService service;
	ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setUp() throws Exception {
		service = Startup.getInstance().getBean(DD_AgentRESTService.class);
	}

	@Test
	public void testIntake() throws JsonParseException, JsonMappingException, IOException {
		DDAgentData agentData = mapper.readValue(DD_AgentRestServiceTest.class.getResourceAsStream("/dd_agent.json"), DDAgentData.class);
		HttpServletRequest request =EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(request.getAttribute("_tenant_id_")).andReturn("e10adc3949ba59abbe56e057f20f88dd");
		EasyMock.replay(request);
		service.intake(agentData, request);
	}
}
