# coding: utf-8
import pf
import nfs
import json
import traceback

from tornado import web, gen
from tornado.ioloop import IOLoop
from ci import logger
from ci.constants import PROGRESS_TEMPLATE_SIGN, WINDOWS, AIX, SUSE
from ci.notification.dingding import Dingding
from ci.utils import maybe_download_python, get_branch_by_ref, cd, execute


platform = pf.get_platform()


class TestHandler(web.RequestHandler):

    @gen.coroutine
    def get(self):
        self.write('test webhook for ci server')

    @gen.coroutine
    def post(self):
        payload = json.loads(self.request.body)
        project_url = payload['repository']['url']
        project_name = payload['repository']['name']
        project_branch = get_branch_by_ref(payload['ref'])
        ip = payload['ip']

        # test
        result = reason = ''
        try:
            Tester(project_name, project_url, project_branch).run()
            result = u'单元测试：成功\n' \
                     u'测试环境：{}{}-{}({})\n'.format(
                        platform.dist, platform.version, platform.cpu, ip)
        except Exception:
            result = u'单元测试：失败\n' \
                     u'测试环境：{}{}-{}({})\n'.format(
                        platform.dist, platform.version, platform.cpu, ip)
            reason = traceback.format_exc()
            logger.error(reason)
            raise web.HTTPError(reason=reason)
        finally:
            IOLoop.current().spawn_callback(Dingding.send_push,
                                            payload,
                                            result,
                                            reason=reason)


class Tester(object):

    def __init__(self, project_name, project_url, project_branch):
        self.project_name = project_name
        self.project_url = project_url
        self.project_branch = project_branch
        self.test_funcs = {
            'agent': self._test_agent,
            'manager': self._test_manager,
            'manager-web': self._test_manager_web,
            'dispatcher': self._test_dispatcher
        }

    def run(self):
        logger.info(PROGRESS_TEMPLATE_SIGN.format('Start testing "{}"'
                                                  .format(self.project_name)))
        self.check_project()
        self.test_funcs[self.project_name]()
        logger.info(PROGRESS_TEMPLATE_SIGN.format('Test "{}" done'
                                                  .format(self.project_name)))

    def check_project(self):
        self._check(self.project_name, self.project_url, self.project_branch)

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

    def _test_agent(self):
        with cd(self.project_name):
            production = 'requirements/production.txt'
            development = 'requirements/development.txt'
            # 修改 production
            if AIX or SUSE:
                with open(production) as f:
                    lines = f.readlines()
                lines = [line for line in lines if not
                         line.strip().startswith('paramiko')]
                with open(production, 'w') as f:
                    f.writelines(lines)

            # pip 安装依赖
            python, pip = maybe_download_python()
            if WINDOWS:
                execute([pip, 'install', '-U', '-r', development], shell=False)
            else:
                execute([python, pip, 'install', '-U', '-r', development], shell=False)

            # 单元测试
            execute([python, '-m', 'pytest', 'framework'], shell=False)

    def _test_manager(self):
        pass

    def _test_manager_web(self):
        pass

    def _test_dispatcher(self):
        pass
