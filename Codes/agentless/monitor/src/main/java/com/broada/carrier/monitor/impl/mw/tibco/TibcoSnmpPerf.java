package com.broada.carrier.monitor.impl.mw.tibco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.broada.snmp.InstanceItem;
import com.broada.snmp.SnmpException;
import com.broada.snmp.SnmpNotFoundException;
import com.broada.snmp.SnmpUtil;
import com.broada.snmp.SnmpWalk;
import com.broada.snmp.SnmpWalkUtil;

public class TibcoSnmpPerf {
  public static final String TRDP_MC_DATA_PKT = "rvTrdpMCDataPktTotal";
  public static final String TRDP_MC_BAD_PKT = "rvTrdpMCBadPktTotal";
  public static final String TRDP_MC_SEQGAP_PKT = "rvTrdpMCSeqGapTotal";
  public static final String TRDP_MC_NULL_PKT = "rvTrdpMCNullPktTotal";

  public static final String TRDP_RT_REQ_PKT = "rvTrdpRtReqPktTotal";
  public static final String TRDP_RT_REJ_PKT = "rvTrdpRtRejPktTotal";
  public static final String TRDP_RT_BAD_PKT = "rvTrdpRtBadPktTotal";

  public static final String PTP_TOTAL_DATA = "rvPtpDataPktTotal";
  public static final String PTP_NAK_PKT = "rvPtpNakPktTotal";
  public static final String PTP_BAD_PKT = "rvPtpBadPktTotal";

  //rvSubjTable.rvSubjEntry
  public static final String SUB_ENTRY_SUB_PORT = "rvSubjPort";
  public static final String SUB_ENTRY_SUB_NAME = "rvSubject";
  public static final String SUB_ENTRY_SUB_MSGS = "rvSubjMsgs";
  public static final String SUB_ENTRY_SUB_BYTES = "rvSubjBytes";

  public static Map<String, String> oids = new HashMap<String, String>();
  {
    oids.put("rvTrdpMCDataPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.1.1");
    oids.put("rvTrdpMCNullPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.1.5");
    oids.put("rvTrdpMCSeqGapTotal", ".1.3.6.1.4.1.2000.7.1.100.1.1.6");
    oids.put("rvTrdpMCBadPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.1.7");

    oids.put("rvTrdpRtReqPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.2.1");
    oids.put("rvTrdpRtRejPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.2.3");
    oids.put("rvTrdpRtBadPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.2.4");

    oids.put("rvPtpDataPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.3.1");
    oids.put("rvPtpNakPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.3.5");
    oids.put("rvPtpBadPktTotal", ".1.3.6.1.4.1.2000.7.1.100.1.3.6");

    oids.put("rvSubjPort", ".1.3.6.1.4.1.2000.7.1.100.13.1.1");
    oids.put("rvSubject", ".1.3.6.1.4.1.2000.7.1.100.13.1.2");
    oids.put("rvSubjMsgs", ".1.3.6.1.4.1.2000.7.1.100.13.1.3");
    oids.put("rvSubjBytes", ".1.3.6.1.4.1.2000.7.1.100.13.1.4");

  }
  
  private SnmpWalk snmpwalk=null;

  public TibcoSnmpPerf() {
    this("localhost", 161, "public", SnmpUtil.getVersion("v1"));
  }

  public TibcoSnmpPerf(String host, int port, String community, int version) {
    snmpwalk = new SnmpWalk(version, host, port, community);
  }

  public Map<String, String> getOidItemPerf(String[] columns) throws Exception {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < columns.length; i++) {
      String oid = (String) oids.get(columns[i]);
      long value = SnmpWalkUtil.getFirstLongValue(snmpwalk, oid);
      map.put(columns[i], Long.toString(value));
    }
    return map;
  }

  public List<String> getSubjEntrys() throws SnmpNotFoundException, SnmpException, Exception {
    List<String> entrys = new ArrayList<String>();
    InstanceItem[] items = null;
    items = SnmpWalkUtil.getInstanceItems(snmpwalk, ".1.3.6.1.4.1.2000.7.1.100.13.1.3");
    for (int i = 0; i < items.length; i++) {
      entrys.add(items[i].getInstance());
    }
    return entrys;
  }

  public Map<Object, String> getSubjEntrysPerf(String[] columns) throws Exception {
    if (columns == null || columns.length == 0 ||
        !oids.keySet().containsAll(Arrays.asList(columns))) {
      throw new Exception("无效的选项");
    }
    List<String> instance = this.getSubjEntrys();

    Map<Object, String> value = new HashMap<Object, String>();
 
    for (int i = 0; i < columns.length; i++) {
      String oid = (String) oids.get(columns[i]);
      if (oid == null)
        continue;
      for (Iterator<String> iter = instance.iterator(); iter.hasNext(); ) {
        String instkey = (String)iter.next();
        if (instkey == null)
          continue;
        double dv = 0.0;
        dv = SnmpWalkUtil.getExpressionValue(snmpwalk, oid, instkey);
        value.put(oid + "." + instkey, Double.toString(dv));
      }
    }
    return value;
  }
  
  /**
   * 关闭相关连接,释放资源
   */
  public void close(){
    if(snmpwalk!=null){
      snmpwalk.close();
    }
  }
  
}
