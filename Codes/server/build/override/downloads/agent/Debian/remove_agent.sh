export LANG=zh_CN.UTF-8
#!/bin/bash
# Datadog Agent remove script.
# pre

remove_py_compiled_files()
{
    # Delete all the .pyc files in the embedded dir that are part of the agent's package
    if [ -f "/opt/datadog-agent/embedded/.py_compiled_files.txt" ]; then
        # (commented lines are filtered out)
        cat /opt/datadog-agent/embedded/.py_compiled_files.txt | grep -v '^#' | xargs sudo rm -f
    fi
}


	if [ -f "/etc/init.d/datadog-agent" ]; then
            # We're uninstalling.
            sudo /etc/init.d/datadog-agent stop

            remove_py_compiled_files
	else
		echo "[ ${Red}FAILED ${RCol}] Your system do not install dd-agent.";
		exit 1;
	fi


# Delete all.pyc files in the agent's dir
find /opt/datadog-agent/agent -name '*.py[co]' -type f -delete || echo 'Unable to delete .pyc files'

# post
        # We're uninstalling.
        sudo getent passwd dd-agent > /dev/null && userdel dd-agent
        sudo getent group dd-agent >/dev/null && groupdel dd-agent
        sudo rm -rf /opt/datadog-agent
        sudo rm -rf /var/log/datadog
        sudo rm -rf /etc/dd-agent
        sudo rm -rf /var/log/datadog
		sudo rm -rf /etc/init.d/datadog-agent
