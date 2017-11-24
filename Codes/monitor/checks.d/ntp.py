import socket
import time
from datetime import datetime

import ntplib
import requests

from checks import AgentCheck
from config import get_config
from utils.ntp import get_ntp_args, set_user_ntp_settings

DEFAULT_OFFSET_THRESHOLD = 60


class NtpCheck(AgentCheck):
    DEFAULT_MIN_COLLECTION_INTERVAL = 900

    def check(self, instance):

        offset_threshold = instance.get('offset_threshold', DEFAULT_OFFSET_THRESHOLD)
        try:
            offset_threshold = int(offset_threshold)
        except (TypeError, ValueError):
            raise Exception(
                'Must specify an integer value for offset_threshold. Configured value is %s' % repr(offset_threshold))

        set_user_ntp_settings(dict(instance))

        req_args = get_ntp_args()

        self.log.debug("Using ntp host: {0}".format(req_args['host']))

        try:
            ntp_stats = ntplib.NTPClient().request(**req_args)
        except ntplib.NTPException:
            self.log.debug("Could not connect to NTP Server {0}".format(
                req_args['host']))
            ntp_offset = self.get_server_time()
            self.log.info("ntplib ntpexcption error:{}".format(ntp_offset))
            if ntp_offset:
                self.update_check(ntp_offset, time.time(), offset_threshold)

        except socket.gaierror as e:
            ntp_offset = self.get_server_time()

            self.log.info("socker error:{}".format(ntp_offset))
            if ntp_offset:
                self.update_check(ntp_offset, time.time(), offset_threshold)

        else:
            ntp_offset = ntp_stats.offset

            ntp_ts = ntp_stats.recv_time

            self.update_check(ntp_offset, ntp_ts, offset_threshold)

    def get_server_time(self):

        agent_config = get_config(parse_args=False)
        get_url = agent_config['m_url'] + '/correct/time?api_key=' + agent_config['api_key']
        start_time = time.time()
        result = requests.get(get_url, verify=False)
        delay_time = time.time() - start_time
        ntp_offset = ""
        if result.status_code == 200:
            time_str = result.content
            ntp_offset = datetime.fromtimestamp(time.time()) - datetime.fromtimestamp(
                int(time_str) / 10.0 ** (len(time_str) - 10))
            return ntp_offset.total_seconds() + delay_time
        return ntp_offset

    def update_check(self, ntp_offset, ntp_ts, offset_threshold):

        service_check_msg = None
        self.log.info("ntp offset:{}".format(ntp_offset))
        self.gauge('ntp.offset', ntp_offset, timestamp=ntp_ts)
        if abs(ntp_offset) > offset_threshold:
            status = AgentCheck.CRITICAL
            service_check_msg = "Offset {0} secs higher than offset threshold ({1} secs)".format(ntp_offset,
                                                                                                 offset_threshold)
        else:
            status = AgentCheck.OK
        self.service_check('ntp.in_sync', status, timestamp=ntp_ts, message=service_check_msg)
