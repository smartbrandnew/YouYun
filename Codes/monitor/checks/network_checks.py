import threading
import time
from Queue import Empty, Queue
from collections import defaultdict

from checks import AgentCheck
from checks.libs.thread_pool import Pool
from config import _is_affirmative

TIMEOUT = 180
DEFAULT_SIZE_POOL = 6
MAX_LOOP_ITERATIONS = 1000
FAILURE = "FAILURE"


class Status:
    DOWN = "DOWN"
    WARNING = "WARNING"
    CRITICAL = "CRITICAL"
    UP = "UP"


class EventType:
    DOWN = "servicecheck.state_change.down"
    UP = "servicecheck.state_change.up"


class NetworkCheck(AgentCheck):
    SOURCE_TYPE_NAME = 'servicecheck'
    SERVICE_CHECK_PREFIX = 'network_check'

    STATUS_TO_SERVICE_CHECK = {
        Status.UP: AgentCheck.OK,
        Status.WARNING: AgentCheck.WARNING,
        Status.CRITICAL: AgentCheck.CRITICAL,
        Status.DOWN: AgentCheck.CRITICAL,
    }

    def __init__(self, name, init_config, agentConfig, instances):
        AgentCheck.__init__(self, name, init_config, agentConfig, instances)

        self.statuses = {}
        self.notified = {}
        self.nb_failures = 0
        self.pool_started = False

        names = []
        for inst in instances:
            name = inst.get('name', None)
            if not name:
                raise Exception("All instances should have a 'name' parameter,"
                                " error on instance: {0}".format(inst))
            if name in names:
                raise Exception("Duplicate names for instances with name {0}"
                                .format(inst['name']))

    def stop(self):
        self.stop_pool()
        self.pool_started = False

    def start_pool(self):

        self.log.info("Starting Thread Pool")
        default_size = min(self.instance_count(), DEFAULT_SIZE_POOL)
        self.pool_size = int(self.init_config.get('threads_count', default_size))

        self.pool = Pool(self.pool_size)

        self.resultsq = Queue()
        self.jobs_status = {}
        self.jobs_results = {}
        self.pool_started = True

    def stop_pool(self):
        self.log.info("Stopping Thread Pool")
        if self.pool_started:
            self.pool.terminate()
            self.pool.join()
            self.jobs_status.clear()
            assert self.pool.get_nworkers() == 0

    def restart_pool(self):
        self.stop_pool()
        self.start_pool()

    def check(self, instance):
        if not self.pool_started:
            self.start_pool()
        if threading.activeCount() > 5 * self.pool_size + 5:
            raise Exception("Thread number (%s) is exploding. Skipping this check" % threading.activeCount())
        self._process_results()
        self._clean()
        name = instance.get('name', None)
        if name is None:
            self.log.error('Each service check must have a name')
            return

        if name not in self.jobs_status:
            self.jobs_status[name] = time.time()
            self.jobs_results[name] = self.pool.apply_async(self._process, args=(instance,))
        else:
            self.log.error("Instance: %s skipped because it's already running." % name)

    def _process(self, instance):
        try:
            statuses = self._check(instance)

            if isinstance(statuses, tuple):
                status, msg = statuses
                self.resultsq.put((status, msg, None, instance))

            elif isinstance(statuses, list):
                for status in statuses:
                    sc_name, status, msg = status
                    self.resultsq.put((status, msg, sc_name, instance))

        except Exception:
            result = (FAILURE, FAILURE, FAILURE, instance)
            self.resultsq.put(result)

    def _process_results(self):
        for i in xrange(MAX_LOOP_ITERATIONS):
            try:
                status, msg, sc_name, instance = self.resultsq.get_nowait()
            except Empty:
                break

            instance_name = instance['name']
            if status == FAILURE:
                self.nb_failures += 1
                if self.nb_failures >= self.pool_size - 1:
                    self.nb_failures = 0
                    self.restart_pool()

                self._clean_job(instance_name)
                continue

            self.report_as_service_check(sc_name, status, instance, msg)

            skip_event = _is_affirmative(instance.get('skip_event', False))
            if not skip_event:
                self.warning(
                    "Using events for service checks is deprecated in favor of monitors and will be removed in future versions of the Monitor Agent.")
                event = None

                if instance_name not in self.statuses:
                    self.statuses[instance_name] = defaultdict(list)

                self.statuses[instance_name][sc_name].append(status)

                window = int(instance.get('window', 1))

                if window > 256:
                    self.log.warning("Maximum window size (256) exceeded, defaulting it to 256")
                    window = 256

                threshold = instance.get('threshold', 1)

                if len(self.statuses[instance_name][sc_name]) > window:
                    self.statuses[instance_name][sc_name].pop(0)

                nb_failures = self.statuses[instance_name][sc_name].count(Status.DOWN)

                if nb_failures >= threshold:
                    if self.notified.get((instance_name, sc_name), Status.UP) != Status.DOWN:
                        event = self._create_status_event(sc_name, status, msg, instance)
                        self.notified[(instance_name, sc_name)] = Status.DOWN
                else:
                    if self.notified.get((instance_name, sc_name), Status.UP) != Status.UP:
                        event = self._create_status_event(sc_name, status, msg, instance)
                        self.notified[(instance_name, sc_name)] = Status.UP

                if event is not None:
                    self.events.append(event)

            self._clean_job(instance_name)

    def _clean_job(self, instance_name):
        if instance_name in self.jobs_status:
            self.log.debug("Instance: %s cleaned from jobs status." % instance_name)
            del self.jobs_status[instance_name]

        if instance_name in self.jobs_results:
            self.log.debug("Instance: %s cleaned from jobs results." % instance_name)
            ret = self.jobs_results[instance_name].get()
            if isinstance(ret, Exception):
                self.log.exception("Exception in worker thread: {0}".format(ret))
            del self.jobs_results[instance_name]

    def _check(self, instance):
        raise NotImplementedError

    def _clean(self):
        now = time.time()
        for name, start_time in self.jobs_status.iteritems():
            if now - start_time > TIMEOUT:
                self.log.critical("Restarting Pool. One check is stuck: %s" % name)
                self.restart_pool()
                break
