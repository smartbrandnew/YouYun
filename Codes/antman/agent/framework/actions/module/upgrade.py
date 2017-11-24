# coding: utf-8
from tornado import gen

import nfs
import sys
import json
from framework.actions.module.base import Base
from framework.actions.module.constants import (PKG_DIR, PKG_UPGRADE_DIR,
                                                PKG_YAML_NAME)
from framework.actions.module.package import PkgHelper
from framework.actions.module.config import Config
from framework.actions.module.uninstall import Uninstall
from framework.actions.module.install import Install


class Upgrade(Base):
    """
        To install modules::
        [
            {
                "module_name": "module name",
                "filename": "module file name"
                "env":{}
            }

    """

    @gen.coroutine
    def execute(self):
        try:
            for module in self.modules:
                module_name = module['module_name']
                module_env = self.deal_env(module.get('env'))
                if not module.get('filename'):
                    yield self.reporter.log_error('Filename for {} is None'
                                                  ''.format(module_name))
                    continue
                if not nfs.exists(nfs.join(PKG_DIR, module_name)):
                    yield self.reporter.log_error('{} is not installed！'
                                                  ''.format(module_name))
                    continue
                yield self.reporter.log_ok('Begin to Upgrade {}'
                                           ''.format(module_name))
                pkg_path = yield self.do_pre(module['filename'], module_env)
                uninstall = Uninstall([module], self.io_loop, False)
                uninstall.reporter = self.reporter
                yield uninstall.remove(module_name)
                yield self.do_install(module)
                nfs.remove(pkg_path)
            yield self.reporter.log_ok('Finish upgrade modules!', True)
            yield self.circle_cmd('reloadconfig')
        except Exception as e:
            yield self.reporter.log_error(str(e), True)
            sys.exit(1)

    @gen.coroutine
    def do_pre(self, filename, module_env):
        yield self.download_pkg(filename)
        pkg_path, pkg_info = PkgHelper.uncompress(filename, PKG_UPGRADE_DIR)
        config = Config(nfs.join(pkg_path, PKG_YAML_NAME))
        config.exec_lifecycle_script(
            'pre_upgrade', cwd=pkg_path, env=module_env)
        yield self.reporter.log_ok('Finish exec pre_upgrade!')
        raise gen.Return(pkg_path)

    @gen.coroutine
    def do_install(self, module):
        install = Install([module], self.io_loop, False)
        install.reporter = self.reporter
        yield install.execute()


if __name__ == '__main__':
    message = sys.stdin.read()
    if not message:
        print('The stdin is None, please input the right args with stdin!')
        sys.exit(1)

    upgrade = Upgrade(json.loads(message))
    upgrade.run()
