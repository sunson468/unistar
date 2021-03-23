#!/bin/sh
while  [[ `pgrep -f "unistar-this"` != "" ]]
do
  echo '先关闭进程'
  source ./stop.sh
done
nohup java -Dfile.encoding=UTF-8 \
	   -jar unistar-central.jar --unistar-this >/dev/null 2>&1 &
sleep 5s
tail -f logs/unistar-central.log
