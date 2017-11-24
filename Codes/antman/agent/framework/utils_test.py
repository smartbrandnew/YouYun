import sys
from framework import settings
from framework.utils import update_query_params
from framework.utils import normalize_cmdline
from framework.utils import normalize_env


def test_update_query_params():
    url = update_query_params('http://127.0.0.1:18858/module/entity',
                              {'a': 15,
                               'b': True,
                               'c': 'test'})
    assert 'a=15' in url
    assert 'b=True' in url
    assert 'c=test' in url


def test_update_query_params_twice():
    url = update_query_params('http://127.0.0.1:18858/module/', {'cursor': 5})
    assert url == 'http://127.0.0.1:18858/module/?cursor=5'

    new_url = update_query_params(url, {'cursor': 8})
    assert new_url == 'http://127.0.0.1:18858/module/?cursor=8'


def test_normalize_cmdline():
    if settings.IS_WINDOWS:
        # string cmdline
        cmdline = 'c:\\a/b "c:\\a/b" c:/a\\b "C:/a\\b" ./a\\b "./a\\b" .\\a/b "./a/b" /?'
        cmdline = normalize_cmdline(cmdline)
        assert cmdline == r'c:\a\b "c:\a\b" c:\a\b "C:\a\b" .\a\b "a\b" .\a\b "a\b" /?'

        # list cmdline
        cmdline = [
            'c:\\a/b', 'c:\\a/b', 'c:/a\\b', 'C:/a\\b', './a\\b', './a\\b',
            '.\\a/b', './a/b', '/?'
        ]
        cmdline = normalize_cmdline(cmdline)
        assert cmdline == [
            'c:\\a\\b', 'c:\\a\\b', 'c:\\a\\b', 'C:\\a\\b', 'a\\b', 'a\\b',
            'a\\b', 'a\\b', '/?'
        ]
    else:
        # string cmdline
        cmdline = 'a/b /a/b'
        cmdline = normalize_cmdline(cmdline)
        assert cmdline == cmdline

        # list cmdline
        cmdline = ['a/b', '/a/b']
        cmdline = normalize_cmdline(cmdline)
        assert cmdline == cmdline

    cmdline = 'python xxx'
    cmdline = normalize_cmdline(cmdline)
    assert cmdline == '"{}" xxx'.format(sys.executable)

    cmdline = ['python', 'xxx']
    cmdline = normalize_cmdline(cmdline)
    assert cmdline == [sys.executable, 'xxx']


def test_normalize_env():
    if settings.IS_WINDOWS:
        env = {'A1': 'c:\\a/b', 'A2': './a/b', 'A3': '.\\a\\b'}
        env = normalize_env(env, relpath_prefix='a')
        assert env == {'A1': 'c:\\a\\b', 'A2': 'a\\a\\b', 'A3': 'a\\a\\b'}
    else:
        env = {
            'A1': '/a/b',
            'A2': './a/b',
        }
        env = normalize_env(env, relpath_prefix='/a')
        assert env == {
            'A1': '/a/b',
            'A2': '/a/a/b',
        }

    env = {'A1': 'python a'}
    env = normalize_env(env)
    assert env == {'A1': '"{}" a'.format(sys.executable)}
