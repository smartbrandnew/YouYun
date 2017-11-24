export LANG=zh_CN.UTF-8
#!/bin/bash
# Monitor Agent remove script.
# pre
LINUX_DISTRIBUTION=$(grep -Eo "(Debian|Ubuntu|RedHat|CentOS|openSUSE|Amazon)" /etc/issue)

remove_py_compiled_files()
{
    # Delete all the .pyc files in the embedded dir that are part of the agent's package
    if [ -f "/opt/datadog-agent/embedded/.py_compiled_files.txt" ]; then
        # (commented lines are filtered out)
        cat /opt/datadog-agent/embedded/.py_compiled_files.txt | grep -v '^#' | xargs rm -f
    fi
}


if [ -f "/etc/redhat-release" ] || [ "$LINUX_DISTRIBUTION" == "RedHat" ] || [ "$LINUX_DISTRIBUTION" == "CentOS" ] || [ "$LINUX_DISTRIBUTION" == "openSUSE" ] || [ "$LINUX_DISTRIBUTION" == "Amazon" ]; then
	if [ -f "/etc/init.d/datamonitor-agent" ]; then
            # We're uninstalling.
            /etc/init.d/datamonitor-agent stop

            remove_py_compiled_files
	else
		echo "[ ${Red}FAILED ${RCol}] Your system is currently not supported by this script.";
		exit 1;
	fi
else
    echo "[ ${Red}FAILED ${RCol}] Your system is currently not supported by this script.";
    exit 1;
fi

# Delete all.pyc files in the agent's dir
find /opt/datadog-agent/agent -name '*.py[co]' -type f -delete || echo 'Unable to delete .pyc files'

# post
if [ -f "/etc/redhat-release" ] || [ "$LINUX_DISTRIBUTION" == "RedHat" ] || [ "$LINUX_DISTRIBUTION" == "CentOS" ] || [ "$LINUX_DISTRIBUTION" == "openSUSE" ] || [ "$LINUX_DISTRIBUTION" == "Amazon" ]; then
        # We're uninstalling.
        getent passwd m-agent > /dev/null && userdel m-agent
        getent group m-agent >/dev/null && groupdel m-agent
        rm -rf /opt/datadog-agent
        rm -rf /var/log/datamonitor
        rm -rf /etc/monitor-agent
		rm -rf /etc/init.d/datamonitor-agent
else
      echo "[ ${Red}FAILED ${RCol}] Your system is currently not supported by this script.";
      exit 1;
fi
exit 0
