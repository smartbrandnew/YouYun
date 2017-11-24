from urlparse import urljoin

import requests

from checks import AgentCheck

DEFAULT_RM_URI = 'http://localhost:8088'
DEFAULT_TIMEOUT = 5

INFO_URI = '/ws/v1/cluster/info'

YARN_CLUSTER_METRICS_PATH = '/ws/v1/cluster/metrics'
YARN_APPS_PATH = '/ws/v1/cluster/apps'

YARN_NODES_PATH = '/ws/v1/cluster/nodes'

GAUGE = 'gauge'

SERVICE_CHECK_NAME = 'yarn.can_connect'

YARN_CLUSTER_METRICS_ELEMENT = 'clusterMetrics'

YARN_CLUSTER_METRICS = {
    'appsSubmitted': ('yarn.metrics.apps_submitted', GAUGE),
    'appsCompleted': ('yarn.metrics.apps_completed', GAUGE),
    'appsPending': ('yarn.metrics.apps_pending', GAUGE),
    'appsRunning': ('yarn.metrics.apps_running', GAUGE),
    'appsFailed': ('yarn.metrics.apps_failed', GAUGE),
    'appsKilled': ('yarn.metrics.apps_killed', GAUGE),
    'reservedMB': ('yarn.metrics.reserved_mb', GAUGE),
    'availableMB': ('yarn.metrics.available_mb', GAUGE),
    'allocatedMB': ('yarn.metrics.allocated_mb', GAUGE),
    'totalMB': ('yarn.metrics.total_mb', GAUGE),
    'reservedVirtualCores': ('yarn.metrics.reserved_virtual_cores', GAUGE),
    'availableVirtualCores': ('yarn.metrics.available_virtual_cores', GAUGE),
    'allocatedVirtualCores': ('yarn.metrics.allocated_virtual_cores', GAUGE),
    'totalVirtualCores': ('yarn.metrics.total_virtual_cores', GAUGE),
    'containersAllocated': ('yarn.metrics.containers_allocated', GAUGE),
    'containersReserved': ('yarn.metrics.containers_reserved', GAUGE),
    'containersPending': ('yarn.metrics.containers_pending', GAUGE),
    'totalNodes': ('yarn.metrics.total_nodes', GAUGE),
    'activeNodes': ('yarn.metrics.active_nodes', GAUGE),
    'lostNodes': ('yarn.metrics.lost_nodes', GAUGE),
    'unhealthyNodes': ('yarn.metrics.unhealthy_nodes', GAUGE),
    'decommissionedNodes': ('yarn.metrics.decommissioned_nodes', GAUGE),
    'rebootedNodes': ('yarn.metrics.rebooted_nodes', GAUGE),
}

YARN_APP_METRICS = {
    'progress': ('yarn.apps.progress', GAUGE),
    'startedTime': ('yarn.apps.started_time', GAUGE),
    'finishedTime': ('yarn.apps.finished_time', GAUGE),
    'elapsedTime': ('yarn.apps.elapsed_time', GAUGE),
    'allocatedMB': ('yarn.apps.allocated_mb', GAUGE),
    'allocatedVCores': ('yarn.apps.allocated_vcores', GAUGE),
    'runningContainers': ('yarn.apps.running_containers', GAUGE),
    'memorySeconds': ('yarn.apps.memory_seconds', GAUGE),
    'vcoreSeconds': ('yarn.apps.vcore_seconds', GAUGE),
}

YARN_NODE_METRICS = {
    'lastHealthUpdate': ('yarn.node.last_health_update', GAUGE),
    'usedMemoryMB': ('yarn.node.used_memory_mb', GAUGE),
    'availMemoryMB': ('yarn.node.avail_memory_mb', GAUGE),
    'usedVirtualCores': ('yarn.node.used_virtual_cores', GAUGE),
    'availableVirtualCores': ('yarn.node.available_virtual_cores', GAUGE),
    'numContainers': ('yarn.node.num_containers', GAUGE),
}


class YarnCheck(AgentCheck):
    def check(self, instance):

        rm_address = instance.get('resourcemanager_uri', DEFAULT_RM_URI)

        cluster_id = self._get_cluster_id(rm_address)

        self._yarn_cluster_metrics(cluster_id, rm_address)
        self._yarn_app_metrics(rm_address)
        self._yarn_node_metrics(cluster_id, rm_address)

    def _get_cluster_id(self, rm_address):

        info_json = self._rest_request_to_json(rm_address, INFO_URI)

        cluster_id = info_json.get('clusterInfo', {}).get('id')
        if cluster_id is not None:
            return cluster_id

        raise Exception('Unable to retrieve cluster ID from ResourceManger')

    def _yarn_cluster_metrics(self, cluster_id, rm_address):

        metrics_json = self._rest_request_to_json(rm_address, YARN_CLUSTER_METRICS_PATH)

        if metrics_json:
            tags = ['cluster_id:' + str(cluster_id)]

            yarn_metrics = metrics_json[YARN_CLUSTER_METRICS_ELEMENT]

            if yarn_metrics is not None:
                self._set_yarn_metrics_from_json(tags, yarn_metrics, YARN_CLUSTER_METRICS)

    def _yarn_app_metrics(self, rm_address):

        metrics_json = self._rest_request_to_json(rm_address, YARN_APPS_PATH)

        if metrics_json:
            if metrics_json['apps'] is not None:
                if metrics_json['apps']['app'] is not None:

                    for app_json in metrics_json['apps']['app']:
                        cluster_id = app_json['clusterId']
                        app_id = app_json['id']

                        tags = ['cluster_id:' + str(cluster_id), 'app_id:' + str(app_id)]

                        self._set_yarn_metrics_from_json(tags, app_json, YARN_APP_METRICS)

    def _yarn_node_metrics(self, cluster_id, rm_address):

        metrics_json = self._rest_request_to_json(rm_address, YARN_NODES_PATH)

        if metrics_json:
            if metrics_json['nodes'] is not None:
                if metrics_json['nodes']['node'] is not None:

                    tags = ['cluster_id:' + str(cluster_id)]

                    for node_json in metrics_json['nodes']['node']:
                        node_id = node_json['id']
                        tags.append('node_id:' + str(node_id))

                        self._set_yarn_metrics_from_json(tags, node_json, YARN_NODE_METRICS)

    def _set_yarn_metrics_from_json(self, tags, metrics_json, yarn_metrics):

        for status, metric in yarn_metrics.iteritems():
            metric_name, metric_type = metric

            if metrics_json.get(status) is not None:
                self._set_metric(metric_name,
                                 metric_type,
                                 metrics_json[status],
                                 tags)

    def _set_metric(self, metric_name, metric_type, value, tags=None, device_name=None):

        if metric_type == GAUGE:
            self.gauge(metric_name, value, tags=tags, device_name=device_name)
        else:
            self.log.error('Metric type "%s" unknown' % (metric_type))

    def _rest_request_to_json(self, address, object_path):

        response_json = None

        service_check_tags = ['instance:%s' % self.hostname]

        url = urljoin(address, object_path)

        try:
            response = requests.get(url)
            response.raise_for_status()
            response_json = response.json()

        except requests.exceptions.Timeout as e:
            self.service_check(SERVICE_CHECK_NAME,
                               AgentCheck.CRITICAL,
                               tags=service_check_tags,
                               message="Request timeout: {0}, {1}".format(url, e))
            raise

        except (requests.exceptions.HTTPError,
                requests.exceptions.InvalidURL,
                requests.exceptions.ConnectionError) as e:
            self.service_check(SERVICE_CHECK_NAME,
                               AgentCheck.CRITICAL,
                               tags=service_check_tags,
                               message="Request failed: {0}, {1}".format(url, e))
            raise

        except ValueError as e:
            self.service_check(SERVICE_CHECK_NAME,
                               AgentCheck.CRITICAL,
                               tags=service_check_tags,
                               message=str(e))
            raise

        else:
            self.service_check(SERVICE_CHECK_NAME,
                               AgentCheck.OK,
                               tags=service_check_tags,
                               message='Connection to %s was successful' % url)

        return response_json
