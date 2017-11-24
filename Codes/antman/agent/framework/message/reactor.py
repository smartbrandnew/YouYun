class MessageReactor(object):

    def __init__(self):
        self._msg_handlers = {}

    def add_handler(self, msg_type, msg_handler):
        self._msg_handlers[msg_type] = msg_handler

    def feed(self, msg):
        msg_handler = self._msg_handlers[msg.type]
        if msg_handler:
            msg_handler.handle(msg)
        else:
            raise Exception('Invalid message type {0}'.format(msg.type))


reactor = MessageReactor()
