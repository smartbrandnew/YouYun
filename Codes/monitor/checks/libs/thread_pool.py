import Queue
import sys
import threading

SENTINEL = "QUIT"


def is_sentinel(obj):
    return type(obj) is str and obj == SENTINEL


class TimeoutError(Exception):
    pass


class PoolWorker(threading.Thread):
    def __init__(self, workq, *args, **kwds):
        threading.Thread.__init__(self, *args, **kwds)
        self._workq = workq
        self.running = False

    def run(self):
        while 1:
            self.running = True
            workunit = self._workq.get()
            if is_sentinel(workunit):
                break

            workunit.process()
        self.running = False


class Pool(object):
    def __init__(self, nworkers, name="Pool"):

        self._workq = Queue.Queue()
        self._closed = False
        self._workers = []
        for idx in xrange(nworkers):
            thr = PoolWorker(self._workq, name="Worker-%s-%d" % (name, idx))
            try:
                thr.start()
            except:

                self.terminate()
                raise
            else:
                self._workers.append(thr)

    def get_nworkers(self):
        return len([w for w in self._workers if w.running])

    def apply(self, func, args=(), kwds=dict()):

        return self.apply_async(func, args, kwds).get()

    def map(self, func, iterable, chunksize=None):

        return self.map_async(func, iterable, chunksize).get()

    def imap(self, func, iterable, chunksize=1):

        collector = OrderedResultCollector(as_iterator=True)
        self._create_sequences(func, iterable, chunksize, collector)
        return iter(collector)

    def imap_unordered(self, func, iterable, chunksize=1):

        collector = UnorderedResultCollector()
        self._create_sequences(func, iterable, chunksize, collector)
        return iter(collector)

    def apply_async(self, func, args=(), kwds=dict(), callback=None):

        assert not self._closed
        apply_result = ApplyResult(callback=callback)
        job = Job(func, args, kwds, apply_result)
        self._workq.put(job)
        return apply_result

    def map_async(self, func, iterable, chunksize=None, callback=None):

        apply_result = ApplyResult(callback=callback)
        collector = OrderedResultCollector(apply_result, as_iterator=False)
        self._create_sequences(func, iterable, chunksize, collector)
        return apply_result

    def imap_async(self, func, iterable, chunksize=None, callback=None):

        apply_result = ApplyResult(callback=callback)
        collector = OrderedResultCollector(apply_result, as_iterator=True)
        self._create_sequences(func, iterable, chunksize, collector)
        return apply_result

    def imap_unordered_async(self, func, iterable, chunksize=None,
                             callback=None):

        apply_result = ApplyResult(callback=callback)
        collector = UnorderedResultCollector(apply_result)
        self._create_sequences(func, iterable, chunksize, collector)
        return apply_result

    def close(self):

        self._closed = True

    def terminate(self):

        self.close()

        try:
            while 1:
                self._workq.get_nowait()
        except Queue.Empty:
            pass

        for thr in self._workers:
            self._workq.put(SENTINEL)

    def join(self):

        for thr in self._workers:
            thr.join()

    def _create_sequences(self, func, iterable, chunksize, collector=None):

        assert not self._closed
        sequences = []
        results = []
        it_ = iter(iterable)
        exit_loop = False
        while not exit_loop:
            seq = []
            for i in xrange(chunksize or 1):
                try:
                    arg = it_.next()
                except StopIteration:
                    exit_loop = True
                    break
                apply_result = ApplyResult(collector)
                job = Job(func, (arg,), {}, apply_result)
                seq.append(job)
                results.append(apply_result)
            sequences.append(JobSequence(seq))

        for seq in sequences:
            self._workq.put(seq)

        return sequences


class WorkUnit(object):
    def process(self):
        raise NotImplementedError("Children must override Process")


class Job(WorkUnit):
    def __init__(self, func, args, kwds, apply_result):

        WorkUnit.__init__(self)
        self._func = func
        self._args = args
        self._kwds = kwds
        self._result = apply_result

    def process(self):

        try:
            result = self._func(*self._args, **self._kwds)
        except:
            self._result._set_exception()
        else:
            self._result._set_value(result)


class JobSequence(WorkUnit):
    def __init__(self, jobs):
        WorkUnit.__init__(self)
        self._jobs = jobs

    def process(self):
        for job in self._jobs:
            job.process()


class ApplyResult(object):
    def __init__(self, collector=None, callback=None):

        self._success = False
        self._event = threading.Event()
        self._data = None
        self._collector = None
        self._callback = callback

        if collector is not None:
            collector.register_result(self)
            self._collector = collector

    def get(self, timeout=None):

        if not self.wait(timeout):
            raise TimeoutError("Result not available within %fs" % timeout)
        if self._success:
            return self._data
        raise self._data[0], self._data[1], self._data[2]

    def wait(self, timeout=None):

        self._event.wait(timeout)
        return self._event.isSet()

    def ready(self):

        return self._event.isSet()

    def successful(self):

        assert self.ready()
        return self._success

    def _set_value(self, value):

        assert not self.ready()
        self._data = value
        self._success = True
        self._event.set()
        if self._collector is not None:
            self._collector.notify_ready(self)
        if self._callback is not None:
            try:
                self._callback(value)
            except:
                pass

    def _set_exception(self):

        assert not self.ready()
        self._data = sys.exc_info()
        self._success = False
        self._event.set()
        if self._collector is not None:
            self._collector.notify_ready(self)


class AbstractResultCollector(object):
    def __init__(self, to_notify):
        self._to_notify = to_notify

    def register_result(self, apply_result):
        raise NotImplementedError("Children classes must implement it")

    def notify_ready(self, apply_result):
        raise NotImplementedError("Children classes must implement it")

    def _get_result(self, idx, timeout=None):
        raise NotImplementedError("Children classes must implement it")

    def __iter__(self):
        return CollectorIterator(self)


class CollectorIterator(object):
    def __init__(self, collector):
        self._collector = collector
        self._idx = 0

    def __iter__(self):
        return self

    def next(self, timeout=None):

        try:
            apply_result = self._collector._get_result(self._idx, timeout)
        except IndexError:

            self._idx = 0
            raise StopIteration
        except:
            self._idx = 0
            raise
        self._idx += 1
        assert apply_result.ready()
        return apply_result.get(0)


class UnorderedResultCollector(AbstractResultCollector):
    def __init__(self, to_notify=None):

        AbstractResultCollector.__init__(self, to_notify)
        self._cond = threading.Condition()
        self._collection = []
        self._expected = 0

    def register_result(self, apply_result):

        self._expected += 1

    def _get_result(self, idx, timeout=None):

        self._cond.acquire()
        try:
            if idx >= self._expected:
                raise IndexError
            elif idx < len(self._collection):
                return self._collection[idx]
            elif idx != len(self._collection):

                raise IndexError()
            else:
                self._cond.wait(timeout=timeout)
                try:
                    return self._collection[idx]
                except IndexError:

                    raise TimeoutError("Timeout while waiting for results")
        finally:
            self._cond.release()

    def notify_ready(self, apply_result):

        first_item = False
        self._cond.acquire()
        try:
            self._collection.append(apply_result)
            first_item = (len(self._collection) == 1)

            self._cond.notifyAll()
        finally:
            self._cond.release()

        if first_item and self._to_notify is not None:
            self._to_notify._set_value(iter(self))


class OrderedResultCollector(AbstractResultCollector):
    def __init__(self, to_notify=None, as_iterator=True):

        AbstractResultCollector.__init__(self, to_notify)
        self._results = []
        self._lock = threading.Lock()
        self._remaining = 0
        self._as_iterator = as_iterator

    def register_result(self, apply_result):

        self._results.append(apply_result)
        self._remaining += 1

    def _get_result(self, idx, timeout=None):

        res = self._results[idx]
        res.wait(timeout)
        return res

    def notify_ready(self, apply_result):

        got_first = False
        got_last = False
        self._lock.acquire()
        try:
            assert self._remaining > 0
            got_first = (len(self._results) == self._remaining)
            self._remaining -= 1
            got_last = (self._remaining == 0)
        finally:
            self._lock.release()

        if self._to_notify is not None:
            if self._as_iterator and got_first:
                self._to_notify._set_value(iter(self))
            elif not self._as_iterator and got_last:
                try:
                    lst = [r.get(0) for r in self._results]
                except:
                    self._to_notify._set_exception()
                else:
                    self._to_notify._set_value(lst)


def _test():
    import thread
    import time

    def f(x):
        return x * x

    def work(seconds):
        print "[%d] Start to work for %fs..." % (thread.get_ident(), seconds)
        time.sleep(seconds)
        print "[%d] Work done (%fs)." % (thread.get_ident(), seconds)
        return "%d slept %fs" % (thread.get_ident(), seconds)

    pool = Pool(9)

    result = pool.apply_async(f, (10,))
    print result.get(timeout=1)

    print pool.map(f, range(10))

    it = pool.imap(f, range(10))
    print it.next()
    print it.next()
    print it.next(timeout=1)

    result = pool.apply_async(time.sleep, (3,))
    try:
        print result.get(timeout=1)
    except TimeoutError:
        print "Good. Got expected timeout exception."
    else:
        assert False, "Expected exception !"
    print result.get()

    def cb(s):
        print "Result ready: %s" % s

    for res in pool.imap(work, xrange(10, 3, -1), chunksize=4):
        print "Item:", res

    for res in pool.imap_unordered(work, xrange(10, 3, -1)):
        print "Item:", res

    result = pool.map_async(work, xrange(10), callback=cb)
    try:
        print result.get(timeout=1)
    except TimeoutError:
        print "Good. Got expected timeout exception."
    else:
        assert False, "Expected exception !"
    print result.get()

    result = pool.imap_async(work, xrange(3, 10), callback=cb)
    try:
        print result.get(timeout=1)
    except TimeoutError:
        print "Good. Got expected timeout exception."
    else:
        assert False, "Expected exception !"
    for i in result.get():
        print "Item:", i
    print "### Loop again:"
    for i in result.get():
        print "Item2:", i

    result = pool.imap_unordered_async(work, xrange(10, 3, -1), callback=cb)
    try:
        print result.get(timeout=1)
    except TimeoutError:
        print "Good. Got expected timeout exception."
    else:
        assert False, "Expected exception !"
    for i in result.get():
        print "Item1:", i
    for i in result.get():
        print "Item2:", i
    r = result.get()
    for i in r:
        print "Item3:", i
    for i in r:
        print "Item4:", i
    for i in r:
        print "Item5:", i

    result = pool.imap_unordered_async(work, xrange(2, -10, -1), callback=cb)
    time.sleep(3)
    try:
        for i in result.get():
            print "Got item:", i
    except IOError:
        print "Good. Got expected exception:"

    result = pool.imap_async(work, xrange(2, -10, -1), callback=cb)
    time.sleep(3)
    try:
        for i in result.get():
            print "Got item:", i
    except IOError:
        print "Good. Got expected exception:"

    pool.terminate()
    print "End of tests"


if __name__ == "__main__":
    _test()
