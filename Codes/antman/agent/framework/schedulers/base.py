from tornado import gen


class BaseScheduler(object):

    @gen.coroutine
    def eventloop(self):
        raise NotImplementedError()
