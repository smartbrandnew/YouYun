import cProfile
import logging
import pstats
from cStringIO import StringIO

log = logging.getLogger('collector')


class AgentProfiler(object):
    PSTATS_LIMIT = 20
    DUMP_TO_FILE = True
    STATS_DUMP_FILE = './collector-stats.dmp'

    def __init__(self):
        self._enabled = False
        self._profiler = None

    def enable_profiling(self):
        if not self._profiler:
            self._profiler = cProfile.Profile()

        self._profiler.enable()
        log.debug("Agent profiling is enabled")

    def disable_profiling(self):

        self._profiler.disable()
        s = StringIO()
        ps = pstats.Stats(self._profiler, stream=s).sort_stats("cumulative")
        ps.print_stats(self.PSTATS_LIMIT)
        log.debug(s.getvalue())
        log.debug("Agent profiling is disabled")
        if self.DUMP_TO_FILE:
            log.debug("Pstats dumps are enabled. Dumping pstats output to {0}".format(self.STATS_DUMP_FILE))
            ps.dump_stats(self.STATS_DUMP_FILE)

    @staticmethod
    def wrap_profiling(func):
        def wrapped_func(*args, **kwargs):
            try:
                profiler = cProfile.Profile()
                profiler.enable()
                log.debug("Agent profiling is enabled")
            except Exception:
                log.warn("Cannot enable profiler")

            ret_val = func(*args, **kwargs)

            try:
                profiler.disable()
                s = StringIO()
                ps = pstats.Stats(profiler, stream=s).sort_stats("cumulative")
                ps.print_stats(AgentProfiler.PSTATS_LIMIT)
                log.info(s.getvalue())
            except Exception:
                log.warn("Cannot disable profiler")

            return ret_val

        return wrapped_func


def pretty_statistics(stats):
    before = stats.get('before')
    after = stats.get('after')

    mem_before = before.get('memory_info')
    mem_after = after.get('memory_info')

    if mem_before and mem_after:
        return """
            Memory Before (RSS): {0}
            Memory After (RSS): {1}
            Difference (RSS): {2}
            Memory Before (VMS): {3}
            Memory After (VMS): {4}
            Difference (VMS): {5}
            """.format(mem_before['rss'], mem_after['rss'], mem_after['rss'] - mem_before['rss'],
                       mem_before['vms'], mem_after['vms'], mem_after['vms'] - mem_before['vms'])
    else:
        return ""
