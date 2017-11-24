#!/usr/bin/env bash
ANT_BIN_DIR=$(cd "$(dirname "$0")"; pwd)
ANT_ROOT_DIR=$(dirname "$ANT_BIN_DIR")
ANT_PYTHON=$ANT_ROOT_DIR/.embedded/bin/python

ps -ef | grep "$ANT_PYTHON"|grep "upgrade" | grep -v grep | awk '{{print $2}}' | xargs kill -9
