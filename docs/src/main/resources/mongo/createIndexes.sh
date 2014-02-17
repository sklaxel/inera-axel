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

echo "------------------------------ $0 START ------------------------------"

if [ $# -eq 0 ]
  then
    echo "ERROR: Missing Mongo database name"
    exit 1
  else
    MONGO_DB_NAME=$1
fi

BASEDIR=`dirname $0`
BASEDIR="`cd \"$BASEDIR\" 2>/dev/null && pwd`"

echo "basedir=$BASEDIR"
cd $BASEDIR

mongo localhost:27017/$MONGO_DB_NAME createIndexes.js
echo "------------------------------ $0 END ------------------------------"
