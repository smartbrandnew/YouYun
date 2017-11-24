class PingResponse():
    def __init__(self, sent, received, times):
        self.sent = sent
        self.received = received
        self.times = times
        self.mini = 0
        self.maxi = 0
        self.avg = 0
        self.summary = 0

        for time in self.times:
            if time > 0:
                if self.mini == 0 or self.mini > time:
                    self.mini = time
                if self.maxi == 0 or self.maxi < time:
                    self.maxi = time
                self.summary += time

        if self.received > 0:
            self.avg = self.summary / self.received

    def get_sent(self):
        return self.sent

    def get_received(self):
        return self.received

    def get_times(self):
        return self.times

    def get_lost_percent(self):
        return ((self.sent - self.received) * 100.0 / self.sent)

    def get_min(self):
        return self.mini

    def get_avg(self):
        return self.avg

    def get_max(self):
        return self.maxi

    def __str__(self):
        return "%s[sent: %d received: %d lost %d%% min/avg/max: \
            %d/%d/%d(ms) times: %s(ms)]" % (
                self.__class__.__name__,
                self.sent, self.received,
                self.get_lost_percent(),
                self.mini,
                self.avg,
                self.maxi,
                str(self.times)
            )

if __name__ == '__main__':
    test = PingResponse(2, 1, [40, 20, 30])
    print test.get_sent()
    print test.get_received()
    print test.get_times()
    print test.get_lost_percent()
    print test.get_min()
    print test.get_avg()
    print test.get_max()
