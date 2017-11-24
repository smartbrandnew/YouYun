from tornado import gen
from tornado.ioloop import IOLoop
from framework.config import config
from framework.schedulers.status import StatusScheduler


def test_modules_status():
    exc_info = []
    io_loop = IOLoop()

    @gen.coroutine
    def collect_modules_status(cls):
        cls._modules_status = [{'a': 'a'}]

    @gen.coroutine
    def check_modules_status():
        yield gen.sleep(0.01)
        try:
            module_status = yield StatusScheduler.get_modules_status()
            assert module_status == [{'a': 'a'}]
        except Exception as e:
            exc_info.append(e)
        finally:
            io_loop.stop()

    scheduler = StatusScheduler()
    StatusScheduler.collect_modules_status = classmethod(collect_modules_status)

    with config.mock({'heartbeat': 0.01}):
        io_loop.spawn_callback(scheduler.eventloop)
        io_loop.spawn_callback(check_modules_status)
        io_loop.start()

    if exc_info:
        raise exc_info[0]
