package com.broada.carrier.monitor.impl.storage.emc.vnx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MathUtil {
	public static double percentages(String denominator, String molecule){
		Log log = LogFactory.getLog(MathUtil.class);
		float total = Float.valueOf(molecule );
		float userTotal = Float.valueOf(denominator);
		if(total == 0.0 && userTotal == 0.0){
			return 0;
		}else
			//由于采集到的总容量以M为单位，使用量以G为单位，所以需要进行一次单位转换
			userTotal = userTotal*1024;
			//log.warn("userTotal转换单位为M，使用容量为："+userTotal+"M!");
			return (Math.floor(userTotal / total  * 100 *100 )/100);
	}
	
	public static double unusedpercentages(String denominator, String molecule){
		Log log = LogFactory.getLog(MathUtil.class);
		float total = Float.valueOf(molecule );
		float userTotal = Float.valueOf(denominator);
		if(total == 0.0 && userTotal == 0.0){
			return 0;
		}else
			//由于采集到的总容量以M为单位，使用量以G为单位，所以需要进行一次单位转换
			//userTotal = userTotal*1024;
		return Math.floor(total/1024-userTotal);
			//return (Math.floor(userTotal / total  * 100 *100 )/100);
	}
	
	public static double MB2GB(String molecule) {
		float total = Float.valueOf(molecule );
		return Math.floor(total/1024);
	}
}
