# coding: utf-8
import argparse
import cmd
import getopt
import json
import logging
import os
import shlex
import sys
import textwrap
import traceback

# import pygments if here
try:
    import pygments  # NOQA
    from pygments.lexers import get_lexer_for_mimetype
    from pygments.formatters import TerminalFormatter
except ImportError:
    pygments = False  # NOQA

from circle import __version__
from circle.client import CircleClient
from circle.commands import get_commands
from circle.errors import CallError, ArgumentError, TimeOutError, MessageError
from circle.constants import DEFAULT_ENDPOINT_DEALER

USAGE = 'circlectl [options] command [args]'
VERSION = 'Circlectl Version: {}'.format(__version__)
TIMEOUT_MSG = """

A time out usually happens in one of those cases:

#1 The circle daemon could not be reached.
#2 The circle daemon took too long to perform the operation

For #1, make sure you are hitting the right place
by checking your --endpoint option.

For #2, if you are not expecting a result to
come back, increase your timeout option value
(particularly with waiting switches)
"""


def prettify(jsonobj, prettify=True):
    """ prettiffy JSON output """
    if not prettify:
        return json.dumps(jsonobj)

    json_str = json.dumps(jsonobj, indent=2, sort_keys=True)
    if pygments:
        try:
            lexer = get_lexer_for_mimetype('application/json')
            return pygments.highlight(json_str, lexer, TerminalFormatter())
        except:
            pass

    return json_str


def _get_switch_str(opt):
    """
    Output just the '-r, --rev [VAL]' part of the option string.
    """
    if opt[2] is None or opt[2] is True or opt[2] is False:
        default = ''
    else:
        default = '[VAL]'
    if opt[0]:
        # has a short and long option
        return '-{}, --{} {}'.format(opt[0], opt[1], default)
    else:
        # only has a long option
        return '--{} {}'.format(opt[1], default)


class ControllerRunner(object):

    def __init__(self, commands, client):
        self.commands = commands
        self.client = client

    def run(self, args):
        opts = {}
        command = self.commands[args.command]
        for option in command.options:
            name = option[1]
            if name in args:
                opts[name] = getattr(args, name)

        if hasattr(args, 'start'):
            opts['start'] = args.start

        if args.endpoint is None and command.msg_type != 'dealer':
            args.endpoint = DEFAULT_ENDPOINT_DEALER

        msg = command.message(*args.args, **opts)
        return self.handle_dealer(msg)

    def handle_dealer(self, msg):
        if isinstance(msg, list):
            # Fix me, should handle every return msg, not only one
            for c in msg:
                return self._handle_msg(self.client, c['msg'])
        else:
            return self._handle_msg(self.client, msg)

    def _handle_msg(self, client, msg):
        ret_msg = client.call(msg)
        if ret_msg.get('status') == 'error':
            reason = ret_msg.get('reason')
            if 'time out' in reason:
                raise TimeOutError(TIMEOUT_MSG)
            elif 'invalid' in reason:
                raise MessageError(reason)
            else:
                raise CallError(reason)
        return ret_msg


class ControllerApp(object):

    def __init__(self, commands, client=None):
        self.commands = commands
        self.client = client

    def run(self, args):
        try:
            return self.dispatch(args)
        except getopt.GetoptError as e:
            print('Error: {}\n'.format(str(e)))
            self.display_help()
            return 2
        except CallError as e:
            sys.stderr.write('{}\n'.format(str(e)))
            return 1
        except ArgumentError as e:
            sys.stderr.write('{}\n'.format(str(e)))
            return 1
        except KeyboardInterrupt:
            return 1
        except Exception:
            sys.stderr.write(traceback.format_exc())
            return 1

    def dispatch(self, args):
        opts = {}
        command = self.commands[args.command]
        for option in command.options:
            name = option[1]
            if name in args:
                opts[name] = getattr(args, name)

        if args.help:
            print(textwrap.dedent(command.__doc__))
            return 0
        else:
            if hasattr(args, 'start'):
                opts['start'] = args.start

            if args.endpoint is None and command.msg_type != 'dealer':
                args.endpoint = DEFAULT_ENDPOINT_DEALER

            msg = command.message(*args.args, **opts)
            return self.handle_dealer(command, self.globalopts, msg,
                                      args.endpoint, args.timeout)

    def _console(self, client, command, opts, msg):
        if opts['json']:
            return prettify(client.call(msg), prettify=opts['prettify'])
        else:
            return command.console_msg(client.call(msg))

    def handle_dealer(self, command, opts, msg, endpoint, timeout):
        if endpoint is not None:
            client = CircleClient(endpoint=endpoint, timeout=timeout)
        else:
            client = self.client

        try:
            if isinstance(msg, list):
                for i, c in enumerate(msg):
                    clm = self._console(client, c['cmd'], opts, c['msg'])
                    print('%s: %s' % (i, clm))
            else:
                print(self._console(client, command, opts, msg))
        except CallError as e:
            msg = str(e)
            if 'timed out' in str(e).lower():
                msg += TIMEOUT_MSG
            sys.stderr.write(msg)
            return 1
        finally:
            if endpoint is not None:
                client.stop()

        return 0


class CircleCtl(cmd.Cmd, object):
    """CircleCtl tool."""
    prompt = '(circlectl) '

    def __new__(cls, client, commands, *args, **kw):
        """Auto add do and complete methods for all known commands."""
        cls.commands = commands
        cls.controller = ControllerApp(commands, client)
        cls.client = client
        for name, command in commands.items():
            cls._add_do_cmd(name, command)
            cls._add_complete_cmd(name, command)
        return super(CircleCtl, cls).__new__(cls, *args, **kw)

    def __init__(self, client, *args, **kwargs):
        super(CircleCtl, self).__init__()

    @classmethod
    def _add_do_cmd(cls, cmd_name, command):

        def inner_do_cmd(cls, line):
            arguments = parse_arguments([cmd_name] + shlex.split(line),
                                        cls.commands)
            cls.controller.run(arguments['args'])

        inner_do_cmd.__doc__ = textwrap.dedent(command.__doc__)
        inner_do_cmd.__name__ = 'do_{}'.format(cmd_name)
        setattr(cls, inner_do_cmd.__name__, inner_do_cmd)

    @classmethod
    def _add_complete_cmd(cls, cmd_name, command):

        def inner_complete_cmd(cls, *args, **kwargs):
            if hasattr(command, 'autocomplete'):
                try:
                    return command.autocomplete(cls.client, *args, **kwargs)
                except Exception as e:
                    sys.stderr.write(str(e) + '\n')
                    traceback.print_exc(file=sys.stderr)
            else:
                return []

        inner_complete_cmd.__doc__ = 'Complete the {} command'.format(cmd_name)
        inner_complete_cmd.__name__ = 'complete_{}'.format(cmd_name)
        setattr(cls, inner_complete_cmd.__name__, inner_complete_cmd)

    def do_EOF(self, line):
        return True

    def postloop(self):
        sys.stdout.write('\n')

    def autocomplete(self, autocomplete=False, words=None, cword=None):
        """
        Output completion suggestions for BASH.

        The output of this function is passed to BASH's `COMREPLY` variable and
        treated as completion suggestions. `COMREPLY` expects a space
        separated string as the result.

        The `COMP_WORDS` and `COMP_CWORD` BASH environment variables are used
        to get information about the cli input. Please refer to the BASH
        man-page for more information about this variables.

        Subcommand options are saved as pairs. A pair consists of
        the long option string (e.g. '--exclude') and a boolean
        value indicating if the option requires arguments. When printing to
        stdout, a equal sign is appended to options which require arguments.

        Note: If debugging this function, it is recommended to write the debug
        output in a separate file. Otherwise the debug output will be treated
        and formatted as potential completion suggestions.
        """
        autocomplete = autocomplete or 'AUTO_COMPLETE' in os.environ

        # Don't complete if user hasn't sourced bash_completion file.
        if not autocomplete:
            return

        words = words or os.environ['COMP_WORDS'].split()[1:]
        cword = cword or int(os.environ['COMP_CWORD'])

        try:
            curr = words[cword - 1]
        except IndexError:
            curr = ''

        subcommands = get_commands()

        if cword == 1:  # if completing the command name
            print(' '.join(
                sorted([x for x in subcommands if x.startswith(curr)])))
        sys.exit(1)

    def start(self, globalopts):
        self.autocomplete()

        self.controller.globalopts = globalopts

        args = globalopts['args']
        parser = globalopts['parser']

        if hasattr(args, 'command'):
            sys.exit(self.controller.run(globalopts['args']))

        if args.help:
            for command in sorted(self.commands.keys()):
                doc = textwrap.dedent(self.commands[command].__doc__)
                help = doc.split('\n')[0]
                parser.add_argument(command, help=help)
            parser.print_help()
            sys.exit(0)

        # no command, no --help: enter the CLI
        print(VERSION)
        self.do_status('')
        try:
            self.cmdloop()
        except KeyboardInterrupt:
            sys.stdout.write('\n')
        sys.exit(0)


class _Help(argparse.HelpFormatter):

    commands = None

    def _metavar_formatter(self, action, default_metavar):
        if action.dest != 'command':
            return super(_Help, self)._metavar_formatter(action,
                                                         default_metavar)

        commands = sorted(self.commands.items())
        max_len = max([len(name) for name, help in commands])

        output = []
        for name, command in commands:
            output.append('\t%-*s\t%s' % (max_len, name, command.short))

        def format(tuple_size):
            res = '\n'.join(output)
            return (res,) * tuple_size

        return format

    def start_section(self, heading):
        if heading == 'positional arguments':
            heading = 'Commands'
        super(_Help, self).start_section(heading)


def parse_arguments(args, commands):
    _Help.commands = commands

    options = {
        'endpoint': {
            'default': None,
            'help': 'connection endpoint'
        },
        'timeout': {
            'default': 60,
            'help': 'connection timeout',
            'type': int
        },
        'help': {
            'default': False,
            'action': 'store_true',
            'help': 'Show help and exit'
        },
        'json': {
            'default': False,
            'action': 'store_true',
            'help': 'output to JSON'
        },
        'prettify': {
            'default': False,
            'action': 'store_true',
            'help': 'prettify output'
        },
        'version': {
            'default': False,
            'action': 'version',
            'version': VERSION,
            'help': 'display version and exit'
        },
    }

    parser = argparse.ArgumentParser(
        description='Controls a circle daemon',
        formatter_class=_Help,
        usage=USAGE,
        add_help=False)

    for option in sorted(options.keys()):
        parser.add_argument('--' + option, **options[option])

    if any([value in commands for value in args]):
        subparsers = parser.add_subparsers(dest='command')

        for command, klass in commands.items():

            subparser = subparsers.add_parser(command)
            subparser.add_argument('args', nargs='*', help=argparse.SUPPRESS)
            for option in klass.options:
                __, name, default, desc = option
                if isinstance(default, bool):
                    action = 'store_true'
                else:
                    action = 'store'

                subparser.add_argument(
                    '--' + name, action=action, default=default, help=desc)

    args = parser.parse_args(args)

    opts = {'args': args, 'parser': parser}
    for option in options:
        opts[option] = getattr(args, option)
    return opts


def handle_cli():
    logging.basicConfig()
    commands = get_commands()
    opts = parse_arguments(sys.argv[1:], commands)
    if opts['endpoint'] is None:
        opts['endpoint'] = os.environ.get('CIRCUSCTL_ENDPOINT',
                                          DEFAULT_ENDPOINT_DEALER)
    client = CircleClient(endpoint=opts['endpoint'], timeout=opts['timeout'])

    CircleCtl(client, commands).start(opts)
