package com.broada.carrier.monitor.impl.host.snmp.disk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.broada.common.util.Unit;
import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpRow;
import com.broada.snmputil.SnmpTable;
import com.broada.snmputil.SnmpTarget;
import com.broada.snmputil.SnmpValue;

/**
 * 通过SNMP获取和管理磁盘信息的类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class SnmpDiskManager {
	public static final class DiskAreaInfo {
		public final String instance;//="0";
		public final String label;//="所有分区";    
		private long size;
		private long used;

		public DiskAreaInfo(String instance, String label, long size, long used) {
			this.instance = instance;
			this.label = label;
			this.size = size;
			this.used = used;
		}

		public String getInstance() {
			return instance;
		}

		public String getLabel() {
			return label;
		}

		/**
		 * 磁盘大小，单位byte
		 * @return
		 */
		public long getSize() {
			return size;
		}

		/**
		 * 磁盘大小，单位byte
		 * @return
		 */
		public long getUsed() {
			return used;
		}

		public String toString() {
			return String.format("%s[%s size: %s usage: %s%%]", getClass().getSimpleName(), label, Unit.B.formatPrefer(size),
					getUsage());
		}

		public double getUsage() {
			if (size == 0)
				return 0;
			return Math.round(used * 1000.0 / size) / 10;
		}

		/**
		 * 磁盘空闲大小，单位byte
		 * @return
		 */
		public long getFree() {
			if (size == 0)
				return 0;
			return size - used;
		}
	}

	private static final SnmpOID OID_DISKTYPE = new SnmpOID(".1.3.6.1.2.1.25.2.3.1.2");
	private static final SnmpOID OID_DISKDESC = new SnmpOID(".1.3.6.1.2.1.25.2.3.1.3");
	private static final SnmpOID OID_UNIT_BYTE = new SnmpOID(".1.3.6.1.2.1.25.2.3.1.4");
	private static final SnmpOID OID_DISKSIZE = new SnmpOID(".1.3.6.1.2.1.25.2.3.1.5");
	private static final SnmpOID OID_DISKUSED = new SnmpOID(".1.3.6.1.2.1.25.2.3.1.6");
	private static final SnmpOID[] OID_DISK = new SnmpOID[] { OID_DISKTYPE, OID_DISKDESC, OID_UNIT_BYTE, OID_DISKSIZE,
			OID_DISKUSED };
	private static final String TYPE_FIXEDISK = ".1.3.6.1.2.1.25.2.1.4";

	private static final SnmpOID OID_HPUX_DISKBLOCKTOTAL = new SnmpOID(".1.3.6.1.4.1.11.2.3.1.2.2.1.4");
	private static final SnmpOID OID_HPUX_DISKBLOCKFREE = new SnmpOID(".1.3.6.1.4.1.11.2.3.1.2.2.1.5");
	private static final SnmpOID OID_HPUX_DISKBLOCKSIZE = new SnmpOID(".1.3.6.1.4.1.11.2.3.1.2.2.1.7");
	private static final SnmpOID OID_HPUX_DISKDESC = new SnmpOID(".1.3.6.1.4.1.11.2.3.1.2.2.1.10");
	private static final SnmpOID[] OID_HPUX = new SnmpOID[] { OID_HPUX_DISKBLOCKTOTAL, OID_HPUX_DISKBLOCKFREE,
			OID_HPUX_DISKBLOCKSIZE, OID_HPUX_DISKDESC };

	private SnmpTarget target;

	public SnmpDiskManager(SnmpTarget target) {
		this.target = target;
	}

	private List<DiskAreaInfo> getHpux() throws SnmpException {
		target.setDiscardErrorRow(true);
		SnmpTable table = Snmp.walkTable(target, OID_HPUX);

		List<DiskAreaInfo> list = new ArrayList<DiskAreaInfo>();
		if (table == null || table.getRows().isEmpty())
			return list;

		// 找出所有是硬盘类型的分区
		long sumTotal = 0;
		long sumFree = 0;
		for (Object obj : table.getRows()) {
			SnmpRow row = (SnmpRow) obj;
			String instance = row.getInstance().toString();
			SnmpValue blockTotalVar = row.getCell(OID_HPUX_DISKBLOCKTOTAL).getValue();
			SnmpValue blockFreeVar = row.getCell(OID_HPUX_DISKBLOCKFREE).getValue();
			SnmpValue blockSize = row.getCell(OID_HPUX_DISKBLOCKSIZE).getValue();

			long total = blockTotalVar.toLong() * blockSize.toLong();
			long free = blockFreeVar.toLong() * blockSize.toLong();
			sumTotal += total;
			sumFree += free;
			list.add(new DiskAreaInfo(instance.substring(1), row.getCell(OID_HPUX_DISKDESC).getValue().toText(), total, total
					- free));
		}

		if (list.size() > 0)
			list.add(new DiskAreaInfo("0", "所有", sumTotal, sumTotal - sumFree));
		return list;
	}

	private List<DiskAreaInfo> getStandard() throws SnmpException {
		SnmpTable table = Snmp.walkTable(target, OID_DISK);

		List<DiskAreaInfo> list = new ArrayList<DiskAreaInfo>();
		if (table == null || table.getRows().isEmpty())
			return list;

		//找出所有是硬盘类型的分区    
		long sumTotal = 0;
		long sumUsed = 0;
		for (Object obj : table.getRows()) {
			SnmpRow row = (SnmpRow) obj;
			if (TYPE_FIXEDISK.equals("." + row.getCell(OID_DISKTYPE).getValue().toText())) {
				SnmpValue descVar = row.getCell(OID_DISKDESC).getValue();
				SnmpValue unitByte = row.getCell(OID_UNIT_BYTE).getValue();
				SnmpValue sizeVar = row.getCell(OID_DISKSIZE).getValue();
				SnmpValue usedVar = row.getCell(OID_DISKUSED).getValue();

				long size = sizeVar.toLong() * unitByte.toLong();
				long used = usedVar.toLong() * unitByte.toLong();
				sumTotal += size;
				sumUsed += used;
				String desc = descVar.toText();
				String[] descs = desc.split(" ");
				list.add(new DiskAreaInfo(row.getInstance().toString().substring(1), descs[0], size, used));
			}
		}

		if (list.size() > 0)
			list.add(new DiskAreaInfo("0", "所有", sumTotal, sumUsed));

		return list;
	}

	/**
	 * 得到硬盘的磁盘分区
	 * @return 返回DiskAreaInfo对象列表
	 * @throws SnmpException
	 * @throws SnmpException 
	 */
	public List<DiskAreaInfo> getFixeDiskArea() throws SnmpException {
		List<DiskAreaInfo> result = getStandard();
		if (result == null || result.isEmpty())
			result = getHpux();
		return result;
	}
}