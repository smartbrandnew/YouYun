from __future__ import absolute_import
from library.utils import create_ssh
import time
import requests
from library.utils import (
    parase_yaml_file, excutor_cmd)

from library.constants import (
    PACKAGE_NAME, ANT_BUILD, DIST_ROOT,DEPLOY_MECHINE,
    SAVE_PACKAGE_MECHINE, BASE_DIR, INSTALL_CMD,
    TEMP_INSTALL_PATH, INSTALL_DIR,
    DOCKER_MACHINE, VERSION, DOCKER_URL)


class InstallLibrary(object):
    def demo(self, host, username, password):
        with create_ssh(host, username, password) as ssh:
            excutor_cmd(ssh, 'ls -l /tmp')

    def upload_omp_package(self, project_name, version):
        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']
        with create_ssh(ip, user, passwd) as ssh:
            excutor_cmd(ssh, 'mkdir -p {}'.format(TEMP_INSTALL_PATH))

        file_name = PACKAGE_NAME[project_name].format(version=version)
        ip, user, passwd = SAVE_PACKAGE_MECHINE['ip'], \
                           SAVE_PACKAGE_MECHINE['user'], \
                           SAVE_PACKAGE_MECHINE['passwd']
        with create_ssh(ip, user, passwd) as ssh:
            source_path = '{}/{}'.format(DIST_ROOT, file_name)
            target_path = 'root@{}:{}'.format(
                DEPLOY_MECHINE['ip'],
                TEMP_INSTALL_PATH)
            print 'scp {} {}'.format(source_path, target_path)
            excutor_cmd(ssh, 'scp {} {}'.format(source_path, target_path), debug=True)

    def install_omp_package(self, project_name, version):
        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']
        file_name = PACKAGE_NAME[project_name].format(version=version)
        file_name = '{}/{}'.format(TEMP_INSTALL_PATH, file_name)
        file_path = 'platform-ant-{}'.format(project_name)
        install_script = '{}/{}/{}'.format(TEMP_INSTALL_PATH, file_path, 'install.sh')
        install_command = INSTALL_CMD.format(install_script)
        print install_command
        with create_ssh(ip, user, passwd) as ssh:
            excutor_cmd(ssh, 'tar zxf {} -C {}'.format(file_name, TEMP_INSTALL_PATH), debug=True)
            excutor_cmd(ssh, install_command, debug=True)
        return (ip, user, passwd, INSTALL_DIR[project_name])

    def uninstall_omp_package(self, project_name):
        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']
        yaml_file = "{}/{}".format(BASE_DIR, 'project_command.yaml')
        uninstall_cmd = parase_yaml_file(yaml_file)[project_name]['exec_uninstall']
        with create_ssh(ip, user, passwd) as ssh:
            excutor_cmd(ssh, uninstall_cmd)
        return (ip, user, passwd, INSTALL_DIR[project_name])

    def start_omp_package(self, project_name):
        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']
        yaml_file = "{}/{}".format(BASE_DIR, 'project_command.yaml')
        start_cmd = parase_yaml_file(yaml_file)[project_name]['exec_start']
        print start_cmd
        with create_ssh(ip, user, passwd) as ssh:
            excutor_cmd(ssh, start_cmd, debug=True)
        time.sleep(3)

    def stop_omp_package(self, project_name):
        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']
        yaml_file = "{}/{}".format(BASE_DIR, 'project_command.yaml')
        stop_cmd = parase_yaml_file(yaml_file)[project_name]['exec_stop']
        print stop_cmd
        with create_ssh(ip, user, passwd) as ssh:
            excutor_cmd(ssh, stop_cmd, debug=True)

    def check_if_exists_dependecies(self, dependencies):
        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']
        with create_ssh(ip, user, passwd) as ssh:
            for item in dependencies.split(','):
                out, err = excutor_cmd(ssh, 'ls {}'.format(INSTALL_DIR[item]))
                if not out:
                    self.install_omp_package(item)

    def create_docker_images(self, version):
        ip, user, passwd = DOCKER_MACHINE['ip'], \
                           DOCKER_MACHINE['user'], \
                           DOCKER_MACHINE['passwd']
        print ip, user, passwd
        with create_ssh(ip, user, passwd) as ssh:
            excutor_cmd(ssh, 'mkdir -p {}'.format(DIST_ROOT))
            for project in ['nginx', 'python', 'node', 'dispatcher', 'manager']:
                if project in ['dispatcher', 'manager']:
                    VERSION[project] = VERSION[project].format(version)
                project_version = VERSION[project]
                project_name = PACKAGE_NAME[project]
                project_name = project_name.format(version=project_version)
                source_path = '{}@{}:{}/{}'.format(
                    SAVE_PACKAGE_MECHINE['user'],
                    SAVE_PACKAGE_MECHINE['ip'],
                    DIST_ROOT,
                    project_name
                )
                target_path = DIST_ROOT
                excutor_cmd(ssh, 'scp {} {}'.format(source_path, target_path), debug=True)

            excutor_cmd(ssh, 'cd {} && git pull origin master'.format(ANT_BUILD))
            excutor_cmd(ssh, 'cd {} && sh {} {}'.format(ANT_BUILD, 'docker_build.sh', version), debug=True)

    def check_if_docker_images_create_sucess(self, images_name):
        ip, user, passwd = DOCKER_MACHINE['ip'], \
                           DOCKER_MACHINE['user'], \
                           DOCKER_MACHINE['passwd']
        with create_ssh(ip, user, passwd) as ssh:
            out, err = excutor_cmd(ssh, 'docker images|grep {} |grep -v grep |wc -l'.format(images_name))
            if not out:
                raise Exception('Images create faild')

    def check_if_docker_upload_sucess(self, images_name):
        response = requests.get(DOCKER_URL).content
        if images_name not in response:
            raise Exception('images upload faild')






















