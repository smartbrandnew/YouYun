# -*- coding: UTF-8 -*-


class Bdcom(object):

    cpu = {
        "default-cpu-5s": "1.3.6.1.4.1.3320.2.1.56",
        "old-cpu-5s": "1.3.6.1.4.1.3320.9.109.1.1.1.1.3",
        "new-cpu-5m": "1.3.6.1.4.1.3320.9.109.3.2.4.4",
        "old-cpu-5m": "1.3.6.1.4.1.3320.9.109.1.1.1.1.5",
        "new-cpu-1m": "1.3.6.1.4.1.3320.9.109.3.2.4.3",
        "old-cpu-1m": "1.3.6.1.4.1.3320.9.109.1.1.1.1.4",
        "default-cpu-1m": "1.3.6.1.4.1.3320.2.1.57",
        "default-cpu-5m": "1.3.6.1.4.1.3320.2.1.58",
        "new-cpu-5s": "1.3.6.1.4.1.3320.9.109.3.2.4.2",
        "nat-cpu": "1.3.6.1.4.1.3320.9.100.1.5 / 100"
    }

    mem = {
        "nat-mem": "1.3.6.1.4.1.3320.9.100.1.6 / 100",
        "bdcom-mem": "1.3.6.1.4.1.3320.9.48.1",
        "default-mem": "(1.3.6.1.4.1.3320.9.48.1.1.1.5 / (1.3.6.1.4.1.3320.9.48.1.1.1.5 + 1.3.6.1.4.1.3320.9.48.1.1.1.6)) * 100",
        "default-mem-5m": "1.3.6.1.4.1.3320.9.48.1.2.1.2",
        "default-mem-1m": "1.3.6.1.4.1.3320.9.48.1.2.1.1",
        "default-mem-10m": "1.3.6.1.4.1.3320.9.48.1.2.1.3"
    }

    temp = {
    }

    def __str__(self):
        return 'Bdcom'


class HP(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.11.2.14.11.5.1.9.6.1"
    }

    mem = {
        "default-mem": "(1.3.6.1.4.1.11.2.14.11.5.1.1.2.2.1.1.7 / 1.3.6.1.4.1.11.2.14.11.5.1.1.2.2.1.1.5) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'HP'


class Linksys(object):

    cpu = {
        "default-cpu-1m": "1.3.6.1.4.1.89.1.8",
        "default-cpu-5m": "1.3.6.1.4.1.89.1.9",
        "default-cpu-1s": "1.3.6.1.4.1.89.1.7"
    }

    mem = {
    }

    temp = {
    }

    def __str__(self):
        return 'Linksys'


class Alliedtelesis(object):

    cpu = {
        "default-cpu-10s": "1.3.6.1.4.1.207.8.4.4.3.3.4",
        "default-cpu-5m": "1.3.6.1.4.1.207.8.4.4.3.3.7",
        "default-cpu-1m": "1.3.6.1.4.1.207.8.4.4.3.3.3"
    }

    mem = {
        "default-mem": "(100 - 1.3.6.1.4.1.207.8.4.4.3.7.1)"
    }

    temp = {
    }

    def __str__(self):
        return 'Alliedtelesis'


class Neusoft(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.8596.1.1.1.2.1 * 100"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.8596.1.1.1.2.2 * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Neusoft'


class Angelltech(object):

    cpu = {
        "default-cpu": "(1.3.6.1.4.1.27150.1.1.1.1 / 10)"
    }

    mem = {
        "default-mem": "(1.3.6.1.4.1.27150.1.1.1.2 / 10)"
    }

    temp = {
    }

    def __str__(self):
        return 'Angelltech'


class Nortel(object):

    cpu = {
        "default-cpu-1m": "1.3.6.1.4.1.45.1.6.3.8.1.1.5",
        "default-cpu-10m": "1.3.6.1.4.1.45.1.6.3.8.1.1.6",
        "default-cpu-10s": "1.3.6.1.4.1.45.1.6.3.8.1.1.11"
    }

    mem = {
        "default-mem": "(100 - 1.3.6.1.4.1.45.1.6.3.8.1.1.9)"
    }

    temp = {
    }

    def __str__(self):
        return 'Nortel'


class Red_Giant(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.4881.1.1.10.2.36.1.1.3",
        "default-cpu-5s": "1.3.6.1.4.1.4881.1.1.10.2.36.1.1.1",
        "default-cpu-1m": "1.3.6.1.4.1.4881.1.1.10.2.36.1.1.2"
    }

    mem = {
        "default-mem-1": "1.3.6.1.4.1.4881.1.1.10.2.35.1.1.1.3",
        "default-mem-0": "1.3.6.1.4.1.4881.1.1.10.2.35.1.1.1.3.0"
    }

    temp = {
    }

    def __str__(self):
        return 'Red-Giant'


class InternetSecurityOneLtd(object):

    cpu = {
        "default-cpu": "(1.3.6.1.4.1.8885.1.8.11.1 * 100)"
    }

    mem = {
        "default-mem": "(1.3.6.1.4.1.8885.1.8.11.2.1.2 / 1.3.6.1.4.1.8885.1.8.11.2.1.1 ) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'InternetSecurityOneLtd'


class Enterasys(object):

    cpu = {
        "default-cpu-5m": "1.3.6.1.4.1.5624.1.2.49.1.1.1.1.4",
        "default-cpu-1m": "1.3.6.1.4.1.5624.1.2.49.1.1.1.1.3",
        "default-cpu-5s": "1.3.6.1.4.1.5624.1.2.49.1.1.1.1.2"
    }

    mem = {
        "default-mem": " (1.3.6.1.4.1.5624.1.2.49.1.3.1.1.5 / 1.3.6.1.4.1.5624.1.2.49.1.3.1.1.4) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Enterasys'


class Cisco(object):

    cpu = {
        "cpu-5s": "1.3.6.1.4.1.9.9.109.1.1.1.1.6",
        "old-cpu-5m": "1.3.6.1.4.1.9.2.1.58",
        "old-cpu-1m": "1.3.6.1.4.1.9.2.1.57",
        "cpu-5m": "1.3.6.1.4.1.9.9.109.1.1.1.1.8",
        "default-cpu-1m": "1.3.6.1.4.1.9.9.109.1.1.1.1.4",
        "current-cpu": "1.3.6.1.4.1.14179.1.1.5.1",
        "new-cpu": "1.3.6.1.4.1.9.9.305.1.1.1",
        "old-cpu-5s": "1.3.6.1.4.1.9.2.1.56",
        "cpu-1m": "1.3.6.1.4.1.9.9.109.1.1.1.1.7",
        "default-cpu-5m": "1.3.6.1.4.1.9.9.109.1.1.1.1.5",
        "default-cpu-5s": "1.3.6.1.4.1.9.9.109.1.1.1.1.3"
    }

    mem = {
        "new-mem": "1.3.6.1.4.1.9.9.305.1.1.2",
        "default-mem": "(100 - (1.3.6.1.4.1.9.9.109.1.1.1.1.12 / 1.3.6.1.4.1.9.9.109.1.1.1.1.13) * 100)",
        "IXR-mem": "(1.3.6.1.4.1.9.9.221.1.1.1.1.18 / (1.3.6.1.4.1.9.9.221.1.1.1.1.18 + 1.3.6.1.4.1.9.9.221.1.1.1.1.20)) * 100",
        "default-mem": "(1.3.6.1.4.1.9.9.48.1.1.1.5 / (1.3.6.1.4.1.9.9.48.1.1.1.5 + 1.3.6.1.4.1.9.9.48.1.1.1.6)) * 100",
        "current-mem": "((1.3.6.1.4.1.14179.1.1.5.2 - 1.3.6.1.4.1.14179.1.1.5.3) / 1.3.6.1.4.1.14179.1.1.5.2) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Cisco'


class Micom(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.335.1.4.1.3.1.1"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.335.1.4.1.3.1.3"
    }

    temp = {
    }

    def __str__(self):
        return 'Micom'


class Array(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.7564.30.1"
    }

    mem = {
    }

    temp = {
    }

    def __str__(self):
        return 'Array'


class Unkonwn(object):

    cpu = {
        "default-cpu-5m": "1.3.6.1.4.1.2011.6.3.4.1.4",
        "default-cpu-5s": "1.3.6.1.4.1.2011.6.3.4.1.2",
        "default-cpu-1m": "1.3.6.1.4.1.2011.6.3.4.1.3"
    }

    mem = {
        "default-mem": "((1.3.6.1.4.1.2011.6.3.5.1.1.2 - 1.3.6.1.4.1.2011.6.3.5.1.1.3) / 1.3.6.1.4.1.2011.6.3.5.1.1.2) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Unkonwn'


class Maipu(object):

    cpu = {
        "default-cpu": "100 - 1.3.6.1.4.1.5651.6.7.2.100.1.8.1.5",
        "cpu": "1.3.6.1.4.1.5651.3.20.1.1.3.5.1.10",
        "new-cpu": "1.3.6.1.4.1.5651.3.20.1.1.1.9",
        "default-cpu2": "100 - 1.3.6.1.4.1.5651.6.7.2.100.1.11.4"
    }

    mem = {
        "default-mem2": "(1.3.6.1.4.1.5651.6.7.2.100.1.11.7 / 1.3.6.1.4.1.5651.6.7.2.100.1.11.6) * 100",
        "default-mem": "(1.3.6.1.4.1.5651.6.7.2.100.1.8.1.8 / 1.3.6.1.4.1.5651.6.7.2.100.1.8.1.7) * 100",
        "new-ram": "1.3.6.1.4.1.5651.3.20.1.1.3.5.1.10",
        "ram": "1.3.6.1.4.1.5651.3.20.1.1.1.9"
    }

    temp = {
    }

    def __str__(self):
        return 'Maipu'


class Nokia(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.94.1.21.1.7.1"
    }

    mem = {
    }

    temp = {
    }

    def __str__(self):
        return 'Nokia'


class Netscreen(object):

    cpu = {
        "default-cpu-1m": "1.3.6.1.4.1.3224.16.1.2",
        "default-cpu-15m": "1.3.6.1.4.1.3224.16.1.4",
        "default-cpu": "1.3.6.1.4.1.3224.16.1.3"
    }

    mem = {
        "default-mem": "(1.3.6.1.4.1.3224.16.2.1 / (1.3.6.1.4.1.3224.16.2.1 + 1.3.6.1.4.1.3224.16.2.2)) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Netscreen'


class Netgear(object):

    cpu = {
        "default-cpu-1m": "1.3.6.1.4.1.4526.19.1.32.1.4.1.8.11",
        "default-cpu-5m": "1.3.6.1.4.1.4526.19.1.32.1.4.1.9.11",
        "default-cpu-5s": "1.3.6.1.4.1.4526.19.1.32.1.4.1.5.11"
    }

    mem = {
        "new-mem": "((1.3.6.1.4.1.4526.19.1.32.2.2.1.2.11 - 1.3.6.1.4.1.4526.19.1.32.2.2.1.3.11) / 1.3.6.1.4.1.4526.19.1.32.2.2.1.2.11) * 100",
        "default-mem": "((1.3.6.1.4.1.4526.10.1.1.5.2 - 1.3.6.1.4.1.4526.10.1.1.5.1) / 1.3.6.1.4.1.4526.10.1.1.5.2) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Netgear'


class Moxa(object):

    cpu = {
        "default-cpu-30s": "1.3.6.1.4.1.8691.7.26.1.54",
        "default-cpu-5s": "1.3.6.1.4.1.8691.7.26.1.53",
        "default-cpu-5m": "1.3.6.1.4.1.8691.7.26.1.55"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.8691.7.26.1.59"
    }

    temp = {
    }

    def __str__(self):
        return 'Moxa'


class ZTE(object):

    cpu = {
        "600-cpu-5m": "1.3.6.1.4.1.3902.3.600.2.1.1.7",
        "6003-cpu-5m": "1.3.6.1.4.1.3902.3.6003.2.1.1.9",
        "low-cpu-2m": "1.3.6.1.4.1.3902.15.2.2.1.3",
        "default-cpu-2m": "1.3.6.1.4.1.3902.15.2.6.1.3",
        "default-cpu-5s": "1.3.6.1.4.1.3902.15.2.6.1.1",
        "new-cpu-30s": "1.3.6.1.4.1.3902.3.3.1.1.7",
        "low-cpu-30s": "1.3.6.1.4.1.3902.15.2.2.1.2",
        "default-cpu-30s": "1.3.6.1.4.1.3902.15.2.6.1.2",
        "new-cpu-2m": "1.3.6.1.4.1.3902.3.3.1.1.5",
        "low-cpu-5s": "1.3.6.1.4.1.3902.15.2.2.1.1",
        "new-cpu-5s": "1.3.6.1.4.1.3902.3.3.1.1.6"
    }

    mem = {
        "low-mem": "1.3.6.1.4.1.3902.15.2.2.1.5",
        "new-mem": "1.3.6.1.4.1.3902.3.3.1.1.4",
        "6003-mem": "1.3.6.1.4.1.3902.3.6003.2.1.1.6",
        "600-mem": "1.3.6.1.4.1.3902.3.600.2.1.1.4",
        "default-mem": "1.3.6.1.4.1.3902.15.2.6.1.5"
    }

    temp = {
    }

    def __str__(self):
        return 'ZTE'


class Lenovo(object):

    cpu = {
        "old-cpu": "(100 - 1.3.6.1.4.1.9833.1.1.11.11)",
        "default-cpu": "(100 - 1.3.6.1.4.1.9833.1.4.1.1.4)"
    }

    mem = {
        "default-mem": "((1.3.6.1.4.1.9833.1.4.2.5 - 1.3.6.1.4.1.9833.1.4.2.6) / 1.3.6.1.4.1.9833.1.4.2.5) * 100",
        "old-mem": "((1.3.6.1.4.1.9833.1.1.4.5 - 1.3.6.1.4.1.9833.1.1.4.6) / 1.3.6.1.4.1.9833.1.1.4.5) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Lenovo'


class Enterasys(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.52.2501.1.270.2.1.1.2"
    }

    mem = {
        "default-mem": " (1.3.6.1.4.1.52.2501.1.270.4.1.1.6 / 1.3.6.1.4.1.52.2501.1.270.4.1.1.4) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Enterasys'


class Redware(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.89.35.1.53",
        "default-cpu-1m": "1.3.6.1.4.1.89.35.1.113",
        "default-cpu-5s": "1.3.6.1.4.1.89.35.1.112"
    }

    mem = {
    }

    temp = {
    }

    def __str__(self):
        return 'Redware'


class Alcatel(object):

    cpu = {
        "rad-cpu-1m": "1.3.6.1.4.1.89.1.8",
        "default-cpu-1m": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.14",
        "rad-cpu-5m": "1.3.6.1.4.1.89.1.9",
        "default-cpu-1hmax": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.16",
        "rad-cpu-1s": "1.3.6.1.4.1.89.1.7",
        "default-cpu-1s": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.13",
        "default-cpu-1h": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.15"
    }

    mem = {
        "default-mem-1m": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.10",
        "default-mem-1h": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.11",
        "default-mem-1s": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.9",
        "default-mem-1hmax": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.12"
    }

    temp = {
    }

    def __str__(self):
        return 'Alcatel'


class Juniper(object):

    cpu = {
        "default-cpu-1m": "1.3.6.1.4.1.2636.3.1.13.1.8",
        "default-cpu-ive": "1.3.6.1.4.1.12532.10"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.2636.3.1.16",
        "default-mem-heap": "1.3.6.1.4.1.2636.3.1.13.1.12",
        "default-mem-buffer": "1.3.6.1.4.1.2636.3.1.13.1.11",
        "default-mem-ive": "1.3.6.1.4.1.12532.11"
    }

    temp = {
    }

    def __str__(self):
        return 'Juniper'


class Bluecoat(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.3417.2.4.1.1.1.4"
    }

    mem = {
    }

    temp = {
    }

    def __str__(self):
        return 'Bluecoat'


class H3C(object):

    cpu = {
        "ar-cpu-5s": "1.3.6.1.4.1.2011.2.2.4.12",
        "new-cpu": "1.3.6.1.4.1.2011.10.2.6.1.1.1.1.6",
        "default-cpu-5s": "1.3.6.1.4.1.2011.6.1.1.1.2",
        "f5-cpu": "(100 - 1.3.6.1.4.1.2021.11.11)",
        "h3c-cpu": "1.3.6.1.4.1.25506.2.6.1.1.1.1.6",
        "default-cpu": "1.3.6.1.4.1.2011.6.1.1.1.4",
        "ar-cpu-5m": "1.3.6.1.4.1.2011.2.2.4.13",
        "default-cpu-1m": "1.3.6.1.4.1.2011.6.1.1.1.3"
    }

    mem = {
        "ar-mem": "1.3.6.1.4.1.2011.2.2.5.1 / (1.3.6.1.4.1.2011.2.2.5.1 + 1.3.6.1.4.1.2011.2.2.5.2) * 100",
        "new-mem": "1.3.6.1.4.1.2011.10.2.6.1.1.1.1.8",
        "f5-mem": "((1.3.6.1.4.1.2021.4.5 - 1.3.6.1.4.1.2021.4.6) / 1.3.6.1.4.1.2021.4.5) * 100",
        "default-mem": "((1.3.6.1.4.1.2011.6.1.2.1.1.2 - 1.3.6.1.4.1.2011.6.1.2.1.1.3) / 1.3.6.1.4.1.2011.6.1.2.1.1.2) * 100",
        "h3c-mem": "1.3.6.1.4.1.25506.2.6.1.1.1.1.8"
    }

    temp = {
    }

    def __str__(self):
        return 'H3C'


class Nsfocus(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.19849.2.3.1",
        "s5xxx-cpu": "1.3.6.1.4.1.2011.5.25.31.1.1.1.1.5"
    }

    mem = {
        "s5xxx-mem": "1.3.6.1.4.1.2011.5.25.31.1.1.1.1.7",
        "default-mem": "1.3.6.1.4.1.19849.2.3.2"
    }

    temp = {
    }

    def __str__(self):
        return 'Nsfocus'


class Fortinet(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.12356.1.8",
        "current-cpu": "1.3.6.1.4.1.12356.101.4.1.3"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.12356.1.9",
        "current-mem": "1.3.6.1.4.1.12356.101.4.1.4"
    }

    temp = {
    }

    def __str__(self):
        return 'Fortinet'


class DigitalChina(object):

    cpu = {
        "new-cpu-5s": "(100 - 1.3.6.1.4.1.6339.99.1.8)",
        "new-cpu-30s": "(100 - 1.3.6.1.4.1.6339.99.1.9)",
        "cpu-5m": "(100 - 1.3.6.1.4.1.6339.100.1.8.11.4)",
        "old-cpu-1m": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.14",
        "default-cpu-5s": "(100 - 1.3.6.1.4.1.6339.100.1.8.1.3)",
        "default-cpu": "1.3.6.1.4.1.6339.100.1.8.1.3",
        "cpu-30s": "(100 - 1.3.6.1.4.1.6339.100.1.11.3)",
        "firewall-cpu": "1.3.6.1.4.1.6339.2.2.3",
        "new-cpu-5m": "(100 - 1.3.6.1.4.1.6339.99.1.10)",
        "default-cpu-5m": "(100 - 1.3.6.1.4.1.6339.100.1.8.1.5)",
        "router-cpu": "(10000 - 1.3.6.1.4.1.6339.9.100.1.5) / 100",
        "cpu-5s": "(100 - 1.3.6.1.4.1.6339.100.1.11.2)",
        "utm-cpu": "(1.3.6.1.4.1.8885.1.8.11.1 * 100)",
        "default-cpu-30s": "(100 - 1.3.6.1.4.1.6339.100.1.8.1.4)"
    }

    mem = {
        "firewall-mem": "(1.3.6.1.4.1.6339.2.2.5 / 1.3.6.1.4.1.6339.2.2.4 ) * 100",
        "mem": "(1.3.6.1.4.1.6339.100.1.11.7 / 1.3.6.1.4.1.6339.100.1.11.6 ) * 100",
        "utm-mem": "(1.3.6.1.4.1.8885.1.8.11.2.1.2 / 1.3.6.1.4.1.8885.1.8.11.2.1.1 ) * 100",
        "router-mem": "1.3.6.1.4.1.6339.9.100.1.6 / 100",
        "old-mem-1m": "1.3.6.1.4.1.6486.800.1.2.1.16.1.1.1.10",
        "default-mem": "(1.3.6.1.4.1.6339.100.1.8.1.8 / 1.3.6.1.4.1.6339.100.1.8.1.7 ) * 100",
        "new-mem": "(1.3.6.1.4.1.6339.100.1.8.1.5 / 1.3.6.1.4.1.6339.100.1.8.1.4 ) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'DigitalChina'


class Foundry(object):

    cpu = {
        "default-cpu-5s": "1.3.6.1.4.1.1991.1.1.2.1.51",
        "default-cpu-1s": "1.3.6.1.4.1.1991.1.1.2.1.50",
        "default-cpu-1m": "1.3.6.1.4.1.1991.1.1.2.1.52"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.1991.1.1.2.1.53"
    }

    temp = {
    }

    def __str__(self):
        return 'Foundry'


class Opzoon(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.37449.9.109.1.1.1.1"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.37449.9.48.1"
    }

    temp = {
    }

    def __str__(self):
        return 'Opzoon'


class Airespace(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.14179.1.1.5.1"
    }

    mem = {
        "default-mem": "((1.3.6.1.4.1.14179.1.1.5.2 - 1.3.6.1.4.1.14179.1.1.5.3) / 1.3.6.1.4.1.14179.1.1.5.2) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Airespace'


class Secgate(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.24968.1.3.9"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.24968.1.3.10"
    }

    temp = {
    }

    def __str__(self):
        return 'Secgate'


class F5(object):

    cpu = {
        "default-cpu": "(100 - 1.3.6.1.4.1.2021.11.11)"
    }

    mem = {
        "default-mem": "((1.3.6.1.4.1.2021.4.5 - 1.3.6.1.4.1.2021.4.6) / 1.3.6.1.4.1.2021.4.5) * 100",
        "tmm-mem": "(1.3.6.1.4.1.3375.2.1.1.2.1.45 / 1.3.6.1.4.1.3375.2.1.1.2.1.44) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'F5'


class Venustech(object):

    cpu = {
        "default-cpu": "(1.3.6.1.4.1.15227.1.3.1.1.1 * 100)"
    }

    mem = {
        "default-mem": "(1.3.6.1.4.1.15227.1.3.1.1.2 * 100)"
    }

    temp = {
    }

    def __str__(self):
        return 'Venustech'


class Nortel(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.2272.1.1.20"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.2272.1.1.47"
    }

    temp = {
    }

    def __str__(self):
        return 'Nortel'


class Harbour(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.8212.1.1.4.1.1.4 / 100"
    }

    mem = {
        "default-mem": "((1.3.6.1.4.1.8212.1.1.4.1.1.7 - 1.3.6.1.4.1.8212.1.1.4.1.1.8) / 1.3.6.1.4.1.8212.1.1.4.1.1.7) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Harbour'


class NetScaler(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.5951.4.1.1.41.1"
    }

    mem = {
        "default-mem": "1.3.6.1.4.1.5951.4.1.1.41.2"
    }

    temp = {
    }

    def __str__(self):
        return 'NetScaler'


class Extreme(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.1916.1.1.1.28"
    }

    mem = {
        "default-mem": "(1.3.6.1.4.1.3375.1.1.77 / (1.3.6.1.4.1.3375.1.1.78 / 1024)) * 100",
        "new-mem": "(1.3.6.1.4.1.3375.1.1.1.2.15 / (1.3.6.1.4.1.3375.1.1.1.2.14 / 1024)) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Extreme'


class Hillstone(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.28557.2.2.1.3",
        "new-cpu": "1.3.6.1.4.1.28557.2.2.3"
    }

    mem = {
        "new-mem": "(1.3.6.1.4.1.28557.2.2.5 / 1.3.6.1.4.1.28557.2.2.4 ) * 100",
        "default-mem": "(1.3.6.1.4.1.28557.2.2.1.5 / 1.3.6.1.4.1.28557.2.2.1.4 ) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Hillstone'


class Topsec(object):

    cpu = {
        "cpu-5": "1.3.6.1.4.1.14331.5.5.1.4.2 / 10",
        "default-cpu": "1.3.6.1.4.1.14331.5.5.1.4.5"
    }

    mem = {
        "default-cpu": "1.3.6.1.4.1.14331.5.5.1.4.6",
        "mem-5": "1.3.6.1.4.1.14331.5.5.1.4.3 / 10"
    }

    temp = {
    }

    def __str__(self):
        return 'Topsec'


class Centec(object):

    cpu = {
        "default-cpu": "(100 - 1.3.6.1.4.1.27975.1.2.11)"
    }

    mem = {
        "default-mem": "(1.3.6.1.4.1.27975.1.1.12 / 1.3.6.1.4.1.27975.1.1.5)*100"
    }

    temp = {
    }

    def __str__(self):
        return 'Centec'


class Sangfor(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.35047.1.3"
    }

    mem = {
    }

    temp = {
    }

    def __str__(self):
        return 'Sangfor'


class Alteon(object):

    cpu = {
        "default-cpu-64s": "(1.3.6.1.4.1.1872.2.1.8.16.5 + 1.3.6.1.4.1.1872.2.1.8.16.6) / 2",
        "default-cpu-4s": "(1.3.6.1.4.1.1872.2.1.8.16.3 + 1.3.6.1.4.1.1872.2.1.8.16.4) / 2",
        "default-cpu-1s": "(1.3.6.1.4.1.1872.2.1.8.16.1 + 1.3.6.1.4.1.1872.2.1.8.16.2) / 2"
    }

    mem = {
        "default-mem": "(1.3.6.1.4.1.1872.2.1.8.12.1 - 1.3.6.1.4.1.1872.2.1.8.12.2) / 1.3.6.1.4.1.1872.2.1.8.12.1 * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Alteon'


class Secworld(object):

    cpu = {
        "default-cpu": "(100 - 1.3.6.1.4.1.2021.11.11)"
    }

    mem = {
        "default-mem": "((1.3.6.1.4.1.2021.4.5 - 1.3.6.1.4.1.2021.4.6) / 1.3.6.1.4.1.2021.4.5) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'Secworld'


class HUAWEI(object):

    cpu = {
        "default-cpu": "1.3.6.1.4.1.2011.6.1.1.1.4",
        "default-cpu-1m": "1.3.6.1.4.1.2011.6.1.1.1.3",
        "default-cpu-5s": "1.3.6.1.4.1.2011.6.1.1.1.2"
    }

    mem = {
        "default-mem": "((1.3.6.1.4.1.2011.6.1.2.1.1.2 - 1.3.6.1.4.1.2011.6.1.2.1.1.3) / 1.3.6.1.4.1.2011.6.1.2.1.1.2) * 100"
    }

    temp = {
    }

    def __str__(self):
        return 'HUAWEI'


class Future(object):

    cpu = {
        "default-cpu": "((1.3.6.1.4.1.2021.11.50.0+1.3.6.1.4.1.2021.11.51.0+1.3.6.1.4.1.2021.11.52.0)*100)/(1.3.6.1.4.1.2021.11.50.0+1.3.6.1.4.1.2021.11.51.0+1.3.6.1.4.1.2021.11.52.0+1.3.6.1.4.1.2021.11.53.0)"
    }

    mem = {
        "default-mem": "((1.3.6.1.4.1.2021.4.5.0-1.3.6.1.4.1.2021.4.11.0-1.3.6.1.4.1.2021.4.14.0-1.3.6.1.4.1.2021.4.15.0)*100)/1.3.6.1.4.1.2021.4.11.0"
    }

    temp = {
    }

    def __str__(self):
        return 'Future'
