# coding: utf-8
import time
import json
import logging
from concurrent.futures import ProcessPoolExecutor

from tornado import gen
from framework.config import config
from framework.message.models import Message
from framework.message.transfers.base import BaseTransfer
from framework.ioloop import get_io_loop
from boto3.exceptions import Boto3Error

logger = logging.getLogger('default')

executor = ProcessPoolExecutor(max_workers=1)


def post_to_kinesis(kinesis_kwargs, stream_name, records):
    """将 records 放入到 kinesis stream 中"""
    if not records:
        return

    import boto3
    kinesis = boto3.client(**kinesis_kwargs)
    response = kinesis.put_records(StreamName=stream_name, Records=records)

    if response['ResponseMetadata']['HTTPStatusCode'] != 200:
        raise Boto3Error


class KinesisTransfer(BaseTransfer):
    max_records_num = 500
    interval = 0.2

    def __init__(self):
        self._stream_name = config['kinesis_stream']
        self._pending_records = []

        if not self._stream_name:
            raise Boto3Error('kinesis_stream should not be empty')

        self._kinesis_kwargs = dict(
            service_name='kinesis',
            aws_access_key_id=config['aws_access_key_id'],
            aws_secret_access_key=config['aws_secret_access_key'],
            region_name=config['aws_region'])
        get_io_loop().spawn_callback(self.run)

    @gen.coroutine
    def send(self, messages, identity='default'):
        if isinstance(messages, Message):
            messages = [messages]
        self.add_records(messages, partition_key=identity)
        raise gen.Return(True)

    def add_records(self, messages, partition_key):
        """ 将 record 加入到 Poster 的 _pending_records 列表中
        """
        records = [{
            'Data': json.dumps(msg),
            'PartitionKey': partition_key
        } for msg in messages]
        self._pending_records.extend(records)

    def get_pending_records_num(self):
        return len(self._pending_records)

    @gen.coroutine
    def run(self):
        while True:
            if self.get_pending_records_num > self.max_records_num:
                records = self._pending_records[:self.max_records_num]
                self._pending_records = self._pending_records[
                    self.max_records_num:]
            else:
                records = self._pending_records
                self._pending_records = []

            last = time.time()
            try:
                yield executor.submit(post_to_kinesis, self._kinesis_kwargs,
                                      self._stream_name, records)
            except Exception as e:
                logger.error(str(e), exc_info=True)

            # 每个分片每秒最多写入5次，保证至少0.2s一次循环
            interval = time.time() - last
            if interval < self.interval:
                yield gen.sleep(self.interval - interval)
