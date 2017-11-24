#!/bin/bash

INSTALL_DIR=/opt/uyun/platform/dispatcher
ROOT_DIR=$(pwd)
local_node=/usr/local/bin/node
local_pm2=/usr/local/bin/pm2
EXCLUDE_DIRS=('module.yaml', 'install.py', 'install.sh', 'circle',
                'circle-conf', 'mix', 'INTERFACE.md')

if [ ! -d ${INSTALL_DIR}'/logs' ];
then
    mkdir -p ${INSTALL_DIR}'/logs'
fi

for dir in $(ls $ROOT_DIR)
do
    if [[ "${EXCLUDE_DIRS[@]}" =~ $dir ]];
    then
        continue
    fi
    cp -r $dir $INSTALL_DIR
done


if [ ! -f $local_node ];
then
    if [ -L $local_node ];
    then
        rm -rf $local_node
    fi
    ln -s ${INSTALL_DIR}'/node/bin/node' $local_node
fi


if [ ! -f $local_pm2 ];
then
    if [ -L $local_pm2 ];
    then
        rm -rf $local_pm2
    fi
    ln -s ${INSTALL_DIR}'/node/lib/node_modules/pm2/bin/pm2' $local_pm2
fi


if [ ! -d '/root/.pm2/' ];
then
    mkdir '/root/.pm2/'
elif [ ! -d '/root/.pm2/node_modules' ]
then
    if [ -L '/root/.pm2/node_modules' ];
    then
        echo 'delete node_modules'
        rm -rf '/root/.pm2/node_modules'
    fi
    ln -s ${INSTALL_DIR}'/node/lib/node_modules' '/root/.pm2/node_modules'
fi

if [ ! -f $INSTALL_DIR'/node/lib/node_modules/pm2-logrotate/package.json' ];
then
    pm2 install pm2-logrotate
fi

pm2 set module-db:pm2-logrotate true
pm2 set pm2-logrotate:retain 5 
pm2 set pm2-logrotate:max_size 50M 
pm2 set pm2-logrotate:compress true
pm2 kill










