# coding: utf-8
import os
import tarfile
from glob import glob

import nfs
import semver
import yaml
import yaml.scanner
import pf

from constants import (PKG_YAML_NAME, PKG_DIR, PKG_CACHE_DIR,
                       PKG_UNCOMPRESS_DIR, REQUIRED_PKG_FIELDS, PLATFORM_LIST,
                       FULLNAME_SEP)
from framework.actions.errors import MessageError, NotExistsError, NotMatchError

version_diff_msg = "The version of module {!r} is different from it's {} info"
version_invalid_msg = 'The version {!r} of modules {!r} is invalid'
be_string_msg = 'The {!r} value type of module {!r} should be string'
be_dict_msg = 'The {!r} value  of module {!r} type should be dict'
be_string_list_empty_msg = 'The {!r} value type of module {!r}  ' \
                           'should be string or list or dict or empty'
be_platform_err_msg = 'The {!r} value of module {!r} should be ' \
                      'system@version or platform!'
fullname_format_msg = 'module {!r} should be the format of name@version or ' \
                      'name@version@otherinfo'


class PkgHelper(object):

    @classmethod
    def uncompress(cls, fullname, parent_dir, overwrite=True):
        parent_dir = parent_dir if parent_dir else PKG_DIR
        src = os.path.join(PKG_CACHE_DIR, fullname)

        if not os.path.exists(src):
            raise NotExistsError('The cache of '.format(fullname))

        # ensure make target_dir
        for dir_name in [parent_dir, PKG_UNCOMPRESS_DIR]:
            if not nfs.exists(dir_name):
                nfs.makedirs(dir_name, 0750)

        # clear PKG_UNCOMPRESS_DIR
        for name in os.listdir(PKG_UNCOMPRESS_DIR):
            nfs.remove(os.path.join(PKG_UNCOMPRESS_DIR, name))

        # extract file
        with tarfile.open(src) as tar:
            tar.extractall(PKG_UNCOMPRESS_DIR)

        extract_path = os.path.join(PKG_UNCOMPRESS_DIR,
                                    os.listdir(PKG_UNCOMPRESS_DIR)[0])

        pkg_yml_path = os.path.join(extract_path, PKG_YAML_NAME)
        pkg_info = cls.get_info(pkg_yml_path, fullname)

        dst = os.path.join(parent_dir, pkg_info['name'])
        if not overwrite and os.path.exists(dst):
            return dst
        nfs.rename(extract_path, dst, overwrite)
        return dst, pkg_info

    @classmethod
    def get_info(cls, pkg_yml_path, name):
        """
        If tgz is True, uncompress pkgs to PKG_SERVER_CACHE_DIR,
        and allow uncompress names like "base_1.0.0_win", "base_1.0.0_linux".

        Else, uncompress pkgs to PKG_DIR,
        only allow uncompress names like "base_1.0.0".
        """
        # Check if package.yml exists
        if not os.path.exists(pkg_yml_path):
            raise NotExistsError('The {} of {}'.format(PKG_YAML_NAME, name))

        try:
            with open(pkg_yml_path) as pkg_info:
                info = yaml.load(pkg_info.read())
            cls.check_info(info['name'], info)
            return info
        except yaml.scanner.ScannerError:
            raise MessageError('The {} of {} is invalid yaml format'
                               .format(PKG_YAML_NAME, name))

    @classmethod
    def check_info(cls, module_name, info):
        # Check required package fields
        if set(REQUIRED_PKG_FIELDS) - set(info.keys()):
            raise MessageError('{} requires {}'
                               .format(info, REQUIRED_PKG_FIELDS))

        # Check fields type
        for field in REQUIRED_PKG_FIELDS:
            if not isinstance(info[field], basestring):
                raise MessageError(be_string_msg.format(field, module_name))

        # Check name and version
        # we should remove DISABLE_POSTFIX from version if have
        version = info['version'].strip()
        if not semver.validate(version):
            raise MessageError(version_invalid_msg.format(version, module_name))

        # dict fields
        for field in ('platforms', 'scripts', 'dependencies'):
            if field not in info:
                continue

            if not isinstance(info[field], (type(None), dict)):
                raise MessageError(be_dict_msg.format(field, module_name))

            if info[field] is None:
                info[field] = {}

            if field == 'scripts':
                for value in info[field].values():
                    if not isinstance(value,
                                      (type(None), basestring, list, dict)):
                        raise MessageError(
                            be_string_list_empty_msg.format(field, module_name))
            elif field == 'platforms':
                for key in info[field].keys():
                    if FULLNAME_SEP not in key and key not in PLATFORM_LIST:
                        raise MessageError(
                            be_platform_err_msg.format(field, module_name))
            else:
                for value in info[field].values():
                    if not isinstance(value, list):
                        raise MessageError(
                            be_string_msg.format(field, module_name))

        # 'main' field
        if 'run' in info and not isinstance(info['run'],
                                            (basestring, list, dict)):
            raise MessageError(be_string_list_empty_msg.format('run'))

        # 'priority' field
        if 'priority' in info and not isinstance(info['priority'], int):
            raise MessageError(be_string_list_empty_msg.format('priority'))

    @classmethod
    def check_module(cls, module_name, info):
        if not module_name:
            return
        if module_name.strip() != info['name'].strip():
            raise MessageError("The name of module {!r} is different from it's "
                               "{} info !!!".format(module_name, PKG_YAML_NAME))
        cls.check_platforms(info['platforms'])

    @classmethod
    def check_platforms(cls, platforms):
        if not platforms:
            return

        cur_platform = pf.get_platform()
        cur_version = semver.Version(cur_platform.version)
        cur_kernel = semver.Version(cur_platform.kernel)

        for platform_name, cpus in platforms.iteritems():
            if isinstance(cpus, (str, int)):
                cpus = [int(cpus)] if cpus.strip() else None
            cpus = cpus or [32, 64]

            # Check cpu
            if cur_platform.cpu not in cpus:
                continue

            # Check system
            system, version = pf.split_platform(platform_name)
            if system in (cur_platform.system, cur_platform.dist):
                if not version:
                    return
                elif (system == 'Windows' and semver.Spec(version).match(
                        cur_kernel)) or \
                        semver.Spec(version).match(cur_version):
                    return
        raise NotMatchError("Current {!r} doesn't match platform require: {!r}"
                            .format(cur_platform, platforms))

    @staticmethod
    def delete(module_name):
        glob_path = os.path.join(PKG_DIR, module_name)
        for pkg_path in glob(glob_path):
            nfs.remove(pkg_path)
