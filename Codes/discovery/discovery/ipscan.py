import json
import os
import sys
import time

from discovery import logger
from libnmap.process import NmapProcess

try:
    from framework.task.logger import TaskLogger, EngineType
except ImportError:
    pass


class IpScan(object):
    loop_time = 5

    def __init__(self):
        super(IpScan, self).__init__()
        agent_id = os.getenv('ANT_AGENT_ID')
        task_id = os.getenv('ANT_TASK_ID')
        stdin = sys.stdin.read()
        args = json.loads(stdin).get(agent_id)
        self.tasklogger = TaskLogger(task_id=task_id,
                                     engine=EngineType.REQUESTS)
        self.ip_ranges = args['ip_ranges']
        self.ports = [str(port) for port in args['ports']]
        self.progress = 0

    def run(self):
        options = '-sT -sU -p{} -v -O'.format(','.join(self.ports))
        logger.debug('Ip scan options: {!r}, ip_ranges: {}'.format(
            options, self.ip_ranges))

        nmp_proc = NmapProcess(self.ip_ranges, options)
        nmp_proc.run_background()

        while nmp_proc.is_running():
            self.progress = float(nmp_proc.progress)
            log = {'progress': self.progress, 'log': ''}
            self._log(log)
            time.sleep(self.loop_time)

        if nmp_proc.rc == 0:
            stdout = nmp_proc.stdout
            self._result(stdout)
        else:
            sys.stderr.write(nmp_proc.stdout)
            sys.stderr.close()
            sys.exit(2)

    def _log(self, log):
        logger.debug('log: {}'.format(log))
        self.tasklogger.log(json.dumps(log))

    def _result(self, result):
        logger.info('result: {}'.format(result))
        self.tasklogger.result(result)


if __name__ == '__main__':
    ipscan_executor = IpScan()
    ipscan_executor.run()
