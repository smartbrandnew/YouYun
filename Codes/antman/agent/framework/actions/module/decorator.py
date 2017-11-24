# coding: utf-8


class LazyProperty(object):
    """
    LazyProperty decorator can make property lazy computed.
    It means computation will occur when first get property.
    """

    def __init__(self, func):
        self.func = func

    def __get__(self, instance, class_):
        if instance is None:
            return self
        else:
            value = self.func(instance)
            setattr(instance, self.func.__name__, value)
            return value
