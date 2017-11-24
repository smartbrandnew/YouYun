from urlparse import urljoin
from urlparse import urlsplit
from urlparse import urlunsplit

import requests
from requests.exceptions import Timeout, HTTPError, InvalidURL, ConnectionError
from simplejson import JSONDecodeError

from checks import AgentCheck

YARN_SERVICE_CHECK = 'mapreduce.resource_manager.can_connect'
MAPREDUCE_SERVICE_CHECK = 'mapreduce.application_master.can_connect'

INFO_PATH = 'ws/v1/cluster/info'
YARN_APPS_PATH = 'ws/v1/cluster/apps'
MAPREDUCE_JOBS_PATH = 'ws/v1/mapreduce/jobs'

YARN_APPLICATION_TYPES = 'MAPREDUCE'
YARN_APPLICATION_STATES = 'RUNNING'

HISTOGRAM = 'histogram'

MAPREDUCE_JOB_METRICS = {
    'elapsedTime': ('mapreduce.job.elapsed_time', HISTOGRAM),
    'mapsTotal': ('mapreduce.job.maps_total', HISTOGRAM),
    'mapsCompleted': ('mapreduce.job.maps_completed', HISTOGRAM),
    'reducesTotal': ('mapreduce.job.reduces_total', HISTOGRAM),
    'reducesCompleted': ('mapreduce.job.reduces_completed', HISTOGRAM),
    'mapsPending': ('mapreduce.job.maps_pending', HISTOGRAM),
    'mapsRunning': ('mapreduce.job.maps_running', HISTOGRAM),
    'reducesPending': ('mapreduce.job.reduces_pending', HISTOGRAM),
    'reducesRunning': ('mapreduce.job.reduces_running', HISTOGRAM),
    'newReduceAttempts': ('mapreduce.job.new_reduce_attempts', HISTOGRAM),
    'runningReduceAttempts': ('mapreduce.job.running_reduce_attempts', HISTOGRAM),
    'failedReduceAttempts': ('mapreduce.job.failed_reduce_attempts', HISTOGRAM),
    'killedReduceAttempts': ('mapreduce.job.killed_reduce_attempts', HISTOGRAM),
    'successfulReduceAttempts': ('mapreduce.job.successful_reduce_attempts', HISTOGRAM),
    'newMapAttempts': ('mapreduce.job.new_map_attempts', HISTOGRAM),
    'runningMapAttempts': ('mapreduce.job.running_map_attempts', HISTOGRAM),
    'failedMapAttempts': ('mapreduce.job.failed_map_attempts', HISTOGRAM),
    'killedMapAttempts': ('mapreduce.job.killed_map_attempts', HISTOGRAM),
    'successfulMapAttempts': ('mapreduce.job.successful_map_attempts', HISTOGRAM),
}

MAPREDUCE_JOB_COUNTER_METRICS = {
    'reduceCounterValue': ('mapreduce.job.counter.reduce_counter_value', HISTOGRAM),
    'mapCounterValue': ('mapreduce.job.counter.map_counter_value', HISTOGRAM),
    'totalCounterValue': ('mapreduce.job.counter.total_counter_value', HISTOGRAM),
}

MAPREDUCE_MAP_TASK_METRICS = {
    'progress': ('mapreduce.job.map.task.progress', HISTOGRAM),
    'elapsedTime': ('mapreduce.job.map.task.elapsed_time', HISTOGRAM)
}

MAPREDUCE_REDUCE_TASK_METRICS = {
    'progress': ('mapreduce.job.reduce.task.progress', HISTOGRAM),
    'elapsedTime': ('mapreduce.job.reduce.task.elapsed_time', HISTOGRAM)
}


class MapReduceCheck(AgentCheck):
    def __init__(self, name, init_config, agentConfig, instances=None):
        AgentCheck.__init__(self, name, init_config, agentConfig, instances)

        self.general_counters = self._parse_general_counters(init_config)

        self.job_specific_counters = self._parse_job_specific_counters(init_config)

    def check(self, instance):
        rm_address = instance.get('resourcemanager_uri')
        if rm_address is None:
            raise Exception('The ResourceManager URL must be specified in the instance configuration')

        cluster_id = self._get_cluster_id(rm_address)

        running_apps = self._get_running_app_ids(rm_address)

        self.service_check(YARN_SERVICE_CHECK,
                           AgentCheck.OK,
                           tags=['url:%s' % rm_address],
                           message='Connection to ResourceManager "%s" was successful' % rm_address)

        running_jobs = self._mapreduce_job_metrics(running_apps, cluster_id)

        self._mapreduce_job_counters_metrics(running_jobs, cluster_id)

        self._mapreduce_task_metrics(running_jobs, cluster_id)

        if running_jobs:
            job_id, metrics = running_jobs.items()[0]
            am_address = self._get_url_base(metrics['tracking_url'])

            self.service_check(MAPREDUCE_SERVICE_CHECK,
                               AgentCheck.OK,
                               tags=['url:%s' % am_address],
                               message='Connection to ApplicationManager "%s" was successful' % am_address)

    def _parse_general_counters(self, init_config):
        job_counter = {}

        if init_config.get('general_counters'):

            for counter_group in init_config['general_counters']:
                counter_group_name = counter_group.get('counter_group_name')
                counters = counter_group.get('counters')

                if not counter_group_name:
                    raise Exception('"general_counters" must contain a valid "counter_group_name"')

                if not counters:
                    raise Exception('"general_counters" must contain a list of "counters"')

                if counter_group_name not in job_counter:
                    job_counter[counter_group_name] = []

                for counter in counters:
                    counter_name = counter.get('counter_name')

                    if not counter_name:
                        raise Exception('At least one "counter_name" should be specified in the list of "counters"')

                    job_counter[counter_group_name].append(counter_name)

        return job_counter

    def _parse_job_specific_counters(self, init_config):
        job_counter = {}

        if init_config.get('job_specific_counters'):

            for job in init_config['job_specific_counters']:
                job_name = job.get('job_name')
                metrics = job.get('metrics')

                if not job_name:
                    raise Exception('Counter metrics must have a "job_name"')

                if not metrics:
                    raise Exception('Jobs specified in counter metrics must contain at least one metric')

                if job_name not in job_counter:
                    job_counter[job_name] = {}

                for metric in metrics:
                    counter_group_name = metric.get('counter_group_name')
                    counters = metric.get('counters')

                    if not counter_group_name:
                        raise Exception('Each counter metric must contain a valid "counter_group_name"')

                    if not counters:
                        raise Exception('Each counter metric must contain a list of "counters"')

                    if counter_group_name not in job_counter[job_name]:
                        job_counter[job_name][counter_group_name] = []

                    for counter in counters:
                        counter_name = counter.get('counter_name')

                        if not counter_name:
                            raise Exception('At least one "counter_name" should be specified in the list of "counters"')

                        job_counter[job_name][counter_group_name].append(counter_name)

        return job_counter

    def _get_cluster_id(self, rm_address):
        info_json = self._rest_request_to_json(rm_address,
                                               INFO_PATH,
                                               YARN_SERVICE_CHECK)

        return info_json.get('clusterInfo', {}).get('id')

    def _get_running_app_ids(self, rm_address, **kwargs):
        metrics_json = self._rest_request_to_json(rm_address,
                                                  YARN_APPS_PATH,
                                                  YARN_SERVICE_CHECK,
                                                  states=YARN_APPLICATION_STATES,
                                                  applicationTypes=YARN_APPLICATION_TYPES)

        running_apps = {}

        if metrics_json.get('apps'):
            if metrics_json['apps'].get('app') is not None:

                for app_json in metrics_json['apps']['app']:
                    app_id = app_json.get('id')
                    tracking_url = app_json.get('trackingUrl')
                    app_name = app_json.get('name')

                    if app_id and tracking_url and app_name:
                        running_apps[app_id] = (app_name, tracking_url)

        return running_apps

    def _mapreduce_job_metrics(self, running_apps, cluster_id):
        running_jobs = {}

        for app_id, (app_name, tracking_url) in running_apps.iteritems():

            metrics_json = self._rest_request_to_json(tracking_url,
                                                      MAPREDUCE_JOBS_PATH,
                                                      MAPREDUCE_SERVICE_CHECK)

            if metrics_json.get('jobs'):
                if metrics_json['jobs'].get('job'):

                    for job_json in metrics_json['jobs']['job']:
                        job_id = job_json.get('id')
                        job_name = job_json.get('name')
                        user_name = job_json.get('user')

                        if job_id and job_name and user_name:
                            running_jobs[str(job_id)] = {'job_name': str(job_name),
                                                         'app_name': str(app_name),
                                                         'user_name': str(user_name),
                                                         'tracking_url': self._join_url_dir(tracking_url,
                                                                                            MAPREDUCE_JOBS_PATH,
                                                                                            job_id)}

                            tags = ['cluster_id:' + str(cluster_id),
                                    'app_name:' + str(app_name),
                                    'user_name:' + str(user_name),
                                    'job_name:' + str(job_name)]

                            self._set_metrics_from_json(tags, job_json, MAPREDUCE_JOB_METRICS)

        return running_jobs

    def _mapreduce_job_counters_metrics(self, running_jobs, cluster_id):
        for job_id, job_metrics in running_jobs.iteritems():
            job_name = job_metrics['job_name']

            if self.general_counters or (job_name in self.job_specific_counters):
                job_specific_metrics = self.job_specific_counters.get(job_name)

                metrics_json = self._rest_request_to_json(job_metrics['tracking_url'],
                                                          'counters',
                                                          MAPREDUCE_SERVICE_CHECK)

                if metrics_json.get('jobCounters'):
                    if metrics_json['jobCounters'].get('counterGroup'):

                        for counter_group in metrics_json['jobCounters']['counterGroup']:
                            group_name = counter_group.get('counterGroupName')

                            if group_name:
                                counter_metrics = set([])

                                if job_specific_metrics and group_name in job_specific_metrics:
                                    counter_metrics = counter_metrics.union(job_specific_metrics[group_name])

                                if group_name in self.general_counters:
                                    counter_metrics = counter_metrics.union(self.general_counters[group_name])

                                if counter_metrics:
                                    if counter_group.get('counter'):
                                        for counter in counter_group['counter']:
                                            counter_name = counter.get('name')

                                            if counter_name and counter_name in counter_metrics:
                                                tags = ['cluster_id:' + str(cluster_id),
                                                        'app_name:' + job_metrics.get('app_name'),
                                                        'user_name:' + job_metrics.get('user_name'),
                                                        'job_name:' + job_name,
                                                        'counter_name:' + str(counter_name).lower()]

                                                self._set_metrics_from_json(tags,
                                                                            counter,
                                                                            MAPREDUCE_JOB_COUNTER_METRICS)

    def _mapreduce_task_metrics(self, running_jobs, cluster_id):
        for job_id, job_stats in running_jobs.iteritems():

            metrics_json = self._rest_request_to_json(job_stats['tracking_url'],
                                                      'tasks',
                                                      MAPREDUCE_SERVICE_CHECK)

            if metrics_json.get('tasks'):
                if metrics_json['tasks'].get('task'):

                    for task in metrics_json['tasks']['task']:
                        task_type = task.get('type')

                        if task_type:
                            tags = ['cluster_id:' + str(cluster_id),
                                    'app_name:' + job_stats['app_name'],
                                    'user_name:' + job_stats['user_name'],
                                    'job_name:' + job_stats['job_name'],
                                    'task_type:' + str(task_type).lower()
                                    ]

                            if task_type == 'MAP':
                                self._set_metrics_from_json(tags, task, MAPREDUCE_MAP_TASK_METRICS)

                            elif task_type == 'REDUCE':
                                self._set_metrics_from_json(tags, task, MAPREDUCE_REDUCE_TASK_METRICS)

    def _set_metrics_from_json(self, tags, metrics_json, metrics):
        for status, (metric_name, metric_type) in metrics.iteritems():
            metric_status = metrics_json.get(status)

            if metric_status is not None:
                self._set_metric(metric_name,
                                 metric_type,
                                 metric_status,
                                 tags)

    def _set_metric(self, metric_name, metric_type, value, tags=None, device_name=None):
        if metric_type == HISTOGRAM:
            self.histogram(metric_name, value, tags=tags, device_name=device_name)
        else:
            self.log.error('Metric type "%s" unknown' % (metric_type))

    def _rest_request_to_json(self, address, object_path, service_name, *args, **kwargs):
        response_json = None

        service_check_tags = ['url:%s' % self._get_url_base(address)]

        url = address

        if object_path:
            url = self._join_url_dir(url, object_path)

        if args:
            for directory in args:
                url = self._join_url_dir(url, directory)

        self.log.debug('Attempting to connect to "%s"' % url)

        if kwargs:
            query = '&'.join(['{0}={1}'.format(key, value) for key, value in kwargs.iteritems()])
            url = urljoin(url, '?' + query)

        try:
            response = requests.get(url)
            response.raise_for_status()
            response_json = response.json()

        except Timeout as e:
            self.service_check(service_name,
                               AgentCheck.CRITICAL,
                               tags=service_check_tags,
                               message="Request timeout: {0}, {1}".format(url, e))
            raise

        except (HTTPError,
                InvalidURL,
                ConnectionError) as e:
            self.service_check(service_name,
                               AgentCheck.CRITICAL,
                               tags=service_check_tags,
                               message="Request failed: {0}, {1}".format(url, e))
            raise

        except JSONDecodeError as e:
            self.service_check(service_name,
                               AgentCheck.CRITICAL,
                               tags=service_check_tags,
                               message='JSON Parse failed: {0}, {1}'.format(url, e))
            raise

        except ValueError as e:
            self.service_check(service_name,
                               AgentCheck.CRITICAL,
                               tags=service_check_tags,
                               message=str(e))
            raise

        return response_json

    def _join_url_dir(self, url, *args):
        for path in args:
            url = url.rstrip('/') + '/'
            url = urljoin(url, path.lstrip('/'))

        return url

    def _get_url_base(self, url):
        s = urlsplit(url)
        return urlunsplit([s.scheme, s.netloc, '', '', ''])
