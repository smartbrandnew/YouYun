package com.broada.carrier.monitor.impl.mw.websphere.entity.was;

import java.util.ArrayList;
import java.util.List;

import com.broada.carrier.monitor.impl.common.convertor.MonResultConvertor;

/**
 * @author lixy Sep 16, 2008 2:34:03 PM
 */
public class Item {
  private String id;
  private String code;
  private String name;
  private String eleId;
  private String attr;
  private String value;
  private String bsh;
  private List<MonResultConvertor> convertors = new ArrayList<MonResultConvertor>();

  public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEleId() {
    return eleId;
  }

  public void setEleId(String eleId) {
    this.eleId = eleId;
  }

  public String getAttr() {
    return attr;
  }

  public void setAttr(String attr) {
    this.attr = attr;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBsh() {
    return bsh;
  }

  public void setBsh(String bsh) {
    this.bsh = bsh;
  }

  public List<MonResultConvertor> getConvertors() {
    return convertors;
  }

  public void addConvertor(MonResultConvertor convertor) {
    this.convertors.add(convertor);
  }

}
