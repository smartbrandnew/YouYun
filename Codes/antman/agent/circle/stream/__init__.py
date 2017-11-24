from Queue import Queue, Empty

from circle.util import resolve_name
from circle.stream.file_stream import FileStream
from circle.stream.file_stream import WatchedFileStream
from circle.stream.file_stream import TimedRotatingFileStream
from circle.stream.redirector import Redirector


class QueueStream(Queue):

    def __init__(self, **kwargs):
        Queue.__init__(self)

    def __call__(self, data):
        self.put(data)

    def close(self):
        pass


def get_stream(conf, reload=False):
    if conf:
        # we can have 'stream' or 'class' or 'filename'
        if 'class' in conf:
            class_name = conf.pop('class')
            if not "." in class_name:
                cls = globals()[class_name]
                inst = cls(**conf)
            else:
                inst = resolve_name(class_name, reload=reload)(**conf)
        elif 'stream' in conf:
            inst = conf['stream']
        elif 'filename' in conf:
            inst = FileStream(**conf)
        else:
            raise ValueError("stream configuration invalid")

        return inst
