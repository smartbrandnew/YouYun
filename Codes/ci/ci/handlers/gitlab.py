import re
import json
import traceback

from tornado import web, gen, httpclient
from tornado.ioloop import IOLoop
from ci import logger
from ci.utils import get_config
from ci.constants import TEST_TEMPLATE_URL, DEPLOY_TEMPLATE_URL


class GitLabHandler(web.RequestHandler):

    def __init__(self, application, request, **kwargs):
        super(GitLabHandler, self).__init__(application, request, **kwargs)
        self.http_client = httpclient.AsyncHTTPClient()

    @gen.coroutine
    def get(self):
        self.write('gitlab webhook for gitlab')

    @gen.coroutine
    def post(self):
        payload = json.loads(self.request.body)
        project_name = payload['repository']['name']

        test_machine = get_config()['test_machine']
        for ip in test_machine[project_name]:
            payload['ip'] = ip
            IOLoop.current().spawn_callback(self.send_to_ci_agent,
                                            TEST_TEMPLATE_URL.format(ip),
                                            json.dumps(payload))

        # deploy_machine = get_config()['deploy_machine']
        # for ip in deploy_machine[project_name]:
        #     payload['ip'] = ip
        #     self.send_to_ci_agent(DEPLOY_TEMPLATE_URL.format(ip),
        #                           json.dumps(payload))

    @gen.coroutine
    def send_to_ci_agent(self, url, body):
        try:
            logger.debug('CI Agent ({}) start testing and deploying'
                         .format(url))
            yield self.http_client.fetch(
                url,
                method='POST',
                body=body,
                request_timeout=0
            )
            logger.debug('CI Agent ({}) test and deploy success'.format(url))
        except httpclient.HTTPError as e:
            logger.error('CI Agent ({}) test and deploy error'.format(url))
            if e.response:
                logger.error(e.response.body)
            else:
                logger.error(str(e))
        except Exception:
            logger.error('CI Agent ({}) test and deploy error'.format(url))
            logger.error(traceback.format_exc())
