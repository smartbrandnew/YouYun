# coding: utf-8
import json
import traceback

from tornado import gen, httpclient
from ding_client import DingClient

from ci import logger
from ci.constants import PROGRESS_TEMPLATE_SIGN
from ci.utils import get_branch_by_ref, get_chinese_name, get_config

action_names = {
    'open': u'发起',
    'update': u'更新',
    'merge': u'合并',
    'close': u'关闭',
    'reopen': u'再次打开'
}

assignee_mobiles = {
    115: '18768113738',  # 王斌鑫
    122: '18767104664',  # 竺夏栋
    280: '18758010950',  # 王冬情
    259: '15658008923',  # 沈剑芬
    249: '18606517392',  # 钟锦威
    203: '18061791570',  # 徐志刚
    207: '17682313530'   # 董晨
}


class Dingding(object):
    http_client = httpclient.AsyncHTTPClient()
    url = get_config()['dingding'].get('url')

    @classmethod
    @gen.coroutine
    def _send(cls, title=None, text=None, is_atall=False, at_mobiles=None):
        logger.info(PROGRESS_TEMPLATE_SIGN.format('Send to dingding'))

        headers = {'Content-Type': 'application/json'}
        if is_atall:
            msg = {
                'msgtype': 'text',
                'text': {
                    'content': u'{}\n{}'.format(title, text)
                },
                'at': {
                    'isAtAll': True
                }
            }
        elif at_mobiles:
            msg = {
                'msgtype': 'text',
                'text': {
                    'content': u'{}\n{}'.format(title, text)
                },
                'at': {
                    'atMobiles': at_mobiles,
                    'isAtAll': False
                }
            }
        else:
            msg = {
                'msgtype': 'markdown',
                'markdown': {
                    'title': title,
                    'text': u'{}\n{}'.format(title, text)
                }
            }
        logger.debug(msg)

        try:
            yield cls.http_client.fetch(
                cls.url,
                method='POST',
                headers=headers,
                body=json.dumps(
                    msg, encoding='utf8'),
                validate_cert=False)
        except httpclient.HTTPError as e:
            if e.response:
                logger.error(e.response.body)
            logger.error(traceback.format_exc())
        except Exception:
            logger.error(traceback.format_exc())

    @classmethod
    @gen.coroutine
    def send_merge_request(cls, payload, result, action='open', reason=None):
        username = get_chinese_name(payload['user']['username'])
        project_name = payload['repository']['name']
        project_url = payload['repository']['homepage']
        action_name = action_names.get(action, action)

        object_attributes = payload['object_attributes']
        source_branch = object_attributes['source_branch']
        target_branch = object_attributes['target_branch']
        mr_title = object_attributes['title']
        mr_description = object_attributes['description']
        mr_url = object_attributes['url']

        title = u'{} 在 [{}]({}) 项目中{}了 Merge Request'.format(
            username, project_name, project_url, action_name)
        text = u'#### 合并分支: {} -> {}\n' \
               u'#### MR 标题: {}\n' \
               u'#### MR 描述: {}\n' \
               u'#### [点击查看MR]({})\n' \
               u'#### {}'.format(
            source_branch, target_branch, mr_title, mr_description, mr_url,
            result)
        if reason:
            text += u'\n#### 失败原因: {}'.format(reason)
        yield cls._send(title, text)

    @classmethod
    @gen.coroutine
    def send_push(cls, payload, result, reason=None):
        username = get_chinese_name(payload['user_name'])
        project_name = payload['repository']['name']
        project_url = payload['repository']['homepage']
        branch = get_branch_by_ref(payload['ref'])

        commits_text = ''
        for commit in payload['commits']:
            commits_text += u'\n#### [{}]({}): {}'.format(
                commit['id'][:8], commit['url'],
                commit['message'].rstrip('\n'))

        title = u'{} 在 [{}]({}) 项目中发起了 Push'.format(username, project_name,
                                                    project_url)
        text = u'#### 分支: {}\n' \
               u'#### 提交内容: {}\n' \
               u'#### {}'.format(branch, commits_text, result)
        if reason:
            text += u'\n#### 失败原因: {}'.format(reason)
        yield cls._send(title, text)

    @classmethod
    @gen.coroutine
    def send_workday_notify(cls):
        title = u'每日例会通知'
        text = u'小伙伴们，请把 “昨日完成” 和 “今日计划” 发到群里哈~\n' \
               u'也不要忘记很快就要开例会咯！\n'
        yield cls._send(title, text, is_atall=True)

    @classmethod
    @gen.coroutine
    def send_at_assignee(cls, assignee_id):
        mobile = assignee_mobiles.get(assignee_id)
        if mobile:
            yield cls._send(u'请注意', '', at_mobiles=[mobile])
        else:
            print u'======= assignee_id:{} 不被识别 ======='.format(assignee_id)

    @classmethod
    @gen.coroutine
    def send_comment(cls, payload):
        username = get_chinese_name(payload['user']['username'])
        project_name = payload['repository']['name']
        project_url = payload['repository']['homepage']
        comment_url = payload['object_attributes']['url']
        comment = payload['object_attributes']['note']

        title = u'{} 在 [{}]({}) 项目中发起了 评论'.format(username, project_name,
                                                  project_url)
        text = u'#### 内容: {}\n' \
               u'#### [点击查看评论]({})\n'.format(comment, comment_url)

        yield cls._send(title, text)

    @classmethod
    @gen.coroutine
    def send_notify_to(cls, payload):
        client = DingClient(
            "ding1aeb3554483ce275",
            "Yu7PKg0Lhy8XVrgpys4Kq5_91bi6K6V-d3JnvgDD2PfqQ8lan3BXNGm4bXDX83FY")
        user = client.find_user(user_name="董晨")
        user.send_text()
