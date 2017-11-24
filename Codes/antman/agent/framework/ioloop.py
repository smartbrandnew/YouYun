from tornado.ioloop import IOLoop


def get_io_loop():
    return IOLoop.current()
