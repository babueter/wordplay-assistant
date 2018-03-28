/*
*/

#include <tiles.h>

const char LETTERS[] = {'A','B','C','D','E','F','G','H','I','J','K','L','M',
                  'N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};

const char POINTS[]  = {  1,  3,  3,  2,  1,  4,  2,  4,  1,  8,  5,  1,  3,
                    1,  1,  3, 10,  1,  1,  1,  1,  4,  4,  8,  4, 10};

const char TILES[] = {'*','*',
                'A','A','A','A','A','A','A','A','A',
                'B','B',
                'C','C',
                'D','D','D','D',
                'E','E','E','E','E','E','E','E','E','E','E','E',
                'F','F',
                'G','G','G',
                'H','H',
                'I','I','I','I','I','I','I','I','I',
                'J',
                'K',
                'L','L','L','L',
                'M','M',
                'N','N','N','N','N','N',
                'O','O','O','O','O','O','O','O',
                'P','P',
                'Q',
                'R','R','R','R','R','R',
                'S','S','S','S',
                'T','T','T','T','T','T',
                'U','U','U','U',
                'V','V',
                'W','W',
                'X',
                'Y','Y',
                'Z',};


int tile_score (char tile) {
  int i;

  if ( tile < 'A' ) {
    return 0;
  }
  if ( tile > 'Z' ) {
    return 0;
  }

  for (i=0; i<26; i++) {
    if ( LETTERS[i] == tile ) {
      return POINTS[i];
    }
  }

  return 0;
}

