_counter_type_calculators = {}


class UndefinedCalculator(Exception):
    pass


def calculator(counter_type):
    def set_calculator(func):
        _counter_type_calculators[counter_type] = func
        return func

    return set_calculator


def get_calculator(counter_type):
    try:
        return _counter_type_calculators[counter_type]
    except KeyError:
        raise UndefinedCalculator


def get_raw(previous, current, property_name):
    return current[property_name]


@calculator(65536)
def calculate_perf_counter_rawcount(previous, current, property_name):
    return current[property_name]


@calculator(65792)
def calculate_perf_counter_large_rawcount(previous, current, property_name):
    return current[property_name]


@calculator(542180608)
def calculate_perf_100nsec_timer(previous, current, property_name):
    n0 = previous[property_name]
    n1 = current[property_name]
    d0 = previous["Timestamp_Sys100NS"]
    d1 = current["Timestamp_Sys100NS"]

    if n0 is None or n1 is None:
        return

    return (n1 - n0) / (d1 - d0) * 100


@calculator(272696576)
def calculate_perf_counter_bulk_count(previous, current, property_name):
    n0 = previous[property_name]
    n1 = current[property_name]
    d0 = previous["Timestamp_Sys100NS"]
    d1 = current["Timestamp_Sys100NS"]
    f = current["Frequency_Sys100NS"]

    if n0 is None or n1 is None:
        return

    return (n1 - n0) / ((d1 - d0) / f)


@calculator(272696320)
def calculate_perf_counter_counter(previous, current, property_name):
    n0 = previous[property_name]
    n1 = current[property_name]
    d0 = previous["Timestamp_Sys100NS"]
    d1 = current["Timestamp_Sys100NS"]
    f = current["Frequency_Sys100NS"]

    if n0 is None or n1 is None:
        return

    return (n1 - n0) / ((d1 - d0) / f)
