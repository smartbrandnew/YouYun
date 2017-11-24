import functools
from threading import Thread

_thread_by_func = {}


class TimeoutException(Exception):
    pass


class ThreadMethod(Thread):
    def __init__(self, target, args, kwargs):
        Thread.__init__(self)
        self.setDaemon(True)
        self.target, self.args, self.kwargs = target, args, kwargs
        self.start()

    def run(self):
        try:
            self.result = self.target(*self.args, **self.kwargs)
        except Exception, e:
            self.exception = e
        else:
            self.exception = None


def timeout(timeout):
    def decorator(func):
        @functools.wraps(func)
        def wrapper(*args, **kwargs):
            key = "{0}:{1}:{2}:{3}".format(id(func), func.__name__, args, kwargs)

            if key in _thread_by_func:
                worker = _thread_by_func[key]
            else:
                worker = ThreadMethod(func, args, kwargs)
                _thread_by_func[key] = worker

            worker.join(timeout)
            if worker.is_alive():
                raise TimeoutException()

            del _thread_by_func[key]

            if worker.exception:
                raise worker.exception
            else:
                return worker.result

        return wrapper

    return decorator
