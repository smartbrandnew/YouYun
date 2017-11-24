package com.broada.carrier.monitor.impl.mw.weblogic.agent.ejb;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.Property;

public class EJBRuntimeInformation {
	private static final Log logger = LogFactory.getLog(EJBRuntimeInformation.class);
	private String ejbName;

	private String ejbType;

	protected long transactionsCommittedTotalCount;

	protected long transactionsRolledBackTotalCount;

	protected long transactionsTimedOutTotalCount;

	private long accessTotalCount;

	private long beansInUseCurrentCount;

	private long destroyedTotalCount;

	private long missTotalCount;

	private long pooledBeansCurrentCount;

	private long waiterCurrentCount;

	private long lockEntriesCurrentCount;

	private long lockManagerAccessCount;

	private long activationCount;

	private long cacheAccessCount;

	private long cachedBeansCurrentCount;

	private long cacheMissCount;

	private long passivationCount;

	private long cacheHitCount;

	public long getAccessTotalCount() {
		return accessTotalCount;
	}

	public void setAccessTotalCount(long accessTotalCount) {
		this.accessTotalCount = accessTotalCount;
	}

	public long getActivationCount() {
		return activationCount;
	}

	public void setActivationCount(long activationCount) {
		this.activationCount = activationCount;
	}

	public long getBeansInUseCurrentCount() {
		return beansInUseCurrentCount;
	}

	public void setBeansInUseCurrentCount(long beansInUseCurrentCount) {
		this.beansInUseCurrentCount = beansInUseCurrentCount;
	}

	public long getCacheAccessCount() {
		return cacheAccessCount;
	}

	public void setCacheAccessCount(long cacheAccessCount) {
		this.cacheAccessCount = cacheAccessCount;
	}

	public long getCachedBeansCurrentCount() {
		return cachedBeansCurrentCount;
	}

	public void setCachedBeansCurrentCount(long cachedBeansCurrentCount) {
		this.cachedBeansCurrentCount = cachedBeansCurrentCount;
	}

	public long getCacheHitCount() {
		return cacheHitCount;
	}

	public void setCacheHitCount(long cacheHitCount) {
		this.cacheHitCount = cacheHitCount;
	}

	public long getCacheMissCount() {
		return cacheMissCount;
	}

	public void setCacheMissCount(long cacheMissCount) {
		this.cacheMissCount = cacheMissCount;
	}

	public long getDestroyedTotalCount() {
		return destroyedTotalCount;
	}

	public void setDestroyedTotalCount(long destroyedTotalCount) {
		this.destroyedTotalCount = destroyedTotalCount;
	}

	public long getLockEntriesCurrentCount() {
		return lockEntriesCurrentCount;
	}

	public void setLockEntriesCurrentCount(long lockEntriesCurrentCount) {
		this.lockEntriesCurrentCount = lockEntriesCurrentCount;
	}

	public long getLockManagerAccessCount() {
		return lockManagerAccessCount;
	}

	public void setLockManagerAccessCount(long lockManagerAccessCount) {
		this.lockManagerAccessCount = lockManagerAccessCount;
	}

	public long getMissTotalCount() {
		return missTotalCount;
	}

	public void setMissTotalCount(long missTotalCount) {
		this.missTotalCount = missTotalCount;
	}

	public long getPassivationCount() {
		return passivationCount;
	}

	public void setPassivationCount(long passivationCount) {
		this.passivationCount = passivationCount;
	}

	public long getPooledBeansCurrentCount() {
		return pooledBeansCurrentCount;
	}

	public void setPooledBeansCurrentCount(long pooledBeansCurrentCount) {
		this.pooledBeansCurrentCount = pooledBeansCurrentCount;
	}

	public long getWaiterCurrentCount() {
		return waiterCurrentCount;
	}

	public void setWaiterCurrentCount(long waiterCurrentCount) {
		this.waiterCurrentCount = waiterCurrentCount;
	}

	public long getTransactionsCommittedTotalCount() {
		return transactionsCommittedTotalCount;
	}

	public void setTransactionsCommittedTotalCount(
			long transactionsCommittedTotalCount) {
		this.transactionsCommittedTotalCount = transactionsCommittedTotalCount;
	}

	public long getTransactionsRolledBackTotalCount() {
		return transactionsRolledBackTotalCount;
	}

	public void setTransactionsRolledBackTotalCount(
			long transactionsRolledBackTotalCount) {
		this.transactionsRolledBackTotalCount = transactionsRolledBackTotalCount;
	}

	public long getTransactionsTimedOutTotalCount() {
		return transactionsTimedOutTotalCount;
	}

	public void setTransactionsTimedOutTotalCount(
			long transactionsTimedOutTotalCount) {
		this.transactionsTimedOutTotalCount = transactionsTimedOutTotalCount;
	}

	public String getEjbName() {
		return ejbName;
	}

	public void setEjbName(String ejbName) {
		this.ejbName = ejbName;
	}

	public String getEjbType() {
		return ejbType;
	}

	public void setEjbType(String ejbType) {
		this.ejbType = ejbType;
	}

	public void addProperty(Property ejbProperty) throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("==>添加:" + ejbProperty.getName() + "==" + ejbProperty.getValue());
		}
		if (PropertyUtils.isWriteable(this, ejbProperty.getName()))
			BeanUtils.setProperty(this, ejbProperty.getName(), ejbProperty
					.getValue());
	}
}
