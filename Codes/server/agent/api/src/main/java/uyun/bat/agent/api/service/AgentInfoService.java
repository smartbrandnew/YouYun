package uyun.bat.agent.api.service;


import uyun.bat.agent.api.entity.Agent;

import javax.servlet.http.HttpServletRequest;

public interface AgentInfoService {
	/**
	 * 保存agent信息
	 * @param agent
	 * @param request
     * @return
     */
	boolean saveAgent(Agent agent, HttpServletRequest request);
}
