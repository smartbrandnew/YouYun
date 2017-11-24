import imp
import os
import re
import sys

from util import windows_friendly_colon_split

WINDOWS_PATH = re.compile('[A-Z]:.*', re.IGNORECASE)


def imp_type_for_filename(filename):
    for type_data in imp.get_suffixes():
        extension = type_data[0]
        if filename.endswith(extension):
            return type_data
    return None


def load_qualified_module(full_module_name, path=None):
    remaining_pieces = full_module_name.split('.')
    done_pieces = []
    file_obj = None
    while remaining_pieces:
        try:
            done_pieces.append(remaining_pieces.pop(0))
            curr_module_name = '.'.join(done_pieces)
            (file_obj, filename, description) = imp.find_module(
                done_pieces[-1], path)
            package_module = imp.load_module(
                curr_module_name, file_obj, filename, description)
            path = getattr(package_module, '__path__', None) or [filename]
        finally:
            if file_obj:
                file_obj.close()
    return package_module


def module_name_for_filename(filename):
    all_segments = filename.split(os.sep)
    path_elements = all_segments[:-1]
    module_elements = [all_segments[-1].rsplit('.', 1)[0]]
    while True:
        init_path = os.path.join(*(path_elements + ['__init__.py']))
        if path_elements[0] is "":
            init_path = '/' + init_path
        if os.path.exists(init_path):
            module_elements.insert(0, path_elements.pop())
        else:
            break
    modulename = '.'.join(module_elements)
    basename = '/'.join(path_elements)
    return (basename, modulename)


def get_module(name):
    if name.startswith('/') or WINDOWS_PATH.match(name):
        basename, modulename = module_name_for_filename(name)
        path = [basename]
    else:
        modulename = name
        path = None
    if modulename in sys.modules:
        return sys.modules[modulename]
    return load_qualified_module(modulename, path)


def load(config_string, default_name=None):
    split = windows_friendly_colon_split(config_string)
    if len(split) > 1:
        module_name, object_name = ":".join(split[:-1]), split[-1]
    else:
        module_name, object_name = config_string, default_name
    module = get_module(module_name)
    if object_name:
        return getattr(module, object_name)
    else:
        return module
