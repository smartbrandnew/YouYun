class AlreadyExist(Exception):
    """Raised when exists"""
    pass


class NotExistError(Exception):
    """Raised when not exists"""
    pass


class MessageError(Exception):
    """ error raised when a message is invalid """
    pass


class CallError(Exception):
    pass


class ArgumentError(Exception):
    """Exception raised when one argument or the number of
    arguments is invalid"""
    pass


class ConflictError(Exception):
    """Exception raised when one exclusive command or service
    is already running in background"""
    pass


class TimeOutError(Exception):
    """Exception raised when operate times out."""
    pass
