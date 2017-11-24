# coding: utf-8


class MessageWarn(Exception):

    def __init__(self, msg):
        super(MessageWarn, self).__init__(msg)


class MessageError(Exception):

    def __init__(self, msg):
        super(MessageError, self).__init__(msg)


class AlreadyExistsError(Exception):

    def __init__(self, name):
        super(AlreadyExistsError,
              self).__init__('{} already exits'.format(name))


class NotExistsError(Exception):

    def __init__(self, name):
        super(NotExistsError, self).__init__('{} not exits'.format(name))


class StopError(Exception):
    pass


class StartError(Exception):
    pass


class NotMatchError(Exception):
    pass


class SecurityError(MessageError):
    pass


class SpawnError(MessageError):
    pass


class RegisterServiceError(Exception):
    pass
