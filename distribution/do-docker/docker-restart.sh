#!/bin/sh
set -x
VERSION=`cat VERSION.txt`

docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)
docker run -d --add-host=host.docker.internal:host-gateway -v /home/lovemap/logs:/tmp -p 8090:8090 -e SPRING_PROFILES_ACTIVE=do registry.digitalocean.com/lovemap-registry/lovemap-backend:$VERSION
