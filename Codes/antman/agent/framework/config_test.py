from framework.config import config


def test_config_mock():
    with config.mock({'upstream': 'aaa', 'tenant': 'bbb'}):
        assert config['upstream'] == 'aaa'
        assert config['tenant'] == 'bbb'
        with config.mock({'upstream': 'xxx', 'tenant': 'yyy'}):
            assert config['upstream'] == 'xxx'
            assert config['tenant'] == 'yyy'
        assert config['upstream'] == 'aaa'
        assert config['tenant'] == 'bbb'
