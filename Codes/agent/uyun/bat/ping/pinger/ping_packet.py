#!/C:/Python27

import time
import itertools


class PingPacket():
    nextSeq = itertools.count(int(time.time()))

    def __init__(self):
        self.seq = PingPacket.nextSeq.next()
        self.sentTime = 0
        self.receivedTime = 0

    def get_seq(self):
        return self.seq

    def get_sent_time(self):
        return self.sentTime

    def get_received_time(self):
        return self.receivedTime

    def set_sent_time(self, sentTime):
        self.sentTime = sentTime

    def set_received_time(self, receivedTime):
        self.receivedTime = receivedTime

    def is_ok(self):
        return self.receivedTime > 0

    def set_failed(self):
        self.receivedTime = -1

    def __str__(self):
        return '%s[seq: %d]' % (self.__class__.__name__, self.seq)

if __name__ == '__main__':
    test = PingPacket()
    print test
