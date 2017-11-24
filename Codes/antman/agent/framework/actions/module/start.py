# coding: utf-8

import json
import sys

from tornado import gen

from framework.actions.module.base import Base


class Start(Base):
    """\
        Start modules with glob name
        ==============================================

        Event name ("%s"), event body (a dict, JSON object)
        -----------


        To get modules' information::

            {
                "glob_name": "package glob name"
            }

        The response return the string.
    """

    @gen.coroutine
    def execute(self):
        try:
            for module in self.modules:
                ret = yield self.circle_cmd('start', module['module_name'])
                yield self.reporter.log_ok(ret)
            yield self.reporter.log_ok('Finish restart all!', True)
        except Exception as e:
            yield self.reporter.log_error(str(e), True)
            sys.exit(1)


if __name__ == '__main__':
    message = sys.stdin.read()
    if not message:
        print('The stdin is None, please input the right args with stdin!')
        sys.exit(1)

    start = Start(json.loads(message))
    start.run()
