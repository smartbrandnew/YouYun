#!/bin/bash

HOST_ID='c0ba787fc33d517d8cf51aa5000b8c59'
HOSTNAME='localhost.localdomain'
server_base_url="http://10.1.53.101/monitor"
apikey="e10adc3949ba59abbe56e057f2gg88dd"

LINUX_DISTRIBUTION=$(grep -Eo "(Debian|Ubuntu|RedHat|CentOS)" /etc/issue)
if [ "$LINUX_DISTRIBUTION"x == "Ubuntu"x ]; then
    OS="Debian"
elif [ "$LINUX_DISTRIBUTION"x == "Debian"x ]; then
    OS="Debian"
else
    OS="RedHat"
fi

function generate_datapoint(){
    current=`date "+%Y-%m-%d %H:%M:%S"`
    timeStamp=`date -d "$current" +%s`
    datapoint="{\"hostId\":\"$1\",\"host\":\"$2\",\"metric\":\"$3\",\"timestamp\":\"$timeStamp\",\"value\":$4,\"tags\":$5,\"type\":\"$6\"}"
}

function generate_json(){
    datapoints="["
    local datapoints_array=($1)
    datapoints_count=${#datapoints_array[*]}
    for ((i=0; i<$datapoints_count; i++));  
    do  
        datapoint=${datapoints_array[$i]}
        datapoints="$datapoints$datapoint,"
    done
    result_count=`echo ${#datapoints}`
    temp=`expr $result_count - 1`
    datapoints=`echo ${datapoints%,*}`
    datapoints="$datapoints]"
}

function post_json(){
    curl -X POST "$server_base_url/openapi/v2/datapoints?api_key=$apikey" -H Content-type:application/json --data $1
}


function monitor_service_debian(){
    echo "-------monitor service debian-------"
}

function monitor_service_redhat(){
    echo "-------monitor service redhat-------"
    declare -a datapoints_array
    declare -a running_service
    sname_list=`systemctl list-units --type=service | awk '{print $1}'`
    sname_list_all=`ls -lh  /usr/lib/systemd/system | grep ^-r | awk '{print $9}'`
    for sname in $sname_list
    do
        sstatus=`systemctl list-units --type=service | grep $sname | awk '{print $4}'`
        if [ "$sstatus"x = 'running'x ]; then
            generate_datapoint $HOST_ID $HOSTNAME 'system.service.state' 1 '["host:'$HOSTNAME'","service:'$sname'"]' 'gauge'
            datapoints_array=("${datapoints_array[@]}" $datapoint)
            running_service=("${running_service[@]}" $sname)
        fi
    done
    for sname in $sname_list_all
    do
        if echo "${running_service[@]}" | grep -w $sname &>/dev/null; then
            continue
        else
            generate_datapoint $HOST_ID $HOSTNAME 'system.service.state' 0 '["host:'$HOSTNAME'","service:'$sname'"]' 'gauge'
            datapoints_array=("${datapoints_array[@]}" $datapoint)
        fi
    done

    generate_json "${datapoints_array[*]}"
    post_json "$datapoints"
}


if [ $OS = "Debian" ]; then
    monitor_service_debian 
else
    monitor_service_redhat 
fi