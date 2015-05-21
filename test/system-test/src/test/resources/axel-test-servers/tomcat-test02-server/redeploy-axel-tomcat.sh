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

# ------------------------------------------------------------
# CONSTANTS
# ------------------------------------------------------------
EXPECTED_JAVA_VERSION="1.7"
EXPECTED_TOMCAT_VERSION="apache-tomcat-7.0.47"
EXPECTED_AXEL_WAR="axel-war-1.0-SNAPSHOT"
EXPECTED_ACTIVEMQ_DIR="activemq"

# ------------------------------------------------------------
# FUNCTIONS
# ------------------------------------------------------------
display_usage() {
  echo "This script must be run with the following parameters."
  echo -e "\nUsage:\n$0 --mongodb-name=<name> [--activemq=external]\n"
  echo -e "Till Exempel:\n$0 --mongodb-name=axel-r2m --activemq=external\n"
}

display_prerequisites_missing() {
  echo "The following prerequisite is missing:"
  echo -e "- $1\n"
}

exit_if_file_not_exists() {
  local filename=$1
  if [ ! -f $filename ]; then
    echo "Exiting due to missing file '$filename'"
    exit 1
  fi
}

exit_if_process_not_running() {
  local process_name=$1
  if (( $(ps -ef | grep -v grep | grep $process_name | wc -l) == 0 )); then
    echo "Exiting due to not running process '$process_name'"
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

verify_java_settings() {
  if [ -z $JAVA_HOME ]; then
    display_prerequisites_missing "Environment variable JAVA_HOME"
    exit 1
  else
    JAVA_EXE=$JAVA_HOME/bin/java
    $JAVA_EXE -version 2> tmp.ver
    VERSION=`cat tmp.ver | grep "java version" | awk '{ print substr($3, 2, length($3)-2); }'`
    rm tmp.ver
    VERSION=`echo $VERSION | awk '{ print substr($1, 1, 3); }'`
    if [ "$VERSION" != "$EXPECTED_JAVA_VERSION" ]
    then
      display_prerequisites_missing "JAVA version of JAVA_HOME ($JAVA_HOME) is incorrect. The expected version is $EXPECTED_JAVA_VERSION."
      exit 1
    fi
  fi
}

# ------------------------------------------------------------
# ------------------------------------------------------------
# ------------------------------------------------------------
# MAIN
# ------------------------------------------------------------
# ------------------------------------------------------------
# ------------------------------------------------------------
verify_java_settings

# ------------------------------------------------------------
# Läs in parametrar
# ------------------------------------------------------------
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

if [ -z $MONGO_DB_NAME ]
  then
    echo "ERROR: Parameter för Mongo databasnamn saknas"
    display_usage
    exit 1
fi

echo "ACTIVEMQ_MODE=${ACTIVEMQ_MODE}"
echo "MONGO_DB_NAME=${MONGO_DB_NAME}"

# ------------------------------------------------------------
# Sätt upp Axel under tomcat
# ------------------------------------------------------------
export JAVA_MAX_MEM=1024m
export JAVA_MAX_PERM_MEM=384m

BASEDIR=`dirname $0`
BASEDIR="`cd \"$BASEDIR\" 2>/dev/null && pwd`"

echo "basedir=$BASEDIR"
cd $BASEDIR

echo "pwd="
pwd

if [ ! -d $EXPECTED_TOMCAT_VERSION ]; then
  display_prerequisites_missing "Tomcat installation $BASEDIR/$EXPECTED_TOMCAT_VERSION is missing."
  exit 1;
fi

if [ -f $EXPECTED_TOMCAT_VERSION/tomcat.pid ] > /dev/null; then
  echo "Stoppar tomcat"
  kill -9 `cat $EXPECTED_TOMCAT_VERSION/tomcat.pid`
  rm -f $EXPECTED_TOMCAT_VERSION/tomcat.pid
  rm -f $EXPECTED_TOMCAT_VERSION/logs/catalina.out
  echo "sleep 20"
  sleep 20
fi

echo "Tar bort gamla axel war-filer..."
rm -rfv $EXPECTED_TOMCAT_VERSION/webapps/*.war
rm -rfv $EXPECTED_TOMCAT_VERSION/webapps/shs*
rm -rfv $EXPECTED_TOMCAT_VERSION/webapps/riv-shs*
rm -rfv $EXPECTED_TOMCAT_VERSION/webapps/monitoring*

echo "Ta bort gamla $EXPECTED_AXEL_WAR"
rm -rf $EXPECTED_AXEL_WAR

exit_if_file_not_exists $EXPECTED_AXEL_WAR.tar.gz
echo "Packar upp axel"
tar xvfz $EXPECTED_AXEL_WAR.tar.gz
if [ $? -ne 0 ]; then
    echo "Kunde inte packa upp axel"
    exit 1
fi
chmod 755 ${EXPECTED_AXEL_WAR}/bin/

echo "Kopierar war-filer till tomcat...."
cp $EXPECTED_AXEL_WAR/webapps/riv-shs-war-1.0-SNAPSHOT.war $EXPECTED_TOMCAT_VERSION/webapps/riv-shs.war
cp $EXPECTED_AXEL_WAR/webapps/shs-broker-war-1.0-SNAPSHOT.war $EXPECTED_TOMCAT_VERSION/webapps/shs.war 
cp $EXPECTED_AXEL_WAR/webapps/monitoring-war-1.0-SNAPSHOT.war $EXPECTED_TOMCAT_VERSION/webapps/monitoring.war 

echo "Kopierar config filer"
PROPERTY_FILE=config/etc/shs-cmdline.properties
exit_if_file_not_exists $PROPERTY_FILE
cp $PROPERTY_FILE $EXPECTED_AXEL_WAR/etc

# ------------------------------------------------------------
# Skapa Mongo indexer
# ------------------------------------------------------------
exit_if_process_not_running mongod
echo "Skapar Mongo indexer"
$EXPECTED_AXEL_WAR/docs/mongo/createIndexes.sh $MONGO_DB_NAME
if [ $? -ne 0 ]; then
    echo "Kunde inte skapa Mongo index"
    exit 1
fi

# ------------------------------------------------------------
# Starta ActiveMQ
# ------------------------------------------------------------
if [ "$ACTIVEMQ_MODE" = "external" ]; then
  if [ ! -d $EXPECTED_ACTIVEMQ_DIR ]; then
    display_prerequisites_missing "ActiveMQ installation $BASEDIR/$EXPECTED_ACTIVEMQ_DIR is missing."
    exit 1;
  fi

  if [ -f $EXPECTED_ACTIVEMQ_DIR/data/activemq.pid ] > /dev/null; then
    echo "Stoppar ActiveMQ"
    kill -9 `cat $EXPECTED_ACTIVEMQ_DIR/data/activemq.pid`
    rm -f $EXPECTED_ACTIVEMQ_DIR/data/activemq.pid
    rm -f $EXPECTED_ACTIVEMQ_DIR/data/activemq.log
    sleep 3
  fi

  echo "Startar ActiveMQ"
  $EXPECTED_ACTIVEMQ_DIR/bin/activemq start

  # Skapa en tom loggfil
  touch $EXPECTED_ACTIVEMQ_DIR/data/activemq.log

  echo "Väntar tills ActiveMQ är startat"
  tail -f $EXPECTED_ACTIVEMQ_DIR/data/activemq.log &

  until grep "INFO  | ActiveMQ WebConsole available at" $EXPECTED_ACTIVEMQ_DIR/data/activemq.log; do
    sleep 10
  done
else
  # Inget behöver göras för att det ska köras embedded ActiveMQ
  echo "Runs embedded ActiveMQ"
fi

# ------------------------------------------------------------
# Starta tomcat
# ------------------------------------------------------------
echo "Startar tomcat"
$EXPECTED_TOMCAT_VERSION/bin/startup.sh

echo "Väntar ett bra tag"
tail -f $EXPECTED_TOMCAT_VERSION/logs/catalina.out &

until grep "INFO: Server startup in" $EXPECTED_TOMCAT_VERSION/logs/catalina.out; do
    sleep 10
done
