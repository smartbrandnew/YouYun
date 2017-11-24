from circle.commands.base import Command
from circle.errors import MessageError
from circle.util import convert_opt

_OPTIONS = ('endpoint', 'stats_endpoint', 'pubsub_endpoint', 'check_delay',
            'multicast_endpoint')


class GlobalOptions(Command):
    """\
        Get the arbiter options
        =======================

        This command return the arbiter options

        TCP Message
        -----------

        ::

            {
                "command": "globaloptions",
                "properties": {
                    "key1": "val1",
                    ..
                }
            }

        A message contains 2 properties:

        - keys: list, The option keys for which you want to get the values

        The response return an object with a property "options"
        containing the list of key/value returned by circle.

        eg::

            {
                "status": "ok",
                "options": {
                    "check_delay": 1,
                    ...
                },
                time': 1332202594.754644
            }



        Command line
        ------------

        ::

            $ circlectl globaloptions


        Options
        -------

        Options Keys are:

        - endpoint: the controller TCP endpoint
        - pubsub_endpoint: the pubsub endpoint
        - check_delay: the delay between two controller points
        - multicast_endpoint: the multicast endpoint for circled cluster
          auto-discovery
    """

    name = "globaloptions"
    properties = []

    def message(self, *args, **opts):
        if len(args) > 0:
            return self.make_message(option=args[0])
        else:
            return self.make_message()

    def execute(self, arbiter, props):
        wanted = props.get('option')
        if wanted:
            if wanted not in _OPTIONS:
                raise MessageError('%r not an existing option' % wanted)
            options = (wanted,)
        else:
            options = _OPTIONS

        res = {}

        for option in options:
            res[option] = getattr(arbiter, option)

        return {"options": res}

    def console_msg(self, msg):
        if msg['status'] == "ok":
            ret = []
            for k, v in msg.get('options', {}).items():
                ret.append("%s: %s" % (k, convert_opt(k, v)))
            return "\n".join(ret)
        return msg['reason']
