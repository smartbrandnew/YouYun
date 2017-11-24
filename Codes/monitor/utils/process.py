import errno
import inspect
import os

try:
    import psutil
except ImportError:
    psutil = None

from utils.platform import Platform


def is_my_process(pid):
    pid_existence = pid_exists(pid)

    if not psutil or not pid_existence:
        return pid_existence

    if Platform.is_windows():
        return True
    else:
        try:
            command = psutil.Process(pid).cmdline() or []
        except psutil.Error:
            return False
        exec_name = os.path.basename(inspect.stack()[-1][1]).lower()
        return len(command) > 1 and exec_name in command[1].lower()


def pid_exists(pid):
    if psutil:
        return psutil.pid_exists(pid)

    if Platform.is_windows():
        import ctypes
        kernel32 = ctypes.windll.kernel32
        synchronize = 0x100000

        process = kernel32.OpenProcess(synchronize, 0, pid)
        if process != 0:
            kernel32.CloseHandle(process)
            return True
        else:
            return False

    if pid == 0:
        return True
    try:
        os.kill(pid, 0)
    except OSError as err:
        if err.errno == errno.ESRCH:
            return False
        elif err.errno == errno.EPERM:
            return True
        else:
            raise err
    else:
        return True
