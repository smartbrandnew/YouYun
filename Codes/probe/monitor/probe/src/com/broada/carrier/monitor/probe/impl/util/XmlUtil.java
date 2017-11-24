package com.broada.carrier.monitor.probe.impl.util;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XmlUtil {  
  /** 
   * 根据xml路径得到rootElement 
   * @param xmlPath 
   * @return 
   * @throws IOException  
   */  
  public static Element getXMLRoot(InputStream in) throws IOException {  
      SAXBuilder builder = new SAXBuilder();  
      Document doc = null;  
      Element root = null;  
      try {  
          doc = builder.build(in);  
          root = doc.getRootElement();  
      } catch (JDOMException e) {  
          e.printStackTrace();  
      } catch (IOException e) {  
          e.printStackTrace();  
      }finally{  
          in.close();  
      }  
      return root;  
  }  
    
  /** 
   * 根据xml路径得到rootElement 重载方法 
   * @param xmlPath 
   * @return 
   * @throws IOException  
   */  
  public static Element getXMLRoot(String xmlPath)  {  
      SAXBuilder builder = new SAXBuilder();  
      FileInputStream in = null;  
      Document doc = null;  
      Element root = null;  
      try {  
          in = new FileInputStream(new File(xmlPath));  
          doc = builder.build(in);  
          root = doc.getRootElement();  
      } catch (JDOMException e) {  
          e.printStackTrace();  
      } catch (IOException e) {  
          e.printStackTrace();  
      } finally {  
          try {  
              if(in != null) {  
                  in.close();  
              }  
          } catch (IOException e) {  
              e.printStackTrace();  
          }  
      }  
      return root;  
  }  
    
  /** 
   * 根据xml文档对象Docuemnt生成xml文件到指定路径 
   * @param doc 
   * @param xmlPath 
   */  
  public static void createXML(Document doc, String xmlPath) throws Exception{  
      XMLOutputter outputter = null;   
      Format format = Format.getCompactFormat();   
      format.setEncoding("UTF-8");   
      format.setIndent("    ");   
      outputter = new XMLOutputter(format);   
      FileOutputStream out = null;  
      try {  
          out = new FileOutputStream(xmlPath);  
          outputter.output(doc, out);  
      } catch (FileNotFoundException e) {  
          e.printStackTrace();  
          throw e;  
      } catch (IOException e) {  
          e.printStackTrace();  
          throw e;  
      } finally {  
          if(out != null)  {  
              out.close();  
          }  
      }  
  }  
}  