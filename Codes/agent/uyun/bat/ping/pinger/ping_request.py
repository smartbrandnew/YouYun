class PingRequest():
    DEFAULT_TIMEOUT = 1000
    DEFAULT_COUNT = 4
    DEFAULT_TTL = 127
    DEFAULT_SIZE = 32

    def __init__(
        self, ip, timeout=DEFAULT_TIMEOUT,
        count=DEFAULT_COUNT,
        ttl=DEFAULT_TTL, size=DEFAULT_SIZE,
        stop_when_online=False
    ):
        self.ip = ip
        self.timeout = timeout
        self.count = count
        self.ttl = ttl
        self.size = size
        self.stop_when_online = stop_when_online

    def is_stop_when_online(self):
        return self.stop_when_online

    def get_ip(self):
        return self.ip

    def get_count(self):
        return self.count

    def get_timeout(self):
        return self.timeout

    def get_ttl(self):
        return self.ttl

    def get_size(self):
        return self.size

    def __str__(self):
        return "%s[ip: %s timeout: %s count: %d ttl: %d]" % (
            self.__class__.__name__,
            self.ip, self.timeout,
            self.count,
            self.ttl
        )
