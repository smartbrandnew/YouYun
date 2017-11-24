from tornado import gen
from tornado.testing import AsyncTestCase, gen_test
from framework.ioloop import get_io_loop
from framework.config import config
from framework.message.transfers import kinesis
from framework.message.transfers.kinesis import KinesisTransfer


def mock_post_to_kinesis(*args):
    pass


class KinesisTransferTest(AsyncTestCase):

    def setUp(self):
        super(KinesisTransferTest, self).setUp()
        kinesis.post_to_kinesis = mock_post_to_kinesis
        KinesisTransfer.max_records_num = 2
        KinesisTransfer.interval = 0.01
        with config.mock({
                'kinesis_stream': 'test',
                'aws_region': 'ap-northeast-1'
        }):
            self.transfer = KinesisTransfer()

    def get_new_ioloop(self):
        return get_io_loop()

    @gen_test
    def test_send(self):
        yield self.transfer.send([{
            'id': 'xxx',
            'type': 'yyy',
            'body': 'zzz'
        }, {
            'id': 'aaa',
            'type': 'bbb',
            'body': 'ccc'
        }, {
            'id': 'aaa1',
            'type': 'bbb1',
            'body': 'ccc1'
        }])
        while self.transfer.get_pending_records_num():
            self.assertTrue(self.transfer.get_pending_records_num() in
                            (3, 1, 0))
            yield gen.sleep(KinesisTransfer.interval)
