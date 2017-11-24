# coding: utf-8
from . import unittest

from sysutil import add_unit


class TestCommon(unittest.TestCase):

    def test_add_unit(self):

        @add_unit()
        def no_unit():
            return 1

        self.assertEqual(no_unit(), 1)

        @add_unit()
        def mix_unit(value=1, add_unit=False):
            return value

        self.assertEqual(mix_unit(), 1)
        self.assertEqual(mix_unit(add_unit=True), '1 B')
        self.assertEqual(mix_unit(value=1111, add_unit=True), '1 KB')

        @add_unit(u'个')
        def general_unit(add_unit=False):
            return 1

        self.assertEqual(general_unit(), 1)
        self.assertEqual(general_unit(True), u'1 个')

        @add_unit('MB', precision=3)
        def bytes_unit(value=1, add_unit=False):
            return value

        self.assertEqual(bytes_unit(add_unit=True), '0.000 MB')
        self.assertEqual(bytes_unit(value=1111, add_unit=True), '0.001 MB')
