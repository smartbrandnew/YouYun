# -*- coding: UTF-8 -*-

'''
需要改进：
    fjc 已改进，使用log，1，报错方式修改，不要用print语句;
    2，snmp_get_nex, snmp_get_next_v3 实现方式
    很扭曲，有什么更好的Python SNMP get_next
    实现？

    fjc 2016-09-06 16:43 更新：放弃log配置和display_errors参数;
    支持timeout和retries参数
'''

from pysnmp.entity.rfc3413.oneliner import cmdgen

__author__ = 'fangjc'
__project__ = 'uyun-bat-snmp'
__date__ = '2016/05/01'

def snmp_get_oid(target, oid='.1.3.6.1.2.1.1.1.0'):
    snmp_device = target.get_device()
    timeout = target.get_timeout()
    retries = target.get_retries()
    if len(snmp_device) == 3:
        try:
            result = snmp_get_oid_v1v2c(snmp_device, oid, timeout, retries)
        except Exception:
            result = None
        return result
    elif len(snmp_device) == 2:
        snmp_user = target.get_user()
        auth_proto = target.get_auth_proto()
        encrypt_proto = target.get_encrypt_proto()
        try:
            result = snmp_get_oid_v3(
                snmp_device,
                snmp_user,
                oid,
                timeout,
                retries,
                auth_proto,
                encrypt_proto
            )
        except Exception:
            result = None
        return result


def snmp_walk(target, oid='.1.3.6.1.2.1.1.1.0'):
    snmp_device = target.get_device()
    timeout = target.get_timeout()
    retries = target.get_retries()
    if len(snmp_device) == 3:
        try:
            result = snmp_walk_v1v2c(snmp_device, oid, timeout, retries)
        except Exception:
            result = None
        return result
    elif len(snmp_device) == 2:
        snmp_user = target.get_user()
        auth_proto = target.get_auth_proto()
        encrypt_proto = target.get_encrypt_proto()
        try:
            result = snmp_walk_v3(
                snmp_device,
                snmp_user,
                oid,
                timeout,
                retries,
                auth_proto,
                encrypt_proto
            )
        except Exception:
            result = None
        return result


def snmp_get_next(target, oid='.1.3.6.1.2.1.1.1.0'):
    snmp_device = target.get_device()
    timeout = target.get_timeout()
    retries = target.get_retries()
    if len(snmp_device) == 3:
        try:
            snmp_get_next_v1v2c(snmp_device, oid, timeout, retries)
        except Exception:
            return None
    elif len(snmp_device) == 2:
        snmp_user = target.get_user()
        auth_proto = target.get_auth_proto()
        encrypt_proto = target.get_encrypt_proto()
        try:
            snmp_get_next_v3(
                snmp_device,
                snmp_user,
                oid,
                timeout,
                retries,
                auth_proto,
                encrypt_proto
            )
        except Exception:
            return None


def snmp_get_oid_v3(
        snmp_device,
        snmp_user,
        oid,
        timeout,
        retries,
        auth_proto,
        encrypt_proto
):
    a_user, auth_key, encrypt_key = snmp_user

    auth_proto_map = {
        'sha': cmdgen.usmHMACSHAAuthProtocol,
        'md5': cmdgen.usmHMACMD5AuthProtocol,
        'none': cmdgen.usmNoAuthProtocol
    }

    if auth_proto in auth_proto_map.keys():
        auth_protocol = auth_proto_map[auth_proto]
    else:
        raise ValueError(
            "Invalid authentication protocol specified: %s" % auth_proto)

    encrypt_proto_map = {
        'des': cmdgen.usmDESPrivProtocol,
        '3des': cmdgen.usm3DESEDEPrivProtocol,
        'aes128': cmdgen.usmAesCfb128Protocol,
        'aes192': cmdgen.usmAesCfb192Protocol,
        'aes256': cmdgen.usmAesCfb256Protocol,
        'none': cmdgen.usmNoPrivProtocol,
    }

    if encrypt_proto in encrypt_proto_map.keys():
        encrypt_protocol = encrypt_proto_map[encrypt_proto]
    else:
        raise ValueError(
            "Invalid encryption protocol specified: %s" % encrypt_proto)

    # Create a PYSNMP cmdgen object
    cmd_gen = cmdgen.CommandGenerator()

    (error_detected, error_status, error_index, snmp_binding) = cmd_gen.getCmd(

        cmdgen.UsmUserData(a_user, auth_key, encrypt_key,
                           authProtocol=auth_protocol,
                           privProtocol=encrypt_protocol, ),
        cmdgen.UdpTransportTarget(snmp_device, timeout, retries),
        oid,
        lookupNames=True, lookupValues=True
    )

    if not error_detected:
        return snmp_binding[0][1]
    else:
        return None


def snmp_get_oid_v1v2c(a_device, oid, timeout, retries):
    a_host, community_string, snmp_port = a_device
    snmp_target = (a_host, snmp_port)

    # Create a PYSNMP cmdgen object
    cmd_gen = cmdgen.CommandGenerator()

    (error_detected, error_status, error_index, snmp_binding) = cmd_gen.getCmd(
        cmdgen.CommunityData(community_string),
        cmdgen.UdpTransportTarget(snmp_target, timeout, retries),
        oid,
        lookupNames=True, lookupValues=True
    )

    if not error_detected:
        return snmp_binding[0][1]
    else:
        return None


def snmp_walk_v1v2c(a_device, oid, timeout, retries):
    a_host, community_string, snmp_port = a_device
    snmp_target = (a_host, snmp_port)
    cmd_gen = cmdgen.CommandGenerator()

    (error_detected, error_status, error_index, snmp_bindings) = \
        cmd_gen.nextCmd(
            cmdgen.CommunityData(community_string),
            cmdgen.UdpTransportTarget(snmp_target, timeout, retries),
            oid,
            lookupNames=True, lookupValues=True
        )
    if not error_detected:
        return snmp_bindings
    else:
        return None


def snmp_walk_v3(
        snmp_device,
        snmp_user,
        oid,
        timeout,
        retries,
        auth_proto,
        encrypt_proto
):
    a_user, auth_key, encrypt_key = snmp_user

    auth_proto_map = {
        'sha': cmdgen.usmHMACSHAAuthProtocol,
        'md5': cmdgen.usmHMACMD5AuthProtocol,
        'none': cmdgen.usmNoAuthProtocol
    }

    if auth_proto in auth_proto_map.keys():
        auth_protocol = auth_proto_map[auth_proto]
    else:
        raise ValueError(
            "Invalid authentication protocol specified: %s" % auth_proto)

    encrypt_proto_map = {
        'des': cmdgen.usmDESPrivProtocol,
        '3des': cmdgen.usm3DESEDEPrivProtocol,
        'aes128': cmdgen.usmAesCfb128Protocol,
        'aes192': cmdgen.usmAesCfb192Protocol,
        'aes256': cmdgen.usmAesCfb256Protocol,
        'none': cmdgen.usmNoPrivProtocol,
    }

    if encrypt_proto in encrypt_proto_map.keys():
        encrypt_protocol = encrypt_proto_map[encrypt_proto]
    else:
        raise ValueError(
            "Invalid encryption protocol specified: %s" % encrypt_proto)

    # Create a PYSNMP cmdgen object
    cmd_gen = cmdgen.CommandGenerator()

    (error_detected, error_status, error_index, snmp_bindings) = \
        cmd_gen.nextCmd(
            cmdgen.UsmUserData(
                a_user,
                auth_key,
                encrypt_key,
                authProtocol=auth_protocol,
                privProtocol=encrypt_protocol
            ),
        cmdgen.UdpTransportTarget(snmp_device, timeout, retries),
        oid,
        lookupNames=True, lookupValues=True
    )

    if not error_detected:
        return snmp_bindings
    else:
        return None


def snmp_get_next_v1v2c(a_device, oid, timeout, retries):
    OID = (oid,)

    a_host, community_string, snmp_port = a_device
    snmp_target = (a_host, snmp_port)
    cmd_gen = cmdgen.AsynCommandGenerator()

    cmd_gen.nextCmd(
        cmdgen.CommunityData(community_string),
        cmdgen.UdpTransportTarget(snmp_target, timeout, retries),
        OID,
        (cbFun, None)
    )
    cmd_gen.snmpEngine.transportDispatcher.runDispatcher()


def snmp_get_next_v3(
        snmp_device,
        snmp_user,
        oid,
        timeout,
        retries,
        auth_proto,
        encrypt_proto
):
    OID = (oid,)
    a_user, auth_key, encrypt_key = snmp_user

    auth_proto_map = {
        'sha': cmdgen.usmHMACSHAAuthProtocol,
        'md5': cmdgen.usmHMACMD5AuthProtocol,
        'none': cmdgen.usmNoAuthProtocol
    }

    if auth_proto in auth_proto_map.keys():
        auth_protocol = auth_proto_map[auth_proto]
    else:
        raise ValueError(
            "Invalid authentication protocol specified: %s" % auth_proto)

    encrypt_proto_map = {
        'des': cmdgen.usmDESPrivProtocol,
        '3des': cmdgen.usm3DESEDEPrivProtocol,
        'aes128': cmdgen.usmAesCfb128Protocol,
        'aes192': cmdgen.usmAesCfb192Protocol,
        'aes256': cmdgen.usmAesCfb256Protocol,
        'none': cmdgen.usmNoPrivProtocol,
    }

    if encrypt_proto in encrypt_proto_map.keys():
        encrypt_protocol = encrypt_proto_map[encrypt_proto]
    else:
        raise ValueError(
            "Invalid encryption protocol specified: %s" % encrypt_proto)

    cmd_gen = cmdgen.AsynCommandGenerator()

    cmd_gen.nextCmd(
        cmdgen.UsmUserData(
            a_user,
            auth_key,
            encrypt_key,
            authProtocol=auth_protocol,
            privProtocol=encrypt_protocol
        ),
        cmdgen.UdpTransportTarget(snmp_device, timeout, retries),
        OID,
        (cbFun, None)
    )
    cmd_gen.snmpEngine.transportDispatcher.runDispatcher()


def cbFun(
    sendRequestHandle,
    errorIndication,
    errorStatus,
    errorIndex,
    varBindTable,
    cbCtx
):
    if errorIndication:
        print(errorIndication)
        return 1
    if errorStatus:
        print(errorStatus.prettyPrint())
        return 1
    for varBindRow in varBindTable:
        for oid, val in varBindRow:
            print ('%s = %s' %
                   (oid.prettyPrint(), val and val.prettyPrint() or '?'))
