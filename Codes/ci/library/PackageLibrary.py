from __future__ import absolute_import
import yaml
from os.path import join
from library.utils import create_ssh
from library.utils import (
    parase_yaml_file, excutor_cmd)

from library.constants import (
    PACKAGE_NAME, ANT_BUILD, DIST_ROOT,
    PROJECT_ROOT, BUILD_CMD, WITH_MODULE,
    SAVE_PACKAGE_MECHINE, BASE_DIR,
)


class PackageLibrary(object):
    def demo(self, host, username, password):
        with create_ssh(host, username, password) as ssh:
            excutor_cmd(ssh, 'ls -l /tmp')

    def build_package(self, project_name, branch):
        yaml_file = join(BASE_DIR,'build_machine.yaml')
        info = parase_yaml_file(yaml_file)[project_name]
        project_name = project_name.split('-')[0]
        if project_name == 'dispatcher':
            with_module = WITH_MODULE
        else:
            with_module = ''
        
        ip = info['ip']
        user = info['user']
        passwd = info['passwd']
        system_platform = info['platform']
        system_bit = info['system_bit']
        build_system = info['build_system']
        python_exe = info['python_exe']
        postfix = info['postfix']

        build_cmd = BUILD_CMD.format(
            python_exe,
            project_name,
            branch,
            build_system,
            with_module
        )

        build_cmd = ('cd {} && {}').format(ANT_BUILD, build_cmd)
        print build_cmd
        with create_ssh(ip, user, passwd) as ssh:
            excutor_cmd(ssh, build_cmd, debug=True)
            manifest_path = '{}/{}/{}'.format(PROJECT_ROOT, project_name, 'manifest.yaml')
            module_path = '{}/{}/{}'.format(PROJECT_ROOT, project_name, 'module.yaml')
  
            out, err = excutor_cmd(ssh, 'cat {}'.format(manifest_path), debug=True)
            if not out:
                out, err = excutor_cmd(ssh, 'cat {}'.format(module_path), debug=True)
            project_version = yaml.load(out)['version']
            package_name = PACKAGE_NAME[project_name]
            file_name = package_name.format(
                system_platform=system_platform,
                system_bit=system_bit,
                version=project_version,
                postfix=postfix
            )
            source_path = '{}/{}'.format(DIST_ROOT, file_name)
            target_path = 'root@{}:{}'.format(SAVE_PACKAGE_MECHINE['ip'], DIST_ROOT)
            scp_cmd = 'scp {} {}'.format(source_path, target_path)

            if ip != SAVE_PACKAGE_MECHINE['ip']:
                excutor_cmd(ssh, scp_cmd,debug=True)
        return (ip, user, passwd, source_path)

        
        
        



        

        
        
    


        

        

