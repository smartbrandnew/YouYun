import time
from fnmatch import fnmatch
from os import stat, walk
from os.path import abspath, exists, join

from checks import AgentCheck
from config import _is_affirmative


class DirectoryCheck(AgentCheck):
    SOURCE_TYPE_NAME = 'system'

    def check(self, instance):
        if "directory" not in instance:
            raise Exception('DirectoryCheck: missing "directory" in config')

        directory = instance["directory"]
        abs_directory = abspath(directory)
        name = instance.get("name", directory)
        pattern = instance.get("pattern", "*")
        recursive = _is_affirmative(instance.get("recursive", False))
        dirtagname = instance.get("dirtagname", "name")
        filetagname = instance.get("filetagname", "filename")
        filegauges = _is_affirmative(instance.get("filegauges", False))

        if not exists(abs_directory):
            raise Exception("DirectoryCheck: the directory (%s) does not exist" % abs_directory)

        self._get_stats(abs_directory, name, dirtagname, filetagname, filegauges, pattern, recursive)

    def _get_stats(self, directory, name, dirtagname, filetagname, filegauges, pattern, recursive):
        dirtags = [dirtagname + ":%s" % name]
        directory_bytes = 0
        directory_files = 0
        for root, dirs, files in walk(directory):
            for filename in files:
                filename = join(root, filename)
                if not fnmatch(filename, pattern):
                    continue
                try:
                    file_stat = stat(filename)

                except OSError, ose:
                    self.warning("DirectoryCheck: could not stat file %s - %s" % (filename, ose))
                else:
                    directory_files += 1
                    directory_bytes += file_stat.st_size
                    if filegauges and directory_files <= 20:
                        filetags = list(dirtags)
                        filetags.append(filetagname + ":%s" % filename)
                        self.gauge("system.disk.directory.file.bytes", file_stat.st_size, tags=filetags)
                        self.gauge("system.disk.directory.file.modified_sec_ago", time.time() - file_stat.st_mtime,
                                   tags=filetags)
                        self.gauge("system.disk.directory.file.created_sec_ago", time.time() - file_stat.st_ctime,
                                   tags=filetags)
                    elif not filegauges:
                        self.histogram("system.disk.directory.file.bytes", file_stat.st_size, tags=dirtags)
                        self.histogram("system.disk.directory.file.modified_sec_ago", time.time() - file_stat.st_mtime,
                                       tags=dirtags)
                        self.histogram("system.disk.directory.file.created_sec_ago", time.time() - file_stat.st_ctime,
                                       tags=dirtags)

            if not recursive:
                break

        self.gauge("system.disk.directory.files", directory_files, tags=dirtags)
        self.gauge("system.disk.directory.bytes", directory_bytes, tags=dirtags)
