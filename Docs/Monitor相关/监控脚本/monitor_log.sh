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


echo "-------"monitor log-------""
declare -a datapoints_array
for keyword in $3
do
    num=`cat $1$2 | grep $keyword | wc -l`
    generate_datapoint $HOST_ID $HOSTNAME 'app.log.'$keyword $num '["host:'$HOSTNAME'","'file:$1$2'"]' 'gauge'
    datapoints_array=("${datapoints_array[@]}" $datapoint)
done
generate_json "${datapoints_array[*]}"
post_json "$datapoints"
echo $datapoints