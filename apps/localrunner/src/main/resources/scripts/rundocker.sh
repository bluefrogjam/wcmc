#!/bin/bash

target=/files
awssrc=/home/diego/.aws
awstgt=/root/.aws

if [ $# -lt 4 ]; then
    echo "need 4 parameters: 'source folder', 'method name', 'start idx' and 'end idx'"
    exit 0
fi

source=$1
method=$2
start=$3

echo "source: $source"
echo "method: $method"
echo "start: $start"
echo "tmp-end: $4"

max=`ls -l "$1/"*.txt | wc -l`
if (( $4 < $max )); then
    end=$4
else
    end=$max
fi
echo "end: $end"

for idx in `seq $start $end`; do
    docker run -d --rm --name localrunner-$idx \
     --mount type=bind,source="$source",target=$target \
     --mount type=bind,source=$awssrc,target=$awstgt \
     eros.fiehnlab.ucdavis.edu/carrot-local \
     $target/segment_$idx.txt \
     "$method"
    sleep 1s
done
