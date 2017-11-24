package uyun.bat.datastore.util;

import com.google.common.collect.SetMultimap;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.SliceQuery;

import org.kairosdb.client.builder.QueryBuilder;
import org.kairosdb.client.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.RelativeTime;
import uyun.bat.datastore.balance.KairosdbLoadBalancer;
import uyun.bat.datastore.entity.DataPointsRowKey;
import uyun.bat.datastore.entity.DataPointsRowKeySerializer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class MetricTrashCleaner {
	private static final Logger logger = LoggerFactory.getLogger(MetricTrashCleaner.class);
	private static Cluster cluster;
	private static String clusterName;
	private static Keyspace keySpace;
	public static final String CF_ROW_KEY_INDEX = "row_key_index";
	public static final long ROW_WIDTH = 1814400000L; //3 Weeks wide
	public static final int m_singleRowReadSize = 1024;
	public static DataPointsRowKeySerializer dataPointsRowKeySerializer = new DataPointsRowKeySerializer();
	public static StringSerializer stringSerializer = new StringSerializer();
	private static String cassandraIpaddr;

	@Autowired
	private KairosdbLoadBalancer kairosdbLoadBalancer;
	static {
		cassandraIpaddr = Config.getInstance().get("cassandra.hosts", "localhost");
		clusterName = Config.getInstance().get("cassandra.cluster.name", "Test Cluster");
		String username = Config.getInstance().get("cassandra.username", "");
		String password = Config.getInstance().get("cassandra.password", "");
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			Map<String, String> accessMap = new HashMap<>();
			accessMap.put("username", username);
			accessMap.put("password", password);
			cluster = HFactory.getOrCreateCluster(clusterName,
					new CassandraHostConfigurator(cassandraIpaddr), accessMap);
		} else {
			cluster = HFactory.getOrCreateCluster(clusterName, new CassandraHostConfigurator(cassandraIpaddr));
		}
		keySpace = HFactory.createKeyspace("kairosdb", cluster);
	}

	public boolean deleteMetricData(String metricName, SetMultimap<String, String> tags, long startTime,
			long endTime) {
		deleteData(metricName, tags, startTime, endTime);
		Iterator<DataPointsRowKey> rowKeys = getKeysForQueryIterator(metricName, tags, startTime, endTime);
		while (rowKeys.hasNext()) {
			DataPointsRowKey rowKey = rowKeys.next();
			Mutator<String> mutator = HFactory.createMutator(keySpace, stringSerializer);
			mutator.delete(metricName, CF_ROW_KEY_INDEX, rowKey, dataPointsRowKeySerializer);
		}
		return true;
	}

	public boolean deleteMetricData(String metricName, SetMultimap<String, String> tags, RelativeTime startTime) {
		deleteData(metricName, tags, startTime.getTimeRelativeTo(System.currentTimeMillis()), System.currentTimeMillis());
//		Iterator<DataPointsRowKey> rowKeys = getKeysForQueryIterator(metricName, tags,
//				startTime.getTimeRelativeTo(System.currentTimeMillis()), System.currentTimeMillis());
//		while (rowKeys.hasNext()) {
//			DataPointsRowKey rowKey = rowKeys.next();
//			Mutator<String> mutator = HFactory.createMutator(keySpace, stringSerializer);
//			mutator.delete(metricName, CF_ROW_KEY_INDEX, rowKey, dataPointsRowKeySerializer);
//		}
		return true;
	}

	public boolean deleteMetricData(String metricName, SetMultimap<String, String> tags, RelativeTime startTime,
			RelativeTime endTime) {
		deleteData(metricName, tags, startTime.getTimeRelativeTo(System.currentTimeMillis()),
				endTime.getTimeRelativeTo(System.currentTimeMillis()));
		Iterator<DataPointsRowKey> rowKeys = getKeysForQueryIterator(metricName, tags,
				startTime.getTimeRelativeTo(System.currentTimeMillis()), endTime.getTimeRelativeTo(System.currentTimeMillis()));
		while (rowKeys.hasNext()) {
			DataPointsRowKey rowKey = rowKeys.next();
			Mutator<String> mutator = HFactory.createMutator(keySpace, stringSerializer);
			mutator.delete(metricName, CF_ROW_KEY_INDEX, rowKey, dataPointsRowKeySerializer);
		}
		return true;
	}

	private boolean deleteData(String metricName, SetMultimap<String, String> tags, long startTime,
			long endTime) {
		QueryBuilder builder = QueryBuilder.getInstance();
		builder.setStart(new Date(startTime));
		builder.setEnd(new Date(endTime));
		Map<String, List<String>> tags1 = new HashMap<String, List<String>>();
		for (String key : tags.keySet()) {
			Set<String> set = tags.get(key);
			tags1.put(key, new ArrayList<String>(set));
		}
		builder.addMetric(metricName).addMultiValuedTags(tags1);
		Response response = null;
		try {
			response = kairosdbLoadBalancer.getKairosdbClient().delete(builder);
		} catch (URISyntaxException | IOException e) {
			logger.warn("fail to delete the past useless message: ", e);
		}
		if (response != null) {
			int code = response.getStatusCode();
			if (code == 204) {
				return true;
			}
		}
		return false;
	}

	private Iterator<DataPointsRowKey> getKeysForQueryIterator(String metricName,
			SetMultimap<String, String> tags, long startTime,
			long endTime) {
		Iterator<DataPointsRowKey> ret = null;
		ret = new FilteredRowKeyIterator(metricName, startTime,
				endTime, tags);
		return ret;
	}

	private class FilteredRowKeyIterator implements Iterator<DataPointsRowKey>
	{
		private ColumnSliceIterator<String, DataPointsRowKey, String> m_sliceIterator;

		private ColumnSliceIterator<String, DataPointsRowKey, String> m_continueSliceIterator;
		private DataPointsRowKey m_nextKey;
		private SetMultimap<String, String> m_filterTags;

		public FilteredRowKeyIterator(String metricName, long startTime, long endTime,
				SetMultimap<String, String> filterTags)
		{
			m_filterTags = filterTags;
			SliceQuery<String, DataPointsRowKey, String> sliceQuery =
					HFactory.createSliceQuery(keySpace, StringSerializer.get(),
							new DataPointsRowKeySerializer(true), StringSerializer.get());

			sliceQuery.setColumnFamily(CF_ROW_KEY_INDEX)
					.setKey(metricName);

			if ((startTime < 0) && (endTime >= 0))
			{
				m_sliceIterator = createSliceIterator(sliceQuery, metricName,
						startTime, -1L);

				SliceQuery<String, DataPointsRowKey, String> sliceQuery2 =
						HFactory.createSliceQuery(keySpace, StringSerializer.get(),
								new DataPointsRowKeySerializer(true), StringSerializer.get());

				sliceQuery2.setColumnFamily(CF_ROW_KEY_INDEX)
						.setKey(metricName);

				m_continueSliceIterator = createSliceIterator(sliceQuery2, metricName,
						0, endTime);
			}
			else
			{
				m_sliceIterator = createSliceIterator(sliceQuery, metricName,
						startTime, endTime);
			}

		}

		private ColumnSliceIterator<String, DataPointsRowKey, String> createSliceIterator(
				SliceQuery<String, DataPointsRowKey, String> sliceQuery,
				String metricName, long startTime, long endTime)
		{
			DataPointsRowKey startKey = new DataPointsRowKey(metricName,
					calculateRowTime(startTime), "");

			DataPointsRowKey endKey = new DataPointsRowKey(metricName,
					calculateRowTime(endTime), "");
			endKey.setEndSearchKey(true);

			ColumnSliceIterator<String, DataPointsRowKey, String> iterator = new ColumnSliceIterator<String, DataPointsRowKey, String>(
					sliceQuery,
					startKey, endKey, false, m_singleRowReadSize);

			return (iterator);
		}

		private DataPointsRowKey nextKeyFromIterator(ColumnSliceIterator<String, DataPointsRowKey, String> iterator)
		{
			DataPointsRowKey next = null;

			outer: while (iterator.hasNext())
			{
				DataPointsRowKey rowKey = iterator.next().getName();

				Map<String, String> keyTags = rowKey.getTags();
				for (String tag : m_filterTags.keySet())
				{
					String value = keyTags.get(tag);
					if (value == null || !m_filterTags.get(tag).contains(value))
						continue outer; //Don't want this key
				}

				next = rowKey;
				break;
			}

			return (next);
		}

		@Override
		public boolean hasNext()
		{
			m_nextKey = nextKeyFromIterator(m_sliceIterator);

			if ((m_nextKey == null) && (m_continueSliceIterator != null))
				m_nextKey = nextKeyFromIterator(m_continueSliceIterator);

			return (m_nextKey != null);
		}

		@Override
		public DataPointsRowKey next()
		{
			return m_nextKey;
		}

		@Override
		public void remove()
		{
		}
	}

	public static long calculateRowTime(long timestamp)
	{
		return (timestamp - (Math.abs(timestamp) % ROW_WIDTH));
	}
}
