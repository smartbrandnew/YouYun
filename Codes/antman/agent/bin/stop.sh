#!/usr/bin/env bash
ANT_BIN_DIR=$(cd "$(dirname "$0")"; pwd)
ANT_ROOT_DIR=$(dirname "$ANT_BIN_DIR")
ANT_PYTHON=$ANT_ROOT_DIR/embedded/bin/python

cd "$ANT_ROOT_DIR"
"$ANT_PYTHON" "$ANT_BIN_DIR/circlectl" quit --wait
