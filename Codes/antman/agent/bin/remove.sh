#!/usr/bin/env bash
ANT_BIN_DIR=$(cd "$(dirname "$0")"; pwd)
ANT_ROOT_DIR=$(dirname "$ANT_BIN_DIR")
ANT_DIR=$(dirname "$ANT_ROOT_DIR")
ANT_STOP=$ANT_BIN_DIR/stop.sh
ANT_UPGRADE_STOP=$ANT_BIN_DIR/stop_upgrade.sh
FORCE=$1


remove(){
    cd "$ANT_ROOT_DIR"
    "$ANT_STOP"
    "$ANT_UPGRADE_STOP"
    rm -rf "$ANT_ROOT_DIR" /etc/init.d/ant-agent
    rm -rf "$ANT_ROOT_DIR" /etc/init.d/ant-upgrade
    result=$(echo $ANT_DIR | grep "uyun-ant")
    if [[ "$result" != "" ]]
    then
        rm -rf "$ANT_DIR"
    fi
}


if [ "$FORCE" = "-f" ]
then
    remove
else
    read -p "Are you sure to remove Agent?(yes/no)"
    if [ "$REPLY" = "yes" ];then
       remove
    else
        echo "exit"
    fi
fi
