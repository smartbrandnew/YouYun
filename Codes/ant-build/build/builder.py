# coding: utf-8
"""
Usage:
  build --build-project=<project_name> --branch=<branch> [--system=<build_system>] [--with-module=<module_names>]  [--noencrypt]

Options:
  -h, --help     Show help.
  --build-project=<project_name>   Build project name
  --set-version=<version>          Build project version
  --branch=<branch>                Build project branch.
  --system=<build_system>          Build project system
  --with-module=<plugin_names>     BUild dispatcher with plugins
  --noencrypt                      Build no encrypt package. Default encrypt
"""

import os
import re
import sys
import yaml
import time
from compileall import compile_dir
from subprocess import check_call,Popen,PIPE
import requests
import nfs as fs
import platform
from os.path import join
from build import logger
from build.constants import (DIST_ROOT, ROOT_DIR,AGENT_NAME, PROJECT_ROOT, PKG_NAME,
                             DISPATCHER_NAME,MANAGER_NAME,MANAGERWEB_NAME,MODULE_PROJECT,
                             AGENT_PROJECT,PLUGIN_PROJECT,REPO_NAME,REPO_URL,PYTHON_URL)

from build.errors import NotExistsError
from build.project import GitProject, HttpProject


PROJECT_MAP = {
    'agent': GitProject(
        'agent',
        'git@git.uyunsoft.cn:antman/agent.git'),
    'dispatcher': GitProject(
        'dispatcher',
        'git@git.uyunsoft.cn:antman/dispatcher.git'
    ),
    'manager': GitProject(
        'manager',
        'git@git.uyunsoft.cn:antman/manager.git'
    ),
    'manager-web': GitProject(
        'manager-web',
        'git@git.uyunsoft.cn:antman/manager-web.git'
    ),
    'monitor':GitProject(
        'monitor',
        'git@git.uyunsoft.cn:antman/monitor.git'
    ),
    'discovery':GitProject(
        'discovery',
        'git@git.uyunsoft.cn:hawk/discovery.git',
    ),
    'python':GitProject(
        'python',
        'https://git.uyunsoft.cn/xuzg/platform-ant-python.git'
    ),
    'node':GitProject(
        'node',
        'git@git.uyunsoft.cn:xuzg/platform-ant-node.git'
    ),
    'nginx':GitProject(
        'nginx',
        'https://git.uyunsoft.cn/kelc/platform-ant-nginx.git'
    )

}

class Builder(object): 
    @staticmethod
    def clear_old_dir(dst):
        if fs.exists(dst):
            logger.info('Clear {!r}'.format(dst))
            fs.remove(dst)

    @staticmethod
    def get_version(yaml_file):
        with open(yaml_file,'r') as f:
            content = yaml.load(f)
        return content['name'],content['version']

    @staticmethod
    def encrypt_py_project(target_root):
        for dirname in fs.listdir(target_root):
            if dirname in ('conf', 'embedded'):
                continue
            path = fs.join(target_root, dirname)
            if not fs.isdir(path):
                continue
            compile_dir(path)
            fs.remove(path, filter_files='*.py')

    @classmethod
    def handle_cli(cls,args):
        build_project = None
        for opt,project in PROJECT_MAP.iteritems():
            if args.get('--build-project') == opt:
                build_project = project
                break
     
        if args.get('--system'):
            build_system = args.get('--system')
        else:
            build_system='all'

        if args.get('--with-module'):
            with_module = args.get('--with-module')
        else:
            with_module = ''
        conf_file = join(PROJECT_ROOT,build_project.name,'build.yaml')

        builder = ProjectBuilder(
            build_project,
            args['--branch'],
            conf_file,
            build_system,
            with_module,
            )
        builder.build()

class NewBuilder(object):
    def __init__(self):
        self.build_tasks = {}

    def parse_build_conf(self, build_conf_path):
        with open(build_conf_path) as build_conf:
            content = yaml.load(build_conf)
            for build_pfs, build_task_conf in content.iteritems():
                build_pfs = build_pfs.lower()
                for build_pf in build_pfs.split(','):
                    self.build_tasks[build_pf] = build_task_conf
        return self.build_tasks

class BuildTask(object):
    def __init__(self,build_system,source_dir,project_name,project_version,module_names):
        self.source_dir = source_dir
        self.project_name = project_name
        self.project_version = project_version
        self.target_dir = None
        self.New_Build = NewBuilder()
        self.build_system = build_system
        self.module_names = module_names

    def step_build(self,build_tasks):
        if self.build_system == 'all':
            self.build_system = build_tasks.keys()
        else:
            self.build_system = [self.build_system]
        for task in self.build_system:
            self.target_dir = join(DIST_ROOT,build_tasks[task]['name'])
            for step_commad in build_tasks[task]['steps']:
                step_build = Step(
                    self.source_dir,
                    self.target_dir,
                    self.project_name,
                    self.project_version,
                    self.module_names
                    )
                func, args = step_build.step_commad(step_commad)
                logger.info(args)
                func(*args)

    def task_build(self,conf_file):
        build_tasks = self.New_Build.parse_build_conf(conf_file)
        self.step_build(build_tasks)

class ProjectBuilder(Builder):
    def __init__(self,project,branch,conf_file,build_system,module_names):
        self.project = project
        self.target_root = join(PROJECT_ROOT,self.project.name)
        self.build_branch = branch
        self.build_system = build_system
        self.conf_file = conf_file
        self.module_names = module_names
        self.project_name = None
        self.project_version = None
        
    def build(self):
        #Check project
        self.clear_old_dir(self.target_root)
        self.project.check(branch=self.build_branch)
        if os.path.exists(join(PROJECT_ROOT,'{}/{}'.format(self.project.name,'manifest.yaml'))):
            yaml_file = join(PROJECT_ROOT,'{}/{}'.format(self.project.name,'manifest.yaml'))
        else:
            yaml_file = join(PROJECT_ROOT,'{}/{}'.format(self.project.name,'module.yaml'))
        self.project_name,self.project_version = self.get_version(yaml_file)
        self.clear_old_dir(join(DIST_ROOT,self.project_name))

        #build with yaml file
        for system in self.build_system.lower().split(','):
            builder = BuildTask(
                system,
                self.target_root,
                self.project_name,
                self.project_version,
                self.module_names
                )
            builder.task_build(self.conf_file)

class Step(object):
    def __init__(
        self,
        source_dir,
        target_dir,
        project_name,
        project_version,
        module_names
        ):
        self.func = None
        self.args = None
        self.source_dir = source_dir
        self.target_dir = target_dir
        self.project_name = project_name
        self.project_version = project_version
        self.module_names = module_names
        
    @classmethod
    def download(cls, url, target_path):
        r = requests.get(url)
        if (r.status_code != 200):
            logger.info('Project build faild')
            logger.info('{} do not exists'.format(url))
            sys.exit()
        with open(target_path,'wb') as code:
            code.write(r.content)

    @staticmethod
    def compress(project_name,name,postfix):
        logger.info('Compress from {!r} to {!r}'
                    .format(project_name, DIST_ROOT))
        compress_path = fs.compress(project_name, DIST_ROOT,name = name,
                                    postfix=postfix)
    
    @staticmethod
    def get_output(cmd):
        p = Popen(
            cmd,
            shell=True,
            stdout=PIPE,
            stderr=PIPE,
            stdin=PIPE)
        p.wait()
        out = p.stdout.readlines()
        logger.info(out)
        result = p.communicate()
        if result[1]:
            logger.info('Warning:{}'.format(result[1]))
        return out
            
    @staticmethod
    def copy(source,target,exclude_files, exclude_dirs):
        fs.copy(
            source,
            target,
            exclude_files=exclude_files, 
            exclude_dirs=exclude_dirs,
            symlinks=True
            )

    @staticmethod
    def support_plat(plats,manifest_file):
        plats = eval(plats)
        with open(manifest_file) as f:
            content = yaml.load(f)
        if content['platforms'] == None:
            content['platforms'] = {}
        content['platforms'].update(plats)
        with open(manifest_file,'w') as f:
            yaml.dump(content,f)

    @staticmethod
    def pip_install(python_package,requeriment_txt,exclude_pkgs):
        with open(requeriment_txt) as f:
            to_install = f.readlines()
            for pkg in to_install:
                if '-r' in pkg:
                    print pkg
                    to_install.remove(pkg)
                    txt = pkg.split()[-1]
                    with open(txt) as file:
                        new_install = file.readlines()
                else:
                    new_install = []
            to_install = [x.strip() for x in to_install+new_install]
            if exclude_pkgs:
                for pkg in exclude_pkgs.strip().split(','):
                    if pkg in to_install:
                        to_install.remove(pkg)
        if 'win' in python_package:
            python_exe = ''
            pip_exe = os.path.join(python_package,'Scripts\pip.exe')
            python_url = PYTHON_URL.format(python_package,'zip')
        else:
            python_exe = os.path.join(python_package,'bin/python')
            pip_exe = os.path.join(python_package,'bin/pip')
            python_url = PYTHON_URL.format(python_package,'tgz')

        target_dir = os.path.join(ROOT_DIR,os.path.basename(python_url))
        if not os.path.exists(python_package):
            Step.download(python_url,target_dir)
            fs.uncompress(target_dir,ROOT_DIR)
            fs.remove(target_dir)
        already_install = Step.get_output('{} {} freeze'.format(python_exe,pip_exe))
        already_install = [x.strip() for x in already_install]
        if set(to_install)&set(already_install) != set(to_install):
            for requ in list(set(to_install)-set(already_install)):
                Step.get_output('{} {} install -U {}'.format(python_exe,pip_exe,requ))
    
    @staticmethod
    def with_module(module_names):
        if module_names == '':
            logger.info('Need not modules')
        else:
            for module_name in module_names.split(','):
                module_string=''
                module_version=[]
                module_url = REPO_URL[module_name]
                module_names = [f for f in os.listdir(DIST_ROOT) if os.path.isfile(os.path.join(DIST_ROOT,f))]
                module_names = [f for f in module_names if module_name in f]
                if len(module_names) == 0:
                    content = requests.get(module_url).text
                    module_names=re.findall(r'>({}.*)</a>'.format(module_name),content)
                if len(module_names) == 0:
                    logger.info('Can not find the {} package ,dispatcher build failed'.format(module_name))
                    sys.exit()
                for name in module_names:
                    module_string += name
                module_version = re.findall('[-@](\d+\.\d+\.\d+)',module_string)
                module_version = max(module_version)
            
                for diff_version_module in REPO_NAME[module_name]:
                    source_name = diff_version_module.format(module_version)
                    link_name = diff_version_module.format('latest')
                    source = join(module_url,source_name)
                    target = join(DIST_ROOT,'platform-ant-dispatcher/repo/',source_name)
                    if  os.path.exists(join(DIST_ROOT,source_name)):
                        fs.copy(join(DIST_ROOT,source_name),target)
                    else:
                        Step.download(source,target)

                    fs.chdir(join(DIST_ROOT,'platform-ant-dispatcher/repo/'))
                    check_call('ln -s {} {}'.format(source_name,link_name),shell = True)
                    fs.chdir('../../../')

    @staticmethod
    def add_date(time_format,manifest_file):
        if not time_format:
            time_format='%Y-%m-%d %H:%S'
        try:
            format_time = time.strftime('{}'.format(time_format),time.localtime(time.time()))
        except:
            logger.info('time format Irregular,user the default time format')
            time_format='%Y-%m-%d %H:%S'
            format_time = time.strftime('{}'.format(time_format),time.localtime(time.time()))
        
        format_time = {"build_date":format_time}
        with open(manifest_file) as f:
            content = yaml.load(f)
        content.update(format_time)
        with open(manifest_file,'w') as f:
            yaml.dump(content,f)


    def step_commad(self, step_commad):
        step_info = step_commad.split()
        step_info = [x.format(
            s = self.source_dir,
            t = self.target_dir,
            project_name = self.project_name,
            project_version = self.project_version,
            module_names = self.module_names
            ) for x in step_info]

        command = step_info[0]
        if command == 'copy':
            source = step_info[1]
            target = step_info[2]
            if 'exclude_files' in step_commad:
                exclude_files = eval(step_commad.split('exclude_files=')[1].split()[0])
            else:
                exclude_files = None
            if 'exclude_dirs' in step_commad:
                exclude_dirs = eval(step_commad.split('exclude_dirs=')[1].split()[0])
            else:
                exclude_dirs = None
                
            self.func = self.copy
            self.args = [source, target,exclude_files,exclude_dirs]

        elif command == 'compress':
            source = step_info[1]
            project_name = step_info[2]
            postfix = step_info[3]
            self.func = self.compress
            self.args = [source,project_name,postfix]

        elif command == 'download':
            source = step_info[1]
            target = step_info[2]
            self.func = self.download
            self.args = [source, target]

        elif command == 'makedirs':
            target = step_info[1]
            self.func = fs.makedirs
            self.args = [target]

        elif command == 'cd':
            target = step_info[1]
            self.func = fs.chdir
            self.args = [target]

        elif command == 'uncompress':
            source = step_info[1]
            target = step_info[2]
            self.func = fs.uncompress
            self.args = [source,target]

        elif command == 'rename':
            source = step_info[1]
            target = step_info[2]
            self.func = fs.rename
            self.args = [source,target]

        elif command == 'remove':
            source = step_info[1]
            self.func = fs.remove
            self.args = [source]

        elif command == 'support_plat':
            support_palt = step_info[1]
            file_name = step_info[2]
            self.func = self.support_plat
            self.args = [support_palt,file_name]

        elif command == 'with_module':
            modules = step_info[1]
            self.func = self.with_module
            self.args = [modules]

        elif command == 'pip_install':
            python_package = step_info[1]
            req_txt = step_info[2]
            if 'exclude_pkgs' in step_commad:
                exclude_pkgs = step_commad.split('exclude_pkgs=')[1].split()[0]
            else:
                exclude_pkgs = None
            self.func = self.pip_install
            self.args = [python_package,req_txt,exclude_pkgs]

        elif command == 'add_date':
            if len(step_info)>=3:
                time_format = step_info[1]
                manifest_file = step_info[2]
            else:
                time_format = None
                manifest_file = step_info[1]
            self.func = self.add_date
            self.args = [time_format,manifest_file]
                
             
        else:
            self.func = check_call
            self.args = [[x for x in step_info]]

        return self.func,self.args

if __name__ == '__main__':
    from docopt import docopt

    cli_args = docopt(__doc__)
    Builder.handle_cli(cli_args)
