/*
*/

#include <stdio.h>
#include <string.h>
#include <dawg.h>

Node_Construct* makegaddag(char*);

int main (int argc, char *argv[]) {

 /* Verify argument */
  if ( argc < 2 ) {
    printf("Error: usage: makegaddag <filename>\n");
    exit(0);
  }

 /* Construct the gaddag object */
  Node_Construct *GADDAG = makegaddag(argv[1]);
  
 /* Chose the new name to save it as */
  char *newname = malloc(sizeof(argv[1])+8);
  strcpy(newname, argv[1]);

 /* Replace the extension with .gaddag */
  int i;
  for (i=strlen(newname)-1; i>=0; i--) {
    if ( newname[i] == '.' ) {
      newname[i] = 0x0;
      break;
    }
  }
  strcat(newname, ".gaddag");

 /* Write it to disk */
  printf("Writing to disk...");
  fflush(stdout);
  dawg_save(GADDAG, newname);
  printf("done\n");
  
  return 0;
}

Node_Construct* makegaddag(char *file) {
  Node_Construct *GADDAG = dawg_new_child('@');
  FILE *fin;

  char prefix[20];
  char suffix[20];
  char gaddag_word[20];
  char buffer[20];
  unsigned int i,j;

  printf("Reading in dictionary...");
  fflush(stdout);
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
  printf("done\n");

  dawg_compress(GADDAG);
  return GADDAG;
}
