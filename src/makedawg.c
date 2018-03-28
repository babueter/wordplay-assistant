/*
*/

#include <stdio.h>
#include <string.h>
#include <dawg.h>

Node_Construct* makedawg(char*);

int main (int argc, char *argv[]) {

 /* Verify argument */
  if ( argc < 2 ) {
    printf("Error: usage: makedawg <filename>\n");
    exit(0);
  }

 /* Construct the dawg object */
  Node_Construct *DAWG = makedawg(argv[1]);
  
 /* Chose the new name to save it as */
  char *newname = malloc(sizeof(argv[1])+8);
  strcpy(newname, argv[1]);

 /* Replace the extension with .dawg */
  int i;
  for (i=strlen(newname)-1; i>=0; i--) {
    if ( newname[i] == '.' ) {
      newname[i] = 0x0;
      break;
    }
  }
  strcat(newname, ".dawg");

 /* Write it to disk */
  printf("Writing to disk...");
  fflush(stdout);
  dawg_save(DAWG, newname);
  printf("done\n");
  
  return 0;
}

Node_Construct* makedawg(char *filename) {
  Node_Construct *DAWG = dawg_new_child('@');
  FILE *fin;

  char buffer[20];

  printf("Reading in dictionary...");
  fflush(stdout);
  fin = fopen(filename, "rb");
  while ( fgets(buffer, 20, fin) ) {
    buffer[strlen(buffer)-1] = 0x0;
    dawg_add(DAWG,buffer);
  }
  fclose(fin);
  printf("done\n");

  dawg_compress(DAWG);
  return DAWG;
}
