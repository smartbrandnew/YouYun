import atexit
import cStringIO as StringIO
import glob
from collections import namedtuple
from functools import partial

try:
    import grp
except ImportError:
    grp = None
import logging
import os

try:
    import pwd
except ImportError:
    pwd = None
import re
import stat
import subprocess
import sys
import tarfile
import tempfile
from time import strftime

import requests

from checks.check_status import CollectorStatus, MonitorstatsdStatus, ForwarderStatus
from config import (
    check_yaml,
    get_confd_path,
    get_config,
    get_config_path,
    get_logging_config,
    get_url_endpoint,
)
from jmxfetch import JMXFetch
from util import get_hostname
from utils.jmx import jmx_command, JMXFiles
from utils.platform import Platform

log = logging.getLogger(__name__)


def configcheck():
    all_valid = True
    for conf_path in glob.glob(os.path.join(get_confd_path(), "*.yaml")):
        basename = os.path.basename(conf_path)
        try:
            check_yaml(conf_path)
        except Exception, e:
            all_valid = False
            print "%s contains errors:\n    %s" % (basename, e)
        else:
            print "%s is valid" % basename
    if all_valid:
        print "All yaml files passed. You can now run the Monitor Agent."
        return 0
    else:
        print("Fix the invalid yaml files above in order to start the Monitor Agent. "
              "A useful external tool for yaml parsing can be found at "
              "http://yaml-online-parser.appspot.com/")
        return 1


class Flare(object):
    DATAmonitor_SUPPORT_URL = '/support/flare'

    CredentialPattern = namedtuple('CredentialPattern', ['pattern', 'replacement', 'label'])
    CHECK_CREDENTIALS = [
        CredentialPattern(
            re.compile('( *(\w|_)*pass(word)?:).+'),
            r'\1 ********',
            'password'
        ),
        CredentialPattern(
            re.compile('(.*\ [A-Za-z0-9]+)\:\/\/([A-Za-z0-9]+)\:(.+)\@'),
            r'\1://\2:********@',
            'password in a uri'
        ),
    ]
    MAIN_CREDENTIALS = [
        CredentialPattern(
            re.compile('^api_key: *\w+(\w{5})$'),
            r'api_key: *************************\1',
            'api_key'
        ),
        CredentialPattern(
            re.compile('^(proxy_user|proxy_password): *.+'),
            r'\1: ********',
            'proxy credentials'
        ),
    ]
    COMMENT_REGEX = re.compile('^ *#.*')

    COMPRESSED_FILE = 'datamonitor-agent-{0}.tar.bz2'

    MAX_UPLOAD_SIZE = 10485000
    TIMEOUT = 60

    def __init__(self, cmdline=False, case_id=None):
        self._case_id = case_id
        self._cmdline = cmdline
        self._init_tarfile()
        self._init_permissions_file()
        self._save_logs_path()
        self._config = get_config()
        self._api_key = self._config.get('api_key')
        self._url = "{0}{1}".format(
            get_url_endpoint(self._config.get('m_url'), endpoint_type='flare'),
            self.DATAmonitor_SUPPORT_URL
        )
        self._hostname = get_hostname(self._config)
        self._prefix = "datamonitor-{0}".format(self._hostname)

    @staticmethod
    def check_user_rights():
        if Platform.is_linux() and not os.geteuid() == 0:
            log.warning("You are not root, some information won't be collected")
            choice = raw_input('Are you sure you want to continue [y/N]? ')
            if choice.strip().lower() not in ['yes', 'y']:
                print 'Aborting'
                sys.exit(1)
            else:
                log.warn('Your user has to have at least read access'
                         ' to the logs and conf files of the agent')

    def collect(self):
        if not self._api_key:
            raise Exception('No api_key found')
        log.info("Collecting logs and configuration files:")

        self._add_logs_tar()
        self._add_conf_tar()
        log.info("  * datamonitor-agent configcheck output")
        self._add_command_output_tar('configcheck.log', configcheck)
        log.info("  * datamonitor-agent status output")
        self._add_command_output_tar('status.log', self._supervisor_status)
        log.info("  * datamonitor-agent info output")
        self._add_command_output_tar('info.log', self._info_all)
        self._add_jmxinfo_tar()
        log.info("  * pip freeze")
        self._add_command_output_tar('freeze.log', self._pip_freeze,
                                     command_desc="pip freeze --no-cache-dir")

        log.info("  * log permissions on collected files")
        self._permissions_file.close()
        self._add_file_tar(self._permissions_file.name, 'permissions.log',
                           log_permissions=False)

        log.info("Saving all files to {0}".format(self.tar_path))
        self._tar.close()

    def set_proxy(self, options):
        proxy_settings = self._config.get('proxy_settings')
        if proxy_settings is None:
            return
        userpass = ''
        if proxy_settings.get('user'):
            userpass = "%s:%s@" % (proxy_settings.get('user'),
                                   proxy_settings.get('password'),)

        url = "http://%s%s:%s" % (userpass, proxy_settings.get('host'),
                                  proxy_settings.get('port'),)

        options['proxies'] = {
            "https": url
        }

    def set_ssl_validation(self, options):
        if self._config.get('skip_ssl_validation', False):
            options['verify'] = False
        elif Platform.is_windows():
            options['verify'] = os.path.realpath(os.path.join(
                os.path.dirname(os.path.realpath(__file__)),
                os.pardir, os.pardir,
                'datamonitor-cert.pem'
            ))

    def upload(self, email=None):
        self._check_size()

        if self._cmdline:
            self._ask_for_confirmation()

        if not email:
            email = self._ask_for_email()

        log.info("Uploading {0} to Datamonitor Support".format(self.tar_path))
        url = self._url
        if self._case_id:
            url = '{0}/{1}'.format(self._url, str(self._case_id))
        url = "{0}?api_key={1}".format(url, self._api_key)
        requests_options = {
            'data': {
                'case_id': self._case_id,
                'hostname': self._hostname,
                'email': email
            },
            'files': {'flare_file': open(self.tar_path, 'rb')},
            'timeout': self.TIMEOUT
        }

        self.set_proxy(requests_options)
        self.set_ssl_validation(requests_options)

        self._resp = requests.post(url, **requests_options)
        self._analyse_result()
        return self._case_id

    def _init_tarfile(self):
        self.tar_path = os.path.join(
            tempfile.gettempdir(),
            self.COMPRESSED_FILE.format(strftime("%Y-%m-%d-%H-%M-%S"))
        )

        if os.path.exists(self.tar_path):
            os.remove(self.tar_path)
        self._tar = tarfile.open(self.tar_path, 'w:bz2')

    def _init_permissions_file(self):
        self._permissions_file = tempfile.NamedTemporaryFile(mode='w', prefix='dd', delete=False)
        if Platform.is_unix():
            self._permissions_file_format = "{0:50} | {1:5} | {2:10} | {3:10}\n"
            header = self._permissions_file_format.format("File path", "mode", "owner", "group")
            self._permissions_file.write(header)
            self._permissions_file.write('-' * len(header) + "\n")
        else:
            self._permissions_file.write("Not implemented: file permissions are only logged on Unix platforms")

    def _save_logs_path(self):
        prefix = ''
        if Platform.is_windows():
            prefix = 'windows_'
        config = get_logging_config()
        self._collector_log = config.get('{0}collector_log_file'.format(prefix))
        self._forwarder_log = config.get('{0}forwarder_log_file'.format(prefix))
        self._monitorstatsd_log = config.get('{0}monitorstatsd_log_file'.format(prefix))
        self._jmxfetch_log = config.get('jmxfetch_log_file')
        self._gometro_log = config.get('go-metro_log_file')

    def _add_logs_tar(self):
        self._add_log_file_tar(self._collector_log)
        self._add_log_file_tar(self._forwarder_log)
        self._add_log_file_tar(self._monitorstatsd_log)
        self._add_log_file_tar(self._jmxfetch_log)
        self._add_log_file_tar(self._gometro_log)
        self._add_log_file_tar(
            "{0}/*supervisord.log".format(os.path.dirname(self._collector_log))
        )

    def _add_log_file_tar(self, file_path):
        for f in glob.glob('{0}*'.format(file_path)):
            if self._can_read(f):
                self._add_file_tar(
                    f,
                    os.path.join('log', os.path.basename(f))
                )

    def _add_conf_tar(self):
        conf_path = get_config_path()
        if self._can_read(conf_path, output=False):
            self._add_clean_conf(
                conf_path,
                'etc',
                self.MAIN_CREDENTIALS
            )

        if not Platform.is_windows():
            supervisor_path = os.path.join(
                os.path.dirname(get_config_path()),
                'supervisor.conf'
            )
            if self._can_read(supervisor_path, output=False):
                self._add_clean_conf(
                    supervisor_path,
                    'etc'
                )

        for file_path in glob.glob(os.path.join(get_confd_path(), '*.yaml')) + \
                glob.glob(os.path.join(get_confd_path(), '*.yaml.default')):
            if self._can_read(file_path, output=False):
                self._add_clean_conf(
                    file_path,
                    os.path.join('etc', 'confd'),
                    self.CHECK_CREDENTIALS
                )

    def _add_jmxinfo_tar(self):
        _, _, should_run_jmx = self._capture_output(self._should_run_jmx)
        if should_run_jmx:
            for file_name, file_path in [
                (JMXFiles._STATUS_FILE, JMXFiles.get_status_file_path()),
                (JMXFiles._PYTHON_STATUS_FILE, JMXFiles.get_python_status_file_path())
            ]:
                if self._can_read(file_path, warn=False):
                    self._add_file_tar(
                        file_path,
                        os.path.join('jmxinfo', file_name)
                    )

            for command in ['list_matching_attributes', 'list_everything']:
                log.info("  * datamonitor-agent jmx {0} output".format(command))
                self._add_command_output_tar(
                    os.path.join('jmxinfo', '{0}.log'.format(command)),
                    partial(self._jmx_command_call, command)
                )

            log.info("  * java -version output")
            _, _, java_bin_path = self._capture_output(
                lambda: JMXFetch.get_configuration(get_confd_path())[2] or 'java')
            self._add_command_output_tar(
                os.path.join('jmxinfo', 'java_version.log'),
                lambda: self._java_version(java_bin_path),
                command_desc="{0} -version".format(java_bin_path)
            )

    def _add_file_tar(self, file_path, target_path, log_permissions=True, original_file_path=None):
        target_full_path = os.path.join(self._prefix, target_path)
        if log_permissions and Platform.is_unix():
            stat_file_path = original_file_path or file_path
            file_stat = os.stat(stat_file_path)
            mode = oct(stat.S_IMODE(file_stat.st_mode))
            try:
                uname = pwd.getpwuid(file_stat.st_uid).pw_name
            except KeyError:
                uname = str(file_stat.st_uid)
            try:
                gname = grp.getgrgid(file_stat.st_gid).gr_name
            except KeyError:
                gname = str(file_stat.st_gid)
            self._permissions_file.write(self._permissions_file_format.format(stat_file_path, mode, uname, gname))

        self._tar.add(file_path, target_full_path)

    def _should_run_jmx(self):
        jmx_process = JMXFetch(get_confd_path(), self._config)
        jmx_process.configure(clean_status_file=False)
        return jmx_process.should_run()

    @classmethod
    def _can_read(cls, f, output=True, warn=True):
        if os.access(f, os.R_OK):
            if output:
                log.info("  * {0}".format(f))
            return True
        else:
            if warn:
                log.warn("  * not readable - {0}".format(f))
            return False

    def _add_clean_conf(self, file_path, target_dir, credential_patterns=None):
        basename = os.path.basename(file_path)

        temp_path, log_message = self._strip_credentials(file_path, credential_patterns)
        log.info('  * {0}{1}'.format(file_path, log_message))
        self._add_file_tar(
            temp_path,
            os.path.join(target_dir, basename),
            original_file_path=file_path
        )

    def _strip_credentials(self, file_path, credential_patterns=None):
        if not credential_patterns:
            credential_patterns = []
        credentials_found = set()
        fh, temp_path = tempfile.mkstemp(prefix='dd')
        atexit.register(os.remove, temp_path)
        with os.fdopen(fh, 'w') as temp_file:
            with open(file_path, 'r') as orig_file:
                for line in orig_file.readlines():
                    if not self.COMMENT_REGEX.match(line):
                        clean_line, credential_found = self._clean_credentials(line, credential_patterns)
                        temp_file.write(clean_line)
                        if credential_found:
                            credentials_found.add(credential_found)

        credentials_log = ''
        if len(credentials_found) > 1:
            credentials_log = ' - this file contains credentials ({0}) which' \
                              ' have been removed in the collected version' \
                .format(', '.join(credentials_found))
        elif len(credentials_found) == 1:
            credentials_log = ' - this file contains a credential ({0}) which' \
                              ' has been removed in the collected version' \
                .format(credentials_found.pop())

        return temp_path, credentials_log

    def _clean_credentials(self, line, credential_patterns):
        credential_found = None
        for credential_pattern in credential_patterns:
            if credential_pattern.pattern.match(line):
                line = re.sub(credential_pattern.pattern, credential_pattern.replacement, line)
                credential_found = credential_pattern.label
                break

        return line, credential_found

    def _add_command_output_tar(self, name, command, command_desc=None):
        out, err, _ = self._capture_output(command, print_exc_to_stderr=False)
        fh, temp_path = tempfile.mkstemp(prefix='dd')
        with os.fdopen(fh, 'w') as temp_file:
            if command_desc:
                temp_file.write(">>>> CMD <<<<\n")
                temp_file.write(command_desc)
                temp_file.write("\n")
            temp_file.write(">>>> STDOUT <<<<\n")
            temp_file.write(out.getvalue())
            out.close()
            temp_file.write(">>>> STDERR <<<<\n")
            temp_file.write(err.getvalue())
            err.close()
        self._add_file_tar(temp_path, name, log_permissions=False)
        os.remove(temp_path)

    def _capture_output(self, command, print_exc_to_stderr=True):
        backup_out, backup_err = sys.stdout, sys.stderr
        out, err = StringIO.StringIO(), StringIO.StringIO()
        backup_handlers = logging.root.handlers[:]
        logging.root.handlers = [logging.StreamHandler(out)]
        sys.stdout, sys.stderr = out, err
        return_value = None
        try:
            return_value = command()
        except Exception:
            pass
        finally:
            sys.stdout, sys.stderr = backup_out, backup_err
            logging.root.handlers = backup_handlers

        return out, err, return_value

    def _supervisor_status(self):
        if Platform.is_windows():
            print 'Windows - status not implemented'
        else:
            agent_exec = self._get_path_agent_exec()
            print '{0} status'.format(agent_exec)
            self._print_output_command([agent_exec, 'status'])
            supervisor_exec = self._get_path_supervisor_exec()
            print '{0} status'.format(supervisor_exec)
            self._print_output_command([supervisor_exec,
                                        '-c', self._get_path_supervisor_conf(),
                                        'status'])

    def _get_path_agent_exec(self):
        if Platform.is_mac():
            agent_exec = '/opt/datamonitor-agent/bin/datamonitor-agent'
        else:
            agent_exec = '/etc/init.d/datamonitor-agent'

        if not os.path.isfile(agent_exec):
            agent_exec = os.path.join(
                os.path.dirname(os.path.realpath(__file__)),
                '../../bin/agent'
            )
        return agent_exec

    def _get_path_supervisor_exec(self):
        supervisor_exec = '/opt/datamonitor-agent/bin/supervisorctl'
        if not os.path.isfile(supervisor_exec):
            supervisor_exec = os.path.join(
                os.path.dirname(os.path.realpath(__file__)),
                '../../venv/bin/supervisorctl'
            )
        return supervisor_exec

    def _get_path_supervisor_conf(self):
        if Platform.is_mac():
            supervisor_conf = '/opt/datamonitor-agent/etc/supervisor.conf'
        else:
            supervisor_conf = '/opt/datadog-agent/conf/supervisor.conf'
        if not os.path.isfile(supervisor_conf):
            supervisor_conf = os.path.join(
                os.path.dirname(os.path.realpath(__file__)),
                '../../agent/supervisor.conf'
            )
        return supervisor_conf

    def _print_output_command(self, command):
        try:
            status = subprocess.check_output(command, stderr=subprocess.STDOUT)
        except subprocess.CalledProcessError, e:
            status = 'Not able to get output, exit number {0}, exit output:\n' \
                     '{1}'.format(str(e.returncode), e.output)
        print status

    def _info_all(self):
        CollectorStatus.print_latest_status(verbose=True)
        MonitorstatsdStatus.print_latest_status(verbose=True)
        ForwarderStatus.print_latest_status(verbose=True)

    def _jmx_command_call(self, command):
        try:
            jmx_command([command], self._config, redirect_std_streams=True)
        except Exception as e:
            print "Unable to call jmx command {0}: {1}".format(command, e)

    def _java_version(self, java_bin_path):
        try:
            self._print_output_command([java_bin_path, '-version'])
        except OSError:
            print 'Unable to execute java bin with command: {0}'.format(java_bin_path)

    def _pip_freeze(self):
        try:
            import pip
            pip.main(['freeze', '--no-cache-dir'])
        except ImportError:
            print 'Unable to import pip'

    def _check_size(self):
        if os.path.getsize(self.tar_path) > self.MAX_UPLOAD_SIZE:
            log.info("{0} won't be uploaded, its size is too important.\n"
                     "You can send it directly to support by email.")
            sys.exit(1)

    def _ask_for_confirmation(self):
        print '{0} is going to be uploaded to Datamonitor.'.format(self.tar_path)
        choice = raw_input('Do you want to continue [Y/n]? ')
        if choice.strip().lower() not in ['yes', 'y', '']:
            print 'Aborting (you can still use {0})'.format(self.tar_path)
            sys.exit(1)

    def _ask_for_email(self):

        return raw_input('Please enter your email: ').lower()

    def _analyse_result(self):

        if self._resp.status_code == 400:
            raise Exception('Your request is incorrect: {0}'.format(self._resp.json()['error']))

        self._resp.raise_for_status()
        try:
            self._case_id = self._resp.json()['case_id']

        except ValueError:
            raise Exception('An unknown error has occured - '
                            'Please contact support by email')

        log.info("Your logs were successfully uploaded. For future reference,"
                 " your internal case id is {0}".format(self._case_id))
