package com.broada.carrier.monitor.impl.ew.domino.common;

import com.broada.carrier.monitor.impl.ew.domino.basic.DominoUtil;
import com.broada.carrier.monitor.impl.ew.domino.perf.ValueType;
import com.broada.carrier.monitor.server.api.entity.PerfResult;

import lotus.domino.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * 支持R5，R6，R7版本，提供了便捷的操作模板
 *
 * @author Eric Liu (liudh@broada.com.cn)
 */
public class DominoTemplate {

	private static final Log logger = LogFactory.getLog(DominoTemplate.class);

	private Session sess = null;

	public static DominoTemplate getInstance(String ip, String user, String password, int iorPort) throws Exception {
		DominoTemplate domino = new DominoTemplate();

		try {
			domino.sess = NotesFactory.createSession(ip, user, password);
		} catch (NotesException e) {
			logger.debug("通过标准方式无法获取Session,尝试使用先获取IOR方式.IP=" + ip + ",user=" + user, e);
			String ior = DominoUtil.getIOR(ip, iorPort); // "http://"+ip+":"+port+"/diiop_ior.txt");
			domino.sess = NotesFactory.createSessionWithIOR(ior, user, password);
		}
		return domino;
	}

	public Session getSession() {
		return sess;
	}

	public void recycle() {
		try {
			if (sess != null) {
				sess.recycle();
			}
		} catch (Exception e) {
		}
	}

	protected void finalize() {
		recycle();
	}

	// double int timestamp string
	public Object getFirstDocValue(String dbName, String viewName, String itemName) throws NotesException {
		Database db = null;
		View view = null;
		try {
			db = getDatabase(dbName);
			view = getView(db, viewName);
			Document d = view.getFirstDocument();
			if (d == null)
				throw new NotesException(-1,
						"Domino服务器" + sess.getServerName() + ",数据库" + db.getFileName() + ",视图" + viewName + "中没有文档,无法获取监测信息!");
			Vector v = d.getItemValue(itemName);
			if (v == null || v.size() == 0)
				throw new NotesException(-1,
						"Domino服务器" + sess.getServerName() + ",数据库" + db.getFileName() + ",视图" + viewName + "中的文档无法获取字段" + itemName
								+ "!");
			return v.get(0);
		} finally {
			recycle(view);
			recycle(db);
		}
	}

	public Object[] getFirstDocValues(String dbName, String viewName, String[] itemNames) throws NotesException {
		Database db = null;
		View view = null;
		try {
			db = getDatabase(dbName);
			view = db.getView(viewName);
			Document d = view.getFirstDocument();
			if (d == null)
				throw new NotesException(-1,
						"Domino服务器" + sess.getServerName() + ",数据库" + db.getFileName() + ",视图" + viewName + "中没有文档,无法获取监测信息!");
			Object os[] = new Object[itemNames.length];
			for (int i = 0; i < itemNames.length; i++) {
				String itemName = itemNames[i];
				Vector v = d.getItemValue(itemName);
				if (v != null && v.size() > 0) {
					os[i] = v.get(0);
				} else {
					try {
						os[i] = d.getItemValueString(itemName);
						if (os[i] == null)
							os[i] = new Double(d.getItemValueDouble(itemName));
					} catch (Exception ex) {
						logger.error(
								"Domino服务器" + sess.getServerName() + ",数据库" + db.getFileName() + ",视图" + viewName + "中的文档无法获取字段"
										+ itemName + "!");
					}
				}
			}
			return os;
		} finally {
			recycle(view);
			recycle(db);
		}
	}

	public String[] getDBList() throws NotesException {
		try {
			List l = new ArrayList();
			DbDirectory dbdir = sess.getDbDirectory(sess.getServerName()); // remote iiop need be null arg
			Database db = dbdir.getFirstDatabase(DbDirectory.DATABASE);
			if (db != null) {
				//这里采用fileName，还是pathName还有疑惑，测试感觉应该使用filepath
				l.add(db.getFilePath());
				while ((db = dbdir.getNextDatabase()) != null)
					l.add(db.getFilePath());
			}
			return (String[]) l.toArray(new String[0]);
		} catch (NotesException e) {
			throw new NotesException(-1, "获取Domino服务器" + sess.getServerName() + "的数据库列表出错!", e);
		}
	}

	public double getDBPercentUsed(String dbName) throws NotesException {
		Database db = getDatabase(dbName);
		try {
			return db.getPercentUsed();
		} finally {
			recycle(db);
		}
	}

	private Database getDatabase(String name) throws NotesException {
		Database db = sess.getDatabase(sess.getServerName(), name);
		if (db == null)
			throw new NotesException(-1, "无法获取Domino服务器" + sess.getServerName() + "中的数据库" + name + "!");
		return db;
	}

	/**
	 * db not null
	 */
	private View getView(Database db, String name) throws NotesException {
		View v = db.getView(name);
		if (v == null)
			throw new NotesException(-1, "无法获取Domino服务器" + sess.getServerName() + ",数据库" + db.getFileName() + "中的视图" + name);
		return v;
	}

	private void recycle(View v) {
		if (v != null) {
			try {
				v.recycle();
			} catch (Exception ex) {
			}
		}
	}

	private void recycle(Database db) {
		if (db != null) {
			try {
				db.recycle();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * 根据监测性能项的索引，设置值,数据封装在性能列表perfList中
	 *
	 * @param itemCode
	 * @param perfList
	 * @param i
	 * @param vs
	 * @param itemType
	 */
	public int constructMonitorItem(String itemCode, List<PerfResult> perfList, int i, Object[] vs, int itemType) {
		//该比对所用索引数据皆为数据库中的数据
		if (itemCode.equals("DOMINO_PERF-52")) {//死信率:Mail.Dead/Mail.Delivered
			double mailDead = 0;
			double mailDelivered = 0;
			for (Iterator<PerfResult> itPerf = perfList.iterator(); itPerf.hasNext(); ) {
				PerfResult pr = itPerf.next();
				if (pr.getItemCode().equals("DOMINO_PERF-30")) {
					mailDead = (Double) pr.getValue();
					continue;
				}
				if (pr.getItemCode().equals("DOMINO_PERF-33")) {
					mailDelivered = (Double) pr.getValue();
					continue;
				}
			}
			PerfResult perf = new PerfResult(itemCode, true);
			if (mailDelivered != 0) {
				perf.setValue(
						new BigDecimal((mailDead / mailDelivered) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			} else {
				perf.setValue(0);
			}
			perfList.add(perf);
		} else if (itemCode.equals("DOMINO_PERF-53")) {//总的处理数:Mail.TotalRouted+Mail.TotalFailures
			double TotalRouted = 0;
			for (Iterator<PerfResult> itPerf = perfList.iterator(); itPerf.hasNext(); ) {
				PerfResult pr = itPerf.next();
				if (pr.getItemCode().equals("DOMINO_PERF-31")) {
					TotalRouted = (Double) pr.getValue();
					break;
				}
			}
			PerfResult perfr = new PerfResult(itemCode, true);
			if (vs[i] != null) {
				perfr.setValue(TotalRouted + Double.parseDouble(vs[i].toString()));
			} else {
				perfr.setValue(TotalRouted);
			}
			perfList.add(perfr);
			i++;
		} else if (itemCode.equals("DOMINO_PERF-55")) {//剩余率:Disc.c.Free/Disc.c.Size
			double freeSize = 0;
			for (Iterator<PerfResult> itPerf = perfList.iterator(); itPerf.hasNext(); ) {
				PerfResult pr = itPerf.next();
				if (pr.getItemCode().equals("DOMINO_PERF-54")) {
					freeSize = (Double) pr.getValue();
					break;
				}
			}
			PerfResult perfFree = new PerfResult(itemCode, true);
			if (vs[i] != null) {
				double size = Double.parseDouble(vs[i].toString());
				if (size != 0) {
					perfFree
							.setValue(new BigDecimal((freeSize / size) * 100).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
				} else {
					perfFree.setValue(0);
				}
			} else {
				perfFree.setValue(0);
			}
			perfList.add(perfFree);
			i++;
		} else {
			if (vs[i] != null) {
				PerfResult pr = new PerfResult(itemCode, false);
				if (itemType == ValueType.STRING)
					pr.setStrValue(vs[i].toString());
				else if (itemType == ValueType.DOUBLE)
					pr.setValue(Double.parseDouble(vs[i].toString()));
				else
					pr.setValue(Double.parseDouble(vs[i].toString()));
				perfList.add(pr);
			}
			i++;
		}
		return i;
	}

	// public Object getSessionValue(String sessionMethodName,Object[] args);

	// public Object getDatabaseValue(String dbName,String dbMethodName,Object[] args);

	// public Object getViewValue(String dbName,String viewName,String viewMethodName,Object[] args);

}
