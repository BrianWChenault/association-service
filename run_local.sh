#!/usr/bin/env bash

docker kill association-service
docker rm -v association-service


echo "Running association-service..."
docker run --name association-service \
    -p 9001:9001 \
    -d association-service:1.0
