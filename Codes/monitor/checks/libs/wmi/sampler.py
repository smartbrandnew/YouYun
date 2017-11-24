from copy import deepcopy
from itertools import izip

import pythoncom
import pywintypes
from win32com.client import Dispatch

from checks.libs.wmi.counter_type import get_calculator, get_raw, UndefinedCalculator
from utils.timeout import timeout, TimeoutException


class CaseInsensitiveDict(dict):
    def __setitem__(self, key, value):
        super(CaseInsensitiveDict, self).__setitem__(key.lower(), value)

    def __getitem__(self, key):
        return super(CaseInsensitiveDict, self).__getitem__(key.lower())

    def __contains__(self, key):
        return super(CaseInsensitiveDict, self).__contains__(key.lower())

    def get(self, key):
        return super(CaseInsensitiveDict, self).get(key.lower())


class WMISampler(object):
    def __init__(self, logger, class_name, property_names, filters="", host="localhost",
                 namespace="root\\cimv2", username="", password="", and_props=[], timeout_duration=10):
        self.logger = logger
        self.host = host
        self.namespace = namespace
        self.username = username
        self.password = password

        self.is_raw_perf_class = "_PERFRAWDATA_" in class_name.upper()

        if self.is_raw_perf_class:
            property_names.extend([
                "Timestamp_Sys100NS",
                "Frequency_Sys100NS",
            ])

        self.class_name = class_name
        self.property_names = property_names
        self.filters = filters
        self._and_props = and_props
        self._formatted_filters = None
        self.property_counter_types = None
        self._timeout_duration = timeout_duration
        self._query = timeout(timeout_duration)(self._query)
        self.current_sample = None
        self.previous_sample = None
        self._sampling = False

    @property
    def connection(self):
        return {
            'host': self.host,
            'namespace': self.namespace,
            'username': self.username,
            'password': self.password,
        }

    @property
    def connection_key(self):
        return "{host}:{namespace}:{username}".format(
            host=self.host,
            namespace=self.namespace,
            username=self.username
        )

    @property
    def formatted_filters(self):
        if not self._formatted_filters:
            filters = deepcopy(self.filters)
            self._formatted_filters = self._format_filter(filters, self._and_props)
        return self._formatted_filters

    def sample(self):
        self._sampling = True

        try:
            if self.is_raw_perf_class and not self.previous_sample:
                self.logger.debug(u"Querying for initial sample for raw performance counter.")
                self.current_sample = self._query()

            self.previous_sample = self.current_sample
            self.current_sample = self._query()
        except TimeoutException:
            self.logger.debug(
                u"Query timeout after {timeout}s".format(
                    timeout=self._timeout_duration
                )
            )
            raise
        else:
            self._sampling = False
            self.logger.debug(u"Sample: {0}".format(self.current_sample))

    def __len__(self):
        if self._sampling:
            raise TypeError(
                u"Sampling `WMISampler` object has no len()"
            )

        return len(self.current_sample)

    def __iter__(self):
        if self._sampling:
            raise TypeError(
                u"Sampling `WMISampler` object is not iterable"
            )

        if self.is_raw_perf_class:
            for previous_wmi_object, current_wmi_object in \
                    izip(self.previous_sample, self.current_sample):
                formatted_wmi_object = self._format_property_values(
                    previous_wmi_object,
                    current_wmi_object
                )
                yield formatted_wmi_object
        else:
            for wmi_object in self.current_sample:
                yield wmi_object

    def __getitem__(self, index):
        if self.is_raw_perf_class:
            previous_wmi_object = self.previous_sample[index]
            current_wmi_object = self.current_sample[index]
            formatted_wmi_object = self._format_property_values(
                previous_wmi_object,
                current_wmi_object
            )
            return formatted_wmi_object
        else:
            return self.current_sample[index]

    def __eq__(self, other):
        return self.current_sample == other

    def __str__(self):
        return str(self.current_sample)

    def _get_property_calculator(self, counter_type):
        calculator = get_raw
        try:
            calculator = get_calculator(counter_type)
        except UndefinedCalculator:
            self.logger.warning(
                u"Undefined WMI calculator for counter_type {counter_type}."
                " Values are reported as RAW.".format(
                    counter_type=counter_type
                )
            )

        return calculator

    def _format_property_values(self, previous, current):
        formatted_wmi_object = CaseInsensitiveDict()

        for property_name, property_raw_value in current.iteritems():
            counter_type = self.property_counter_types.get(property_name)
            property_formatted_value = property_raw_value

            if counter_type:
                calculator = self._get_property_calculator(counter_type)
                property_formatted_value = calculator(previous, current, property_name)

            formatted_wmi_object[property_name] = property_formatted_value

        return formatted_wmi_object

    def get_connection(self):
        self.logger.debug(
            u"Connecting to WMI server "
            u"(host={host}, namespace={namespace}, username={username}).".format(
                host=self.host,
                namespace=self.namespace,
                username=self.username
            )
        )

        pythoncom.CoInitialize()
        locator = Dispatch("WbemScripting.SWbemLocator")
        connection = locator.ConnectServer(
            self.host, self.namespace,
            self.username, self.password
        )

        return connection

    @staticmethod
    def _format_filter(filters, and_props=[]):
        def build_where_clause(fltr):
            f = fltr.pop()
            wql = ""
            while f:
                prop, value = f.popitem()

                if isinstance(value, tuple):
                    oper = value[0]
                    value = value[1]
                elif isinstance(value, basestring) and '%' in value:
                    oper = 'LIKE'
                else:
                    oper = '='

                if isinstance(value, list):
                    if not len(value):
                        continue

                    internal_filter = map(lambda x:
                                          (prop, x) if isinstance(x, tuple)
                                          else (prop, ('LIKE', x)) if '%' in x
                                          else (prop, (oper, x)), value)

                    bool_op = ' OR '
                    for p in and_props:
                        if p.lower() in prop.lower():
                            bool_op = ' AND '
                            break

                    clause = bool_op.join(['{0} {1} \'{2}\''.format(k, v[0], v[1]) if isinstance(v, tuple)
                                           else '{0} = \'{1}\''.format(k, v)
                                           for k, v in internal_filter])

                    if bool_op.strip() == 'OR':
                        wql += "( {clause} )".format(
                            clause=clause)
                    else:
                        wql += "{clause}".format(
                            clause=clause)

                else:
                    wql += "{property} {cmp} '{constant}'".format(
                        property=prop,
                        cmp=oper,
                        constant=value)
                if f:
                    wql += " AND "

            if wql.endswith(" AND "):
                wql = wql[:-5]

            if len(fltr) == 0:
                return "( {clause} )".format(clause=wql)

            return "( {clause} ) OR {more}".format(
                clause=wql,
                more=build_where_clause(fltr)
            )

        if not filters:
            return ""

        return " WHERE {clause}".format(clause=build_where_clause(filters))

    def _query(self):
        formated_property_names = ",".join(self.property_names)
        wql = "Select {property_names} from {class_name}{filters}".format(
            property_names=formated_property_names,
            class_name=self.class_name,
            filters=self.formatted_filters,
        )
        self.logger.debug(u"Querying WMI: {0}".format(wql))

        try:
            flag_return_immediately = 0x10  # Default flag.
            flag_forward_only = 0x20
            flag_use_amended_qualifiers = 0x20000

            query_flags = flag_return_immediately | flag_forward_only

            includes_qualifiers = self.is_raw_perf_class and self.property_counter_types is None
            if includes_qualifiers:
                self.property_counter_types = CaseInsensitiveDict()
                query_flags |= flag_use_amended_qualifiers

            raw_results = self.get_connection().ExecQuery(wql, "WQL", query_flags)

            results = self._parse_results(raw_results, includes_qualifiers=includes_qualifiers)

        except pywintypes.com_error:
            self.logger.warning(u"Failed to execute WMI query (%s)", wql, exc_info=True)
            results = []

        return results

    def _parse_results(self, raw_results, includes_qualifiers):
        results = []
        for res in raw_results:
            item = CaseInsensitiveDict()
            for prop_name in self.property_names:
                item[prop_name] = None

            for wmi_property in res.Properties_:
                should_get_qualifier_type = (
                    includes_qualifiers and
                    wmi_property.Name not in self.property_counter_types
                )

                if should_get_qualifier_type:

                    qualifiers = dict((q.Name, q.Value) for q in wmi_property.Qualifiers_)

                    if "CounterType" in qualifiers:
                        counter_type = qualifiers["CounterType"]
                        self.property_counter_types[wmi_property.Name] = counter_type

                        self.logger.debug(
                            u"Caching property qualifier CounterType: "
                            "{class_name}.{property_names} = {counter_type}"
                                .format(
                                class_name=self.class_name,
                                property_names=wmi_property.Name,
                                counter_type=counter_type,
                            )
                        )
                    else:
                        self.logger.debug(
                            u"CounterType qualifier not found for {class_name}.{property_names}"
                                .format(
                                class_name=self.class_name,
                                property_names=wmi_property.Name,
                            )
                        )

                try:
                    item[wmi_property.Name] = float(wmi_property.Value)
                except (TypeError, ValueError):
                    item[wmi_property.Name] = wmi_property.Value

            results.append(item)
        return results
