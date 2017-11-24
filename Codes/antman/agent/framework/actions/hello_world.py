# coding: utf-8
import os

from framework.actions import logger
from framework.actions.reporter import Reporter

if __name__ == '__main__':
    reporter = Reporter(os.environ['ANT_TASK_ID'], logger, asynchronous=False)
    reporter.log_ok('Hello from log')
    reporter.log_ok('Hello from result', done=True)
