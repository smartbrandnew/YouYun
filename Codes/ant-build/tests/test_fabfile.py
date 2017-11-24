# -*- coding:utf-8 -*-

import json
import time
import commands
import sys
from redis.sentinel import Sentinel
from fabric.api import *
from fabric.colors import *

from util import *
INSTALL_BASEDIR = '/opt/ant-server/'
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
AGENT_LSIT = [{'host': '192.168.100.101', 'user': 'root', 'passwd': '123456'},
              {'host': '192.168.100.102', 'user': 'root', 'passwd': '123456'}]

HOST_LIST = {'test_test': ['192.168.100.100', '192.168.100.101',
                           '192.168.100.102'],
             'redis': '192.168.100.10',
             'mongo': '192.168.100.11'}
DOCKER_LIST = []

COMMON = {'ANT_CO_HOST': '192.168.100.10',
          'ANT_CO_PORT': 6379,
          'redis.password': '',
          'mongodb.hosts': '10.1.240.168',
          'mongodb.username': '',
          'mongodb.password': '',
          'uyun.automation.baseurl': ''}


def get_redis_master(redis_sentinel_nodes):
    nodes_list = redis_sentinel_nodes.split(',')
    sentinel = Sentinel([tuple(nodes_list[0].split(':'))], socket_timeout=0.1)
    master = sentinel.discover_master('mymaster')
    if not master:
        sys.exit(1)
    return list(master)


def get_common_config(disconf_host):
    common_url = API.format(disconf_host)
    cmd = 'curl -s "{}" |grep -E "^redis|^mongodb|^uyun.automation.baseurl"' \
          '|sort -n | uniq'.format(common_url)
    status, output = commands.getstatusoutput(cmd)
    if status or not output:
        print 'Get disconf error'
        sys.exit(1)
    for line in output.split('\n'):
        line_list = line.split('=')
        if line_list[0] == 'redis.sentinel.nodes':
            result = get_redis_master(line_list[1])
            COMMON.update({'ANT_CO_HOST': result[0]})
            COMMON.update({'ANT_CO_PORT': result[1]})
        else:
            COMMON.update({line_list[0]: line_list[1]})


def deal_route_config(config_file, dest_config_file, dispatcher_ip):
    local_dict = {'redis': {'host': COMMON['ANT_CO_HOST'],
                            'port': COMMON['ANT_CO_PORT'],
                            'password': COMMON['redis.password'],
                            "reconnectInterval": 1000},
                  'dispatcher': {'address': dispatcher_ip}
                  }
    with open(config_file, 'w') as original:
        original.write(json.dumps(local_dict))
    put(config_file, dest_config_file)


def pre(package_name):
    """prepare tar file"""
    tar_path = os.path.join(INSTALL_BASEDIR, package_name)
    run('rm -rf {} {}'.format(tar_path, INSTALL_BASEDIR))
    run('mkdir -p {}'.format(INSTALL_BASEDIR))
    put("./packages/" + package_name, tar_path)
    run('tar zxf {} -C {}'.format(tar_path, INSTALL_BASEDIR))
    run('rm -rf {}'.format(tar_path))


def deploy_ant_server(host, soft, remoteips, install_type):
    # pre('ant-server.tgz')
    run('rm -rf {}'.format(INSTALL_BASEDIR))
    put('./packages/ant-server', '/opt')
    route_conf = os.path.join(BASE_DIR, 'ant-router-prod.conf.json')
    dst_conf = os.path.join(INSTALL_BASEDIR, 'ant-router-node',
                            'ant-router-prod.conf.json')

    # get_common_config(soft['params']['disconf_host'])
    with cd(INSTALL_BASEDIR):
        deal_route_config(route_conf, dst_conf, host['ip'])
        run("rm -rf /usr/local/bin/node")
        node_path = os.path.join(INSTALL_BASEDIR, 'node/bin/node')
        run('chmod 755 -R .')
        run("ln -s {} /usr/local/bin/node".format(node_path))
        node_modules_path = os.path.join(INSTALL_BASEDIR,
                                         'node/lib/node_modules')
        run("ln -s {} ant-router-node/node_modules"
            "".format(node_modules_path))
        run("./node/lib/node_modules/pm2/bin/pm2 kill")
        run("./node/lib/node_modules/pm2/bin/pm2 kill")
        with cd(os.path.join(INSTALL_BASEDIR, 'ant-router-node')):
            run("../node/lib/node_modules/pm2/bin/pm2 start process.json")
    deploy_ant_depends(soft)


def deploy_ant_depends(soft):
    with cd(INSTALL_BASEDIR):
        run("sed -i 's/localhost/{}/g;s/6379/{}/g;s/password/{}/g;"
            "s/ant_mongodb_host/{}/g;s/ant_mongodb_user/{}/g;"
            "s/ant_mongodb_token/{}/g' process.json"
            "".format(COMMON['ANT_CO_HOST'],
                      COMMON['ANT_CO_PORT'],
                      COMMON['redis.password'],
                      COMMON['mongodb.hosts'],
                      COMMON['mongodb.username'],
                      COMMON['mongodb.password']))
        run("./node/lib/node_modules/pm2/bin/pm2 start process.json")


def deploy_nginx(host, soft, remoteips, install_type):
    install_path = os.path.join(INSTALL_BASEDIR, 'ant-nginx')
    tar_path = os.path.join(INSTALL_BASEDIR, 'ant-nginx-install.tar.gz')
    server_file_dir = os.path.join(INSTALL_BASEDIR, 'repo')
    put('./packages/ant-nginx-install.tar.gz', tar_path)
    run('tar zxf {} -C {} '.format(tar_path, INSTALL_BASEDIR))
    put('./packages/nginx.conf', install_path)
    with cd(install_path):
        run("sed -i 's#server_file_dir#{}#g' nginx.conf".format(
            server_file_dir))
        run("sed -i 's#server_file_dir#{}#g;s#install_base#{}#g' install.sh"
            "".format(server_file_dir, INSTALL_BASEDIR))
        run('sh install.sh')
    run('rm -rf {}'.format(install_path))
    run('rm -rf {}/ant-nginx-install.tar.gz'.format(INSTALL_BASEDIR))


def install_agent():
    python_exec = 'ANT_CO_HOST={} ANT_CO_PORT={} ANT_CO_PASSWORD={} ' \
                  ''.format(COMMON['ANT_CO_HOST'], COMMON['ANT_CO_PORT'],
                            COMMON['redis.password'])
    with cd(INSTALL_BASEDIR):
        for agent in AGENT_LSIT:
            cmd = '{} bin/ant install --host {} --user {} --passwd {} ' \
                  ' --upstream-directly'.format(python_exec, agent['host'],
                                                agent['user'], agent['passwd'])
            out = run(cmd, timeout=60)
            if out.return_code != 0:
                print '对主机{},执行远程安装agent失败!'.format(agent['host'])
        agents = [agent['host'] for agent in AGENT_LSIT]
        time.sleep(5)
        cmd = '{} bin/upm {} list '.format(python_exec, ','.join(agents))
        out = run(cmd, timeout=10)
        if out.return_code != 0:
            print '对主机远程执行upm失败!'


def do_cmd(cmd):
    status, output = commands.getstatusoutput(cmd)
    if status != 0:
        print 'exec command :{} error!'.format(cmd)
        sys.exit(1)


def run_mongo(cmd):
    import subprocess
    p = subprocess.Popen(cmd, stderr=subprocess.STDOUT, stdout=subprocess.PIPE,
                         shell=True)


def start_docker():
    for key, value in HOST_LIST.items():
        if isinstance(value, list):
            for ip in value:
                cmd = 'docker run --net=new_net --ip {} -d {} ' \
                      '/usr/sbin/sshd -D'.format(ip, key)
                do_cmd(cmd)
        elif key == 'redis':
            cmd = 'docker run --net=new_net --ip {} -d {} '.format(value, key)
            do_cmd(cmd)
        else:
            cmd = 'docker run --net=new_net --ip {} -t mongo'.format(value)
            run_mongo(cmd)


def stop_docker():
    cmd = 'docker stop $(docker ps -aq) && docker rm $(docker ps -aq)'
    do_cmd(cmd)


def install():
    start_docker()
    config = Config('config.yaml')
    config.serial_deploy('ant-server&&nginx', deploy_ant_server)
    # config.serial_deploy('ant-server&&nginx', deploy_nginx)
    install_agent()
    stop_docker()
