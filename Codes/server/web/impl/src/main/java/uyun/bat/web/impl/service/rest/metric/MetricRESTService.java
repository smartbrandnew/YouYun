package uyun.bat.web.impl.service.rest.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.QueryMetric;
import uyun.bat.datastore.api.util.StringUtils;
import uyun.bat.web.api.metric.entity.BatchSeries;
import uyun.bat.web.api.metric.entity.MetaData;
import uyun.bat.web.api.metric.entity.MetricDataVO;
import uyun.bat.web.api.metric.entity.MetricMetaVO;
import uyun.bat.web.api.metric.entity.MetricTrashCleanQuery;
import uyun.bat.web.api.metric.entity.Series;
import uyun.bat.web.api.metric.entity.Top;
import uyun.bat.web.api.metric.entity.Unit;
import uyun.bat.web.api.metric.entity.Value;
import uyun.bat.web.api.metric.request.BatchRequestParams;
import uyun.bat.web.api.metric.request.SingleValueRequestParams;
import uyun.bat.web.api.metric.service.MetricWebService;
import uyun.bat.web.impl.common.entity.QueryParam;
import uyun.bat.web.impl.common.entity.Tag;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.entity.TopNQueryParam;
import uyun.bat.web.impl.common.service.ServiceManager;
 
import uyun.bat.web.impl.common.util.IntegrationUtil;
import uyun.bat.web.impl.common.util.QueryUtil;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;

@Service(protocol = "rest", version = "2.0")
@Path("v2/metrics")
public class MetricRESTService implements MetricWebService {

	@POST
	@Path("series/query")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public BatchSeries[] getSeries(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			BatchRequestParams requestParam) {
		 
		if (requestParam.getQ() == null && requestParam.getQ().length == 0)
			throw new IllegalArgumentException("no query!");

		BatchSeries[] batchSeries = new BatchSeries[requestParam.getQ().length];
		for (int k = 0; k < requestParam.getQ().length; k++) {
			BatchSeries bs = new BatchSeries();
			String q = requestParam.getQ()[k];
			QueryParam param = QueryUtil.parseSeriesQuery(q);
			QueryBuilder queryBuilder = new QueryBuilder();
			if (requestParam.getFrom() > System.currentTimeMillis())
				return batchSeries;
			queryBuilder.setStartAbsolute(requestParam.getFrom());
			queryBuilder.setEndAbsolute(requestParam.getTo());
			QueryMetric metric = queryBuilder.addMetric(param.getMetric()).addTenantId(tenantId);
			Unit unit = getUnit(param.getMetric());
			metric.addAggregatorType(AggregatorType.checkByName(param.getAggregator()));
			String scope = "*";
			if (param.getScope() != null && param.getScope().size() > 0) {
				Tag tag = param.getScope().get(0);
				if (!"*".equals(tag.getKey())) {
					metric.addTag(tag.getKey(), tag.getValue());
					StringBuilder sb = new StringBuilder();
					sb.append(tag.getKey());
					sb.append(":");
					sb.append(tag.getValue());
					sb.append(",");
					for (int i = 1; i < param.getScope().size(); i++) {
						tag = param.getScope().get(i);
						metric.addTag(tag.getKey(), tag.getValue());
						sb.append(tag.getKey());
						sb.append(":");
						sb.append(tag.getValue());
						sb.append(",");
					}
					scope = sb.deleteCharAt(sb.length() - 1).toString();
				}
			}
			// 对时间序列的column类型进行特殊处理 让柱状图展现不超过30个
			int interval = "column".equals(requestParam.getTypes()[k]) ? 6 * requestParam.getInterval()
					: requestParam.getInterval();
			// 时间序列也要有groupby了 特殊处理资源app仪表的query
			if (null != param.getGroupBy() && param.getGroupBy() != "*") {
				if (param.getGroupBy().contains(";")) {
					String[] groupBys = param.getGroupBy().split(";");
					for (String gb : groupBys) {
						metric.addGrouper(gb);
					}
				} else
					metric.addGrouper(param.getGroupBy());
			} else
				metric.addGrouper("*");

			// 时间序列添加排除标签
			if (null != param.getExclude() && param.getExclude() != "*") {
				if (param.getExclude().contains(";")) {
					String[] excludes = param.getExclude().split(";");
					for (String ex : excludes) {
						metric.addExclude(ex);
					}
				} else
					metric.addExclude(param.getExclude());
			}
			Map<String, PerfMetric> perfMetricList = ServiceManager.getInstance().getMetricService()
					.querySeriesGroupBy(queryBuilder, interval);
			if (perfMetricList != null && perfMetricList.size() > 0) {
				Series[] series = new Series[perfMetricList.size()];
				int i = 0;
				for (String str : perfMetricList.keySet()) {

					// for (int i = 0; i < perfMetricList.size(); i++) {
					PerfMetric p = perfMetricList.get(str);
					Series s = new Series();
					List<DataPoint> dataPoints = p.getDataPoints();
					if (dataPoints != null && dataPoints.size() > 0) {
						double[][] point = new double[dataPoints.size()][2];
						for (int j = 0; j < dataPoints.size(); j++) {
							point[j][0] = dataPoints.get(j).getTimestamp();
							point[j][1] = Double.parseDouble(dataPoints.get(j).getValue().toString());
						}
						s.setPoints(point);
					}
					s.setScope(str.equals("other") ? scope : str);
					s.setUnit(unit);
					series[i++] = s;
				}
				bs.setSeries(series);
			} else {
				Series[] series = new Series[1];
				Series s = new Series();
				s.setScope(scope);
				s.setPoints(new double[0][]);
				s.setUnit(unit);
				series[0] = s;
				bs.setSeries(series);
			}
			batchSeries[k] = bs;
		}
		return batchSeries;
	}

	@POST
	@Path("top/query")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Top[] getTop(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			BatchRequestParams requestParam) {
		 
		if (requestParam.getQ() == null && requestParam.getQ().length == 0)
			throw new IllegalArgumentException("no query!");
		Top[] topN = new Top[requestParam.getQ().length];
		TopNQueryParam param = QueryUtil.parseTopQuery(requestParam.getQ()[0]);
		QueryBuilder queryBuilder = new QueryBuilder();
		if (requestParam.getFrom() > System.currentTimeMillis())
			return topN;
		queryBuilder.setStartAbsolute(requestParam.getFrom());
		queryBuilder.setEndAbsolute(requestParam.getTo());
		QueryMetric metric = queryBuilder.addMetric(param.getMetric()).addTenantId(tenantId);
		Unit unit = getUnit(param.getMetric());
		if (null != param.getGroupBy() && param.getGroupBy() != "*")
			metric.addGrouper(param.getGroupBy());
		metric.addAggregatorType(AggregatorType.checkByName(param.getAggregator()));
		metric.setLimit(param.getLimit());
		metric.setOrder(QueryMetric.Order.checkByName(param.getOrder()));
		String scope = "*";
		// 用于存分组的tag
		String groupScope = null;
		if (param.getScope() != null && param.getScope().size() > 0) {
			Tag tag = param.getScope().get(0);
			if (!"*".equals(tag.getKey())) {
				metric.addTag(tag.getKey(), tag.getValue());
				StringBuilder sb = new StringBuilder();
				sb.append(tag.getKey());
				sb.append(":");
				sb.append(tag.getValue());
				sb.append(",");
				for (int i = 1; i < param.getScope().size(); i++) {
					tag = param.getScope().get(i);
					metric.addTag(tag.getKey(), tag.getValue());
					groupScope = tag.getKey().equals(param.getGroupBy()) ? tag.getKey() + ":" + tag.getValue()
							: groupScope;
					sb.append(tag.getKey());
					sb.append(":");
					sb.append(tag.getValue());
					sb.append(",");
				}
				scope = sb.deleteCharAt(sb.length() - 1).toString();
			}
		}
		if (null != param.getExclude() && param.getExclude() != "") {
			if (param.getExclude().contains(";")) {
				String[] excludes = param.getExclude().split(";");
				for (String ex : excludes) {
					metric.addExclude(ex);
				}
			} else
				metric.addExclude(param.getExclude());
		}
		int pointNum = param.getLimit();
		List<PerfMetric> perfMetricList = ServiceManager.getInstance().getMetricService().queryTopN(queryBuilder,
				pointNum);
		if (perfMetricList.size() == 0 || perfMetricList == null) {
			topN = new Top[1];
			String temp = groupScope;
			Top top = new Top();
			top.setScope(temp);
			top.setUnit(unit);
			topN[0] = top;
			return topN;
		}
		topN = new Top[perfMetricList.size()];
		for (int i = 0; i < perfMetricList.size(); i++) {
			PerfMetric p = perfMetricList.get(i);
			DataPoint dataPoint = p.getDataPoints().get(0);
			// 如果有分组就取分组的标签
			if (param.getGroupBy() != null) {
				List<String> s = p.getTags().get(param.getGroupBy());
				if (s != null)
					scope = param.getGroupBy() + ":" + s.get(0);
				else scope = "*";
			}
			Top top = new Top();
			top.setScope(scope);
			top.setUnit(unit);
			top.setValue(dataPoint.getValue().toString());
			topN[i] = top;
		}

		return topN;
	}

	@POST
	@Path("value/query")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Value getValue(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			SingleValueRequestParams singleValueRequestParams) {
		 
		if (singleValueRequestParams.getQ() == null && "".equals(singleValueRequestParams.getQ()))
			throw new IllegalArgumentException("no query!");
		Value value = new Value();
		String q = singleValueRequestParams.getQ();
		QueryParam param = QueryUtil.parseValueQuery(q);
		QueryBuilder queryBuilder = new QueryBuilder();
		if (singleValueRequestParams.getFrom() > System.currentTimeMillis())
			return value;
		queryBuilder.setStartAbsolute(singleValueRequestParams.getFrom());
		queryBuilder.setEndAbsolute(singleValueRequestParams.getTo());
		QueryMetric metric = queryBuilder.addMetric(param.getMetric()).addTenantId(tenantId);
		if (param.getScope() != null && param.getScope().size() > 0) {
			Tag tag = param.getScope().get(0);
			if (!"*".equals(tag.getKey())) {
				metric.addTag(tag.getKey(), tag.getValue());
				for (int i = 1; i < param.getScope().size(); i++) {
					tag = param.getScope().get(i);
					metric.addTag(tag.getKey(), tag.getValue());
				}
			}
		}
		Unit unit = getUnit(param.getMetric());
		metric.addAggregatorType(AggregatorType.checkByName(param.getAggregator()));
		PerfMetric perfMetric = ServiceManager.getInstance().getMetricService().queryPerf(queryBuilder);
		if (perfMetric != null && perfMetric.getDataPoints().size() > 0) {
			DataPoint dataPoint = perfMetric.getDataPoints().get(0);
			value.setUnit(unit);
			value.setValue(dataPoint.getValue().toString());
		}

		return value;
	}

	/**
	 * 获取单位
	 */
	private Unit getUnit(String metricName) {
		MetricMetaData meteData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metricName);
		Unit unit = new Unit();
		if (meteData != null) {
			unit.setSymbol(meteData.getUnit());
			unit.setMax(meteData.getValueMax());
			unit.setMin(meteData.getValueMin());
		}
		return unit;
	}



	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	public List<MetricMetaVO> getMetricNames(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@javax.ws.rs.QueryParam("metricName") String metricName, @javax.ws.rs.QueryParam("ranged") String ranged) {
		 

		List<MetricMetaVO> list = new ArrayList<>();
		Map<String, List<String>> maps = new HashMap<>();
		List<String> mames = ServiceManager.getInstance().getMetricService().getMetricNamesByTenantId(tenantId);
		boolean flag = false;
		List<String> temp = new ArrayList<>();

		List<MetricMetaData> metricMetaDatas = new ArrayList<>();
		//如果ranged不为空则过滤有最大最小值范围的指标元数据
		List<String> metaDataNames = new ArrayList<>();
		if (StringUtils.isNotNull(ranged)) {
			metricMetaDatas = ServiceManager.getInstance().getMetricMetaDataService().queryRangedMetaData(tenantId);
			for (MetricMetaData m : metricMetaDatas) {
				metaDataNames.add(m.getName());
			}
		}
		else
			metricMetaDatas= ServiceManager.getInstance().getMetricMetaDataService().queryAll(tenantId);
		for (String n : mames) {
			//只过滤最大最小值
			if(null != ranged && !metaDataNames.contains(n))
				continue;
			String inName = IntegrationUtil.changeMap.get(n) != null ? IntegrationUtil.changeMap.get(n)
					: (n.indexOf(".") == -1 ? n : n.substring(0, n.indexOf("."))).replace("wsnd", "websphere")
							.replace("coss", "业务").replace("business", "业务");
			List<String> ps = maps.get(inName);
			if (ps == null) {
				ps = new ArrayList<String>();
				maps.put(inName, ps);
			}
			flag = false;
			for (MetricMetaData m : metricMetaDatas)
				if (n.equals(m.getName())) {
					flag = true;
					ps.add(n + ":" + m.getcName());
				}
			if (!flag)
				ps.add(n + ":" + "");
		}
		for (String key : maps.keySet()) {
			if (IntegrationUtil.inMap.get(key) != null) {
				MetricMetaVO vo = new MetricMetaVO();
				vo.setMetaDatas(maps.get(key));
				vo.setIntegration(IntegrationUtil.inMap.get(key));
				list.add(vo);
			} else {
				temp.addAll(maps.get(key));
			}

		}
		MetricMetaVO vo = new MetricMetaVO();
		vo.setMetaDatas(temp);
		vo.setIntegration("other");
		list.add(vo);
		Collections.sort(list);
		return list;
	}

	@GET
	@Path("tags/query")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getTagsByMetricName(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@javax.ws.rs.QueryParam("metricName") String metricName, @javax.ws.rs.QueryParam("q") String[] q) {
		 
		List<uyun.bat.common.tag.entity.Tag> tags = new ArrayList<>();
		if (q.length < 0) {
			tags = ServiceManager.getInstance().getMetricService().getTags(tenantId, metricName);
		} else {
			List<uyun.bat.common.tag.entity.Tag> temp = new ArrayList<>();
			for (String query : q) {
				uyun.bat.common.tag.entity.Tag t = new uyun.bat.common.tag.entity.Tag(query.split(":")[0],
						query.split(":").length > 1 ? query.split(":")[1] : "");
				temp.add(t);
			}
			tags = ServiceManager.getInstance().getMetricService().getTagsByTag(tenantId, metricName, temp);
		}
		List<String> result = new ArrayList<String>();
		for (uyun.bat.common.tag.entity.Tag t : tags) {
			if (!"resourceId".equals(t.getKey()))
				result.add(t.getKey() + ":" + t.getValue());
		}
		return result;
	}

	@POST
	@Path("trash/clean")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public boolean deleteTrashData(MetricTrashCleanQuery query) {
		return ServiceManager.getInstance().getMetricService().deleteTrashData(query.getMetricName(),
				query.getTenantId(), query.getTags());
	}

	@GET
	@Path("metricMetaData")
	@Produces(MediaType.APPLICATION_JSON)
	public MetaData getMetricMetaData(@javax.ws.rs.QueryParam("metricName") String metricName) {
		MetricMetaData meta = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metricName);
		MetaData metaData = new MetaData();
		if (meta != null) {
			metaData.setName(metricName);
			metaData.setMax(meta.getValueMax());
			metaData.setMin(meta.getValueMin());
			metaData.setPrecision(meta.getPrecision());
			metaData.setType(meta.getTypeName());
			metaData.setUnit(meta.getUnit());
		}
		return metaData;
	}

	@GET
	@Path("isExist")
	@Produces(MediaType.APPLICATION_JSON)
	public boolean isMetricExist(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 
		int count = ServiceManager.getInstance().getMetricService().getMetricNamesByTenantId(tenantId).size();
		if (count > 0)
			return true;
		return false;
	}

	@GET
	@Path("integration/query")
	@Produces(MediaType.APPLICATION_JSON)
	public List<MetricDataVO> getMetricMetaDataByKey(@javax.ws.rs.QueryParam("integration") String key) {
		// 网络设备元数据单独处理
		if (key.equals("NetCollector")) {
			MetricDataVO md1 = new MetricDataVO("system.net.ping_response_time", "ms", "", "网络设备 ping 响应时间");
			MetricDataVO md2 = new MetricDataVO("system.cpu.idle", "%", "", "网络设备 cpu 空闲率");
			MetricDataVO md7 = new MetricDataVO("system.cpu.pct_usage", "%", "", "网络设备cpu使用率");
			MetricDataVO md3 = new MetricDataVO("system.mem.pct_usage", "%", "", "网络设备内存使用率");
			MetricDataVO md4 = new MetricDataVO("system.port.in_rate", "Kbps", "", "网络设备端口入速率");
			MetricDataVO md5 = new MetricDataVO("system.port.out_rate", "Kbps", "", "网络设备端口出速率");
			MetricDataVO md6 = new MetricDataVO("system.port.status", "", "数字1表示工作状态，数字2表示不工作状态，数字3表示测试状态", "网络设备端口状态");
			MetricDataVO md8 = new MetricDataVO("system.port.bandwidth.out_pct_usage", "%", "", "网络设备端口出带宽使用率");
			MetricDataVO md9 = new MetricDataVO("system.port.bandwidth.in_pct_usage", "%", "", "网络设备端口入带宽使用率");
			MetricDataVO md10 = new MetricDataVO("system.port.bandwidth.pct_usage", "%", "", "网络设备端口带宽使用率");

			return Lists.newArrayList(md1, md2, md7, md3, md4, md5, md6, md8, md9, md10);
		}
		List<MetricMetaData> meta = ServiceManager.getInstance().getMetricMetaDataService().getMetricMetaDataByKey(key);
		List<MetricDataVO> list = new ArrayList<>();
		if (meta != null && meta.size() > 0)
			for (MetricMetaData m : meta) {
				MetricDataVO md = new MetricDataVO(m.getName(), m.getUnit(), m.getcDescr(), m.getcName());
				list.add(md);
			}
		return list;

	}
}
