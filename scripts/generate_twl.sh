#!/bin/bash
#
# NOT Tournament Word List
#

> twl.txt
for LETTER in a b c d e f g h i j k l m n o p q r s t u v w x y z;do
    wget -q http://scrabble.merriam.com/words/start-with/${LETTER}
    grep '<a href=./finder/.*</a>' ${LETTER} | cut -d'>' -f2|cut -d'<' -f1 >> twl.txt
    rm -f ${LETTER}
done
