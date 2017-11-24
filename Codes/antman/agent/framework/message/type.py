class MessageType(object):
    HEARTBEAT = 0  # agent -> server
    INFO = 1  # agent -> server

    NEW_TASK = 100  # server -> agent
    TASK_START = 101  # agent -> server
    TASK_RESULT = 102  # agent -> server
    TASK_LOG = 103  # agent -> server
    KILL_TASK = 104  # server -> agent
