class ExecuteError(Exception):
    def __init__(self, cmd, code, reason):
        super(ExecuteError, self).__init__(
            'Cmd: {} \n'
            'Exit code: {}\n'
            'Reason:\n'
            '{}'.format(cmd, code, reason)
        )
