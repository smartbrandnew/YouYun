import os

from checks import AgentCheck
from utils.subprocess_output import get_subprocess_output


class PostfixCheck(AgentCheck):
    def check(self, instance):
        config = self._get_config(instance)

        directory = config['directory']
        queues = config['queues']
        tags = config['tags']

        self._get_queue_count(directory, queues, tags)

    def _get_config(self, instance):
        directory = instance.get('directory', None)
        queues = instance.get('queues', None)
        tags = instance.get('tags', [])
        if not queues or not directory:
            raise Exception('missing required yaml config entry')

        instance_config = {
            'directory': directory,
            'queues': queues,
            'tags': tags,
        }

        return instance_config

    def _get_queue_count(self, directory, queues, tags):
        for queue in queues:
            queue_path = os.path.join(directory, queue)
            if not os.path.exists(queue_path):
                raise Exception('%s does not exist' % queue_path)

            count = 0
            if os.geteuid() == 0:

                count = sum(len(files) for root, dirs, files in os.walk(queue_path))
            else:

                test_sudo = os.popen('setsid sudo -l < /dev/null').read()
                if test_sudo == 0:
                    output, _, _ = get_subprocess_output(['sudo', 'find', queue_path, '-type', 'f'], self.log)
                    count = len(output.splitlines())
                else:
                    raise Exception('The monitor-agent user does not have sudo access')

            self.gauge('postfix.queue.size', count,
                       tags=tags + ['queue:%s' % queue, 'instance:%s' % os.path.basename(directory)])
