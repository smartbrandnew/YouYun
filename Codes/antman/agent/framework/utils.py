# coding: utf-8
import re
import sys
import nfs
import string
import urllib
import urlparse
import settings


def update_query_params(url, params):
    # type: (str, dict) -> str
    parts = list(urlparse.urlparse(url))
    query = dict(urlparse.parse_qsl(parts[4]))
    query.update(params)
    parts[4] = urllib.urlencode(query)
    new_url = urlparse.urlunparse(parts)
    return new_url


win_path_prefixs = tuple(['{}:\\'.format(c) for c in string.ascii_letters] + [
    '{}:/'.format(c) for c in string.ascii_letters
] + ['.\\', './'])
unix_path_prefixs = tuple(['{}:/'.format(c)
                           for c in string.ascii_letters] + ['./', '/'])


def _is_path(likepath):
    likepath = likepath.strip()
    if settings.IS_WINDOWS:
        return likepath.startswith(win_path_prefixs)
    else:
        return likepath.startswith(unix_path_prefixs)


def _resub_replace_path(matchobj):
    match_str = matchobj.group(0)
    if _is_path(match_str):
        return nfs.normpath(match_str)
    return match_str


def normalize_cmdline(cmdline):
    u"""
        将命令行中的路径标准化，python开头的内容会被替换为sys.executable

        在Windows上会有额外处理，凡是盘符（如C:/, C:\）或./或.\开头的字符串均会
        通过nfs.normpath进行标准化处理
    """

    if settings.IS_WINDOWS:
        if isinstance(cmdline, str):
            cmdline = re.sub('"([\s\S]+?)"',
                             _resub_replace_path,
                             cmdline,
                             flags=re.IGNORECASE)

            likepaths = cmdline.split('"')
            for i, likepath in enumerate(likepaths):
                if _is_path(likepath):
                    likepaths[i] = nfs.normpath(likepath)
            cmdline = '"'.join(likepaths)
        elif isinstance(cmdline, (list, tuple)):
            for i, likepath in enumerate(cmdline):
                if _is_path(likepath):
                    cmdline[i] = nfs.normpath(likepath)
    # 替换 python
    if isinstance(cmdline, str):
        cmdline = cmdline.strip()
        cmdline = re.sub('^ *python',
                         '"{}"'.format(sys.executable.replace('\\', '\\\\')),
                         cmdline,
                         flags=re.IGNORECASE)
    elif isinstance(cmdline, (list, tuple)):
        if cmdline[0].strip().lower() == 'python':
            cmdline[0] = sys.executable

    return cmdline


def normalize_env(env, relpath_prefix=None):
    u"""
        将环境变量中的路径标准化，python开头的内容会被替换为sys.executable

        凡是盘符（如C:/, C:\）或./或.\开头的字符串均会通过nfs.normpath进行标准化处理
        如果提供relpath_prefix，比如relpath_prefix='c:/'，
        环境变量中的相对路径均会改成绝对路径。
    """

    for name, value in env.items():
        value = value.strip()
        if _is_path(value):
            if relpath_prefix:
                env[name] = nfs.normpath(nfs.join(relpath_prefix, value))
            else:
                env[name] = nfs.normpath(value)
        else:
            env[name] = re.sub(
                '^ *python',
                '"{}"'.format(sys.executable.replace('\\', '\\\\')),
                value,
                flags=re.IGNORECASE)
    return env
