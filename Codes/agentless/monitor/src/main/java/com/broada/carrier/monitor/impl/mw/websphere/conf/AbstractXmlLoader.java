package com.broada.carrier.monitor.impl.mw.websphere.conf;

import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


/**
 * @author lixy Sep 17, 2008 10:21:34 AM
 */
public abstract class AbstractXmlLoader {
  
  protected static Document buildDoc(String fileName, Class child) {
    SAXBuilder sb = new SAXBuilder();
    Document doc = null;
    try {
      doc = sb.build(child.getResourceAsStream(fileName));
    } catch (JDOMException e) {
      throw new RuntimeException("解析XML文件[" + fileName + "]出错", e);
    } catch (IOException e) {
      throw new RuntimeException("XML文件[" + fileName + "]不存在", e);
    }
    return doc;
  }
}
