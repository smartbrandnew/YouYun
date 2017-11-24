# stdlib
import time
import unittest

# 3p
from mock import Mock


class MockProcess(Mock):
    """
    A mocked process.
    """
    def __init__(self, agentConfig=None, hostname=None, **options):
        super(MockProcess, self).__init__()
        self.config = agentConfig
        self.hostname = hostname
        self.options = options
        self._foo = options.get('foo')


class TestWin32Agent(unittest.TestCase):
    """
    Test for Windows `agent.py`.
    """
    def setUp(self):
        """
        Mock Windows related Python packages, so it can be tested on any environment.
        """
        import sys
        global ProcessWatchmonitor

        sys.modules['servicemanager'] = Mock()
        sys.modules['win32event'] = Mock()
        sys.modules['win32service'] = Mock()
        sys.modules['win32serviceutil'] = Mock()

        from win32.agent import ProcessWatchmonitor  # noqa

    def test_watchmonitor_max_restarts(self):
        """
        Watchmonitor does not exceed a maximum number of restarts per timeframe.
        """
        # Limit the restart timeframe for test purpose
        ProcessWatchmonitor._RESTART_TIMEFRAME = 1  # noqa

        # Create a Watchmonitor with a mock process
        process = MockProcess("MyConfig", "MyHostname", foo="bar")
        process_watchmonitor = ProcessWatchmonitor("MyProcess", process, max_restarts=2)  # noqa

        # Can restart 2 times
        for x in xrange(0, 2):
            self.assertTrue(process_watchmonitor._can_restart())
            process_watchmonitor.restart()

        # Not 3
        self.assertFalse(process_watchmonitor._can_restart())

        # Can restart after the timeframe is expired
        time.sleep(1)
        self.assertTrue(process_watchmonitor._can_restart())

    def test_watchmonitor_restart(self):
        """
        Watchmonitor restarts processes with the original arguments.
        """
        # Create a Watchmonitor with a mock process
        process = MockProcess("MyConfig", "MyHostname", foo="bar")
        process_watchmonitor = ProcessWatchmonitor("MyProcess", process)  # noqa

        # Restart the process
        process_watchmonitor.restart()
        new_process = process_watchmonitor._process

        # A new process was created with the same attributes
        self.assertNotEquals(process, new_process)
        self.assertEquals(new_process.config, "MyConfig")
        self.assertEquals(new_process.hostname, "MyHostname")
        self.assertEquals(new_process._foo, "bar")
