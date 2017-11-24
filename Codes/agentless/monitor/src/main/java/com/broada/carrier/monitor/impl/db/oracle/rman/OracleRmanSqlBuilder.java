package com.broada.carrier.monitor.impl.db.oracle.rman;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * <p>Title: OracleRmanSqlBuilder</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx (panlx@broada.com.cn)
 * @version 2.4
 */
public class OracleRmanSqlBuilder {

  private static final Map sqls = new HashMap();

  static{
    SAXBuilder sb = new SAXBuilder();
    Document doc = null;
    try {
      doc = sb.build(new File("conf/OracleRmanBak.xml"));
    } catch (JDOMException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Element root = doc.getRootElement();
    Element rmanE = root.getChild("Rman");
    Element bakStatusE = rmanE.getChild("BakStatus");
    sqls.put(bakStatusE.getName(), bakStatusE.getValue());
    Element fullBakE = rmanE.getChild("FullBak");
    sqls.put(fullBakE.getName(), fullBakE.getValue());
    Element incBakE = rmanE.getChild("IncBak");
    sqls.put(incBakE.getName(), incBakE.getValue());
  }
  
  public static String getSql(String key){
    return (String)sqls.get(key);
  }
}
