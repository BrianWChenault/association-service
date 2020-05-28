#!/usr/bin/env bash

THIS_DIRECTORY=$(dirname "${BASH_SOURCE}")

docker kill neo4j
docker rm -v neo4j


echo "Running neo4j..."
docker run --name neo4j \
    --publish=7474:7474 --publish=7687:7687 \
    --volume=$HOME/neo4j/data:/data \
    --volume=$HOME/neo4j/logs:/logs \
    --env NEO4J_AUTH=none \
    -d neo4j:4.0.2

