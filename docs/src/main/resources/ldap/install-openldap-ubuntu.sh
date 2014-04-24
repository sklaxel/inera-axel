#!/bin/bash
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

sudo apt-get install slapd ldap-utils

sudo ldapmodify -Y EXTERNAL -H ldapi:/// -f $DIR/axel-update.ldif
sudo ldapadd -Y EXTERNAL -H ldapi:/// -f $DIR/shs-schema.ldif

sudo ldapadd -D "cn=admin,l=SHS" -W -f $DIR/axel-systemtest.ldif

