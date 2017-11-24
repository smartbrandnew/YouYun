# coding: utf-8
import nfs
import yaml
import os
import shutil

from shutil import Error


class MessageError(Exception):

    def __init__(self, msg):
        super(MessageError, self).__init__(msg)


def get_agent_version():
    from constants import ROOT_DIR
    version = ''
    module_yml = nfs.join(ROOT_DIR, 'manifest.yaml')
    with open(module_yml) as temp:
        module_mes = yaml.load(temp.read())
        if 'version' in module_mes:
            version = module_mes['version']
    return version


def copy_file(src, dst, symlinks=False, errors=[]):
    try:
        if symlinks and os.path.islink(src):
            link_to = os.readlink(src)
            os.symlink(link_to, dst)
        elif os.path.isdir(src):
            copy_files(src, dst, symlinks)
        else:
            shutil.copy2(src, dst)
    except WindowsError:
        pass
    except IOError as err:
        if err.errno not in [5, 13]:
            errors.append((src, dst, str(err)))
    except Error as err:
        errors.extend(err.args[0])
    return errors


def copy_files(src, dst, symlinks=False):
    errors = []
    if os.path.isfile(src):
        copy_file(src, dst, symlinks, errors)
    else:
        names = os.listdir(src)
        if not os.path.exists(dst):
            os.makedirs(dst)

        for name in names:
            src_name = os.path.join(src, name)
            dst_name = os.path.join(dst, name)
            copy_file(src_name, dst_name, symlinks, errors)

        try:
            shutil.copystat(src, dst)
        except WindowsError:
            pass
        except OSError as why:
            if why.errno not in [5, 13]:
                errors.append((src, dst, str(why)))

    if errors:
        raise Error(errors)
