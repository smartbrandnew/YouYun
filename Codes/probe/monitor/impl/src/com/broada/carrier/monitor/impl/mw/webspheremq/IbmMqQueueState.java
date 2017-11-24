package com.broada.carrier.monitor.impl.mw.webspheremq;

public class IbmMqQueueState {
  private static String MQQA_GET_INHIBITED = "禁止";
  private static String MQQA_GET_ALLOWED = "允许";

  public static  String getAllowedDesc(int state) {
    String str = null;
    switch (state) {
    case 0:
      str = MQQA_GET_ALLOWED;
      break;
    case 1:
      str = MQQA_GET_INHIBITED;
      break;
    default:
      str = "";
    }
    return str;
  }
  
  public static  int getAllowedNum(String state) {
  	if(MQQA_GET_ALLOWED.equals(state)){
  		return 0;
  	}
  	if(MQQA_GET_INHIBITED.equals(state)){
  		return 1;
  	}
  	return -1;
  	
  }
}
