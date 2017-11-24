# -*- coding: UTF-8 -*-
# !/C:/Python27

from ..snmp import snmp_get_oid, snmp_walk, Target

SNMP_PORT = 161


class RFC1213_OID():
    SYS_DESCR = ".1.3.6.1.2.1.1.1.0"
    SYS_OBJECTID = ".1.3.6.1.2.1.1.2.0"
    SYS_CONTACT = ".1.3.6.1.2.1.1.4.0"
    SYS_NAME = ".1.3.6.1.2.1.1.5.0"
    SYS_SERVICES = ".1.3.6.1.2.1.1.7.0"
    IP_FORWARDING = ".1.3.6.1.2.1.4.1.0"
    IP_TABLE_IP = ".1.3.6.1.2.1.4.20.1.1"
    IP_TABLE_IFINDEX = ".1.3.6.1.2.1.4.20.1.2"
    IF_TABLE_INDEX = ".1.3.6.1.2.1.2.2.1.1"
    IF_TABLE_DESCR = ".1.3.6.1.2.1.2.2.1.2"
    IF_TABLE_SPEED = ".1.3.6.1.2.1.2.2.1.5"
    IF_TABLE_PHYSADDR = ".1.3.6.1.2.1.2.2.1.6"
    BRIDGE_ADDRESS = ".1.3.6.1.2.1.17.1.1.0"
    STOR_TABLE_DESCR = ".1.3.6.1.2.1.25.2.3.1.3"
    STOR_TABLE_UNIT = ".1.3.6.1.2.1.25.2.3.1.4"
    STOR_TABLE_SIZE = ".1.3.6.1.2.1.25.2.3.1.5"
    IFX_TABLE_SPEED = ".1.3.6.1.2.1.31.1.1.1.15"

    IP_TABLE = [
        IP_TABLE_IP,
        IP_TABLE_IFINDEX
        ]

    IF_TABLE = [
        IF_TABLE_INDEX,
        IF_TABLE_DESCR,
        IF_TABLE_SPEED,
        IF_TABLE_PHYSADDR
        ]

    STORAGE_TABLE = [
        STOR_TABLE_DESCR,
        STOR_TABLE_UNIT,
        STOR_TABLE_SIZE
        ]


SYS_OBJECTED_MAP = {
    ".1.3.6.1.4.1.14331":  		"Firewall",
    ".1.3.6.1.4.1.1588.2.1.1.1":        "Switch",
    ".1.3.6.1.4.1.24968.1.1.1":  	"Firewall",
    ".1.3.6.1.4.1.3224.1.10":  		"Firewall",
    ".1.3.6.1.4.1.3224.1.14":  		"Firewall",
    ".1.3.6.1.4.1.3224.1.28":  		"Firewall",
    ".1.3.6.1.4.1.3224.1.6":  		"Firewall",
    ".1.3.6.1.4.1.3224.1.9":  		"Firewall",
    ".1.3.6.1.4.1.3375":  		"Firewall",
    ".1.3.6.1.4.1.3375.1.1":  		"Firewall",
    ".1.3.6.1.4.1.6339":  		"Switch",
    ".1.3.6.1.4.1.6339.1.1.2":  	"Switch",
    ".1.3.6.1.4.1.6339.1.1.3":  	"Switch",
    ".1.3.6.1.4.1.6339.1.2":  		"Router",
    ".1.3.6.1.4.1.6339.1.22.0":  	"Router",
    ".1.3.6.1.4.1.6339.1.30":  		"Router",
    ".1.3.6.1.4.1.6339.1.4":  		"Switch",
    ".1.3.6.1.4.1.6339.1.60":  		"Router",
    ".1.3.6.1.4.1.641.1":  		"Computer",
    ".1.3.6.1.4.1.8072":  		"PCServer",
    ".1.3.6.1.4.1.8212":  		"Switch",
    ".1.3.6.1.4.1.9.1.392":  		"Firewall",
    ".1.3.6.1.4.1.9.1.393":  		"Firewall",
    ".1.3.6.1.4.1.9.1.451":  		"Firewall",
    ".1.3.6.1.4.1.9833.1.1":  		"Firewall",
    ".1.3.6.1.4.1.12740.17.1":          "DiskArray"
}


class ENTERPRISE_NUMBER():
    HP = 11
    MICROSOFT = 311
    NETSNMP = 2021

    PCSERVER_NUMBERS = [HP, MICROSOFT, NETSNMP]

    NAMES = {
        2: "IBM",
        9: "Cisco",
        HP: "HP",
        23: "Novell",
        34: "Cray",
        36: "DEC",
        42: "Sun",
        43: "3Com",
        45: "Nortel",
        52: "Enterasys",
        74: "AT&T",
        89: "Redware",
        94: "Nokia",
        111: "Oracle",
        122: "Sony",
        161: "Motorola",
        171: "D-Link",
        202: "SMC",
        'MICROSOFT': "Microsoft",
        343: "Intel",
        674: "Dell",
        800: "Alcatel",
        1248: "EPSON",
        1916: "Extreme",
        1991: "Foundry",
        2011: "HUAWEI",
        'NETSNMP': "NetSnmp",
        2636: "Juniper",
        3003: "Alcatel",
        3224: "Netscreen",
        3320: "Bdcom",
        3375: "F5",
        3417: "Bluecoat",
        3902: "ZTE",
        3955: "Linksys",
        4526: 'Netgear',
        4881: "Red-Giant",
        5567: "Riverstone",
        5624: "Enterasys",
        5651: "Maipu",
        5951: "NetScaler",
        6339: "DigitalChina",
        6486: "Alcatel",
        6889: "Avaya",
        9833: "Lenovo",
        12356: "Fortinet",
        12532: "Juniper",
        12740: "EqualLogic",
        14331: "Topsec",
        25506: "H3C"
        }


def discover_type(target):
    type = None
    enterprise_number = None
    ip_forwarding = None
    bridge_address = None
    sys_object_id = None
    sys_services = None
    ip_addr_table = []

    is_router = False
    is_switch = False

    sys_object_id = snmp_get_oid(target, RFC1213_OID.SYS_OBJECTID)
    sys_object_id = str(sys_object_id)
    ls = sys_object_id.split('.')
    if len(ls) > 7:
        enterprise_number = ls[6]

    if sys_object_id:
        for (key, value) in SYS_OBJECTED_MAP.items():
            if key == sys_object_id or (key in sys_object_id):
                type = value
                break

    if not type:
        if enterprise_number:
            for cn in ENTERPRISE_NUMBER.PCSERVER_NUMBERS:
                if cn == enterprise_number:
                    type = "PCServer"
                    break

    # 路由器 交换机 判断
    if not type:
        ip_addr_table = snmp_walk(target, RFC1213_OID.IP_TABLE_IP)
        bridge_address = snmp_get_oid(target, RFC1213_OID.BRIDGE_ADDRESS)
        ip_forwarding = snmp_get_oid(target, RFC1213_OID.IP_FORWARDING)
        sys_services = snmp_get_oid(target, RFC1213_OID.SYS_SERVICES)

        if sys_services and int(sys_services) in [2, 6 ,11]:
            is_switch = True
        if ip_addr_table and len(ip_addr_table) > 1 and ip_forwarding and int(ip_forwarding) == 1:
            is_router = True
        if bridge_address:
            is_switch = True

        if is_switch and is_router:
            type = 'Router'
        elif is_switch:
            type = 'Switch'
        elif is_router:
            type = 'Router'

    return type


def discover_brand(target):
    brand = None

    sys_object_id = snmp_get_oid(target, RFC1213_OID.SYS_OBJECTID)
    if not sys_object_id:
        return

    sys_object_id = str(sys_object_id).split('.')
    if len(sys_object_id) < 7:
        return

    enterprise_number = sys_object_id[6]
    brand = ENTERPRISE_NUMBER.NAMES.get(int(enterprise_number))
    return brand
