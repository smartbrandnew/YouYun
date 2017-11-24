# coding: utf-8
import os

from circle.client import CircleClient
from circle.commands import get_commands
from circle.circlectl import ControllerRunner, parse_arguments
from circle.constants import DEFAULT_ENDPOINT_DEALER


class ControllerAPI(object):
    client = CircleClient(
        endpoint=os.environ.get('CIRCUSCTL_ENDPOINT', DEFAULT_ENDPOINT_DEALER))
    commands = get_commands()
    ctrl_runner = ControllerRunner(commands, client)

    @classmethod
    def run(cls, argv):
        opts = parse_arguments(argv, cls.commands)
        return cls.ctrl_runner.run(opts['args'])
