import re
from functools import wraps
from logging import LogRecord


def log_exceptions(logger):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            try:
                result = func(*args, **kwargs)
            except Exception:
                logger.exception(
                    u"Uncaught exception while running {0}".format(func.__name__)
                )
                raise
            return result

        return wrapper

    return decorator


class RedactedLogRecord(LogRecord, object):
    API_KEY_PATTERN = re.compile('api_key=*\w+(\w{5})')
    API_KEY_REPLACEMENT = r'api_key=*************************\1'

    def getMessage(self):
        message = super(RedactedLogRecord, self).getMessage()

        return re.sub(self.API_KEY_PATTERN, self.API_KEY_REPLACEMENT, message)
