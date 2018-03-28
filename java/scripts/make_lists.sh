#!/bin/bash

TWL=$1
if [ -z "${TWL}" ];then
    echo "Usage: $0 <filename>"
    exit 1
fi

MAKEDAWG=`which makedawg 2>/dev/null`
if [ -z "${MAKEDAWG}" ];then
    echo "Error: could not find makedawg command"
    exit 2
fi

if [ ! -d "src/Data" ];then
    echo "Error: could not find Data directory"
    exit 3
fi

cp $TWL src/Data/twl.txt
grep ^..$ src/Data/twl.txt > src/Data/2letterwords.txt
grep ^...$ src/Data/twl.txt > src/Data/3letterwords.txt
grep ^....$ src/Data/twl.txt > src/Data/4letterwords.txt
grep ^.......$ src/Data/twl.txt > src/Data/7letterwords.txt
grep '[eE][sS][tT]$' src/Data/twl.txt > src/Data/est.txt
grep '[iI][nN][gG]$' src/Data/twl.txt > src/Data/ing.txt
grep '^[uU][nN]' src/Data/twl.txt > src/Data/un.txt
grep '^[pP][rR][eE]' src/Data/twl.txt > src/Data/pre.txt
grep '^[oO][uU][tT]' src/Data/twl.txt > src/Data/out.txt

${MAKEDAWG} src/Data/twl.txt
${MAKEDAWG} src/Data/2letterwords.txt
${MAKEDAWG} src/Data/3letterwords.txt
${MAKEDAWG} src/Data/4letterwords.txt
${MAKEDAWG} src/Data/7letterwords.txt
${MAKEDAWG} src/Data/est.txt
${MAKEDAWG} src/Data/ing.txt
${MAKEDAWG} src/Data/un.txt
${MAKEDAWG} src/Data/pre.txt
${MAKEDAWG} src/Data/out.txt
rm src/Data/*.txt

