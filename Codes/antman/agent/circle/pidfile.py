import errno
import os
import stat
import tempfile


class Pidfile(object):
    """
    Manage a PID file. If a specific name is provided
    it and '"%s.oldpid" % name' will be used. Otherwise
    we create a temp file using tempfile.mkstemp.
    """

    def __init__(self, fpath):
        self.fpath = fpath
        self.pid = os.getpid()
        # set permissions to -rw-r--r--
        self.perm_mode = (stat.S_IRUSR | stat.S_IWUSR | stat.S_IRGRP |
                          stat.S_IROTH)

    def create(self, pid):
        pid = int(pid)
        oldpid = self.validate()
        if oldpid:
            if oldpid == pid:
                return
            raise RuntimeError(
                'pid file "{0}" (pid: {1}) is stale, current pid {2}'
                .format(self.fpath, oldpid, pid))

        self.pid = pid

        # Write pidfile
        if self.fpath:
            fdir = os.path.dirname(self.fpath)
            if fdir and not os.path.isdir(fdir):
                raise RuntimeError("{0} doesn't exist. Can't create pidfile"
                                   .format(fdir))
            flags = os.O_CREAT | os.O_WRONLY | os.O_TRUNC
            fd = os.open(self.fpath, flags, self.perm_mode)
        else:
            fd, self.fpath = tempfile.mkstemp()

        os.chmod(self.fpath, self.perm_mode)
        os.write(fd, '{0}\n'.format(self.pid).encode('utf-8'))
        os.fsync(fd)
        os.close(fd)

    def rename(self, path):
        self.unlink()
        self.fpath = path
        self.create(self.pid)

    def unlink(self):
        """ delete pidfile"""
        try:
            with open(self.fpath, 'r') as f:
                try:
                    pid1 = int(f.read() or 0)
                except ValueError:
                    pid1 = self.pid

            if pid1 == self.pid:
                os.unlink(self.fpath)
        except:
            pass

    def validate(self):
        """ Validate pidfile and make it stale if needed"""
        if not self.fpath:
            return
        try:
            with open(self.fpath, 'r') as f:
                try:
                    wpid = int(f.read() or 0)
                except ValueError:
                    return

                if wpid <= 0:
                    return

                try:
                    os.kill(wpid, 0)
                    return wpid
                except OSError as e:
                    if e.args[0] == errno.ESRCH:
                        return
                    raise
        except ValueError:
            return
        except IOError as e:
            if e.args[0] == errno.ENOENT:
                return
            raise
