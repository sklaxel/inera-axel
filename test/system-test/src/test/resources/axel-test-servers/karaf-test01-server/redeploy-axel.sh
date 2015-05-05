#!/bin/bash
#
# Copyright (C) 2013 Inera AB (http://www.inera.se)
#
# This file is part of Inera Axel (http://code.google.com/p/inera-axel).
#
# Inera Axel is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Inera Axel is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>
#

echo "Script kördes med följande parameter: $0 $@"

export JAVA_MAX_MEM=1024m
export JAVA_MAX_PERM_MEM=384m
export JAVA_HOME=/usr/java/jdk1.7.0_45

AXEL="axel-1.0-SNAPSHOT"
AXEL_USER=axel

if [ $# -eq 0 ]
  then
    echo "ERROR: Parameter för Mongo databasnamn saknas"
    exit 1
  else
    MONGO_DB_NAME=$1
fi

BASEDIR=`dirname $0`
BASEDIR="`cd \"$BASEDIR\" 2>/dev/null && pwd`"

echo "basedir=$BASEDIR"
cd $BASEDIR

echo "pwd="
pwd

if [ -d $AXEL ]; then
    if [ -x $AXEL/bin/stop ] && pgrep -u $AXEL_USER -f "java.*$AXEL" > /dev/null; then
	echo "Stoppar axel"
	$AXEL/bin/stop
	echo "sleep 20"
	sleep 20
    fi
    echo "Tar bort gamla axel"
    rm -rf $AXEL
fi

echo "Packar upp axel"
tar xvfz $AXEL.tar.gz 
if [ $? -ne 0 ]; then
    echo "Kunde inte packa upp axel"
    exit 1
fi

echo "Skapar Mongo indexer"
$AXEL/docs/mongo/createIndexes.sh $MONGO_DB_NAME
if [ $? -ne 0 ]; then
    echo "Kunde inte skapar Mongo indexer"
    exit 1
fi

echo "Startar axel"
nohup $AXEL/bin/start &

# Kill script if Axel has not started in 15 minutes
sleep $((15*60)) && kill $$ &
watchdogpid=$!

while true
do
        status_code=$(curl --write-out %{http_code} --silent --output "healthList.json" -u 'admin:admin' -X POST -d '{"type":"exec", "mbean":"se.inera.axel:name=axel,service=Health,type=HealthView", "operation":"healthList()" }' -H "Accept: application/json" -i http://localhost:8181/hawtio/jolokia)

        if [ $status_code -eq 200 ]; then
                if grep -iq 'ERROR' healthList.json; then
                        echo "Health view reported errors"
                else
                        echo "No errors reported Axel is started"
                        sleep 5
                        break
                fi
        fi
        echo "Axel is not started sleeping 10 seconds"
        sleep 10
done

kill $watchdogpid
echo "Exiterar"
