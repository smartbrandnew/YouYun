from circle.commands.base import Command


class ReloadConfig(Command):
    """\
        Reload the configuration file
        =============================

        This command reloads the configuration file, so changes in the
        configuration file will be reflected in the configuration of
        circle.


        TCP Message
        -----------

        ::

            {
                "command": "reloadconfig",
                "properties": {
                    "path": "<path>",
                    "waiting": False
                }
            }

        The response return the status "ok". If the property graceful is
        set to true the processes will be exited gracefully.


        Command line
        ------------

        ::

            $ circlectl reloadconfig [<path>] [--waiting]

    """
    name = "reloadconfig"
    options = Command.waiting_options

    def message(self, *args, **opts):
        if len(args) == 1:
            return self.make_message(path=args[0], **opts)
        return self.make_message(**opts)

    def execute(self, arbiter, props):
        return arbiter.reload_from_config(props.get('path', None))
