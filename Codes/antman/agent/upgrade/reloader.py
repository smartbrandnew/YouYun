# coding: utf-8
import sys
import os


def reload_module(modify_times):
    for mod in sys.modules.values():
        check_file(mod, modify_times)


def check_file(mod, modify_times):
    if not (mod and hasattr(mod, '__file__') and mod.__file__):
        return
    try:
        modified = os.stat(mod.__file__).st_mtime
    except (OSError, IOError):
        return

    if mod.__file__.endswith('.pyc') and os.path.exists(mod.__file__[:-1]):
        modified = max(os.stat(mod.__file__[:-1]).st_mtime, modified)

    if mod not in modify_times:
        modify_times[mod] = modified
        return

    if modify_times[mod] != modified:
        try:
            reload(mod)
            modify_times[mod] = modified
        except ImportError:
            pass
