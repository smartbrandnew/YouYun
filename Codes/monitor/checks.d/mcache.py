import memcache

from checks import AgentCheck


class Memcache(AgentCheck):
    SOURCE_TYPE_NAME = 'memcached'

    DEFAULT_PORT = 11211

    GAUGES = [
        "total_items",
        "curr_items",
        "limit_maxbytes",
        "uptime",
        "bytes",
        "curr_connections",
        "connection_structures",
        "threads",
        "pointer_size"
    ]

    RATES = [
        "rusage_user",
        "rusage_system",
        "cmd_get",
        "cmd_set",
        "cmd_flush",
        "get_hits",
        "get_misses",
        "delete_misses",
        "delete_hits",
        "evictions",
        "bytes_read",
        "bytes_written",
        "cas_misses",
        "cas_hits",
        "cas_badval",
        "total_connections",
        "listen_disabled_num"
    ]

    SERVICE_CHECK = 'memcache.can_connect'

    def get_library_versions(self):
        return {"memcache": memcache.__version__}

    def _get_metrics(self, server, port, tags):
        mc = None
        service_check_tags = ["host:%s" % server, "port:%s" % port]
        try:
            self.log.debug("Connecting to %s:%s tags:%s", server, port, tags)
            mc = memcache.Client(["%s:%s" % (server, port)])
            raw_stats = mc.get_stats()

            assert len(raw_stats) == 1 and len(raw_stats[0]) == 2, \
                "Malformed response: %s" % raw_stats

            stats = raw_stats[0][1]
            for metric in stats:
                if metric in self.GAUGES:
                    our_metric = self.normalize(metric.lower(), 'memcache')
                    self.gauge(our_metric, float(stats[metric]), tags=tags)

                if metric in self.RATES:
                    our_metric = self.normalize(
                        "{0}_rate".format(metric.lower()), 'memcache')
                    self.rate(our_metric, float(stats[metric]), tags=tags)

            try:
                self.gauge(
                    "memcache.get_hit_percent",
                    100.0 * float(stats["get_hits"]) / float(stats["cmd_get"]),
                    tags=tags,
                )
            except ZeroDivisionError:
                pass

            try:
                self.gauge(
                    "memcache.fill_percent",
                    100.0 * float(stats["bytes"]) / float(stats["limit_maxbytes"]),
                    tags=tags,
                )
            except ZeroDivisionError:
                pass

            try:
                self.gauge(
                    "memcache.avg_item_size",
                    float(stats["bytes"]) / float(stats["curr_items"]),
                    tags=tags,
                )
            except ZeroDivisionError:
                pass

            uptime = stats.get("uptime", 0)
            self.service_check(
                self.SERVICE_CHECK, AgentCheck.OK,
                tags=service_check_tags,
                message="Server has been up for %s seconds" % uptime)
        except AssertionError:
            self.service_check(
                self.SERVICE_CHECK, AgentCheck.CRITICAL,
                tags=service_check_tags,
                message="Unable to fetch stats from server")
            raise Exception(
                "Unable to retrieve stats from memcache instance: {0}:{1}."
                "Please check your configuration".format(server, port))

        if mc is not None:
            mc.disconnect_all()
            self.log.debug("Disconnected from memcached")
        del mc

    def check(self, instance):
        socket = instance.get('socket')
        server = instance.get('url')
        if not server and not socket:
            raise Exception('Either "url" or "socket" must be configured')

        if socket:
            server = 'unix'
            port = socket
        else:
            port = int(instance.get('port', self.DEFAULT_PORT))
        custom_tags = instance.get('tags') or []

        tags = ["url:{0}:{1}".format(server, port)] + custom_tags

        self._get_metrics(server, port, tags)
