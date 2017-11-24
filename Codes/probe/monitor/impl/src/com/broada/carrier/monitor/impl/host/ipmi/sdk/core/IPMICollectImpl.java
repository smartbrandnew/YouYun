package com.broada.carrier.monitor.impl.host.ipmi.sdk.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.common.config.BaseConfig;
import com.broada.carrier.monitor.impl.host.ipmi.disk.DiskInfo;
import com.broada.carrier.monitor.impl.host.ipmi.disk.DiskState;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.BasicInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.ChassisInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.EntityType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.HealthInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.HealthType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMICollect;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIParameter;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.QuotaInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.SDRType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.SensorType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.ServerType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common.ChannelInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common.Constants;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common.Util;

public class IPMICollectImpl implements IPMICollect {
	public static final Log logger = LogFactory.getLog(IPMICollectImpl.class);
	private String command;
	
	public IPMICollectImpl(IPMIParameter param) {
		this.command = createCommandLine(param, null);
	}

	public static String createCommandLine(IPMIParameter param, String command) {
		StringBuilder sb = new StringBuilder();
		BaseConfig config = new BaseConfig();
		String interf = config.getProps().get("ipmi.use.interface", "lanplus");
		sb.append("ipmitool -I ").append(interf);
		sb.append(" -H ").append(param.getHost());
		sb.append(" -U ").append(param.getUsername());
		sb.append(" -P ").append(param.getPassword());
		if (param.getLevel() != null && !param.getLevel().isEmpty())
			sb.append(" -L ").append(param.getLevel());
		if (command != null)
			sb.append(" ").append(command);
		return sb.toString();
	}

	/*
	 * @see com.broada.module.bmc.api.IPMICollect#loadAccount(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	public ServerType checkAccount() throws IPMIException {		
		String command = this.command + " fru print";
		long start = System.currentTimeMillis();
		String[] lineResults;
		try {
			lineResults = Util.exec(command);
			for (String inline : lineResults) {
				if (inline.indexOf("Device not present") > -1) {
					return checkAccountByPower();
				}
				if (inline.indexOf(ServerType.DELL.getLabel()) > -1 && inline.indexOf(ServerType.DELL.getValue()) > -1) {
					return ServerType.DELL;
				}
				if (inline.indexOf(ServerType.HP.getLabel()) > -1 && inline.indexOf(ServerType.HP.getValue()) > -1) {
					return ServerType.HP;
				}
				if (inline.indexOf(ServerType.IBM.getLabel()) > -1 && inline.indexOf(ServerType.IBM.getValue()) > -1) {
					return ServerType.IBM;
				}
			}
			logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - start));
		} catch (IPMIException e) {
			throw e;
		} catch (Exception e) {
			throw new IPMIException(String.format("读取监测服务器账户命令返回信息失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)),
					e);
		}
		return ServerType.UNK;
	}

	private ServerType checkAccountByPower() throws IPMIException {
		String command = this.command + " chassis power status";
		long start = System.currentTimeMillis();
		String[] lineResults;
		try {
			lineResults = Util.exec(command);
			for (String inline : lineResults) {
				if (inline.toLowerCase().indexOf("chassis power is") < 0) {
					return ServerType.ERROR;
				}
			}
			logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - start));
		} catch (IPMIException e) {
			throw e;
		} catch (Exception e) {
			throw new IPMIException(String.format("读取监测服务器账户命令返回信息失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)),
					e);
		}
		return ServerType.UNK;
	}


	/*
	 * @see com.broada.module.bmc.api.IPMICollect#getQuotaInfos(java.util.Map)
	 */
	public List<QuotaInfo> getQuotaInfos(Map<EntityType, List<SensorType>> map) throws IPMIException {
		List<SensorType> list;
		Map<String, QuotaInfo> mapQuota = new HashMap<String, QuotaInfo>();
		List<QuotaInfo> listQuota = new ArrayList<QuotaInfo>();
		String command = this.command + " sdr type Fan";
		String[] strs;
		QuotaInfo qi;
		double value;
		Date date = new Date();
		String[] lineResults;
		try {
			lineResults = Util.exec(command);
			for (String inline : lineResults) {
				if (inline.indexOf("RPM") < 0) {
					continue;
				}
				strs = inline.split("\\|");
				qi = new QuotaInfo();
				qi.setName("风扇-" + strs[0].trim());
				qi.setType(EntityType.FAN);
				value = getValue(strs[4], SensorType.FAN.getValue());
				if (value < 0) {
					continue;
				}
				qi.setFanSpeed(value);
				mapQuota.put(qi.getName(), qi);
			}
			for (EntityType key : map.keySet()) {
				list = map.get(key);
				if (list.size() > 0) {
					listQuota = getQuotaInfos(key, list);
					for (QuotaInfo quotaInfo : listQuota) {
						mapQuota.put(quotaInfo.getName(), quotaInfo);
					}
				}
			}
			logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - date.getTime()));
		} catch (IPMIException e) {
			throw e;
		} catch (Exception e) {
			throw new IPMIException(
					String.format("读取监测服务器风扇信息命令返回信息失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)), e);
		}
		return new ArrayList<QuotaInfo>(mapQuota.values());
	}

	/*
	 * @see
	 * com.broada.module.bmc.api.IPMICollect#getQuotaInfos(com.broada.module.bmc
	 * .api.EntityType, com.broada.module.bmc.api.SensorType)
	 */
	public List<QuotaInfo> getQuotaInfos(EntityType et, List<SensorType> types) throws IPMIException {
		if (types == null || types.size() < 1) {
			return Collections.emptyList();
		}
		List<QuotaInfo> listQuota = new ArrayList<QuotaInfo>();
		String command = this.command + " sdr entity " + et.getValue();
		String[] strs;
		QuotaInfo qi;
		Double d;
		double value;
		Date date = new Date();
		String[] lineResults;
		Map<Double, QuotaInfo> map = new HashMap<Double, QuotaInfo>();
		try {
			lineResults = Util.exec(command);
			for (String inline : lineResults) {
				if (!isContain(types, inline)) {
					continue;
				}
				strs = inline.split("\\|");
				d = Double.valueOf(strs[3].trim());
				qi = map.get(d);
				if (qi == null) {
					qi = new QuotaInfo();
				}
				qi.setName(et.getLabel() + "-" + strs[3].trim());
				if (types.contains(SensorType.CUR) && strs[4].indexOf(SensorType.CUR.getValue()) > -1) {
					value = getValue(strs[4], SensorType.CUR.getValue());
					if (value < 0) {
						continue;
					}
					qi.setCurrent(value);
				}
				if (types.contains(SensorType.POW) && strs[4].indexOf(SensorType.POW.getValue()) > -1) {
					value = getValue(strs[4], SensorType.POW.getValue());
					if (value < 0) {
						continue;
					}
					qi.setPower(value);
				}
				if (types.contains(SensorType.TEM) && strs[4].indexOf(SensorType.TEM.getValue()) > -1) {
					value = getValue(strs[4], SensorType.TEM.getValue());
					if (value < 0) {
						continue;
					}
					qi.setTemperature(value);
				}
				if (types.contains(SensorType.VOL) && strs[4].indexOf(SensorType.VOL.getValue()) > -1) {
					value = getValue(strs[4], SensorType.VOL.getValue());
					if (value < 0) {
						continue;
					}
					qi.setVoltage(value);
				}
				qi.setType(et);
				listQuota.add(qi);
				map.put(d, qi);
			}
			if (types.contains(SensorType.TEM) && et == EntityType.PROCE && listQuota.size() == 0) {
				command = this.command + " sdr list";
				String[] lineResults1;
				Date d2 = new Date();
				lineResults1 = Util.exec(command);
				for (String inline : lineResults1) {
					if (inline.indexOf(SensorType.TEM.getValue()) < 0 || inline.toLowerCase().indexOf("cpu") < 0
							|| inline.toLowerCase().indexOf("|") < 0) {
						continue;
					}
					strs = inline.split("\\|");
					qi = new QuotaInfo();
					qi.setName(strs[0].trim());
					for (String string : strs) {
						if (string.indexOf(SensorType.TEM.getValue()) > -1) {
							qi.setTemperature(getValue(string, SensorType.TEM.getValue()));
						}
					}
					qi.setType(et);
					listQuota.add(qi);
				}
				logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - d2.getTime()));
			}
			logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - date.getTime()));
		} catch (IPMIException e) {
			throw e;
		} catch (Exception e) {
			throw new IPMIException(
					String.format("读取监测服务器指标信息命令返回信息失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)), e);
		}
		return listQuota;
	}

	private boolean isContain(List<SensorType> types, String str) {
		for (SensorType type : types) {
			if (str.indexOf(type.getValue()) > -1) {
				return true;
			}
		}
		return false;
	}

	private double getValue(String str, String unit) {
		if (Util.isEmpty(str)) {
			throw new RuntimeException(String.format("监测值为空"));
		}
		if (str.indexOf(unit) < 0) {
			throw new RuntimeException(String.format("监测值无效"));
		}
		String[] strs = str.trim().split(" ");
		return Double.parseDouble(strs[0]);
	}

	/*
	 * @see com.broada.module.bmc.api.IPMICollect#getChassisInfo()
	 */
	public ChassisInfo getChassisInfo() throws IPMIException {
		String command = this.command + " chassis status";
		String[] strs;
		ChassisInfo ci = new ChassisInfo();
		long start = System.currentTimeMillis();
		String[] lineResults;
		try {
			lineResults = Util.exec(command);
			for (String inline : lineResults) {
				if (inline.indexOf(":") < 0) {
					continue;
				}
				strs = inline.split(":");
				if (Constants.SYSTEM_POWER.equalsIgnoreCase(strs[0].trim())) {
					ci.setSystemPower(strs[1].trim());
				}
				if (Constants.POWER_OVERLOAD.equalsIgnoreCase(strs[0].trim())) {
					ci.setPowerOverload(Boolean.parseBoolean(strs[1].trim()));
				}
				if (Constants.POWER_INTERLOCK.equalsIgnoreCase(strs[0].trim())) {
					ci.setPowerInterlock(strs[1].trim());
				}
				if (Constants.MAIN_POWER_FAULT.equalsIgnoreCase(strs[0].trim())) {
					ci.setMainPowerFault(Boolean.parseBoolean(strs[1].trim()));
				}
				if (Constants.POWER_CONTROL_FAULT.equalsIgnoreCase(strs[0].trim())) {
					ci.setPowerControlFault(Boolean.parseBoolean(strs[1].trim()));
				}
				if (Constants.CHASSIS_INTRUSION.equalsIgnoreCase(strs[0].trim())) {
					ci.setChassisIntrusion(strs[1].trim());
				}
				if (Constants.PANEL_LOCKOUT.equalsIgnoreCase(strs[0].trim())) {
					ci.setPanelLockout(strs[1].trim());
				}
				if (Constants.DRIVE_FAULT.equalsIgnoreCase(strs[0].trim())) {
					ci.setDriverFault(Boolean.parseBoolean(strs[1].trim()));
				}
				if (Constants.COOLING_FAULT.equalsIgnoreCase(strs[0].trim())) {
					ci.setRadiatingFault(Boolean.parseBoolean(strs[1].trim()));
				}
			}
			command = this.command + " sdr entity 10";
			HealthInfo hi;
			boolean bool = true;
			String code;
			List<HealthInfo> his = new ArrayList<HealthInfo>();
			String[] lineResults1 = Util.exec(command);
			for (String inline : lineResults1) {
				if (inline.indexOf("|") < 0) {
					continue;
				}
				inline = inline.toLowerCase();
				if (inline.indexOf(Constants.SDR_CODE_PS) < 0
						&& (inline.indexOf(Constants.SDR_CODE_POWER) < 0 || inline.indexOf(Constants.SDR_CODE_SUPPLY) < 0)) {
					continue;
				}
				strs = inline.split("\\|");
				if (SDRType.parseFromLable(strs[2].trim()) == SDRType.NS) {
					continue;
				}
				hi = new HealthInfo();
				hi.setName("电源-" + strs[0].trim());
				code = SDRType.parseFromLable(strs[2].trim()).getValue();
				hi.setValue(HealthType.parseFromLable(code));
				his.add(hi);
				bool = (code.equals("1")) && bool;
				logger.debug(String.format("电源HealthInfo：%s", hi.getName() + hi.getValue()));
			}
			ci.setSystemPower(bool ? "on" : "off");
			ci.setPowers(his);
			logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - start));
		} catch (IPMIException e) {
			throw e;
		} catch (Throwable e) {
			throw new IPMIException(
					String.format("读取监测服务器状态信息命令返回消息失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)), e);
		}
		return ci;
	}

	public List<BasicInfo> getBasicInfo() throws IPMIException {
		String command = this.command + " fru print";
		List<BasicInfo> bis = new ArrayList<BasicInfo>();
		BasicInfo bi = new BasicInfo();
		String[] strs;
		String name;
		long start = System.currentTimeMillis();
		String[] lineResults;
		try {
			lineResults = Util.exec(command);
			for (String inline : lineResults) {
				if (inline.indexOf("FRU Device Description") > -1) {
					if (bi.isNotEmpty() && StringUtils.isNotBlank(bi.getTitle())) {
						bis.add(bi);
					}
					bi = new BasicInfo();
					if (inline.indexOf("Builtin FRU Device") > -1) {
						bi.setTitle(Constants.BASIC_SEVER_NAME);
					} else {
						strs = inline.split(":");
						if (strs.length > 1) {
							name = strs[1];
							if (name == null || "".equals(name.trim())) {
								continue;
							}
							name = name.trim().toLowerCase();
							if (name.indexOf("ps") > -1 || (name.indexOf("power") > -1 && name.indexOf("supply") > -1)
									|| name.indexOf("cpu") > -1 || name.indexOf("processor") > -1 || name.indexOf("storage") > -1
									|| name.indexOf("disk") > -1 || name.indexOf("memory") > -1 || name.indexOf("ram") > -1
									|| name.indexOf("board") > -1) {
								bi.setTitle(name);
							} else {
								continue;
							}
						}
					}
				}
				if (inline.indexOf("Board Mfg") > -1 || inline.indexOf("Product Manufacturer") > -1) {
					strs = inline.split(":");
					if (strs.length > 1) {
						bi.setMfg(strs[1].trim());
					}
				}
				if (inline.indexOf("Board Product") > -1 || inline.indexOf("Product Name") > -1) {
					strs = inline.split(":");
					if (strs.length > 1) {
						bi.setName(strs[1].trim());
					}
				}
				if (inline.indexOf("Board Serial") > -1 || inline.indexOf("Product Serial") > -1) {
					strs = inline.split(":");
					if (strs.length > 1) {
						bi.setSerial(strs[1].trim());
					}
				}
				if (inline.indexOf("Board Part Number") > -1 || inline.indexOf("Product Part Number") > -1) {
					strs = inline.split(":");
					if (strs.length > 1) {
						bi.setPartNum(strs[1].trim());
					}
				}
				if (inline.indexOf("Capacity") > -1) {
					strs = inline.split(":");
					if (strs.length > 1) {
						bi.setCapacity(strs[1].trim());
					}
				}
				if (inline.indexOf("Input Voltage Range 1") > -1) {
					strs = inline.split(":");
					if (strs.length > 1) {
						bi.setInVoltRange(strs[1].trim());
					}
				}
				if (inline.indexOf("Input Frequency Range") > -1) {
					strs = inline.split(":");
					if (strs.length > 1) {
						bi.setInFreqRange(strs[1].trim());
					}
				}
				if (inline.indexOf("Flags") > -1) {
					strs = inline.split(":");
					if (strs.length > 1) {
						bi.setFlags(strs[1].trim());
					}
				}
			}
			if (bi.isNotEmpty() && StringUtils.isNotBlank(bi.getTitle())) {
				bis.add(bi);
			}
			logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - start));
		} catch (IPMIException e) {
			throw e;
		} catch (Exception e) {
			throw new IPMIException(String.format("读取监测服务器账户命令返回信息失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)),
					e);
		}
		return bis;
	}

	public List<HealthInfo> getHealthInfo() throws IPMIException {
		String command = this.command + " channel info";
		List<HealthInfo> his = new ArrayList<HealthInfo>();
		ChannelInfo bi = new ChannelInfo();
		HealthInfo hi;
		String[] strs;
		long start = System.currentTimeMillis();
		String[] lineResults;
		try {
			lineResults = Util.exec(command);
			for (String inline : lineResults) {
				if (inline.indexOf(":") < 0) {
					continue;
				}
				if (inline.indexOf("Channel") > -1 && inline.indexOf("info") > -1) {
					if (!bi.isEmpty() && StringUtils.isNotBlank(bi.getTitle())) {
						hi = new HealthInfo();
						hi.setName(bi.getTitle());
						hi.setValue(HealthType.NORMAL);
						his.add(hi);
					}
					bi = new ChannelInfo();
					strs = inline.split(":");
					bi.setTitle("网卡-" + strs[0].replace("info", "").trim());
				}
				if (inline.indexOf(Constants.CHANNEL_MEDIUM_TYPE) > -1) {
					strs = inline.split(":");
					bi.setMedium(strs[1].trim());
				}
				if (inline.indexOf(Constants.CHANNEL_PROTOCOL_TYPE) > -1) {
					strs = inline.split(":");
					bi.setMedium(strs[1].trim());
				}
				if (inline.indexOf(Constants.SESSION_SUPPORT) > -1) {
					strs = inline.split(":");
					bi.setMedium(strs[1].trim());
				}
				if (inline.indexOf(Constants.ACTIVE_SESSION_COUNT) > -1) {
					strs = inline.split(":");
					bi.setMedium(strs[1].trim());
				}
				if (inline.indexOf(Constants.PROTOCOL_VENDOR_ID) > -1) {
					strs = inline.split(":");
					bi.setMedium(strs[1].trim());
				}
			}
			if (!bi.isEmpty() && StringUtils.isNotBlank(bi.getTitle())) {
				hi = new HealthInfo();
				hi.setName(bi.getTitle());
				hi.setValue(HealthType.NORMAL);
				his.add(hi);
			}
			command = this.command + " sdr entity 4";
			String[] lineResults1 = Util.exec(command);
				for (String inline : lineResults1) {
				logger.debug(String.format("HealthInfo：%s", inline));
				strs = inline.split("\\|");
				if (strs[0].toLowerCase().indexOf("cntlr") > -1 || strs[0].toLowerCase().indexOf("bay") > -1
						|| strs[0].toLowerCase().indexOf("drive") > -1) {
					if (SDRType.parseFromLable(strs[2].trim()) == SDRType.NS) {
						continue;
					}
					hi = new HealthInfo();
					hi.setName("硬盘-" + strs[0].trim());
					hi.setValue(HealthType.parseFromLable(SDRType.parseFromLable(strs[2].trim()).getValue()));
					his.add(hi);
					logger.debug(String.format("HealthInfo：%s", hi.getName() + hi.getValue()));
				}
			}
			logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - start));
		} catch (IPMIException e) {
			throw e;
		} catch (Exception e) {
			throw new IPMIException(String.format("读取监测服务器账户命令返回信息失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)),
					e);
		}
		return his;
	}

	@Override
	public List<DiskInfo> getDiskInfo() throws IPMIException {
		String command = this.command + " sdr entity 4";
		List<DiskInfo> infos = new ArrayList<DiskInfo>();
		long start = System.currentTimeMillis();
		String[] lineResults;
		try {
			lineResults = Util.exec(command);
			for (String inline : lineResults) {   // 有没有插硬盘
				if (inline.indexOf("Drive Present") < 0 || inline.indexOf("Device Inserted") < 0) {   // 在不在位
					continue;
				}
				DiskInfo bi = new DiskInfo();
				bi.setDiskName(inline.split("|")[0].trim());   // 取硬盘名称
				if (inline.contains("Fault") || inline.contains("Failure")
						|| inline.contains("In Critical Array") 
						|| inline.contains("In Failed Array"))
					bi.setDiskStat(DiskState.ERROR.getCode());
				else
					bi.setDiskStat(DiskState.OK.getCode());
				infos.add(bi);
			}
			logger.debug(String.format("%s读取耗时：%s", command, new Date().getTime() - start));
		} catch (IPMIException e) {
			throw e;
		} catch (Exception e) {
			throw new IPMIException(String.format("读取监测服务器账户命令返回信息失败，错误：%s", (e.getMessage() != null ? e.getMessage() : e)),
					e);
		}
		return infos;
	}
	
}
