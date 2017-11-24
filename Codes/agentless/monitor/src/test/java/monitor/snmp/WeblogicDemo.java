package monitor.snmp;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpTarget;

public class WeblogicDemo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ip = "10.1.11.86";
		SnmpTarget target = new SnmpTarget();
		target.setIp(ip);
		target.setPort(1611);
		target.setVersion(1);
		target.setTimeout(2000);
		target.setRetryTime(3);
		target.setReadCommunity("public");
		
		SnmpWalk walk = new SnmpWalk(target);
		
		try {
			SnmpResult[] results = walk.snmpWalk(".1.3.6.1.4.1.140.625.340.1");
			if(results != null && results.length > 0){
				for(SnmpResult result:results){
					System.out.println("oid:" + result.getOid().toString() + " \t value:" + result.getValue().toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			walk.close();
		}
	}
	
}
