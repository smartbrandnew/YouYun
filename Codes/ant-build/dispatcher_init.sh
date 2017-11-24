#!/bin/bash
set -e
set -x

echo "DISCONF_HOST: '"$DISCONF_HOST"'" > /opt/uyun/platform/dispatcher/config.yaml
echo "DISCONF_PORT: '"$DISCONF_PORT"'" >> /opt/uyun/platform/dispatcher/config.yaml
echo "DISCONF_URL: '"$DISCONF_URL"'" >> /opt/uyun/platform/dispatcher/config.yaml
echo "HOST: '"$HOST"'" >> /opt/uyun/platform/dispatcher/config.yaml
if [ $DECRYPT_URL ];
then
    echo "DECRYPT_URL: '"$DECRYPT_URL"'" >> /opt/uyun/platform/dispatcher/config.yaml
fi
chmod +x /opt/uyun/platform/dispatcher/node/bin/node
chmod +x /opt/uyun/platform/dispatcher/node/bin/npm
chmod +x /opt/uyun/platform/dispatcher/node/lib/node_modules/pm2/bin/pm2

ln -s /opt/uyun/platform/dispatcher/node/bin/node  /usr/local/bin/node
ln -s /opt/uyun/platform/dispatcher/node/bin/npm  /usr/local/bin/npm
ln -s /opt/uyun/platform/dispatcher/node/lib/node_modules/pm2/bin/pm2 /usr/local/bin/pm2

sed  -i "s/user RUNNER;/user root;/g" /opt/uyun/platform/nginx/conf/nginx.conf
mkdir -p /opt/uyun/platform/nginx/logs
/opt/uyun/platform/nginx/sbin/nginx -p /opt/uyun/platform/nginx -c /opt/uyun/platform/nginx/conf/nginx.conf

cd /opt/uyun/platform/dispatcher
pm2 start process.json --no-daemon


