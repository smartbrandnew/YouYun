import logging
import os
from urllib import getproxies
from urlparse import urlparse

log = logging.getLogger(__name__)


def set_no_proxy_settings():
    to_add = ["127.0.0.1", "localhost", "169.254.169.254"]
    no_proxy = os.environ.get("no_proxy", "")
    if not no_proxy.strip():
        no_proxy = []
    else:
        no_proxy = no_proxy.split(',')

    for host in to_add:
        if host not in no_proxy:
            no_proxy.append(host)

    os.environ['no_proxy'] = ','.join(no_proxy)


def get_proxy(agentConfig):
    proxy_settings = {}
    proxy_host = agentConfig.get('proxy_host')
    if proxy_host is not None:
        proxy_settings['host'] = proxy_host
        try:
            proxy_settings['port'] = int(agentConfig.get('proxy_port', 3128))
        except ValueError:
            log.error('Proxy port must be an Integer. Defaulting it to 3128')
            proxy_settings['port'] = 3128

        proxy_settings['user'] = agentConfig.get('proxy_user')
        proxy_settings['password'] = agentConfig.get('proxy_password')
        log.debug("Proxy Settings: %s:*****@%s:%s", proxy_settings['user'],
                  proxy_settings['host'], proxy_settings['port'])
        return proxy_settings

    try:
        proxy = getproxies().get('https')
        if proxy is not None:
            parse = urlparse(proxy)
            proxy_settings['host'] = parse.hostname
            proxy_settings['port'] = int(parse.port)
            proxy_settings['user'] = parse.username
            proxy_settings['password'] = parse.password

            log.debug("Proxy Settings: %s:*****@%s:%s", proxy_settings['user'],
                      proxy_settings['host'], proxy_settings['port'])
            return proxy_settings

    except Exception, e:
        log.debug("Error while trying to fetch proxy settings using urllib %s."
                  "Proxy is probably not set", str(e))

    log.debug("No proxy configured")

    return None
