#!/bin/bash
version=$1
tag="V2.0.$version.${BUILD_NUMBER}"
docker_images=('dispatcher' 'manager' 'module')
for images in ${docker_images[@]}; 
do
    if [ -d 'docker_images/'${images} ];
    then
        rm -rf 'docker_images/'${images}
    fi
    mkdir -p 'docker_images/'${images}
done


cp "./dist/platform-ant-dispatcher-V2.0.${version}.tar.gz" ./docker_images/dispatcher
cp ./dist/platform-ant-python-2.7.13.tar.gz ./docker_images/dispatcher
cp ./dist/platform-ant-node-7.8.0.tar.gz ./docker_images/dispatcher
cp ./dist/platform-ant-nginx-1.8.1.tar.gz ./docker_images/dispatcher
cp ./dispatcher_Dockerfile ./docker_images/dispatcher/Dockerfile
cp ./dispatcher_init.sh ./docker_images/dispatcher/init.sh

cp "./dist/platform-ant-manager-V2.0.${version}.tar.gz" ./docker_images/manager
cp ./manager_Dockerfile ./docker_images/manager/Dockerfile
cp ./manager_init.sh ./docker_images/manager/init.sh

docker ps -a|grep 'platform/ant-dispatcher'|awk '{print "docker rm -f "$1 }'|sh
docker ps -a|grep 'platform/ant-manager'|awk '{print "docker rm -f "$1 }'|sh

docker images|awk '{if ($1=="platform/ant-dispatcher") {print "docker rmi -f "$3 }}'|sh
docker images|awk '{if ($1=="platform/ant-manager") {print "docker rmi -f "$3 }}'|sh

cd docker_images/dispatcher && docker build --build-arg VERSION=$version -t platform/ant-dispatcher:$version ./ && cd ../../
cd docker_images/manager && docker build --build-arg VERSION=$version -t platform/ant-manager:$version ./ && cd ../../

docker tag platform/ant-manager:$version dockerhub.uyuntest.cn:5000/platform/ant-manager:$tag
docker tag platform/ant-dispatcher:$version dockerhub.uyuntest.cn:5000/platform/ant-dispatcher:$tag

docker push dockerhub.uyuntest.cn:5000/platform/ant-manager:$tag
docker push dockerhub.uyuntest.cn:5000/platform/ant-dispatcher:$tag