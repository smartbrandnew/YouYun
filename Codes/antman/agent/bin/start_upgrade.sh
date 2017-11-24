#!/usr/bin/env bash
ANT_BIN_DIR=$(cd "$(dirname "$0")"; pwd)
ANT_ROOT_DIR=$(dirname "$ANT_BIN_DIR")
ANT_PYTHON=$ANT_ROOT_DIR/.embedded/bin/python

cd "$ANT_ROOT_DIR"
nohup "$ANT_PYTHON" -m upgrade > /dev/null 2>&1 &
