import snakebite.client
import snakebite.version

from checks import AgentCheck

try:

    from snakebite.namenode import Namenode
except ImportError:
    Namenode = None

DEFAULT_PORT = 8020


class HDFSCheck(AgentCheck):
    def get_client(self, instance):

        if 'namenode' in instance:
            host, port = instance['namenode'], instance.get('port', DEFAULT_PORT)
            return snakebite.client.Client(host, port)

        if type(instance['namenodes']) != list or len(instance['namenodes']) == 0:
            raise ValueError('"namenodes parameter should be a list of dictionaries.')

        for namenode in instance['namenodes']:
            if type(namenode) != dict:
                raise ValueError('"namenodes parameter should be a list of dictionaries.')

            if "url" not in namenode:
                raise ValueError('Each namenode should specify a "url" parameter.')

        if len(instance['namenodes']) == 1:
            host, port = instance['namenodes'][0]['url'], instance['namenodes'][0].get('port', DEFAULT_PORT)
            return snakebite.client.Client(host, port)

        else:
            if Namenode is None:
                self.warning("HA Mode is not available with snakebite < 2.2.0"
                             "Upgrade to the latest version of snakebiteby running: "
                             "sudo /opt/datamonitor-agent/embedded/bin/pip install --upgrade snakebite")

                host, port = instance['namenodes'][0]['url'], instance['namenodes'][0].get('port', DEFAULT_PORT)
                return snakebite.client.Client(host, port)
            else:
                self.log.debug("Running in HA Mode")
                nodes = []
                for namenode in instance['namenodes']:
                    nodes.append(Namenode(namenode['url'], namenode.get('port', DEFAULT_PORT)))

                return snakebite.client.HAClient(nodes)

    def check(self, instance):
        self.warning('The "hdfs" check is deprecated and will be removed '
                     'in a future version of the agent. Please use the "hdfs_namenode" '
                     'and "hdfs_datanode" checks instead')

        if 'namenode' not in instance and 'namenodes' not in instance:
            raise ValueError('Missing key \'namenode\' in HDFSCheck config')

        tags = instance.get('tags', None)

        hdfs = self.get_client(instance)
        stats = hdfs.df()

        self.gauge('hdfs.used', stats['used'], tags=tags)
        self.gauge('hdfs.free', stats['remaining'], tags=tags)
        self.gauge('hdfs.capacity', stats['capacity'], tags=tags)
        self.gauge('hdfs.in_use', float(stats['used']) /
                   float(stats['capacity']), tags=tags)
        self.gauge('hdfs.under_replicated', stats['under_replicated'],
                   tags=tags)
        self.gauge('hdfs.missing_blocks', stats['missing_blocks'], tags=tags)
        self.gauge('hdfs.corrupt_blocks', stats['corrupt_blocks'], tags=tags)
