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

display_usage() {
  echo "This script must be run with the following parameters."
  echo -e "\nUsage:\n$0 --mongodb-name=<name> [--activemq=external]\n"
  echo -e "Till Exempel:\n$0 --mongodb-name=axel-r2m --activemq=external\n"
}

exit_if_file_not_exists() {
  local filename=$1
  if [ ! -f $filename ]; then
    echo "Exiting due to file '$filename' missing"
    exit 1
  fi
}

kill_backgrounds_job() {
  JOBS=$(jobs -p)
  if [ "$JOBS" ]; then
    # Denna shell script startar flera "tail -f ... &" i bakgrunden som måste termineras
    echo kill background jobs: $JOBS
    kill $JOBS
  fi
}
trap "kill_backgrounds_job" EXIT

# ------------------------------------------------------------
# Läs in parametrar
echo "Script kördes med följande parameter: $0 $@"

for i in "$@"
do
case $i in
  --activemq=*)
  ACTIVEMQ_MODE="${i#*=}"
  shift
  ;;
  --mongodb-name=*)
  MONGO_DB_NAME="${i#*=}"
  shift
  ;;
  *)
          # unknown option
  echo "ERROR: Invalid parameter \"$i\""
  display_usage
  exit 1
  ;;
esac
shift
done

echo "ACTIVEMQ_MODE   = ${ACTIVEMQ_MODE}"
echo "MONGO_DB_NAME   = ${MONGO_DB_NAME}"

if [ -z $MONGO_DB_NAME ]
  then
    echo "ERROR: Parameter för Mongo databasnamn saknas"
    display_usage
    exit 1
fi

# ------------------------------------------------------------
# Sätt upp Axel under tomcat
export JAVA_MAX_MEM=1024m
export JAVA_MAX_PERM_MEM=384m
# export JAVA_HOME=/usr/java/jdk1.7.0_45

TOMCAT="apache-tomcat-7.0.47"
AXEL_TAR_FILE="axel-war-1.0-SNAPSHOT"

BASEDIR=`dirname $0`
BASEDIR="`cd \"$BASEDIR\" 2>/dev/null && pwd`"

echo "basedir=$BASEDIR"
cd $BASEDIR

echo "pwd="
pwd

if [ -d $TOMCAT ]; then
  if [ -f $TOMCAT/tomcat.pid ] > /dev/null; then
    echo "Stoppar axel"
    kill -9 `cat $TOMCAT/tomcat.pid`
    rm -f $TOMCAT/tomcat.pid
    rm -f $TOMCAT/logs/catalina.out
    echo "sleep 20"
    sleep 20
  fi
  
  echo "Tar bort gamla axel war-filer..."
  rm -rfv $TOMCAT/webapps/*.war
  rm -rfv $TOMCAT/webapps/shs*
  rm -rfv $TOMCAT/webapps/riv-shs*
  rm -rfv $TOMCAT/webapps/monitoring*
fi

echo "Ta bort gamla $AXEL_TAR_FILE"
rm -rf $AXEL_TAR_FILE

echo "Packar upp axel"
tar xvfz $AXEL_TAR_FILE.tar.gz
if [ $? -ne 0 ]; then
    echo "Kunde inte packa upp axel"
    exit 1
fi

echo "Kopierar war-filer till tomcat...."
cp $AXEL_TAR_FILE/webapps/riv-shs-war-1.0-SNAPSHOT.war $TOMCAT/webapps/riv-shs.war
cp $AXEL_TAR_FILE/webapps/shs-broker-war-1.0-SNAPSHOT.war $TOMCAT/webapps/shs.war 
cp $AXEL_TAR_FILE/webapps/monitoring-war-1.0-SNAPSHOT.war $TOMCAT/webapps/monitoring.war 

echo "Kopierar config filer"
PROPERTY_FILE=config/etc/shs-cmdline.properties
exit_if_file_not_exists $PROPERTY_FILE
cp $PROPERTY_FILE $AXEL_TAR_FILE/etc

# ------------------------------------------------------------
# Skapa Mongo indexer
echo "Skapar Mongo indexer"
$AXEL_TAR_FILE/docs/mongo/createIndexes.sh $MONGO_DB_NAME
if [ $? -ne 0 ]; then
    echo "Kunde inte skapa Mongo index"
    exit 1
fi

# ------------------------------------------------------------
# Starta ActiveMQ
if [ "$ACTIVEMQ_MODE" = "external" ]; then
  ACTIVEMQ="activemq"

  if [ -f $ACTIVEMQ/data/activemq.pid ] > /dev/null; then
    echo "Stoppar ActiveMQ"
    kill -9 `cat $ACTIVEMQ/data/activemq.pid`
    rm -f $ACTIVEMQ/data/activemq.pid
    rm -f $ACTIVEMQ/data/activemq.log
    sleep 3
  fi

  echo "Startar ActiveMQ"
  $ACTIVEMQ/bin/activemq start

  # Skapa en tom loggfil
  touch $ACTIVEMQ/data/activemq.log

  echo "Väntar tills ActiveMQ är startat"
  tail -f $ACTIVEMQ/data/activemq.log &

  until grep "INFO  | ActiveMQ WebConsole available at" $ACTIVEMQ/data/activemq.log; do
    sleep 10
  done
else
  # Inget behöver göras för att det ska köras embedded ActiveMQ
  echo "Runs embedded ActiveMQ"
fi

# ------------------------------------------------------------
# Starta tomcat
echo "Startar tomcat"
$TOMCAT/bin/startup.sh

echo "Väntar ett bra tag"
tail -f $TOMCAT/logs/catalina.out &

until grep "INFO: Server startup in" $TOMCAT/logs/catalina.out; do
    sleep 10
done
