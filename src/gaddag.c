#include <stdio.h>
#include <string.h>

#include <dawg.h>

Node_Construct* makegaddag(char *file) {
  Node_Construct *GADDAG = dawg_new_child('@');
  FILE *fin;

  char prefix[20];
  char suffix[20];
  char gaddag_word[20];
  char buffer[20];
  unsigned int i,j;

  fin = fopen(file, "rb");
  while ( fgets(buffer, 20, fin) ) {
    buffer[strlen(buffer)-1] = 0x0;

    for (i=0; i<strlen(buffer); i++) {
      for (j=0; j<=i; j++) {
        prefix[j] = buffer[i-j];
      }
      prefix[i+1] = 0x0;

      strcpy(suffix, buffer+i+1);
      strcpy(gaddag_word, prefix);
      if ( i < strlen(buffer)-1 ) {
        strcat(gaddag_word, "#");
        strcat(gaddag_word, suffix);
      }
      dawg_add(GADDAG,gaddag_word);
    }

  }
  fclose(fin);

  dawg_compress(GADDAG);
  return GADDAG;
}

Node_Construct* makedawg(char *filename) {
  Node_Construct *DAWG = dawg_new_child('@');
  FILE *fin;
  char buffer[20];

  fin = fopen(filename, "rb");
  while ( fgets(buffer, 20, fin) ) {
    buffer[strlen(buffer)-1] = 0x0;
    dawg_add(DAWG,buffer);
  }
  fclose(fin);

  dawg_compress(DAWG);
  return DAWG;
}
