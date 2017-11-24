package com.broada.carrier.monitor.impl.icmp;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitorParameter;

/**
 * ICMP 监听器参数类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Eric Liu
 * @version 1.0
 */

public class ICMPParameter extends BaseMonitorParameter {
	private static final long serialVersionUID = 1L;
	static final int RULE_LOOSE=0;
	static final int RULE_STRICT=1;

	public ICMPParameter() {
	}

	public ICMPParameter(String str) {
		super(str);
	}

	protected void create() {    
		setTimeout(100); //超时多少毫秒
		setRequestCount(3); //测试的次数
		setRequestInterval(0); //每次测试延迟多少秒进行
		setRuleType(RULE_LOOSE);
	}

	/**请求超时设置*/
	public void setTimeout(int timeout) {
		set("timeout",String.valueOf(timeout));
	}

	public int getTimeout() {
		return get("timeout", 500);
	}

	/**请求次数*/
	public void setRequestCount(int count) {
		set("requestCount",String.valueOf(count));
	}

	public int getRequestCount() {
		return get("requestCount", 3);
	}

	/**请求间隔*/
	public void setRequestInterval(int interval) {
		set("requestInterval",String.valueOf(interval));
	}

	public int getRequestInterval() {
		return get("requestInterval", 1000);
	}

	public void setTTLAvgLimit(int ttlLimit)
	{
		set("ttlavg",String.valueOf(ttlLimit));
	}

	public int getTTLAvgLimit() {
		return get("ttlavg", 1000);
	}

	public int getRuleType(){
		return get("rule", RULE_STRICT);
	}

	public void setRuleType(int type){
		set("rule",String.valueOf(type));
	}

	/**
	 * 获得扩展监测的IP地址表
	 * @return
	 */
	public List<String> getExtAddrs(){
		String addrs=(String) get("extaddrs");
		if(addrs!=null && addrs.trim().length() >0){
			String[] addrArr = addrs.split(";");
			return Arrays.asList(addrArr);
		}
		return Arrays.asList("");
	}

	/**
	 * 设置扩展IP地址表
	 * @param extAddrs
	 */
	public void setExtAddrs(List<String> extAddrs){
		if(extAddrs==null || extAddrs.size() ==0){
			remove("extaddrs");
		}else{
			StringBuffer sb=new StringBuffer();
			for (Iterator<String> iter = extAddrs.iterator(); iter.hasNext(); ) {
				sb.append(iter.next()+";");
			}
			set("extaddrs",sb.toString());
		}
	}
}