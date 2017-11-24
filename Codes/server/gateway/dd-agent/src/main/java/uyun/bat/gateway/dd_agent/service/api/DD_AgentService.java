package uyun.bat.gateway.dd_agent.service.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import uyun.bat.gateway.dd_agent.entity.DDAgentData;
import uyun.bat.gateway.dd_agent.entity.DDSeries;
import uyun.bat.gateway.dd_agent.entity.DDServiceCheck;
import uyun.bat.gateway.dd_agent.entity.NetDevData;

public interface DD_AgentService {

	void intake(DDAgentData data, HttpServletRequest request);

	void series(DDSeries series, HttpServletRequest request);

	void check_run(List<DDServiceCheck> checks, HttpServletRequest request);

	void ping(NetDevData data, HttpServletRequest request);

	/**
	 * 暂缺
	 */
	void intakeMetrics(String data, HttpServletRequest request);

	/**
	 * 暂缺
	 */
	void intakeMedata(String data, HttpServletRequest request);

	long getCurrentTime(HttpServletRequest request);

}
