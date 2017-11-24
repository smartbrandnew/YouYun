#!/usr/bin/env python
"""sysutil is a cross-platform library for retrieving information on
running processes and system utilization (CPU, memory, disks, network)
in Python.
"""

import os
import sys
try:
    from setuptools import setup, Extension
except ImportError:
    from distutils.core import setup, Extension

HERE = os.path.realpath(os.path.dirname(__file__))

# ...so we can import _common.py
sys.path.insert(0, os.path.join(HERE, "sysutil"))

# requires packages
install_requires = ['psutil==5.3.0.3']
if not sys.platform.startswith("aix"):
    install_requires.append('py-cpuinfo')


def main():
    setup(
        name='sysutil',
        version='0.4.6',
        description=__doc__.replace('\n', '').strip() if __doc__ else '',
        keywords=[
            'sys',
            'util',
        ],
        author='uyun',
        author_email='ant@uyunsoft.cn',
        platforms='Platform Independent',
        packages=['sysutil', 'tests'],
        install_requires=install_requires,
        tests_require=None,
        zip_safe=False,  # http://stackoverflow.com/questions/19548957
        # see: python setup.py register --list-classifiers
        classifiers=[
            'Development Status :: 5 - Production/Stable',
            'Environment :: Console',
            'Environment :: Win32 (MS Windows)',
            'Intended Audience :: Developers',
            'Intended Audience :: Information Technology',
            'Intended Audience :: System Administrators',
            'License :: OSI Approved :: BSD License',
            'Operating System :: MacOS :: MacOS X',
            'Operating System :: Microsoft :: Windows :: Windows NT/2000',
            'Operating System :: Microsoft',
            'Operating System :: OS Independent',
            'Operating System :: POSIX :: BSD :: FreeBSD',
            'Operating System :: POSIX :: BSD :: NetBSD',
            'Operating System :: POSIX :: BSD :: OpenBSD',
            'Operating System :: POSIX :: BSD',
            'Operating System :: POSIX :: Linux',
            'Operating System :: POSIX :: SunOS/Solaris',
            'Operating System :: POSIX',
            'Programming Language :: C',
            'Programming Language :: Python :: 2',
            'Programming Language :: Python :: 2.6',
            'Programming Language :: Python :: 2.7',
            'Programming Language :: Python :: Implementation :: CPython',
            'Programming Language :: Python :: Implementation :: PyPy',
            'Programming Language :: Python',
            'Topic :: Software Development :: Libraries :: Python Modules',
            'Topic :: Software Development :: Libraries',
            'Topic :: System :: Benchmark',
            'Topic :: System :: Hardware',
            'Topic :: System :: Monitoring',
            'Topic :: System :: Networking :: Monitoring',
            'Topic :: System :: Networking',
            'Topic :: System :: Operating System',
            'Topic :: System :: Systems Administration',
            'Topic :: Utilities',
        ],)


if __name__ == '__main__':
    main()
