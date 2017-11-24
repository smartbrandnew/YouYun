import binascii
import os
from stat import ST_INO, ST_SIZE


class TailFile(object):
    CRC_SIZE = 16

    def __init__(self, logger, path, callback):
        self._path = path
        self._f = None
        self._inode = None
        self._size = 0
        self._crc = None
        self._log = logger
        self._callback = callback

    def _open_file(self, move_end=False, pos=False):

        already_open = False
        if self._f is not None:
            self._f.close()
            self._f = None
            already_open = True

        stat = os.stat(self._path)
        inode = stat[ST_INO]
        size = stat[ST_SIZE]

        crc = None
        if size >= self.CRC_SIZE:
            tmp_file = open(self._path, 'r')
            data = tmp_file.read(self.CRC_SIZE)
            crc = binascii.crc32(data)

        if already_open:
            if self._inode is not None and inode != self._inode:
                self._log.debug("File removed, reopening")
                move_end = False
                pos = False

            elif self._size > 0 and size < self._size:
                self._log.debug("File truncated, reopening")
                move_end = False
                pos = False

            if size >= self.CRC_SIZE and self._crc is not None and crc != self._crc:
                self._log.debug("Begining of file modified, reopening")
                move_end = False
                pos = False

        self._inode = inode
        self._size = size
        self._crc = crc

        self._f = open(self._path, 'r')
        if move_end:
            self._log.debug("Opening file %s" % (self._path))
            self._f.seek(0, os.SEEK_END)
        elif pos:
            self._log.debug("Reopening file %s at %s" % (self._path, pos))
            self._f.seek(pos)

        return True

    def tail(self, line_by_line=True, move_end=True):
        try:
            self._open_file(move_end=move_end)

            while True:
                pos = self._f.tell()
                line = self._f.readline()
                if line:
                    line = line.strip(chr(0))
                    if self._callback(line.rstrip("\n")):
                        if line_by_line:
                            yield True
                            pos = self._f.tell()
                            self._open_file(move_end=False, pos=pos)
                        else:
                            continue
                    else:
                        continue
                else:
                    yield True
                    assert pos == self._f.tell()
                    self._open_file(move_end=False, pos=pos)

        except Exception, e:
            # log but survive
            self._log.exception(e)
            raise StopIteration(e)
