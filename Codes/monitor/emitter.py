import logging
import re
import zlib
from hashlib import md5

import requests
import simplejson as json

from config import get_version
from utils.proxy import set_no_proxy_settings

set_no_proxy_settings()

requests_log = logging.getLogger("requests.packages.urllib3")
requests_log.setLevel(logging.WARN)
requests_log.propagate = True

control_chars = ''.join(map(unichr, range(0, 32) + range(127, 160)))
control_char_re = re.compile('[%s]' % re.escape(control_chars))


def remove_control_chars(s):
    return control_char_re.sub('', s)


def http_emitter(message, log, agentConfig, endpoint):
    temp = message
    url = agentConfig['m_url']

    log.debug('http_emitter: attempting postback to ' + url)

    try:
        payload = json.dumps(message)
    except UnicodeDecodeError:
        message = remove_control_chars(message)
        payload = json.dumps(message)

    zipped = zlib.compress(payload)

    log.debug("payload_size=%d, compressed_size=%d, compression_ratio=%.3f"
              % (len(payload), len(zipped), float(len(payload)) / float(len(zipped))))

    apiKey = temp.get('apiKey', None)
    if not apiKey:
        raise Exception("The http emitter requires an api key")

    url = "{0}/intake/{1}?api_key={2}".format(url, endpoint, apiKey)

    try:
        headers = post_headers(agentConfig, zipped)
        r = requests.post(url, data=zipped, timeout=5, headers=headers, verify=False)

        r.raise_for_status()

        if r.status_code >= 200 and r.status_code < 205:
            log.debug("Payload accepted")

    except Exception:
        log.exception("Unable to post payload.")
        try:
            log.error("Received status code: {0}".format(r.status_code))
        except Exception:
            pass


def post_headers(agentConfig, payload):
    return {
        'User-Agent': 'Monitor Agent/%s' % agentConfig['version'],
        'Content-Type': 'application/json',
        'Content-Encoding': 'deflate',
        'Accept': 'text/html, */*',
        'Content-MD5': md5(payload).hexdigest(),
        'DD-Collector-Version': get_version()
    }
