package uyun.bat.gateway.agent.service.serviceapi;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.PageResource;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.QueryMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceOpenApiQuery;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;
import uyun.bat.gateway.agent.entity.DataValue;
import uyun.bat.gateway.agent.entity.ResourceDetailVO;
import uyun.bat.gateway.agent.entity.Series;
import uyun.bat.gateway.agent.entity.chatopsentity.ChatOpsHost;
import uyun.bat.gateway.agent.entity.chatopsentity.ChatOpsHostList;
import uyun.bat.gateway.agent.entity.newentity.SeriesRequestParam1;
import uyun.bat.gateway.agent.exception.IllegalException;
import uyun.bat.gateway.agent.service.api.ChatopsService;
import uyun.bat.gateway.agent.util.HostDetail;
import uyun.bat.gateway.api.service.ServiceManager;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "rest-service", delay = 3000)
@Path("v2")
public class ChatopsRestService implements ChatopsService {

	@GET
	@Path("metric/names/query")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<ServiceApiResMetrics> getMetricNames(@javax.ws.rs.QueryParam("tenant_id") String tenantId,
													 @javax.ws.rs.QueryParam("hostname") String hostname,
													 @javax.ws.rs.QueryParam("ip") String ip, @javax.ws.rs.QueryParam("tags") String tags) {
		if (null == tenantId)
			throw new IllegalException("tenant_id can not be null");
		ResourceServiceQuery query = new ResourceServiceQuery();
		query.setHostname(hostname);
		query.setIpaddr(ip);
		query.setTenantId(tenantId);
		if (tags != null) {
			String tagArray[] = tags.split(";");
			query.setTags(Arrays.asList(tagArray));
		}
		return ServiceManager.getInstance().getMetricService().getMetricNames(query);
	}

	private List<Series> generateGetPerfMetricList(SeriesRequestParam1 param, String tenantId) {
		QueryBuilder queryBuilder = new QueryBuilder();
		long currentTime = System.currentTimeMillis();
		if (StringUtils.isEmpty(param.getMetric()))
			throw new IllegalException("The metric name cannot be null!");
		if (param.getFrom() == 0 || param.getTo() == 0)
			throw new IllegalException("Start or end time is not empty!");
		if (param.getFrom() > currentTime)
			throw new IllegalException("The start time should not be greater than the current system time!");
		if (param.getFrom() > param.getTo())
			throw new IllegalException("The start time should not be greater than the end!");
		//170615 chatops说如果超过3个月不返回异常而是查询一个月
		if (compareDate(new Date(param.getFrom()), new Date(param.getTo())) >= 3){
			param.setFrom(currentTime - 30 * 24 * 60 * 60 * 1000);
			param.setTo(currentTime);
			param.setInterval(14400);
		}
		queryBuilder.setStartAbsolute(param.getFrom());
		queryBuilder.setEndAbsolute(param.getTo());
		QueryMetric metric = queryBuilder.addMetric(param.getMetric()).addTenantId(tenantId);
		metric.addAggregatorType(AggregatorType.checkByName(param.getAggregator()));
		String groupBy = "";
		if (null != param.getGroup_by() && !StringUtils.isEmpty(param.getGroup_by().getTag_key())) {
			groupBy = param.getGroup_by().getTag_key();
			metric.addGrouper(groupBy);
		}
		String scope = "*";
		StringBuilder sb = new StringBuilder();
		if (param.getTags() != null && param.getTags().size() > 0) {
			for (String t : param.getTags()) {
				metric.addTag(t.split(":")[0], t.split(":").length > 1 ? t.split(":")[1] : "");
				sb.append(t);
				sb.append(",");
			}
			scope = sb.deleteCharAt(sb.length() - 1).toString();
		}
		int interval = param.getInterval();
		List<PerfMetric> perfMetricList = ServiceManager.getInstance().getMetricService().querySeries(queryBuilder,
				interval);
		MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService()
				.queryByName(param.getMetric());
		List<Series> list = new ArrayList<Series>();
		if (perfMetricList != null && perfMetricList.size() == 0)
			list.add(new Series(scope, new double[0][0], metaData != null ? metaData.getUnit() : null));
		if (perfMetricList != null && perfMetricList.size() > 0) {
			for (PerfMetric p : perfMetricList) {
				if (p.getDataPoints().size() >= 1000)
					throw new IllegalArgumentException("Set the granularity too small, please reset the interval's value！");
				Series series = new Series();
				if (null != param.getGroup_by() && !StringUtils.isEmpty(param.getGroup_by().getTag_key())
						&& param.getTags() != null && param.getTags().size() == 0)
					scope = groupBy + ":" + p.getTags().get(groupBy);
				series.setScope(scope);
				List<DataPoint> dataPoints = p.getDataPoints();
				if (dataPoints != null && dataPoints.size() > 0) {
					double[][] point = new double[dataPoints.size()][2];
					for (int j = 0; j < dataPoints.size(); j++) {
						point[j][0] = dataPoints.get(j).getTimestamp();
						point[j][1] = Double.parseDouble(dataPoints.get(j).getValue().toString());
					}
					series.setPoints(point);
					series.setUnit(metaData != null ? metaData.getUnit() : null);
					list.add(series);
				}
			}
		}
		return list;
	}

	/**
	 * 校验时间不超过三个月
	 */
	private int compareDate(Date from, Date to) {
		int n = 0;
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		try {
			c1.setTime(from);
			c2.setTime(to);
		} catch (Exception e3) {
			throw new IllegalArgumentException("Time conversion error!");
		}
		while (!c1.after(c2)) {
			n++;
			c1.add(Calendar.MONTH, 1); // 比较月份，月份+1
		}
		n = n - 1;
		return n;
	}

	@GET
	@Path("metric/last_value/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public DataValue getCurrentMetric(@javax.ws.rs.QueryParam("tenant_id") String tenantId,
									  @javax.ws.rs.QueryParam("metric") String metricName, @javax.ws.rs.QueryParam("tags") String tags) {
		if (null == tenantId)
			throw new IllegalException("tenant_id can not be null");
		QueryBuilder builder = QueryBuilder.getInstance();
		QueryMetric metric = builder.addMetric(metricName).addAggregatorType(AggregatorType.last);
		metric.addTenantId(tenantId);
		if (tags != null) {
			String[] tagArray = tags.split(";");
			for (String tag : tagArray) {
				int index = tag.indexOf(":");
				if (index == -1) {
					metric.addTag(tag, "");
				} else {
					metric.addTag(tag.substring(0, index), tag.substring(index + 1));
				}
			}
		}
		//用start半小时会没有数据。。。要么就是获取1h
		//builder.setStart(1, TimeUnit.HOURS);
		long to = System.currentTimeMillis();
		long from = to - 30 * 60 * 1000;
		builder.setStartAbsolute(from);
		builder.setEndAbsolute(to);
		PerfMetric perfMetric = ServiceManager.getInstance().getMetricService().queryCurrentPerfMetric(builder);
		if (perfMetric != null) {
			MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metricName);
			List<DataPoint> dataPoints = perfMetric.getDataPoints();
			if (dataPoints != null && dataPoints.size() > 0) {
				DataPoint dataPoint = dataPoints.get(dataPoints.size() - 1);
				DataValue dataValue = new DataValue();
				dataValue.setTimestamp(dataPoint.getTimestamp());
				dataValue.setValue((double)dataPoint.getValue());
				dataValue.setUnit(metaData != null ? metaData.getUnit() : null);
				return dataValue;
			}
		}
		return null;
	}


	@GET
	@Path("metric/datapoints_picture/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public InputStream getSeriesMetricPic(@javax.ws.rs.QueryParam("tenant_id") String tenantId,
									 @javax.ws.rs.QueryParam("metric") String metricName,
									 @javax.ws.rs.QueryParam("tags") String tags,
									 @javax.ws.rs.QueryParam("from") long from, @javax.ws.rs.QueryParam("to") long to) {
		if (null == tenantId)
			throw new IllegalException("tenant_id can not be null");
		List<String> tag = new ArrayList<>();
		if (!StringUtils.isEmpty(tags))
			tag = Arrays.asList(tags.split(";"));
		int interval = (int) (to - from) / (60 * 60 * 1000);
		//返回点的个数与仪表盘一致
		interval = interval == 0 ? 10 : interval * 10;
		SeriesRequestParam1 requestParams = new SeriesRequestParam1(metricName, tag, from, to, "avg", interval, null);

		List<Series> series = generateGetPerfMetricList(requestParams, tenantId);
		//创建XYDataset对象
		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
		for (Series s : series) {
			XYSeries xyseries = new XYSeries(s.getScope());
			for (int i = 0; i < s.getPoints().length; i++) {
				xyseries.add(s.getPoints()[i][0] / 1000, s.getPoints()[i][1]);
			}
			xySeriesCollection.addSeries(xyseries);
		}
		//生成JFreeChart对象，以及做相应的设置
		JFreeChart freeChart = createChart(xySeriesCollection,metricName);
		//将JFreeChart对象输出到文件，Servlet输出流等
		InputStream is = null;
		try {
			is = writeChartAsBASE64(freeChart, 1200, 400, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return is;
	}

    @GET
    @Path("hosts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
	public ChatOpsHostList getResListByCondition(@QueryParam("tenant_id") String tenantId, @QueryParam("page_index") @DefaultValue("1") int page_index,
									   @QueryParam("page_size") @DefaultValue("20") int page_size,
									   @QueryParam("ip") String ip, @QueryParam("name") String name) {
		if (null == tenantId)
			throw new IllegalException("tenant_id can not be null");
        if (page_size >= 1000)
            throw new IllegalArgumentException("page_size can not be greater than 1000！");
        ResourceOpenApiQuery query = new ResourceOpenApiQuery(tenantId, ip, name, null, null, null,
                null, page_index, page_size);
        PageResource pr = ServiceManager.getInstance().getResourceService().queryResListByCondition(query);
        List<ChatOpsHost> list = new ArrayList<>();
        List<Resource> resources = pr.getResources();
        if (null == resources || resources.size() < 1) {
            return new ChatOpsHostList(page_size, page_index, new ArrayList<ChatOpsHost>());
        }
        for (Resource r : resources) {
            ResourceDetailVO rVO = HostDetail.getResourceDetailById(tenantId, r.getId());
            ChatOpsHost host = new ChatOpsHost(r.getAgentId(), r.getHostname(), r.getIpaddr(), rVO.getInfo());

            list.add(host);
        }
        ChatOpsHostList hostList = new ChatOpsHostList(pr.getCount(), page_size, page_index, list);
        return hostList;
    }

	//根据chart直接转成字节流
	private InputStream writeChartAsBASE64(JFreeChart chart, int width, int height, ChartRenderingInfo info) throws IOException {
		if (chart == null) {
			throw new IllegalArgumentException("Null \'chart\' argument.");
		} else {
			BufferedImage image = chart.createBufferedImage(width, height, 1, info);
			//创建储存图片二进制流的输出流
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			ImageIO.write(image, "png", os);
			InputStream is = new ByteArrayInputStream(os.toByteArray());
			return is;
		}
	}

	// 根据XYDataset创建JFreeChart对象
	private JFreeChart createChart(XYDataset dataset,String metricName) {
		// 创建JFreeChart对象：ChartFactory.createXYLineChart
		JFreeChart jfreechart = ChartFactory.createXYLineChart(metricName, // 标题
				"time", // categoryAxisLabel （category轴，横轴，X轴标签）
				"value", // valueAxisLabel（value轴，纵轴，Y轴的标签）
				dataset, // dataset
				PlotOrientation.VERTICAL,
				true, // legend
				false, // tooltips
				false); // URLs

		// 使用CategoryPlot设置各种参数。以下设置可以省略。
		XYPlot plot = (XYPlot) jfreechart.getPlot();
		// 背景色 透明度
		plot.setBackgroundAlpha(0.5f);
		// 前景色 透明度
		plot.setForegroundAlpha(0.5f);
		return jfreechart;
	}

	public static void main(String[] args) {
		System.out.println(String.valueOf(Double.MAX_VALUE).length());
	}
}
