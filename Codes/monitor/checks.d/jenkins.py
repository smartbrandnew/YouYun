import os
import time
from collections import defaultdict
from glob import glob
from xml.etree.ElementTree import ElementTree

from checks import AgentCheck
from util import get_hostname


class Skip(Exception):
    def __init__(self, reason, dir_name):
        message = 'skipping build or job at %s because %s' % (dir_name, reason)
        Exception.__init__(self, message)


class Jenkins(AgentCheck):
    datetime_format = '%Y-%m-%d_%H-%M-%S'

    def __init__(self, name, init_config, agentConfig):
        AgentCheck.__init__(self, name, init_config, agentConfig)
        self.high_watermarks = {}

    def _timestamp_from_build_file(self, dir_name, tree):
        timestamp = tree.find('timestamp')
        if timestamp is None or not timestamp.text:
            raise Skip('the timestamp cannot be found', dir_name)
        else:
            return int(timestamp.text) / 1000.0

    def _timestamp_from_dirname(self, dir_name):
        if not os.path.isdir(dir_name):
            raise Skip('its not a build directory', dir_name)

        try:
            date_str = os.path.basename(dir_name)
            time_tuple = time.strptime(date_str, self.datetime_format)
            return time.mktime(time_tuple)
        except ValueError:
            return None

    def _get_build_metadata(self, dir_name, watermark):
        if os.path.exists(os.path.join(dir_name, 'jenkins_build.tar.gz')):
            raise Skip('the build has already been archived', dir_name)
        timestamp = self._timestamp_from_dirname(dir_name)
        if timestamp is not None and timestamp <= watermark:
            return None
        build_metadata = os.path.join(dir_name, 'build.xml')

        if not os.access(build_metadata, os.R_OK):
            self.log.debug("Can't read build file at %s" % (build_metadata))
            raise Exception("Can't access build.xml at %s" % (build_metadata))
        else:
            tree = ElementTree()
            tree.parse(build_metadata)
            if timestamp is None:
                try:
                    timestamp = self._timestamp_from_build_file(dir_name, tree)
                    if timestamp <= watermark:
                        return None
                except ValueError:
                    return None
            keys = ['result', 'number', 'duration']

            kv_pairs = ((k, tree.find(k)) for k in keys)
            d = dict([(k, v.text) for k, v in kv_pairs if v is not None])
            d['timestamp'] = timestamp

            try:
                d['branch'] = tree.find('actions') \
                    .find('hudson.plugins.git.util.BuildData') \
                    .find('buildsByBranchName') \
                    .find('entry') \
                    .find('hudson.plugins.git.util.Build') \
                    .find('revision') \
                    .find('branches') \
                    .find('hudson.plugins.git.Branch') \
                    .find('name') \
                    .text
            except Exception:
                pass
            return d

    def _get_build_results(self, instance_key, job_dir):
        job_name = os.path.basename(job_dir)
        try:
            dirs = glob(os.path.join(job_dir, 'builds', '*_*'))
            if len(dirs) == 0:
                dirs = glob(os.path.join(job_dir, 'builds', '[0-9]*'))
            if len(dirs) > 0:
                try:
                    dirs = sorted(dirs, key=lambda x: int(x.split('/')[-1]), reverse=True)
                except ValueError:
                    dirs = sorted(dirs, reverse=True)
                for dir_name in dirs:
                    watermark = self.high_watermarks[instance_key][job_name]
                    try:
                        build_metadata = self._get_build_metadata(dir_name, watermark)
                    except Exception:
                        build_metadata = None
                    if build_metadata is not None:
                        build_result = build_metadata.get('result')
                        if build_result is None:
                            break

                        output = {
                            'job_name': job_name,
                            'event_type': 'build result'
                        }

                        output.update(build_metadata)
                        if 'number' not in output:
                            output['number'] = dir_name.split('/')[-1]
                        self.high_watermarks[instance_key][job_name] = output.get('timestamp')
                        self.log.debug("Processing %s results '%s'" % (job_name, output))
                        yield output

                    else:
                        break
        except Exception, e:
            self.log.error("Error while working on job %s, exception: %s" % (job_name, e))

    def check(self, instance, create_event=True):
        if self.high_watermarks.get(instance.get('name'), None) is None:
            self.high_watermarks[instance.get('name')] = defaultdict(lambda: 0)
            self.check(instance, create_event=False)

        jenkins_home = instance.get('jenkins_home')

        if not jenkins_home:
            raise Exception("No jenkins_home directory set in the config file")

        jenkins_jobs_dir = os.path.join(jenkins_home, 'jobs', '*')
        job_dirs = glob(jenkins_jobs_dir)

        if not job_dirs:
            raise Exception('No jobs found in `%s`! '
                            'Check `jenkins_home` in your config' % (jenkins_jobs_dir))

        for job_dir in job_dirs:
            for output in self._get_build_results(instance.get('name'), job_dir):
                output['host'] = get_hostname(self.agentConfig)
                if create_event:
                    self.log.debug("Creating event for job: %s" % output['job_name'])
                    self.event(output)

                    tags = [
                        'job_name:%s' % output['job_name'],
                        'result:%s' % output['result'],
                        'build_number:%s' % output['number']
                    ]

                    if 'branch' in output:
                        tags.append('branch:%s' % output['branch'])
                    self.gauge("jenkins.job.duration", float(output['duration']) / 1000.0, tags=tags)

                    if output['result'] == 'SUCCESS':
                        self.increment('jenkins.job.success', tags=tags)
                    else:
                        self.increment('jenkins.job.failure', tags=tags)