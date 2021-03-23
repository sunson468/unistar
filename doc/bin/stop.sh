#!/bin/sh
pid=`pgrep -f "unistar-this"`
if [[ $pid != "" ]]; then
  kill $pid
  sleep 5s
fi

