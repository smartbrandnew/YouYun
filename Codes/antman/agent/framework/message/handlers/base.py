from tornado import gen


class BaseMessageHandler(object):

    @gen.coroutine
    def handle(self, msg):
        raise NotImplementedError()
