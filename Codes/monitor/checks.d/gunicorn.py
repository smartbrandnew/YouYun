import time

import psutil

from checks import AgentCheck


class GUnicornCheck(AgentCheck):
    PROC_NAME = 'proc_name'

    CPU_SLEEP_SECS = 0.1

    IDLE_TAGS = ["state:idle"]
    WORKING_TAGS = ["state:working"]
    SVC_NAME = "gunicorn.is_running"

    def get_library_versions(self):
        return {"psutil": psutil.__version__}

    def check(self, instance):
        self.log.debug("Running instance: %s", instance)

        if not instance or self.PROC_NAME not in instance:
            raise GUnicornCheckError("instance must specify: %s" % self.PROC_NAME)

        proc_name = instance.get(self.PROC_NAME)
        master_proc = self._get_master_proc_by_name(proc_name)

        worker_procs = master_proc.children()
        working, idle = self._count_workers(worker_procs)

        msg = "%s working and %s idle workers for %s" % (working, idle, proc_name)
        status = AgentCheck.CRITICAL if working == 0 and idle == 0 else AgentCheck.OK

        self.service_check(self.SVC_NAME, status, tags=['app:' + proc_name], message=msg)

        self.log.debug("instance %s procs - working:%s idle:%s" % (proc_name, working, idle))
        self.gauge("gunicorn.workers", working, self.WORKING_TAGS)
        self.gauge("gunicorn.workers", idle, self.IDLE_TAGS)

    def _count_workers(self, worker_procs):
        working = 0
        idle = 0

        if not worker_procs:
            return working, idle

        cpu_time_by_pid = {}
        for proc in worker_procs:
            try:
                cpu_time_by_pid[proc.pid] = sum(proc.cpu_times())
            except psutil.NoSuchProcess:
                self.warning('Process %s disappeared while scanning' % proc.name)
                continue

        time.sleep(self.CPU_SLEEP_SECS)

        for proc in worker_procs:
            if proc.pid not in cpu_time_by_pid:
                continue
            try:
                cpu_time = sum(proc.cpu_times())
            except Exception:
                self.log.debug("Couldn't collect cpu time for %s" % proc)
                continue
            if cpu_time == cpu_time_by_pid[proc.pid]:
                idle += 1
            else:
                working += 1

        return working, idle

    def _get_master_proc_by_name(self, name):
        master_name = GUnicornCheck._get_master_proc_name(name)
        master_procs = [p for p in psutil.process_iter() if p.cmdline() and p.cmdline()[0] == master_name]
        if len(master_procs) == 0:
            self.service_check(self.SVC_NAME, AgentCheck.CRITICAL, tags=['app:' + name],
                               message="No gunicorn process with name %s found" % name)
            raise GUnicornCheckError("Found no master process with name: %s" % master_name)
        elif len(master_procs) > 1:
            raise GUnicornCheckError("Found more than one master process with name: %s" % master_name)
        else:
            return master_procs[0]

    @staticmethod
    def _get_master_proc_name(name):
        return "gunicorn: master [%s]" % name


class GUnicornCheckError(Exception):
    pass
