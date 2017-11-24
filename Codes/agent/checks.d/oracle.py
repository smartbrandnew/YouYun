# coding: utf-8
import os
import commands
from contextlib import contextmanager

from checks import AgentCheck


SQL_MAP = {
    'oracle.sga.librarycache': ('single',
                                'select bytes/1024/1024 "size" from v$sgastat'
                                ' where name =\'buffer_cache\' '
                                'or name =\'db_block_buffers\' and rownum = 1'),
    'oracle.sga.redolog': ('single',
                           'select bytes/1024/1024 "size" from'
                           ' v$sgastat where name = \'log_buffer\''),
    'oracle.sga.sharepool': ('single',
                             'select bytes/1024/1024 "size" '
                             'from v$sgastat where name = \'log_buffer\''),
}

# element ('target name', 'description', 'query sql', 'single/multi')
METRICS = [
    ('oracle.sga.librarycache',
     'sage library cache',
     'select bytes/1024/1024 "size" from v$sgastat where name =\'buffer_cache\''
     ' or name =\'db_block_buffers\' and rownum = 1',
     'single'),
    ('oracle.sga.redolog',
     'sga redolog',
     'select bytes/1024/1024 "size" from v$sgastat where name = \'log_buffer\'',
     'single'),
    ('oracle.sga.sharepool',
     'sga redolog',
     'select sum(bytes)/1024/1024 "size" from v$sgastat '
     'where pool= \'shared pool\'',
     'single'),
    ('oracle.sga.dictioncache',
     'sga diction cache',
     'select bytes/1024/1024 "size" from v$sgastat '
     'where name =\'dictionary cache\' or name = \'row cache\'',
     'single'),
    ('oracle.redo.loac',
     'redo unlocation',
     'select name,value from v$sysstat '
     'where name=\'redo buffer allocation retries\'',
     'single'),
]


class SupportError(Exception):
    pass


class Oracle(AgentCheck):

    SOURCE_TYPE_NAME = 'oracle'
    SERVICE_CHECK_NAME = 'oracle.can_connect'
    DEFAULT_COMMAND_TIMEOUT = 30

    METRICS = [
        ('oracle.sga.librarycache', 'sga library cache'),
        ('oracle.sga.redolog', 'sga redolog'),
        ('oracle.sga.sharepool', 'sga share pool'),
    ]

    def __init__(self, name, init_config, agentConfig, instances=None):
        super(Oracle, self).__init__(name, init_config, agentConfig, instances)
        # set_oracle_environ()

    def check(self, instance):
        username, password, instance_names = self._get_config(instance)
        if not username or not password or not instance_names:
            raise Exception('Oracle username, password '
                            'and instance name are needed.')

        for instance_name in instance_names:
            with self._connect(username, password, instance_name) as db:
                try:
                    self._report_metrics(db, instance_name)
                except Exception as e:
                    self.log.exception("Gather Oracle info error !")
                    raise e

    def _get_config(self, instance):
        username = instance.get('db_user', '')
        password = instance.get('db_password', '')
        instance_name = instance.get('instance', '')
        if not instance_name:
            self.log.error("The Instance_name is necessary parameters")
        # instance_names = get_oracle_instance()
        else:
            self.log.info('Get Oracle Instances: {}'.format(instance_name))
        return username, password, instance_name

    @contextmanager
    def _connect(self, username, password, instance_name):
        try:
            import cx_Oracle
        except ImportError:
            raise Exception('Import Oracle database connect lib failed,'
                            'please confirm the Oracle was Installed.')
        db = None
        try:
            db = cx_Oracle.connect(username, password, instance_name)
            self.log.debug("Connected to Oracle")
            yield db
        except:
            raise
        finally:
            if db:
                db.close()

    def _report_metrics(self, db, instance_name):
        for metric in self.METRICS:
            if SQL_MAP[metric[0]][0] == 'single':
                result = self._get_single_data(db,
                                               SQL_MAP[metric[0]][1],
                                               metric[1])
                self.gauge(metric[0], result, tags=["name:{}".format(instance_name)])

    def _get_single_data(self, db, sql, desc):
        data = None
        cursor = db.cursor()
        try:
            cursor.execute(sql)
            data = cursor.fetchone()[0]
        except:
            self.warning('Error while fetch {} of variable Oracle'.format(desc))
        finally:
            cursor.close()
        return data


def get_oracle_user():
    cmd = 'ps -ef|grep ora|grep -v grid|head -1|awk \'{print $1}\''
    status, user = commands.getstatusoutput(cmd)
    if status != 0:
        raise Exception('Get Oracle install user Error')
    return user.strip()


def get_oracle_home():
    user = get_oracle_user()
    cmd = 'su - {user} -c \'echo $ORACLE_HOME;echo $LD_LIBRARY_PATH\''\
          .format(user=user)
    status, out = commands.getstatusoutput(cmd)
    if status != 0:
        raise Exception('Get ORACLE_HOME and LD_LIBRARY_PATH Error')
    oracle_home, ld_lib_path = out.split('\n')
    return oracle_home, ld_lib_path


def get_oracle_instance():
    oracle_home = get_oracle_home()
    tns_path = os.path.join(oracle_home, 'network', 'admin', 'tnsname.ora')
    if not os.path.exists(tns_path):
        raise OSError('Please confirm whether the tnsname.ora file exists')
    instance_names = []
    with open(tns_path) as f:
        for line in f:
            line = line.strip()
            if (not line or line.startswith('#') or line.startswith('(')
                    or line.startswith(')')):
                continue
            instance_names.append(line.split()[0])
    return instance_names


def set_oracle_environ():
    oracle_home, ld_lib_path = get_oracle_home()
    os.environ['ORACLE_HOME'] = oracle_home
    old_llp = os.getenv('LD_LIBRARY_PATH')
    if old_llp:
        os.environ['LD_LIBRARY_PATH'] = ('{}:{}'.format(ld_lib_path, old_llp))
    else:
        os.environ['LD_LIBRARY_PATH'] = ld_lib_path
