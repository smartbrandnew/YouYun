#!/bin/bash
check_stat() {
    soft=$1
    shift
    for p in $@
    do
        port=$p
        for i in {1..10}
        do
            echo "Detect whether the $soft $port port is on listening, the number of retries: $i"
            sleep 10

            stat=`ss -anp | grep :$port | grep LISTEN`
            if [ "$stat" != "" ]; then
                echo "$soft $port port is on listening!"
                break;
            fi
            if [ $i -eq 10 ]; then
                echo "$soft $port port isn't on listening,exit!"
                exit 1
            fi
        done
        echo
    done
}