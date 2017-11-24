package uyun.bat.event.api.entity;

import java.util.ArrayList;
import java.util.List;

/**
 *资源类型（1：monitor，2：dd-agent）
 */
public enum EventSourceType {

    MONITOR((short)1,"monitor"),DATADOG_AGENT((short)2,"dd-agent"),OPEN_API((short)3,"open-api");

    EventSourceType(short key,String value) {
    		this.key = key;
        this.value = value;
    }

    private short key;
    private String value;

    public short getKey() {
        return key;
    }

    public void setKey(short key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static Short[] getSourceType(String[] types){
        Short[] sourceType=new Short[]{};
        List<Short> list=new ArrayList<Short>();
        if (null==types||types.length<1){
            return sourceType;
        }
        EventSourceType[] eventSourceTypes= EventSourceType.values();
        for(String type:types){
            for(EventSourceType eventSourceType:eventSourceTypes){
                if (type.equals(eventSourceType.getValue())){
                    list.add(eventSourceType.getKey());
                }
            }
        }
        Short[] sourceTypes=list.toArray(sourceType);
        return sourceTypes;
    }

    public static short getByValue(String s) {
         if (s.equals(MONITOR.getValue())){
             return MONITOR.getKey();
         }else if (s.equals(DATADOG_AGENT.getValue())){
             return DATADOG_AGENT.getKey();
         }else if (s.equals(OPEN_API.getValue())){
        	 	 return OPEN_API.getKey();
				}
        return -1;
    }
    
    public static String checkName(short k) {
      if (1 == k){
          return MONITOR.getValue();
      }else if (2 == k){
          return DATADOG_AGENT.getValue();
      }else{
          return OPEN_API.getValue();
      }
  }
}
