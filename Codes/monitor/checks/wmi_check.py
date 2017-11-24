from collections import namedtuple

from checks import AgentCheck
from checks.libs.wmi.sampler import WMISampler

WMIMetric = namedtuple('WMIMetric', ['name', 'value', 'tags'])


class InvalidWMIQuery(Exception):
    pass


class MissingTagBy(Exception):
    pass


class TagQueryUniquenessFailure(Exception):
    pass


class WinWMICheck(AgentCheck):
    def __init__(self, name, init_config, agentConfig, instances):
        AgentCheck.__init__(self, name, init_config, agentConfig, instances)
        self.wmi_samplers = {}
        self.wmi_props = {}

    def _format_tag_query(self, sampler, wmi_obj, tag_query):

        try:
            link_source_property = int(wmi_obj[tag_query[0]])
            target_class = tag_query[1]
            link_target_class_property = tag_query[2]
            target_property = tag_query[3]
        except IndexError:
            self.log.error(
                u"Wrong `tag_queries` parameter format. "
                "Please refer to the configuration file for more information.")
            raise
        except TypeError:
            self.log.error(
                u"Incorrect 'link source property' in `tag_queries` parameter:"
                " `{wmi_property}` is not a property of `{wmi_class}`".format(
                    wmi_property=tag_query[0],
                    wmi_class=sampler.class_name,
                )
            )
            raise

        return target_class, target_property, [{link_target_class_property: link_source_property}]

    def _raise_on_invalid_tag_query_result(self, sampler, wmi_obj, tag_query):

        target_property = sampler.property_names[0]
        target_class = sampler.class_name

        if len(sampler) != 1:
            message = "no result was returned"
            if len(sampler):
                message = "multiple results returned (one expected)"

            self.log.warning(
                u"Failed to extract a tag from `tag_queries` parameter: {reason}."
                " wmi_object={wmi_obj} - query={tag_query}".format(
                    reason=message,
                    wmi_obj=wmi_obj, tag_query=tag_query,
                )
            )
            raise TagQueryUniquenessFailure

        if sampler[0][target_property] is None:
            self.log.error(
                u"Incorrect 'target property' in `tag_queries` parameter:"
                " `{wmi_property}` is not a property of `{wmi_class}`".format(
                    wmi_property=target_property,
                    wmi_class=target_class,
                )
            )
            raise TypeError

    def _get_tag_query_tag(self, sampler, wmi_obj, tag_query):

        self.log.debug(
            u"`tag_queries` parameter found."
            " wmi_object={wmi_obj} - query={tag_query}".format(
                wmi_obj=wmi_obj, tag_query=tag_query,
            )
        )

        target_class, target_property, filters = \
            self._format_tag_query(sampler, wmi_obj, tag_query)

        tag_query_sampler = WMISampler(
            self.log,
            target_class, [target_property],
            filters=filters,
            **sampler.connection
        )

        tag_query_sampler.sample()

        self._raise_on_invalid_tag_query_result(tag_query_sampler, wmi_obj, tag_query)

        link_value = str(tag_query_sampler[0][target_property]).lower()

        tag = "{tag_name}:{tag_value}".format(
            tag_name=target_property.lower(),
            tag_value="_".join(link_value.split())
        )

        self.log.debug(u"Extracted `tag_queries` tag: '{tag}'".format(tag=tag))
        return tag

    def _extract_metrics(self, wmi_sampler, tag_by, tag_queries, constant_tags):
        if len(wmi_sampler) > 1 and not tag_by:
            raise MissingTagBy(
                u"WMI query returned multiple rows but no `tag_by` value was given."
                " class={wmi_class} - properties={wmi_properties} - filters={filters}".format(
                    wmi_class=wmi_sampler.class_name, wmi_properties=wmi_sampler.property_names,
                    filters=wmi_sampler.filters,
                )
            )

        metrics = []
        tag_by = tag_by.lower()

        for wmi_obj in wmi_sampler:
            tags = list(constant_tags) if constant_tags else []

            for query in tag_queries:
                try:
                    tags.append(self._get_tag_query_tag(wmi_sampler, wmi_obj, query))
                except TagQueryUniquenessFailure:
                    continue

            for wmi_property, wmi_value in wmi_obj.iteritems():
                if wmi_property == tag_by:
                    tag_value = str(wmi_value).lower()
                    if tag_queries and tag_value.find("#") > 0:
                        tag_value = tag_value[:tag_value.find("#")]

                    tags.append(
                        "{name}:{value}".format(
                            name=tag_by, value=tag_value
                        )
                    )
                    continue

                if wmi_property == 'name':
                    continue

                try:
                    metrics.append(WMIMetric(wmi_property, float(wmi_value), tags))
                except ValueError:
                    self.log.warning(u"When extracting metrics with WMI, found a non digit value"
                                     " for property '{0}'.".format(wmi_property))
                    continue
                except TypeError:
                    self.log.warning(u"When extracting metrics with WMI, found a missing property"
                                     " '{0}'".format(wmi_property))
                    continue
        return metrics

    def _submit_metrics(self, metrics, metric_name_and_type_by_property):

        for metric in metrics:
            if metric.name not in metric_name_and_type_by_property:
                continue

            metric_name, metric_type = metric_name_and_type_by_property[metric.name]
            try:
                func = getattr(self, metric_type.lower())
            except AttributeError:
                raise Exception(u"Invalid metric type: {0}".format(metric_type))

            func(metric_name, metric.value, metric.tags)

    def _get_instance_key(self, host, namespace, wmi_class, other=None):

        if other:
            return "{host}:{namespace}:{wmi_class}-{other}".format(
                host=host, namespace=namespace, wmi_class=wmi_class, other=other
            )

        return "{host}:{namespace}:{wmi_class}".format(
            host=host, namespace=namespace, wmi_class=wmi_class,
        )

    def _get_wmi_sampler(self, instance_key, wmi_class, properties, tag_by="", **kwargs):

        properties = properties + [tag_by] if tag_by else properties

        if instance_key not in self.wmi_samplers:
            wmi_sampler = WMISampler(self.log, wmi_class, properties, **kwargs)
            self.wmi_samplers[instance_key] = wmi_sampler

        return self.wmi_samplers[instance_key]

    def _get_wmi_properties(self, instance_key, metrics, tag_queries):

        if instance_key not in self.wmi_props:
            metric_name_by_property = dict(
                (wmi_property.lower(), (metric_name, metric_type))
                for wmi_property, metric_name, metric_type in metrics
            )
            properties = map(lambda x: x[0], metrics + tag_queries)
            self.wmi_props[instance_key] = (metric_name_by_property, properties)

        return self.wmi_props[instance_key]


def from_time(year=None, month=None, day=None, hours=None, minutes=None, seconds=None, microseconds=None,
              timezone=None):
    def str_or_stars(i, length):
        if i is None:
            return "*" * length
        else:
            return str(i).rjust(length, "0")

    wmi_time = ""
    wmi_time += str_or_stars(year, 4)
    wmi_time += str_or_stars(month, 2)
    wmi_time += str_or_stars(day, 2)
    wmi_time += str_or_stars(hours, 2)
    wmi_time += str_or_stars(minutes, 2)
    wmi_time += str_or_stars(seconds, 2)
    wmi_time += "."
    wmi_time += str_or_stars(microseconds, 6)
    if timezone is None:
        wmi_time += "+"
    else:
        try:
            int(timezone)
        except ValueError:
            wmi_time += "+"
        else:
            if timezone >= 0:
                wmi_time += "+"
            else:
                wmi_time += "-"
                timezone = abs(timezone)
                wmi_time += str_or_stars(timezone, 3)

    return wmi_time


def to_time(wmi_time):
    def int_or_none(s, start, end):
        try:
            return int(s[start:end])
        except ValueError:
            return None

    year = int_or_none(wmi_time, 0, 4)
    month = int_or_none(wmi_time, 4, 6)
    day = int_or_none(wmi_time, 6, 8)
    hours = int_or_none(wmi_time, 8, 10)
    minutes = int_or_none(wmi_time, 10, 12)
    seconds = int_or_none(wmi_time, 12, 14)
    microseconds = int_or_none(wmi_time, 15, 21)
    timezone = wmi_time[22:]

    if timezone == "***":
        timezone = None

    return year, month, day, hours, minutes, seconds, microseconds, timezone
