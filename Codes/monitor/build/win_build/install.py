# coding: utf-8

import os
import shutil
import sys
import tempfile
from subprocess import Popen, PIPE, STDOUT

ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
TEMP_DIR = os.path.join(tempfile.gettempdir(), 'monitor_update_temp')
CONF_PATH = os.path.join(ROOT_DIR, 'conf')
CONFD_PATH = os.path.join(ROOT_DIR, 'conf.d')

IS_WIN = os.name == 'nt'


def copy_files(src, dst):
    if not os.path.exists(src):
        print '{} is not exits'.format(src)
    if os.path.exists(dst):
        os.remove(dst)
    shutil.copy(src, dst)


def copy_conf():
    if not os.path.exists(CONF_PATH):
        os.makedirs(CONF_PATH)

    conf_file = os.path.join(TEMP_DIR, 'datamonitor.conf')
    dst_file = os.path.join(CONF_PATH, 'datamonitor.conf')
    copy_files(conf_file, dst_file)


def copy_confd():
    if not os.path.exists(CONFD_PATH):
        os.makedirs(CONFD_PATH)
    temp_confd_dir = os.path.join(TEMP_DIR, 'conf.d')
    files = os.listdir(temp_confd_dir)
    for name in files:
        src_name = os.path.join(temp_confd_dir, name)
        dst_name = os.path.join(CONFD_PATH, name)
        copy_files(src_name, dst_name)


def do_cmd(cmd):
    if IS_WIN:
        from subprocess import CREATE_NEW_CONSOLE
        proc = Popen(cmd, stdout=PIPE, stderr=STDOUT, stdin=PIPE,
                     shell=True, creationflags=CREATE_NEW_CONSOLE)
    else:
        proc = Popen(cmd, stdout=PIPE, stderr=STDOUT, stdin=PIPE,
                     shell=True)

    result, _ = proc.communicate()
    return proc.wait(), result


def check_service():
    if IS_WIN:
        status, _ = do_cmd('sc query MonitorAgent')
        if status != 0:
            return
        cmd = "sc stop MonitorAgent && " \
              "sc config  MonitorAgent start= disabled"
    else:
        cmd = 'service datamonitor-agent  status ' \
              '>/dev/null 2>&1 && echo 0 || echo 1'
        status, output = do_cmd(cmd)

        if status == 0 and output.strip() == '1':
            return
        cmd = 'service datamonitor-agent stop && ' \
              'chkconfig datamonitor-agent off'

    rm_status, rm_output = do_cmd(cmd)
    if rm_status != 0:
        print rm_output
        sys.exit(1)


def init_config(api_key, report_url):
    config_template = 'datamonitor_template'
    with open(config_template, 'r') as conf_template:
        conf_template_content = conf_template.read().format(
            apikey=api_key,
            m_url=report_url)

    config_path = 'datamonitor.conf'
    with open(config_path, 'w') as master:
        master.write(conf_template_content)


def do_install():
    if os.path.exists(TEMP_DIR):
        copy_conf()
        copy_confd()
        shutil.rmtree(TEMP_DIR)
        status = 0
    else:
        if IS_WIN:
            api_key = os.environ.get('ANT_M_API_KEY')
            report_url = os.environ.get('ANT_M_URL')
            if 'monitor' in report_url:
                report_url = report_url + 'api/v2/gateway/dd-agent'
            else:
                report_url = report_url + '/monitor/api/v2/gateway/dd-agent'

            if not api_key or not report_url:
                print 'Please input ANT_M_API_KEY, ANT_M_URL!'
                sys.exit(1)
            init_config(api_key, report_url)
            status = 0
        else:
            install_file = os.path.join(ROOT_DIR, 'install.sh')
            if not os.path.exists(install_file):
                print 'The install.sh is not exist!'
                sys.exit(1)
            status, result = do_cmd('sh install.sh')
            print result
    check_service()
    sys.exit(status)


do_install()
