"""
Test utilities.
"""

from __future__ import print_function

import os
import sys

import sysutil
from sysutil import POSIX
from sysutil import WINDOWS
from sysutil._compat import PY3

if sys.version_info < (2, 7):
    import unittest2 as unittest  # requires "pip install unittest2"
else:
    import unittest

try:
    from unittest import mock  # py3
except ImportError:
    import mock  # NOQA - requires "pip install mock"

if sys.version_info >= (3, 4):
    import enum
else:
    enum = None

if PY3:
    import importlib
    # python <=3.3
    if not hasattr(importlib, 'reload'):
        import imp as importlib
else:
    import imp as importlib
