package com.broada.carrier.monitor.common.spring;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;

import com.broada.component.utils.error.ErrorUtil;

/**
 * 一个spring servlet启动包装器，避免spring context初始化失败时webapp不退出
 * @author Jiangjw
 */
public class FailShutdownDispatcherServlet extends DispatcherServlet {
	private static final Logger logger = LoggerFactory.getLogger(FailShutdownDispatcherServlet.class);
	private static final long serialVersionUID = 1L;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			super.init(config);
		} catch (Throwable e) {
			ErrorUtil.exit(logger, "Spring初始化失败", e, 1);
		}
	}
}
