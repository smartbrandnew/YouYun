import adodbapi

from checks import AgentCheck

ALL_INSTANCES = 'ALL'
VALID_METRIC_TYPES = ('gauge', 'rate', 'histogram')

PERF_LARGE_RAW_BASE = 1073939712
PERF_RAW_LARGE_FRACTION = 537003264
PERF_AVERAGE_BULK = 1073874176
PERF_COUNTER_BULK_COUNT = 272696576
PERF_COUNTER_LARGE_RAWCOUNT = 65792

COUNTER_TYPE_QUERY = '''select distinct cntr_type
                        from sys.dm_os_performance_counters
                        where counter_name = ?;'''

BASE_NAME_QUERY = '''select distinct counter_name
                     from sys.dm_os_performance_counters
                     where (counter_name=? or counter_name=?
                     or counter_name=?) and cntr_type=%s;''' % PERF_LARGE_RAW_BASE

INSTANCES_QUERY = '''select instance_name
                     from sys.dm_os_performance_counters
                     where counter_name=? and instance_name!='_Total';'''

VALUE_AND_BASE_QUERY = '''select cntr_value
                          from sys.dm_os_performance_counters
                          where (counter_name=? or counter_name=?)
                          and instance_name=?
                          order by cntr_type;'''


class SQLConnectionError(Exception):
    pass


class SQLServer(AgentCheck):
    SOURCE_TYPE_NAME = 'sql server'
    SERVICE_CHECK_NAME = 'sqlserver.can_connect'
    DEFAULT_COMMAND_TIMEOUT = 30

    METRICS = [
        ('sqlserver.buffer.cache_hit_ratio', 'Buffer cache hit ratio', ''),
        ('sqlserver.buffer.page_life_expectancy', 'Page life expectancy', ''),
        ('sqlserver.stats.batch_requests', 'Batch Requests/sec', ''),
        ('sqlserver.stats.sql_compilations', 'SQL Compilations/sec', ''),
        ('sqlserver.stats.sql_recompilations', 'SQL Re-Compilations/sec', ''),
        ('sqlserver.stats.connections', 'User Connections', ''),
        ('sqlserver.stats.lock_waits', 'Lock Waits/sec', '_Total'),
        ('sqlserver.access.page_splits', 'Page Splits/sec', ''),
        ('sqlserver.stats.procs_blocked', 'Processes blocked', ''),
        ('sqlserver.buffer.checkpoint_pages', 'Checkpoint pages/sec', '')
    ]

    def __init__(self, name, init_config, agentConfig, instances=None):
        AgentCheck.__init__(self, name, init_config, agentConfig, instances)

        self.connections = {}
        self.failed_connections = {}
        self.instances_metrics = {}

        custom_metrics = init_config.get('custom_metrics', [])
        for instance in instances:
            try:
                self._make_metric_list_to_collect(instance, custom_metrics)
                self.close_db_connections(instance)
            except SQLConnectionError:
                self.log.exception("Skipping SQL Server instance")
                continue

    def _make_metric_list_to_collect(self, instance, custom_metrics):
        metrics_to_collect = []
        for name, counter_name, instance_name in self.METRICS:
            try:
                sql_type, base_name = self.get_sql_type(instance, counter_name)
                metrics_to_collect.append(self.typed_metric(name,
                                                            counter_name,
                                                            base_name,
                                                            None,
                                                            sql_type,
                                                            instance_name,
                                                            None))
            except SQLConnectionError:
                raise
            except Exception:
                self.log.warning("Can't load the metric %s, ignoring", name, exc_info=True)
                continue

        for row in custom_metrics:
            user_type = row.get('type')
            if user_type is not None and user_type not in VALID_METRIC_TYPES:
                self.log.error('%s has an invalid metric type: %s', row['name'], user_type)
            sql_type = None
            try:
                if user_type is None:
                    sql_type, base_name = self.get_sql_type(instance, row['counter_name'])
            except Exception:
                self.log.warning("Can't load the metric %s, ignoring", row['name'], exc_info=True)
                continue

            metrics_to_collect.append(self.typed_metric(row['name'],
                                                        row['counter_name'],
                                                        base_name,
                                                        user_type,
                                                        sql_type,
                                                        row.get('instance_name', ''),
                                                        row.get('tag_by', None)))

        instance_key = self._conn_key(instance)
        self.instances_metrics[instance_key] = metrics_to_collect

    def typed_metric(self, dd_name, sql_name, base_name, user_type, sql_type, instance_name, tag_by):

        metric_type_mapping = {
            PERF_COUNTER_BULK_COUNT: (self.rate, SqlSimpleMetric),
            PERF_COUNTER_LARGE_RAWCOUNT: (self.gauge, SqlSimpleMetric),
            PERF_LARGE_RAW_BASE: (self.gauge, SqlSimpleMetric),
            PERF_RAW_LARGE_FRACTION: (self.gauge, SqlFractionMetric),
            PERF_AVERAGE_BULK: (self.gauge, SqlIncrFractionMetric)
        }
        if user_type is not None:
            metric_type = getattr(self, user_type)
            cls = SqlSimpleMetric

        else:
            metric_type, cls = metric_type_mapping[sql_type]

        return cls(dd_name, sql_name, base_name,
                   metric_type, instance_name, tag_by, self.log)

    def _get_access_info(self, instance):

        host = instance.get('host', '127.0.0.1,1433')
        username = instance.get('username')
        password = instance.get('password')
        database = instance.get('database', 'master')
        return host, username, password, database

    def _conn_key(self, instance):

        host, username, password, database = self._get_access_info(instance)
        return '%s:%s:%s:%s' % (host, username, password, database)

    def _conn_string(self, instance=None, conn_key=None):

        if instance:
            host, username, password, database = self._get_access_info(instance)
        elif conn_key:
            host, username, password, database = conn_key.split(":")
        conn_str = 'Provider=SQLOLEDB;Data Source=%s;Initial Catalog=%s;' \
                   % (host, database)
        if username:
            conn_str += 'User ID=%s;' % (username)
        if password:
            conn_str += 'Password=%s;' % (password)
        if not username and not password:
            conn_str += 'Integrated Security=SSPI;'
        return conn_str

    def get_cursor(self, instance, cache_failure=False):
        conn_key = self._conn_key(instance)
        host = instance.get('host')
        database = instance.get('database')
        service_check_tags = [
            'host:%s' % host,
            'db:%s' % database
        ]

        if conn_key in self.failed_connections:
            raise self.failed_connections[conn_key]

        if conn_key not in self.connections:
            try:
                timeout = int(instance.get('command_timeout',
                                           self.DEFAULT_COMMAND_TIMEOUT))
                conn = adodbapi.connect(self._conn_string(instance=instance),
                                        timeout=timeout)
                self.connections[conn_key] = {'conn': conn, 'timeout': timeout}
                self.service_check(self.SERVICE_CHECK_NAME, AgentCheck.OK,
                                   tags=service_check_tags)
            except Exception as e:
                cx = "%s - %s" % (host, database)
                message = "Unable to connect to SQL Server for instance %s." % cx
                self.service_check(self.SERVICE_CHECK_NAME, AgentCheck.CRITICAL,
                                   tags=service_check_tags, message=message)

                password = instance.get('password')
                if password is not None:
                    tracebk = e.replace(password, "*" * 6)

                cxn_failure_exp = SQLConnectionError("%s \n %s" % (message, tracebk))
                if cache_failure:
                    self.failed_connections[conn_key] = cxn_failure_exp
                raise cxn_failure_exp

        conn = self.connections[conn_key]['conn']
        cursor = conn.cursor()
        return cursor

    def get_sql_type(self, instance, counter_name):
        cursor = self.get_cursor(instance, cache_failure=True)
        cursor.execute(COUNTER_TYPE_QUERY, (counter_name,))
        (sql_type,) = cursor.fetchone()
        if sql_type == PERF_LARGE_RAW_BASE:
            self.log.warning("Metric %s is of type Base and shouldn't be reported this way",
                             counter_name)
        base_name = None
        if sql_type in [PERF_AVERAGE_BULK, PERF_RAW_LARGE_FRACTION]:
            candidates = (counter_name + " base",
                          counter_name.replace("(ms)", "base"),
                          counter_name.replace("Avg ", "") + " base"
                          )
            try:
                cursor.execute(BASE_NAME_QUERY, candidates)
                base_name = cursor.fetchone().counter_name.strip()
                self.log.debug("Got base metric: %s for metric: %s", base_name, counter_name)
            except Exception, e:
                self.log.warning("Could not get counter_name of base for metric: %s", e)

        self.close_cursor(cursor)

        return sql_type, base_name

    def check(self, instance):
        self.open_db_connections(instance)
        cursor = self.get_cursor(instance)

        custom_tags = instance.get('tags', [])
        instance_key = self._conn_key(instance)
        metrics_to_collect = self.instances_metrics[instance_key]

        for metric in metrics_to_collect:
            try:
                metric.fetch_metric(cursor, custom_tags)
            except Exception, e:
                self.log.warning("Could not fetch metric %s: %s" % (metric.datamonitor_name, e))

        self.close_cursor(cursor)
        self.close_db_connections(instance)

    def close_cursor(self, cursor):
        try:
            cursor.close()
        except Exception as e:
            self.log.warning("Could not close adodbapi cursor\n{0}".format(e))

    def close_db_connections(self, instance):
        conn_key = self._conn_key(instance)
        if conn_key not in self.connections:
            return

        try:
            self.connections[conn_key]['conn'].close()
        except Exception as e:
            self.log.warning("Could not close adodbapi db connection\n{0}".format(e))

    def open_db_connections(self, instance):

        conn_key = self._conn_key(instance)
        timeout = int(instance.get('command_timeout',
                                   self.DEFAULT_COMMAND_TIMEOUT))
        try:
            rawconn = adodbapi.connect(self._conn_string(instance=instance),
                                       timeout=timeout)
            if conn_key not in self.connections:
                self.connections[conn_key] = {'conn': rawconn, 'timeout': timeout}
            else:
                try:
                    self.connections[conn_key]['conn'].close()
                except Exception as e:
                    self.log.info("Could not close adodbapi db connection\n{0}".format(e))

                self.connections[conn_key]['conn'] = rawconn
        except Exception as e:
            self.log.warning("Could not connect to SQL Server\n{0}".format(e))


class SqlServerMetric(object):
    def __init__(self, datamonitor_name, sql_name, base_name,
                 report_function, instance, tag_by, logger):
        self.datamonitor_name = datamonitor_name
        self.sql_name = sql_name
        self.base_name = base_name
        self.report_function = report_function
        self.instance = instance
        self.tag_by = tag_by
        self.instances = None
        self.past_values = {}
        self.log = logger

    def fetch_metrics(self, cursor, tags):
        raise NotImplementedError


class SqlSimpleMetric(SqlServerMetric):
    def fetch_metric(self, cursor, tags):
        query_base = '''
                    select instance_name, cntr_value
                    from sys.dm_os_performance_counters
                    where counter_name = ?
                    '''
        if self.instance == ALL_INSTANCES:
            query = query_base + "and instance_name!= '_Total'"
            query_content = (self.sql_name,)
        else:
            query = query_base + "and instance_name=?"
            query_content = (self.sql_name, self.instance)

        cursor.execute(query, query_content)
        rows = cursor.fetchall()
        for instance_name, cntr_value in rows:
            metric_tags = tags
            if self.instance == ALL_INSTANCES:
                metric_tags = metric_tags + ['%s:%s' % (self.tag_by, instance_name.strip())]
            self.report_function(self.datamonitor_name, cntr_value,
                                 tags=metric_tags)


class SqlFractionMetric(SqlServerMetric):
    def set_instances(self, cursor):
        if self.instance == ALL_INSTANCES:
            cursor.execute(INSTANCES_QUERY, (self.sql_name,))
            self.instances = [row.instance_name for row in cursor.fetchall()]
        else:
            self.instances = [self.instance]

    def fetch_metric(self, cursor, tags):
        if self.instances is None:
            self.set_instances(cursor)
        for instance in self.instances:
            cursor.execute(VALUE_AND_BASE_QUERY, (self.sql_name, self.base_name, instance))
            rows = cursor.fetchall()
            if len(rows) != 2:
                self.log.warning("Missing counter to compute fraction for "
                                 "metric %s instance %s, skipping", self.sql_name, instance)
                continue
            value = rows[0, "cntr_value"]
            base = rows[1, "cntr_value"]

            metric_tags = tags
            if self.instance == ALL_INSTANCES:
                metric_tags = metric_tags + ['%s:%s' % (self.tag_by, instance.strip())]
            self.report_fraction(value, base, metric_tags)

    def report_fraction(self, value, base, metric_tags):
        try:
            result = value / float(base)
            self.report_function(self.datamonitor_name, result, tags=metric_tags)
        except ZeroDivisionError:
            self.log.debug("Base value is 0, won't report metric %s for tags %s",
                           self.datamonitor_name, metric_tags)


class SqlIncrFractionMetric(SqlFractionMetric):
    def report_fraction(self, value, base, metric_tags):
        key = "key:" + "".join(metric_tags)
        if key in self.past_values:
            old_value, old_base = self.past_values[key]
            diff_value = value - old_value
            diff_base = base - old_base
            try:
                result = diff_value / float(diff_base)
                self.report_function(self.datamonitor_name, result, tags=metric_tags)
            except ZeroDivisionError:
                self.log.debug("Base value is 0, won't report metric %s for tags %s",
                               self.datamonitor_name, metric_tags)
        self.past_values[key] = (value, base)
