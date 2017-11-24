#!/C:/Python27

import time
import itertools
from ping_response import PingResponse
# from ping_request import PingRequest
# from ping_listener import PingListener


class PingTask():
    nextId = itertools.count(int(time.time()))

    def __init__(self, request, listener):
        self.id = PingTask.nextId.next()
        self.request = request
        self.listener = listener
        self.createTime = time.time()
        self.finishTime = 0
        self.packets = []
        self.currentPacket = 0

    def get_id(self):
        return self.id

    def get_request(self):
        return self.request

    def get_create_time(self):
        return self.createTime

    def get_finished_time(self):
        return self.finishedTime

    def get_packets(self):
        return self.packets

    def create_response(self):
        sent = len(self.packets)
        received = 0
        times = []

        for packet in self.packets:
            if packet.is_ok():
                received += 1
                time = (packet.get_received_time() - packet.get_sent_time()) \
                    * 1000
                if time <= 0:
                    times.append(1)
                elif time < 1:
                    times.append(1)
                else:
                    times.append(int(time))
            else:
                times.append(0)
        return PingResponse(sent, received, times)

    def add_packet(self, packet):
        self.packets.append(packet)
        self.current_packet = packet

    def get_current_packet(self):
        return self.current_packet

    def get_listener(self):
        return self.listener

    def is_finished(self):
        if self.request.is_stop_when_online():
            for packet in self.packets:
                if packet.isOk():
                    return True
        return len(self.packets) == self.request.get_count()

    def __str__(self):
        return '%s[ip: %s timeout: %s count %d ttl: %d]' % (
            self.__class__.__name__,
            self.get_request().get_ip(),
            self.get_request().get_timeout(),
            self.get_request().get_count(),
            self.get_request().get_ttl()
        )

    def hash_cde(self):
        return self.id

    def equals(self, obj):
        return self.id == obj.id

if __name__ == '__main__':
    listener = PingListener()

    test1 = PingTask(PingRequest("www.baidu.com"), listener)
    test2 = PingTask(PingRequest("www,baidu.com"), listener)
    test3 = PingTask(PingRequest("www.sina.com"), listener)

    print test1.equals(test2)
    print test1.equals(test3)
    print test1.equals(test1)

    print test1
