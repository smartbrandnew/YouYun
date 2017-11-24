import contextlib

from framework.config import config
from framework.message.transfers.http import HttpTransfer
from framework.message.transfers.kinesis import KinesisTransfer

transfers = {
    'http': {
        'constructor': HttpTransfer,
        'instance': None
    },
    'kinesis': {
        'constructor': KinesisTransfer,
        'instance': None
    }
}


def get_transfer(scheme):
    if not transfers[scheme]['instance']:
        transfers[scheme]['instance'] = transfers[scheme]['constructor']()
    return transfers[scheme]['instance']


def get_current_transfer():
    return get_transfer(config['transfer'])


@contextlib.contextmanager
def mock_transfer(transfer):
    scheme = config['transfer']
    origin_transfer = transfers[scheme]['instance']
    transfers[scheme]['instance'] = transfer
    yield
    transfers[scheme]['instance'] = origin_transfer
