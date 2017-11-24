#!/usr/bin/env bash


if [ -n "$ANT_M_API_KEY" ]; then
    apikey=$ANT_M_API_KEY
else
    printf "API key not available in ANT_M_API_KEY environment variable."
    exit 1;
fi

if [ -n "$ANT_M_URL" ]; then
    m_url=$ANT_M_URL/monitor/api/v2/gateway/dd-agent
else
    printf "API key not available in ANT_M_URL environment variable."
    exit 1;
fi

if [ -n "$ANT_NTP" ]; then
    ntp=$ANT_NTP
fi

if [ $(echo "$UID") = "0" ]; then
    sudo_cmd=''
else
    sudo_cmd='sudo'
fi

monitor='monitor'
if [[ $ANT_M_URL =~ $monitor ]]
then
    m_url=${ANT_M_URL}api/v2/gateway/dd-agent
else
    m_url=$ANT_M_URL/monitor/api/v2/gateway/dd-agent
fi


printf "\033[34m\n* Adding your API key to the Agent configuration: ./conf/datamonitor.conf\n\033[0m\n"
sh -c "sed 's/{apikey}/$apikey/g' ./conf/datamonitor_template > ./conf/datamonitor.conf"
sh -c "sed -i 's%{m_url}%$m_url%g' ./conf/datamonitor.conf"
