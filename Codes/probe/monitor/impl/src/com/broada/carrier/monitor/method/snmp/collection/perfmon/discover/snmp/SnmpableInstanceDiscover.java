package com.broada.carrier.monitor.method.snmp.collection.perfmon.discover.snmp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.snmp.collection.dynamic.DynamicInstance;
import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.discover.DiscoverUtil;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.discover.InstanceDiscover;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.discover.InstanceDiscoverException;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp.Exp;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp.ExpGroup;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp.ExpGroupCollection;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp.PerfExp;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp.Producer;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpTarget;

/**
 * Snmp协议的实例发现
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-23 17:32:48
 */
public class SnmpableInstanceDiscover implements InstanceDiscover {
  private static final Log logger = LogFactory.getLog(SnmpableInstanceDiscover.class);
  
  /**
   * 
   */
  public SnmpableInstanceDiscover() {
    super();
  }

  /*
   * 
   * @see com.broada.collection.perfmon.discover.InstanceDiscover#discover(com.broada.collection.entity.IpNode, int, com.broada.collection.entity.PerfType)
   */
  public DynamicInstance[] discover(SnmpTarget target,int timeout,PerfType type) throws InstanceDiscoverException{
    if(target==null){
      throw new NullPointerException("进行SNMP实例发现的节点信息不能为空.");
    }
    if(type==null){
      throw new NullPointerException("必须指定需要SNMP实例发现的性能类型.");
    }    
    long producerCode=-1;
    if(producerCode<0){
      //实时获取目标ProducerCode
      try {
        producerCode=DiscoverUtil.getProducerCode(target);
      } catch (SnmpException e) {
        if(logger.isDebugEnabled()){
          logger.debug("获取节点["+target.getIp()+"]的ProducerCode时发生异常.",e);
        }
        throw new InstanceDiscoverException("获取节点的ProducerCode发生异常,可能是配置错误.",e);
      }
      if(producerCode<0){
        if(logger.isDebugEnabled()){
          logger.debug("获取节点["+target.getIp()+"]的ProducerCode失败.");
        }
        return null;
      }
    }
    Producer producer = PerfExp.getInstance().getProducers().get(producerCode);
    if (producer == null) {
      if(logger.isDebugEnabled()){
        logger.debug("获取不到ProducerCode["+producerCode+"]的Producer.");
      }
      throw new InstanceDiscoverException("不支持的ProducerCode["+producerCode+"],请获取对应设备的MIB,提交给技术支持.");
    }
    
    List<SnmpableInstance> insts = new ArrayList<SnmpableInstance>();
    ExpGroupCollection expGroups = producer.getExpGroups().getByType(type);
    for (Iterator<ExpGroup> groupIter = expGroups.iterator(); groupIter.hasNext(); ) {
      ExpGroup group=(ExpGroup)groupIter.next();
      for (Iterator<Exp> iter = group.getExps().iterator(); iter.hasNext();) {
        Exp exp = (Exp)iter.next();
        String[] instances=null;
        try {
          instances = DiscoverUtil.discoverInstances(target,timeout, exp.getInstance());
        } catch (SnmpException e) {
          if(logger.isDebugEnabled()){
            logger.debug("根据OID["+exp.getInstance()+"]获取节点["+target.getIp()+"]的实例时发生异常.",e);
          }
          instances=new String[0];
        }
        if (instances.length > 0){
          if(logger.isDebugEnabled()){
            logger.debug("根据[("+exp.getId()+")"+exp.getInstance()+"]成功获取到"+instances.length+"个实例.");
          }
          for (int i = 0; i < instances.length; i++) {
            String instStr=instances[i];
            if(instStr.startsWith(".")){
              instStr=instStr.substring(1);
            }
            String key=exp.getId()+"."+instStr;
            String name=exp.getType()+"["+instStr+"]";
            SnmpableInstance inst=new SnmpableInstance(key,name,type);
            inst.setIndex(instStr);
            inst.setUtilizeExp(exp.getValue());
            insts.add(inst);
          }
          //同一个group内只有发现有一个就可以了
          break;
        }
      }
    }
    return (DynamicInstance[])insts.toArray(new SnmpableInstance[0]);
  }

}
