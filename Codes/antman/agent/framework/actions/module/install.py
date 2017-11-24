# coding: utf-8
import json
import os
import sys
import traceback
import nfs
import pf
import yaml
from tornado import gen
from collections import OrderedDict
from semver import Spec

from config import CircledConfig, Config
from constants import PKG_YAML_NAME, PKG_DIR
from framework.actions import module_logger

from framework.actions.errors import MessageError
from framework.actions.module.base import Base
from framework.actions.module.package import PkgHelper
from framework.actions.module.lockfile import LockFile


class Install(Base):
    """
        To install modules::
        [
            {
                "module_name": "module name",
                "filename": "module file name"
                "env":{}
            }
        ]
    """

    def __init__(self, *args, **kwargs):
        super(Install, self).__init__(*args, **kwargs)
        self.platform = pf.get_platform()
        self.modules_info = {}

    @gen.coroutine
    def execute(self):
        try:
            for module in self.modules:
                module_name = module['module_name']
                module_env = self.deal_env(module.get('env'))

                if not module.get('filename'):
                    yield self.reporter.log_error(
                        'Filename for {} is None'.format(module_name))
                    continue
                if nfs.exists(nfs.join(PKG_DIR, module_name)) and \
                        LockFile.check_pkg(module_name):
                    yield self.reporter.log_error(
                        '{} is already installed!'.format(module_name))
                    continue
                yield self.reporter.log_ok('Begin to install {}'.format(
                    module_name))
                required_pkgs = OrderedDict()
                env_params = {}
                yield self.deal_dependencies(
                    module['filename'],
                    required_pkgs,
                    env_params,
                    name=module_name)

                yield self.install(module_env, required_pkgs, env_params)
                LockFile.add_pkg(module_name,
                                 required_pkgs[module_name]['config'])

            # After install and reload

            yield self.reporter.log_ok('Finish install modules!', True)

            if self.reload_config:
                yield self.circle_cmd('reloadconfig')
        except Exception as e:
            module_logger.error(traceback.format_exc())
            yield self.reporter.log_error(str(e), True)
            sys.exit(1)

    @gen.coroutine
    def deal_dependencies(self,
                          filename,
                          required_pkgs,
                          env,
                          parent=None,
                          name=None):
        yield self.download_pkg(filename)
        pkg_path, pkg_info = PkgHelper.uncompress(filename, parent)
        # PkgHelper.check_module(name, pkg_info)

        config = Config(nfs.join(pkg_path, PKG_YAML_NAME))
        if not os.path.exists(pkg_path):
            raise MessageError("Uncompress {!r} failed !".format(filename))

        depends_env = config.content.get('env')
        if depends_env:
            env.update({
                key: value
                for key, value in depends_env.items() if key not in env.keys()
            })

        if 'dependencies' in pkg_info:
            depend_pkg = self.get_depend_pkg(pkg_info['dependencies'])
            if not depend_pkg:
                raise MessageError('The dependencies of {} is not support '
                                   'for this system !!'.format(filename))
            if not parent:
                parent = pkg_path

            for pkg in depend_pkg:
                yield self.deal_dependencies(pkg, required_pkgs, env, parent)

        required_pkgs[config.content.get('name')] = {
            'pkg_path': pkg_path,
            'pkg_info': pkg_info,
            'config': config
        }

    def get_depend_pkg(self, depends):
        for platform in depends.keys():
            system, version = pf.split_platform(platform)
            if system in (
                    self.platform.system, self.platform.dist, '{}{}'.format(
                        self.platform.system, self.platform.cpu), '{}{}'.format(
                            self.platform.dist, self.platform.cpu)):
                if not version:
                    return depends[platform]
                else:
                    if os.name == 'nt':
                        if Spec(version).match(self.platform.kernel):
                            return depends[platform]
                    else:
                        if Spec(version).match(self.platform.version):
                            return depends[platform]
        return None

    @gen.coroutine
    def install(self, module_env, required_pkgs, pkg_env_params):
        try:
            for module_name, pkg_dict in required_pkgs.items():
                pkg_info = pkg_dict['pkg_info']
                yield self.reporter.log_ok(
                    'Begin to exec the post_install of {}'.format(module_name))
                config = pkg_dict['config']
                config.exec_lifecycle_script(
                    'post_install', cwd=pkg_dict['pkg_path'], env=module_env)
                yield self.reporter.log_ok('Finish exec post_install!')
                # Add conf file for circled
                circled_config = CircledConfig(module_name)
                # Add module work dir
                env_params = config.content.get('env')
                if env_params:
                    env_params.update({'ANT_MODULE_ROOT': pkg_dict['pkg_path']})
                else:
                    env_params = {'ANT_MODULE_ROOT': pkg_dict['pkg_path']}

                env_params.update(pkg_env_params)
                circled_config.generate_file(
                    config.content.get('run'), env_params,
                    config.content.get('scripts'))

                pkg_info['env'] = env_params
                yml_path = nfs.join(pkg_dict['pkg_path'], PKG_YAML_NAME)
                with open(yml_path, 'w') as f:
                    yaml.dump(pkg_info, f, default_flow_style=False)
                yield self.reporter.log_ok('Install "{}" successfully'
                                           ''.format(module_name))
        except Exception as e:
            module_logger.debug(e, exc_info=True)
            raise MessageError(e)


if __name__ == '__main__':
    message = sys.stdin.read()
    if not message:
        print('The stdin is None, please input the right args with stdin!')
        sys.exit(1)

    install = Install(json.loads(message))
    install.run()
