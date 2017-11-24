package com.broada.carrier.monitor.impl.common.convertor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.impl.common.convertor.impl.DoubleDecimalFormattor;
import com.broada.carrier.monitor.impl.common.convertor.impl.Kb2MbConvertor;
import com.broada.carrier.monitor.impl.common.convertor.impl.Msel2SecConvertor;
import com.broada.utils.ListUtil;

public class ConvertUtil {

  /**
   * 单位转换
   * 
   * @param convertor
   * @param srcValue
   * @return
   */
  public static double doConvert(MonResultConvertor convertor, double srcValue) {
    return convertor.doConvert(srcValue);
  }

  /**
   * 按convertors内转换器的顺序进行单位转换
   * 
   * @param convertors  该List内对象支持MonResultUnitConvertor或是MonResultUnitConvertor的Spring配置的ID
   * @param srcValue  转换前的数据
   * @return 转换后的数据
   */
  @SuppressWarnings("unchecked")
  public static double doConvert(List convertors, double srcValue) {
    if (ListUtil.isNullOrEmpty(convertors)) {
      return srcValue;
    }
    double tmp = srcValue;
    for (int i = 0; i < convertors.size(); i++) {
      MonResultConvertor convertor = buildUnitConvertor(convertors.get(i));
      tmp = convertor.doConvert(tmp);
    }
    return tmp;
  }

  public static MonResultConvertor buildUnitConvertor(Object obj) {
    if (obj instanceof MonResultConvertor) {
      return (MonResultConvertor) obj;
    } else if (obj instanceof String) {
    	return convertors.get(obj);    	      
    } else {
      throw new RuntimeException("不支持的对象类型" + obj.getClass().getName());
    }
  }
  
  private static Map<String, MonResultConvertor> convertors;
  
  static {
  	convertors = new HashMap<String, MonResultConvertor>();
  	convertors.put("doubleDecimalFormattor", new DoubleDecimalFormattor());
  	convertors.put("kb2MbConvertor", new Kb2MbConvertor());
  	convertors.put("msel2SecConvertor", new Msel2SecConvertor());  	
  }  
}
