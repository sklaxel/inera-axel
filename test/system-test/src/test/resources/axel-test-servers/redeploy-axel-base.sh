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

# ------------------------------------------------------------
# FUNCTIONS
# ------------------------------------------------------------
display_usage() {
  echo "This script must be run with the following parameters."
  echo -e "\nUsage:\n$0 --mongodb-name=<name> [--activemq=external]\n"
  echo -e "Till Exempel:\n$0 --mongodb-name=axel\n"
}

display_prerequisites_missing() {
  echo "The following prerequisite is missing:"
  echo -e "- $1\n"
}

exit_if_process_not_running() {
  local process_name=$1
  if (( $(ps -ef | grep -v grep | grep $process_name | wc -l) == 0 )); then
    echo "Exiting due to not running process '$process_name'"
    exit 1
  fi
}

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

ACTIVEMQ_MODE=internal
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
# Check that Mongo is running
# ------------------------------------------------------------
exit_if_process_not_running mongod
