# !/C:/Python27

from threading import Event


class PingListener():
    def __init__(self):
        self.event = Event()
        self.response = None

    def finished(self, request, response):
        self.response = response
        self.event.set()
        self.event.clear()

    def wait_response(self, timeout):
        self.event.wait(timeout)
        return self.response
