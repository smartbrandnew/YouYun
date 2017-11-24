#!/bin/sh

PATH=/opt/datamonitor-agent/embedded/bin:/opt/datamonitor-agent/bin:$PATH

exec /opt/datamonitor-agent/bin/supervisord -c /opt/datadog-agent/conf/supervisor.conf
