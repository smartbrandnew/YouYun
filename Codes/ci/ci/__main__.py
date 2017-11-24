import sys
import tornado.web
import tornado.ioloop

from ci import logger
from ci.constants import PORT
from ci.handlers import DeployHandler, GitLabHandler, TestHandler


def start(type_):
    logger.info('Starting ci {}'.format(type_))
    handlers = [
        ('/deploy', DeployHandler),
        ('/test', TestHandler)
    ]
    if type_ == 'server':
        handlers.append(('/center/gitlab', GitLabHandler))
    elif type_ != 'agent':
        logger.error('start type must be "server" or "agent"')
        exit(1)

    application = tornado.web.Application(handlers)
    application.listen(PORT)
    io_loop = tornado.ioloop.IOLoop.instance()
    io_loop.start()


if __name__ == '__main__':
    type_ = 'agent'
    if len(sys.argv) >= 2:
        type_ = sys.argv[1]
    start(type_)
