import os
import sys

from discovery import logger
from subprocess import Popen, PIPE, STDOUT


class DeepScan(object):
    def __init__(self):
        super(DeepScan, self).__init__()
        cwd = os.getcwd()
        self.java = os.path.join(os.getenv('JRE_HOME'), 'bin', 'java')
        self.lib = os.path.join(cwd, 'discovery', 'deepscan', 'lib', '*')
        self.userdir = os.path.join(cwd, 'discovery', 'deepscan')

    def run(self):
        # java -Xmx800m -XX:MaxPermSize=128m -classpath ./libs/*
        # -Duser.dir=./scripts com.execute.GroovyStartupEncrypt
        cmd = [
            self.java,
            '-Xmx800m',
            '-XX:MaxPermSize=128m',
            '-classpath',
            self.lib,
            '-Duser.dir={}'.format(self.userdir),
            'com.execute.GroovyStartupEncrypt'
        ]
        logger.debug('Deep scan cmd: {}'.format(cmd))

        proc = Popen(
            args=cmd,
            stdout=PIPE,
            stderr=STDOUT,
            shell=False
        )

        proc.wait()
        if proc.poll() != 0:
            sys.stderr.write(proc.stdout.read())
            sys.stderr.close()
            sys.exit(2)


if __name__ == '__main__':
    deepscan_executor = DeepScan()
    deepscan_executor.run()
