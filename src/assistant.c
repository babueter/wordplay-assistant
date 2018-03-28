#include <stdio.h>
#include <sys/time.h>
#include <unistd.h>

#include <dawg.h>
#include <wordplay.h>
#include <jduplicate.h>

#define GADDAG_FILE "twl.gaddag"

/* Debugging methods, chose prior to compiling */
#define FINDWORDS 1
#define ANALYSE_LOG 2
#define MEMLEAK_FINDWORDS 3
#define MEMLEAK_ANALYSE_LOG 4

#define DEFAULT_ACTION FINDWORDS

struct timeval real_start, real_end;

int main (int argc, char *argv[]) {
  /* Node_Construct *GADDAG; */
  Move move;
  move_clear(&move);

  struct timeval real_start, real_end;

 /* Counters */
  int i,j;

 /* Get args */
  if ( argc < 2 ) {
    if ( DEFAULT_ACTION == FINDWORDS || DEFAULT_ACTION == MEMLEAK_FINDWORDS ) {
      printf("Error: usage: assistant <rack>\n");
    } else {
      printf("Error: usage: assistant <filename>\n");
    }
    exit(0);
  }
  Node *GADDAG = dawg_load(GADDAG_FILE);
  Master_GADDAG = GADDAG;

 /* Initialize the master board */
  board_init();

 /* Find words based on supplied rack, print, exit */
  if ( DEFAULT_ACTION == FINDWORDS ) {
    gettimeofday(&real_start, NULL);

    MovePackage *package = move_package_new();
    word_findall(argv[1], GADDAG, package, move);
    move_package_print(package);

    gettimeofday(&real_end, NULL);
    float total_sec, total_usec, total_time;

    total_sec = real_end.tv_sec - real_start.tv_sec;
    total_usec = real_end.tv_usec - real_start.tv_usec;

    total_time = total_sec + (total_usec / 1000000);
    printf("\nTotal Time: %.3f sec\n\n", total_time);
  }

 /* Analize a Jduplicate log file */
  if ( DEFAULT_ACTION == ANALYSE_LOG ) {
    jdup_analyse_game(argv[1]);
    jdup_analyse_game(argv[1]);
  }

 /* Loop, calling findowrds over 10 times, pausing so you can evaluate memory ussage */
  if ( DEFAULT_ACTION == MEMLEAK_FINDWORDS ) {
    MovePackage *package;
    for (i=0; i<10; i++) {
      printf("Starting run %d...", i+1);
      fflush(stdout);
      for (j=0; j<10; j++) {
        package = move_package_new();
        word_findall(argv[1], GADDAG, package, move);
        move_package_free(package);
      }

      printf("done\n");
      sleep(10);
    }
  }

 /* Loop, calling jdup_analyse_game over 10 times, pausing so you can evaluate memory ussage */
  if ( DEFAULT_ACTION == MEMLEAK_ANALYSE_LOG ) {
    for (i=0; i<10; i++) {
      printf("Starting run %d...", i+1);
      fflush(stdout);

      jdup_analyse_game(argv[1]);

      printf("done\n");
      sleep(10);
    }
  }

  return(0);
}
