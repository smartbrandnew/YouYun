#!/bin/bash

HOST_ID='c0ba787fc33d517d8cf51aa5000b8c59'
HOSTNAME='localhost.localdomain'
server_base_url="http://10.1.53.101/monitor"
apikey="e10adc3949ba59abbe56e057f2gg88dd"

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


echo "-------"monitor file-------""
declare -a datapoints_array

cd $1
if [ "$2"x = ""x ];then
    file_num=`ls -lh |wc -l`
elif [ "$2"x = "all"x ];then
    file_num=`ls -lR|grep "^-"|wc -l`
fi
generate_datapoint $HOST_ID $HOSTNAME "system.file.number" $file_num '["host:'$HOSTNAME'","file:'$1'"]' 'gauge'
datapoints_array[0]=$datapoint
generate_json "${datapoints_array[*]}"
post_json "$datapoints"

echo $datapoints