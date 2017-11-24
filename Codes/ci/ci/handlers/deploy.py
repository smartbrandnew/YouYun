# coding: utf-8
import os
import pf
import nfs
import json
import traceback

from tornado import web, gen
from ci import logger
from ci.notification.dingding import Dingding
from ci.utils import (excutor_cmd, get_branch_by_ref, get_version,
                      scp_upload_file, cd, get_config, execute)
from ci.constants import (AGENT_BUILD_TEMPLATE_CMD, INSTALL_DIR, REPO_DIR,
                          PROGRESS_TEMPLATE_SIGN, WINDOWS, SUSE)

platform = pf.get_platform()
ant_build_url = get_config()['projects']['ant-build']


class DeployHandler(web.RequestHandler):
    @gen.coroutine
    def get(self):
        self.write('deploy webhook for ci server')

    @gen.coroutine
    def post(self):
        payload = json.loads(self.request.body)
        if payload['object_kind'] == 'push':
            project_name = payload['repository']['name'],
            project_url = payload['repository']['url'],
            project_branch = get_branch_by_ref(payload['ref'])

            # only deploy branch 'master'
            if project_branch == 'master':
                result = reason = ''
                try:
                    Deployer(project_name, project_url, project_branch).run()
                    result = u'自动部署：成功\n' \
                             u'部署环境：{}{}-{}'.format(
                        platform.dist, platform.version, platform.cpu)
                except Exception:
                    result = u'自动部署：失败\n' \
                             u'部署环境：{}{}-{}'.format(
                        platform.dist, platform.version, platform.cpu)
                    reason = traceback.format_exc()
                finally:
                    yield Dingding.send_push(payload, result, reason=reason)


class Deployer(object):
    def __init__(self, project_name, project_url, project_branch):
        self.project_name = project_name
        self.project_url = project_url
        self.project_branch = project_branch
        self.deploy_funcs = {
            'manager': self.deploy_manager,
            'manager-web': self.deploy_manager_web,
            'dispatcher': self.deploy_dispatcher,
            'agent': self.deploy_agent,
            'platform-ant-nginx': self.deploy_nginx,
            'platform-ant-node': self.deploy_node,
            'platform-ant-python': self.deploy_python,
        }

    def run(self):
        logger.info(PROGRESS_TEMPLATE_SIGN.format('Start deploying "{}"'
                                                  .format(self.project_name)))
        self.check_project()
        self.deploy_funcs[self.project_name]()
        logger.info(PROGRESS_TEMPLATE_SIGN.format('Deploy "{}" done'
                                                  .format(self.project_name)))

    def check_project(self):
        self._check(self.project_name, self.project_url, self.project_branch)

    def check_ant_build(self):
        self._check('ant-build', ant_build_url, 'master')

    @staticmethod
    def _check(project_name, project_url, project_branch='master'):
        if not nfs.exists(project_name):
            execute('git clone {} {}'.format(project_url, project_name))
            with cd(project_name):
                execute('git checkout {}'.format(project_branch))
        else:
            with cd(project_name):
                execute('git checkout {}'.format(project_branch))
                execute('git pull')

    @gen.coroutine
    def deploy_manager(self):
        with cd(self.project_name):
            execute('mvn clean install -Dmaven.test.skip=true')

        cmd = 'sh {}/bin/stop.sh'.format(INSTALL_DIR[self.project_name])
        excutor_cmd(cmd)
        for file in ['manager-core/bin', 'manager-core/lib',
                     'manager-core/module']:
            local_path = os.path.join(self.project_name, file)
            server_path = INSTALL_DIR[self.project_name]
            scp_upload_file(local_path, server_path)
        scp_upload_file(
            '{}/manager-core/conf/i18n'.format(self.project_name),
            '{}/conf/il8n'.format(INSTALL_DIR[self.project_name]))
        scp_upload_file(
            '{}/manager-core/conf/web.xml'.format(self.project_name),
            '{}/conf/web.xml'.format(INSTALL_DIR[self.project_name]))
        cmd = 'source /etc/profile && su - uyun -c "sh {}/bin/start.sh"' \
            .format(INSTALL_DIR[self.project_name])
        excutor_cmd(cmd)

    @gen.coroutine
    def deploy_manager_web(self):
        with cd(self.project_name):
            execute(['npm', 'install'])
            execute(['npm', 'run', 'build'])

        for filename in ['static', 'frontend', 'build', 'index.html']:
            local_path = os.path.join(self.project_name, filename)
            server_path = INSTALL_DIR[self.project_name]
            scp_upload_file(local_path, server_path)

    @gen.coroutine
    def deploy_dispatcher(self):
        with cd(self.project_name):
            execute(['../node/bin/node', '../node/bin/yarn'])
            execute(['../node/bin/node',
                     '../node/lib/node_modules/npm/bin/npm-cli.js', 'run',
                     'build'])
            if nfs.exists('dispatcher'):
                nfs.remove('dispatcher')
        nfs.rename('dist', 'dispatcher')

        cmd = 'su  uyun -c "pm2 delete all"'
        excutor_cmd(cmd)
        for file in ['node_modules', 'bin', 'scripts', 'install.sh',
                     'uninstall.sh', 'check_status.sh', 'dispatcher']:
            local_path = os.path.join(self.project_name, file)
            server_path = INSTALL_DIR[self.project_name]
            scp_upload_file(local_path, server_path)
        cmd = 'cd {} && su  uyun -c "pm2 start process.json"'.format(
            INSTALL_DIR[self.project_name])
        excutor_cmd(cmd)

    @gen.coroutine
    def deploy_agent(self):
        if SUSE and platform.version.startswith('10'):
            system = 'suse10-{}'.format(platform.cpu)
        else:
            system = '{}-{}'.format(platform.system, platform.cpu)

        if WINDOWS:
            postfix = 'zip'
            python_path = 'venv/Scripts/python.exe'
        else:
            postfix = 'tar.gz'
            python_path = 'venv/bin/python'

        build_cmd = AGENT_BUILD_TEMPLATE_CMD.format(
            pyton=python_path,
            branch=self.project_branch,
            system=system)

        agent_version = get_version(
            '{}/manifest.yaml'.format(self.project_name))
        agent_name = 'agent-{}-{}-{}.{}'.format(system,
                                                platform.cpu,
                                                agent_version,
                                                postfix)

        with cd(self.project_name):
            execute(build_cmd)
            local_path = os.path.join('dist', agent_name)
            server_path = REPO_DIR
            scp_upload_file(local_path, server_path)

    def deploy_discovery(self):
        pass

    def deploy_nginx(self):
        pass

    def deploy_python(self):
        pass

    def deploy_node(self):
        pass
