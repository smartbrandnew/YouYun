from circle.commands.base import Command
from circle.errors import ArgumentError
from circle.util import convert_opt


class Options(Command):
    """\
        Get the value of all options for a watcher
        ==========================================

        This command returns all option values for a given watcher.

        TCP Message
        -----------

        ::

            {
                "command": "options",
                "properties": {
                    "name": "nameofwatcher",
                }
            }

        A message contains 1 property:

        - name: name of watcher

        The response object has a property ``options`` which is a
        dictionary of option names and values.

        eg::

            {
                "status": "ok",
                "options": {
                    "graceful_timeout": 300,
                    "send_hup": True,
                    ...
                },
                time': 1332202594.754644
            }


        Command line
        ------------

        ::

            $ circlectl options <name>


        Options
        -------

        - <name>: name of the watcher

        Options Keys are:

        - numprocesses: integer, number of processes
        - warmup_delay: integer or number, delay to wait between process
          spawning in seconds
        - working_dir: string, directory where the process will be executed
        - uid: string or integer, user ID used to launch the process
        - gid: string or integer, group ID used to launch the process
        - send_hup: boolean, if TRU the signal HUP will be used on reload
        - shell: boolean, will run the command in the shell environment if
          true
        - cmd: string, The command line used to launch the process
        - env: object, define the environnement in which the process will be
          launch
        - retry_in: integer or number, time in seconds we wait before we retry
          to launch the process if the maximum number of attempts
          has been reach.
        - max_retry: integer, The maximum of retries loops
        - graceful_timeout: integer or number, time we wait before we
          definitely kill a process.
        - priority: used to sort watchers in the arbiter
        - singleton: if True, a singleton watcher.
        - max_age: time a process can live before being restarted
        - max_age_variance: variable additional time to live, avoids
          stampeding herd.
    """

    name = "options"
    properties = ['name']

    def message(self, *args, **opts):

        if len(args) < 1:
            raise ArgumentError("number of arguments invalid")

        return self.make_message(name=args[0])

    def execute(self, arbiter, props):
        watcher = self._get_watcher(arbiter, props['name'])
        return {"options": dict(watcher.options())}

    def console_msg(self, msg):
        if msg['status'] == "ok":
            ret = []
            for k, v in msg.get('options', {}).items():
                ret.append("%s: %s" % (k, convert_opt(k, v)))
            return "\n".join(ret)
        return self.console_error(msg)
