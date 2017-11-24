package com.broada.carrier.monitor.impl.mw.weblogic.agent.ejb;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.basic.WLSBasicMonitorUtil;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.utils.StringUtil;

public class WLSEJBMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(WLSEJBMonitor.class);
  private static final String ITEMIDX_WLSEJB_ACTIVATION = "WLSEJB-1";

  private static final String ITEMIDX_WLSEJB_PASSIVATION = "WLSEJB-2";

  private static final String ITEMIDX_WLSEJB_POOLEDBEANS = "WLSEJB-3";

  private static final String ITEMIDX_WLSEJB_TRANSACTIONSCOMMITTED = "WLSEJB-4";

  private static final String ITEMIDX_WLSEJB_TRANSACTIONSROLLEDBACK = "WLSEJB-5";

  private static final String ITEMIDX_WLSEJB_TRANSACTIONSTIMEDOUT = "WLSEJB-6";

  private static final String ITEMIDX_WLSEJB_ACCESS = "WLSEJB-7";

  private static final String EJB_DESCRIPTION = "{0}:{1}-被访问{2}次,激活{3}次,钝化{4}次,缓存了{5}个实例,事务提交次数{6}次,事务回滚次数{7},事务超时次数{8}次";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    EJBCollection ejbCollection = null;
    WebLogicJMXOption option = new WebLogicJMXOption(context.getMethod());
    long respTime = System.currentTimeMillis();
    try {
      ejbCollection = WLSEJBMonitorUtil.getEjbCollection(getUrl(option));
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("获取失败", e);
      }
      result.setResultDesc("获取失败");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    //计算响应时间
    respTime=System.currentTimeMillis()-respTime;
    if(respTime<=0){
      respTime=1;
    }
    result.setResponseTime(respTime);
    List ejbs = ejbCollection.getEjbCollections();
    for (int index = 0; index < ejbs.size(); index++) {
      EJBRuntimeInformation ejbRuntimeInformation = (EJBRuntimeInformation) ejbs.get(index);
      MonitorResultRow row = new MonitorResultRow(ejbRuntimeInformation.getEjbName());
      row.setIndicator(ITEMIDX_WLSEJB_ACTIVATION, ejbRuntimeInformation.getActivationCount());
      row.setIndicator(ITEMIDX_WLSEJB_PASSIVATION, ejbRuntimeInformation.getPassivationCount());
      row.setIndicator(ITEMIDX_WLSEJB_POOLEDBEANS, ejbRuntimeInformation.getPooledBeansCurrentCount());
      row.setIndicator(ITEMIDX_WLSEJB_TRANSACTIONSCOMMITTED, ejbRuntimeInformation
          .getTransactionsCommittedTotalCount());
      row.setIndicator(ITEMIDX_WLSEJB_TRANSACTIONSROLLEDBACK, ejbRuntimeInformation
          .getTransactionsRolledBackTotalCount());
      row.setIndicator(ITEMIDX_WLSEJB_TRANSACTIONSTIMEDOUT, ejbRuntimeInformation
          .getTransactionsTimedOutTotalCount());
      row.setIndicator(ITEMIDX_WLSEJB_ACCESS, ejbRuntimeInformation.getAccessTotalCount());
      result.addRow(row);
      String ejbDesc = MessageFormat.format(EJB_DESCRIPTION, new Object[] { ejbRuntimeInformation.getEjbType(),
          ejbRuntimeInformation.getEjbName(), "" + ejbRuntimeInformation.getAccessTotalCount(),
          "" + ejbRuntimeInformation.getActivationCount(), "" + ejbRuntimeInformation.getPassivationCount(),
          "" + ejbRuntimeInformation.getPooledBeansCurrentCount(),
          "" + ejbRuntimeInformation.getTransactionsCommittedTotalCount(),
          "" + ejbRuntimeInformation.getTransactionsRolledBackTotalCount(),
          "" + ejbRuntimeInformation.getTransactionsTimedOutTotalCount() });

      result.setResultDesc(StringUtil.isNullOrBlank(result.getResultDesc()) ? ejbDesc
          : (result.getResultDesc() + "\n" + ejbDesc));
    }
    return result;
  }

  private String getUrl(WebLogicJMXOption webLogicJMXOption) throws Exception {
    return WLSBasicMonitorUtil.getEJBInfoUrl(webLogicJMXOption);
  }
}
