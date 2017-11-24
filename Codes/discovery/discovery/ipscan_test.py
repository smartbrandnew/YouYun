import json
import os
import sys
from unittest import TestCase

from mock import Mock, patch

from discovery import ipscan
from discovery.ipscan import IpScan


class IPScanTest(TestCase):
    def setUp(self):
        super(IPScanTest, self).setUp()
        ipscan.TaskLogger = Mock()
        ipscan.EngineType = Mock()
        os.environ['ANT_AGENT_ID'] = 'test_agent_id'
        sys.stdin = Mock()
        sys.stdin.read = lambda: json.dumps({
            'test_agent_id': {
                'ip_ranges': ['10.1.61.251'],
                'ports': [80]
            }
        })

    @patch('discovery.ipscan.NmapProcess')
    def test_ipscan(self, NmapProcess):
        NmapProcess.return_value = nmap = Mock()
        nmap.is_running.side_effect = [
            True,
            False
        ]
        nmap.progress = 0
        nmap.etc = ''
        nmap.tasks = ''
        nmap.rc = 0
        nmap.stdout = ''

        IpScan.loop_time = 0.1
        ipscan_executor = IpScan()
        ipscan_executor.run()
        ipscan.TaskLogger().log.assert_called_once_with(
            '{"progress": 0.0, "log": ""}')
        ipscan.TaskLogger().result.assert_called_once_with('')
