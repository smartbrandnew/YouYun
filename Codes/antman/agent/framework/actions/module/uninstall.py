# coding: utf-8

import json
import re
import sys

import nfs
from tornado import gen

from config import CircledConfig, Config
from constants import PKG_DIR, PKG_YAML_NAME
from framework.message.handlers import task
from framework.actions import module_logger
from framework.actions.module.base import Base
from framework.actions.module.lockfile import LockFile


class Uninstall(Base):
    """\
        Remove one or many modules
        To remove modules::

            [{
                "module_name": *****
            }]

    """

    @gen.coroutine
    def execute(self):
        try:
            for module in self.modules:
                module_name = module['module_name']
                yield self.remove(module_name)
                LockFile.remove_pkg(module_name)

            yield self.reporter.log_ok('Finish remove, reload config!', True)
            if self.reload_config:
                yield self.circle_cmd('reloadconfig')
        except Exception as e:
            yield self.reporter.log_error(e, True)
            sys.exit(1)

    @staticmethod
    def pre_stop(module_name):
        for task_id, running_task in task.running_tasks.items():
            try:
                if running_task.module_name == module_name:
                    running_task.stop()
                    task.running_tasks.pop(task_id)
            except Exception as e:
                module_logger.error(e)

    @gen.coroutine
    def remove(self, module_name):
        try:
            ret = yield self.circle_cmd('stop', module_name)
            yield self.reporter.log_ok(ret)
        except Exception as e:
            if not re.match(r'program[\s\S]+not found', str(e)):
                yield self.reporter.log_error(str(e))
                raise gen.Return(str(e))
        self.pre_stop(module_name)
        config = Config(nfs.join(PKG_DIR, module_name, PKG_YAML_NAME))

        # Execute pre_remove
        config.exec_lifecycle_script('pre_remove')
        # Remove
        nfs.remove(nfs.join(PKG_DIR, module_name))
        # Execute post_remove
        config.exec_lifecycle_script('post_remove')
        # Remove conf file for circled
        circled_config = CircledConfig(module_name)
        circled_config.remove_file()
        # Result
        yield self.reporter.log_ok('Remove {!r} successfully'
                                   ''.format(module_name))


if __name__ == '__main__':
    message = sys.stdin.read()
    if not message:
        print('The stdin is None, please input the right args with stdin!')
        sys.exit(1)

    uninstall = Uninstall(json.loads(message))
    uninstall.run()
