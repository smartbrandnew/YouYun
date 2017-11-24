package uyun.bat.event.api.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 警告级别（0：成功，1：消息，2：警告，3：紧急）
 */
public enum EventServerityType {

    SUCCESS((short)0,"success"),INFO((short)1,"info"),WARNING((short) 2,"warning"),ERROR((short)3,"error");

    EventServerityType(short key,String value){
        this.key=key;
        this.value=value;
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

    public static Short[] getServerityType(String[] types){
        Short[] serverityType=new Short[]{};
        if (null==types||types.length<1){
            return serverityType;
        }
        List<Short> list=new ArrayList<Short>();
        EventServerityType[] eventServerityTypes= EventServerityType.values();
        for(String type:types){
            for(EventServerityType eventServerityType:eventServerityTypes){
                if (type.equals(eventServerityType.getValue())){
                    list.add(eventServerityType.getKey());
                }
            }
        }
        Short[] serverityTypes=list.toArray(serverityType);
        return serverityTypes;
    }

    public static String checkName(short k) {
      if (0 == k){
          return SUCCESS.getValue();
      }else if (1 == k){
          return INFO.getValue();
      }else if (2 == k){
          return WARNING.getValue();
      }else{
          return ERROR.getValue();
      }
  }
    public static EventServerityType checkByValue(String name) {
        for (EventServerityType value : EventServerityType.values()) {
            if (value.getValue().equals(name))
                return value;
        }
        return null;
    }

}