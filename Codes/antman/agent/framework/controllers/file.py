# coding: utf-8
import nfs
import os
import logging
import time
from tornado import web, gen
from tornado.locks import Semaphore
from tornado.httpclient import AsyncHTTPClient
from framework import settings
from framework.config import config

MAX_BODY_SIZE = 4 * 1024.0 * 1024.0 * 1024.0  # 4GB
GMT_FORMAT = '%a, %d %b %Y %H:%M:%S GMT'
AsyncHTTPClient.configure(None, max_body_size=MAX_BODY_SIZE)

logger = logging.getLogger('default')
semaphore = Semaphore(config.get('file_service_semaphore', 5))


class FileHandler(web.RequestHandler):

    @gen.coroutine
    def get(self):
        self.file_name = self.get_argument('filename')  # type: str
        self.space_dir = nfs.join(settings.REPO_DIR,
                                  settings.REPO_ANT_SPACENAME)
        if not nfs.exists(self.space_dir):
            nfs.makedirs(self.space_dir)
        self.file_path = nfs.join(self.space_dir, self.file_name)
        lock_file_name = nfs.extsep + self.file_name + nfs.extsep + 'lock'
        self.lock_file = nfs.join(self.space_dir, lock_file_name)
        logger.info('#%d Request file: %s', id(self.request), self.file_name)

        if nfs.exists(self.lock_file):
            yield self.wait_for_file_complete()
        else:
            is_cache_hit = yield self.try_to_return_file_cache()
            if is_cache_hit:
                return
            logger.info('#%d File cache missed: %s',
                        id(self.request), self.file_path)
            nfs.touch(self.lock_file)
            yield self.request_file_from_upstream()

    @gen.coroutine
    def try_to_return_file_cache(self):
        is_cache_hit = False
        if nfs.exists(self.file_path):
            flag = yield self.check_file_mtime()
            if flag:
                logger.info('#%d File cache hit: %s',
                            id(self.request), self.file_path)
                self.write(self.file_path)  # 直接返回本地缓存文件的路径
                is_cache_hit = True
            else:
                logger.info('#{} The cache file is too old and need to '
                            'download the new file'.format(id(self.request)))
                nfs.remove(self.file_path)
        raise gen.Return(is_cache_hit)

    @gen.coroutine
    def check_file_mtime(self):
        is_match = False
        try:
            http_client = AsyncHTTPClient()
            sep = '' if config['upstream'].endswith('/') else '/'
            url = '{upstream}{sep}file/{filename}'.format(
                upstream=config['upstream'], sep=sep, filename=self.file_name)
            response = yield http_client.fetch(
                url, method="HEAD", validate_cert=False)
            m_time = response.headers.get('Last-Modified', None)
            if m_time:
                m_time = time.mktime(time.strptime(m_time, GMT_FORMAT))
                file_m_time = os.stat(self.file_path).st_mtime
                if m_time and file_m_time and m_time == file_m_time:
                    is_match = True
                else:
                    logger.error('#{} The m_time from server is {}, the m_time '
                                 'from cache is {} !'.format(
                                     id(self.request), m_time, file_m_time))
        except Exception as e:
            logger.error('#{} Get Last-Modified from server error: {}'
                         .format(id(self.request), e))
        raise gen.Return(is_match)

    @gen.coroutine
    def wait_for_file_complete(self):
        logger.info('#%d File lock exists, waiting for complete: %s',
                    id(self.request), self.file_path)
        lock_watch_interval = config.get('file_service_lock_watch_interval',
                                         5.0)
        current_timeout = 0.0
        request_timeout = config.get('file_service_request_timeout', 3600.0)
        while current_timeout < request_timeout:
            yield gen.sleep(lock_watch_interval)
            current_timeout += lock_watch_interval
            if not nfs.exists(self.lock_file) and nfs.exists(self.file_path):
                self.write(self.file_path)  # 文件缓存完毕，返回本地缓存文件的路径
                return
            else:
                logger.info('#%d Waiting for file complete: %s',
                            id(self.request), self.file_path)
        # 等待文件缓存超时
        self.send_error(504, message='Waiting for file complete timeout')

    def on_file_chunk(self, chunk):
        if self.temp_file and not self.temp_file.closed:
            self.temp_file.write(chunk)

    @gen.coroutine
    def request_file_from_upstream(self):
        # 不存在本地缓存，也不存在lock文件，向上游请求下载
        try:
            yield semaphore.acquire()  # 文件下载临界区，防止AsyncHTTPClient资源耗尽
            self.temp_file = open(self.file_path, 'wb')
            http_client = AsyncHTTPClient()
            sep = '' if config['upstream'].endswith('/') else '/'
            url = '{upstream}{sep}file/{filename}'.format(
                upstream=config['upstream'], sep=sep, filename=self.file_name)

            response = yield http_client.fetch(
                url,
                validate_cert=False,
                streaming_callback=self.on_file_chunk,
                connect_timeout=config.get('file_service_connect_timeout',
                                           3600.0),
                request_timeout=config.get('file_service_request_timeout',
                                           3600.0))
            self.generate_response(response)
        except Exception as exc:
            logger.error(
                '#%d Error while fetching %s: %s',
                id(self.request),
                self.file_name,
                exc,
                exc_info=True)
            self.send_error(500, message=exc)
        finally:
            yield semaphore.release()
            self.close_file_resource()

    def generate_response(self, response):
        if response.code == 200:
            logger.info('#%d Complete, change file last-modified',
                        id(self.request))
            if self.temp_file and not self.temp_file.closed:
                self.temp_file.close()
            m_time = response.headers.get('Last-Modified', None)
            m_time = time.mktime(time.strptime(m_time, GMT_FORMAT)) \
                if m_time else time.time()
            # 将文件的修改时间改成和server端相同，来判断文件是否更新了
            os.utime(self.file_path, (int(time.time()), int(m_time)))
            self.write(self.file_path)
        else:
            logger.error('#%d Non-200 file response from upstream: %d',
                         id(self.request), response.code)
            self.send_error(
                500,
                message='Non-200 file response from upstream:{}'
                .format(response.code))

    def close_file_resource(self):
        try:
            if self.temp_file and not self.temp_file.closed:
                self.temp_file.close()
            if nfs.exists(self.lock_file):
                nfs.remove(self.lock_file)
        except Exception as exc:
            logger.error(
                '#%d Error while closing resource (%s): %s',
                id(self.request),
                self.file_path,
                exc,
                exc_info=True)
            self.send_error(500, message=exc)  # FIXME: 有可能是请求结束后调用
