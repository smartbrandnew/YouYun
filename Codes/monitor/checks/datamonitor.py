import glob
import os
import re
import sys
import time
from datetime import datetime
from itertools import groupby

import modules
from checks import LaconicFilter
from util import windows_friendly_colon_split
from utils.tailfile import TailFile

if hasattr('some string', 'partition'):
    def partition(s, sep):
        return s.partition(sep)
else:
    def partition(s, sep):
        pos = s.find(sep)
        if pos == -1:
            return (s, sep, '')
        else:
            return s[0:pos], sep, s[pos + len(sep):]


def point_sorter(p):
    return (p[1], p[0], p[3].get('host_name', None), p[3].get('device_name', None))


class EventDefaults(object):
    EVENT_TYPE = 'monitorstream_event'
    EVENT_OBJECT = 'monitorstream_event:default'


class monitorstreams(object):
    @classmethod
    def init(cls, logger, config):
        monitorstreams_config = config.get('monitorstreams', None)
        if monitorstreams_config:
            monitorstreams = cls._instantiate_monitorstreams(logger, config, monitorstreams_config)
        else:
            monitorstreams = []

        logger.info("monitorstream parsers: %s" % repr(monitorstreams))

        return cls(logger, monitorstreams)

    def __init__(self, logger, monitorstreams):
        self.logger = logger
        self.monitorstreams = monitorstreams

    @classmethod
    def _instantiate_monitorstreams(cls, logger, config, monitorstreams_config):

        monitorstreams = []
        for config_item in monitorstreams_config.split(','):
            try:
                config_item = config_item.strip()
                parts = windows_friendly_colon_split(config_item)

                if len(parts) == 2:
                    logger.warn("Invalid monitorstream: %s" % ':'.join(parts))
                    continue

                log_path = cls._get_monitorstream_log_paths(parts[0]) if len(parts) else []
                parser_spec = ':'.join(parts[1:3]) if len(parts) >= 3 else None
                parser_args = parts[3:] if len(parts) >= 3 else None

                for path in log_path:
                    monitorstreams.append(monitorstream.init(
                        logger,
                        log_path=path,
                        parser_spec=parser_spec,
                        parser_args=parser_args,
                        config=config))
            except Exception:
                logger.exception("Cannot build monitorstream")

        return monitorstreams

    @classmethod
    def _get_monitorstream_log_paths(cls, path):
        if '*' not in path:
            return [path]
        return glob.glob(path)

    def check(self, agentConfig, move_end=True):
        if not self.monitorstreams:
            return {}

        output = {}
        for monitorstream in self.monitorstreams:
            try:
                result = monitorstream.check(agentConfig, move_end)
                assert type(result) == type(output), "monitorstream.check must return a dictionary"
                for k in result:
                    if k in output:
                        output[k].extend(result[k])
                    else:
                        output[k] = result[k]
            except Exception:
                self.logger.exception("Error in parsing %s" % (monitorstream.log_path))
        return output


class monitorstream(object):
    @classmethod
    def init(cls, logger, log_path, parser_spec=None, parser_args=None, config=None):
        class_based = False
        parse_func = None
        parse_args = tuple(parser_args or ())

        if parser_spec:
            try:
                parse_func = modules.load(parser_spec)
                if isinstance(parse_func, type):
                    logger.info('Instantiating class-based monitorstream')
                    parse_func = parse_func(
                        user_args=parse_args or (),
                        logger=logger,
                        log_path=log_path,
                        config=config,
                    )
                    parse_args = ()
                    class_based = True
                else:
                    logger.info('Instantiating function-based monitorstream')
            except Exception as e:
                logger.exception(e)
                logger.error('Could not load monitorstream line parser "%s" PYTHONPATH=%s' % (
                    parser_spec,
                    os.environ.get('PYTHONPATH', ''))
                             )
            logger.info("monitorstream: parsing %s with %s (requested %s)" % (log_path, parse_func, parser_spec))
        else:
            logger.info("monitorstream: parsing %s with default parser" % log_path)

        return cls(logger, log_path, parse_func, parse_args, class_based=class_based)

    def __init__(self, logger, log_path, parse_func=None, parse_args=(), class_based=False):
        self.logger = logger
        self.class_based = class_based

        self.logger.addFilter(LaconicFilter("monitorstream"))

        self.log_path = log_path
        self.parse_func = parse_func or self._default_line_parser
        self.parse_args = parse_args

        self._gen = None
        self._values = None
        self._freq = 15
        self._error_count = 0L
        self._line_count = 0L
        self.parser_state = {}

    def check(self, agentConfig, move_end=True):
        if self.log_path:
            self._freq = int(agentConfig.get('check_freq', 15))
            self._values = []
            self._events = []

            if self._gen is None:
                self._gen = TailFile(self.logger, self.log_path, self._line_parser).tail(line_by_line=False,
                                                                                         move_end=move_end)

            try:
                self._gen.next()
                self.logger.debug("Done monitorstream check for file {0}".format(self.log_path))
                self.logger.debug("Found {0} metric points".format(len(self._values)))
            except StopIteration, e:
                self.logger.exception(e)
                self.logger.warn("Can't tail %s file" % self.log_path)

            check_output = self._aggregate(self._values)
            if self._events:
                check_output.update({"monitorstreamEvents": self._events})
                self.logger.debug("Found {0} events".format(len(self._events)))
            return check_output
        else:
            return {}

    def _line_parser(self, line):
        try:
            parsed = None
            if self.class_based:
                parsed = self.parse_func.parse_line(line)
            else:
                try:
                    parsed = self.parse_func(self.logger, line, self.parser_state, *self.parse_args)
                except TypeError:
                    parsed = self.parse_func(self.logger, line)

            self._line_count += 1

            if parsed is None:
                return

            if isinstance(parsed, (tuple, dict)):
                parsed = [parsed]

            for datum in parsed:
                if isinstance(datum, dict):
                    if 'msg_title' not in datum and 'msg_text' not in datum:
                        continue

                    if 'event_type' not in datum:
                        datum['event_type'] = EventDefaults.EVENT_TYPE
                    if 'timestamp' not in datum:
                        datum['timestamp'] = time.time()

                    if 'event_object' in datum or 'aggregation_key' in datum:
                        datum['aggregation_key'] = datum.get('event_object', datum.get('aggregation_key'))
                    else:
                        datum['aggregation_key'] = EventDefaults.EVENT_OBJECT
                    datum['event_object'] = datum['aggregation_key']

                    self._events.append(datum)
                    continue

                try:
                    metric, ts, value, attrs = datum
                except Exception:
                    continue

                invalid_reasons = []
                try:

                    ts = (int(float(ts)) / self._freq) * self._freq
                    date = datetime.fromtimestamp(ts)
                    assert date.year > 1990
                except Exception:
                    invalid_reasons.append('invalid timestamp')

                try:
                    value = float(value)
                except Exception:
                    invalid_reasons.append('invalid metric value')

                if invalid_reasons:
                    self.logger.debug('Invalid parsed values %s (%s): "%s"',
                                      repr(datum), ', '.join(invalid_reasons), line)
                else:
                    self._values.append((metric, ts, value, attrs))
        except Exception:
            self.logger.debug("Error while parsing line %s" % line, exc_info=True)
            self._error_count += 1
            self.logger.error("Parser error: %s out of %s" % (self._error_count, self._line_count))

    def _default_line_parser(self, logger, line):
        sep = ' '
        metric, _, line = partition(line.strip(), sep)
        timestamp, _, line = partition(line.strip(), sep)
        value, _, line = partition(line.strip(), sep)

        attributes = {}
        try:
            while line:
                keyval, _, line = partition(line.strip(), sep)
                key, val = keyval.split('=', 1)
                attributes[key] = val
        except Exception:
            logger.debug(e)

        return metric, timestamp, value, attributes

    def _aggregate(self, values):

        output = []

        values.sort(key=point_sorter)

        for (timestamp, metric, host_name, device_name), val_attrs in groupby(values, key=point_sorter):
            attributes = {}
            vals = []
            for _metric, _timestamp, v, a in val_attrs:
                try:
                    v = float(v)
                    vals.append(v)
                    attributes.update(a)
                except Exception:
                    self.logger.debug("Could not convert %s into a float", v)

            if len(vals) == 1:
                val = vals[0]
            elif len(vals) > 1:
                val = vals[-1]
            else:
                continue

            metric_type = str(attributes.get('metric_type', '')).lower()
            if metric_type == 'gauge':
                val = float(val)
            elif metric_type == 'counter':
                val = sum(vals)

            output.append((metric, timestamp, val, attributes))

        if output:
            return {"monitorstream": output}
        else:
            return {}


class RollupLP:
    pass


class DdForwarder(object):
    QUEUE_SIZE = "queue_size"
    QUEUE_COUNT = "queue_count"

    RE_QUEUE_STAT = re.compile(r"\[.*\] Queue size: at (.*), (\d+) transaction\(s\), (\d+) KB")

    def __init__(self, logger, config):
        self.log_path = config.get('ddforwarder_log', '/var/log/ddforwarder.log')
        self.logger = logger
        self._gen = None

    def _init_metrics(self):
        self.metrics = {}

    def _add_metric(self, name, value, ts):

        if name in self.metrics:
            self.metrics[name].append((ts, value))
        else:
            self.metrics[name] = [(ts, value)]

    def _parse_line(self, line):

        try:
            m = self.RE_QUEUE_STAT.match(line)
            if m is not None:
                ts, count, size = m.groups()
                self._add_metric(self.QUEUE_SIZE, size, round(float(ts)))
                self._add_metric(self.QUEUE_COUNT, count, round(float(ts)))
        except Exception, e:
            self.logger.exception(e)

    def check(self, agentConfig, move_end=True):

        if self.log_path and os.path.isfile(self.log_path):

            self._init_metrics()

            if self._gen is None:
                self._gen = TailFile(self.logger, self.log_path, self._parse_line).tail(line_by_line=False,
                                                                                        move_end=move_end)

            try:
                self._gen.next()
                self.logger.debug("Done ddforwarder check for file %s" % self.log_path)
            except StopIteration, e:
                self.logger.exception(e)
                self.logger.warn("Can't tail %s file" % self.log_path)

            return {'ddforwarder': self.metrics}
        else:
            self.logger.debug("Can't tail datamonitor forwarder log file: %s" % self.log_path)
            return {}


def testddForwarder():
    import logging

    logger = logging.getLogger("monitoragent.checks.datamonitor")
    logger.setLevel(logging.DEBUG)
    logger.addHandler(logging.StreamHandler())

    config = {'api_key': 'my_apikey', 'ddforwarder_log': sys.argv[1]}
    dd = DdForwarder(logger, config)
    m = dd.check(config, move_end=False)
    while True:
        print m
        time.sleep(5)
        m = dd.check(config)


if __name__ == '__main__':
    testddForwarder()
