from framework.message.transfers import mock_transfer, get_current_transfer


def test_mock_transfer():

    class MockedTransfer(object):
        pass

    mocked_transfer = MockedTransfer()

    with mock_transfer(mocked_transfer):
        transfer = get_current_transfer()
        assert transfer is mocked_transfer
