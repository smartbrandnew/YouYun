import uuid

from config import get_config
from util import get_hostname


def get_uuid():
    agentconfig = get_config()
    agentuuid = agentconfig.get("uuid", '')
    if agentuuid:
        return agentuuid

    return uuid.uuid5(uuid.NAMESPACE_DNS, get_hostname() + str(uuid.getnode())).hex