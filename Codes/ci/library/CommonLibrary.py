from __future__ import absolute_import
from library.utils import create_ssh
from library.utils import (
    parase_yaml_file, excutor_cmd)
from library.constants import (DEPLOY_MECHINE, BASE_DIR)


class CommonLibrary(object):
    def check_if_exist_process(self, project_name):
        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']
        yaml_file = "{}/{}".format(BASE_DIR, 'project_command.yaml')
        check_cmd = parase_yaml_file(yaml_file)[project_name]['exec_status']
        with create_ssh(ip, user, passwd) as ssh:
            out, err = excutor_cmd(ssh, check_cmd)
            if int(out) != 0:
                raise Exception("{} check faild".format(check_cmd))

    def check_if_not_exist_process(self, project_name):
        ip, user, passwd = DEPLOY_MECHINE['ip'], \
                           DEPLOY_MECHINE['user'], \
                           DEPLOY_MECHINE['passwd']
        yaml_file = "{}/{}".format(BASE_DIR, 'project_command.yaml')
        check_cmd = parase_yaml_file(yaml_file)[project_name]['exec_status']
        with create_ssh(ip, user, passwd) as ssh:
            out, err = excutor_cmd(ssh, check_cmd)
            if int(out) == 0:
                raise Exception("{} check faild".format(check_cmd))

    def check_if_exist_file(self, filepath):
        ip, user, passwd, file_path = filepath
        with create_ssh(ip, user, passwd) as ssh:
            stdout, stderr = excutor_cmd(ssh, 'ls {}'.format(file_path))
            if not stdout:
                raise Exception('{} not found in {}'.format(file_path, ip))


    def check_if_not_exist_file(self, filepath):
        ip, user, passwd, file_path = filepath
        with create_ssh(ip, user, passwd) as ssh:
            stdout, stderr = excutor_cmd(ssh, 'ls {}'.format(file_path))
            if stdout:
                raise Exception('{} not found in {}'.format(file_path, ip))