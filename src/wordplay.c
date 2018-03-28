/*
*/
#include <stdlib.h>

#include <dawg.h>
#include <board.h>
#include <wordplay.h>

#define NOT_HOOK 0
#define VERTICAL_HOOK 1
#define HORIZONTAL_HOOK 2
#define MULTI_HOOK 3

static int move_cmp(const void*, const void*);
void word_findall_hook (Board*, char*, int, int, unsigned int, int, Move, Node*, MovePackage*);
char* word_letters_used(Board*, char*, int, int, char);
int word_valid_crossword(Board*, char, int, int, char);
boolean word_is_valid(char *);
boolean word_is_hook_cell(Board*, int, int);

Move* move_best_starting_position (char *word) {
/* Find the best starting position on a blank board */
  Board *board = board_new();
  Move *best_move = move_new();

  best_move->x = 7;
  best_move->y = 7;

  int length = strlen(word);
  strcpy(best_move->word, word);
  
 /* Start at grid position 7,7 and slide the word left, keep track of best move */
  int x;
  int score;
  for (x=7; x>7-length; x--) {
    score = board_score(board, word, 7, x, HORIZONTAL);
    if ( score > best_move->score ) {
      best_move->score = score;
      best_move->x = x;
    }
  }
  free(board);
  return best_move;
}

Move* move_new () {
/* Create a blank move and return it */
  Move *new = malloc(sizeof(Move));

  move_clear(new);
  return new;
}

void move_clear (Move *move) {
  move->y = 0;
  move->x = 0;
  move->direction = HORIZONTAL;
  move->word[0] = 0x0;
  move->leave[0] = 0x0;
  move->score = 0;
  move->adjusted_score = 0;
}

MovePackage* move_package_new() {
/* Create and initialize a moves package */
  MovePackage *new = malloc(sizeof(MovePackage));

  int i;
  for (i=0; i<MAX_PACKAGE_SIZE; i++) {
    new->moves[i] = NULL;
  }

  new->count = 0;
  return new;
}

void move_package_add(MovePackage *package, Move *move) {
/* Add move to the package */
  int lowest_score = 1000;
  int lowest_move = 1000;

  int i;
  for (i=0; i<MAX_PACKAGE_SIZE; i++) {
   /* If null, we must be at the end of the list */
    if ( package->moves[i] == NULL ) {
      package->moves[i] = move;

     /* If the package count is wrong, then we bail */
      if ( package->count != i ) {
        printf("Error: Package has size of %d instead of %d\n", package->count, i);
        exit(1);
      }

     /* Increase the package count and return */
      package->count = i+1;
      return;

   /* Make sure this move doesnt already exist */
    } else {
      if ( package->moves[i]->x == move->x && package->moves[i]->y == move->y ) {
        if ( strcmp(package->moves[i]->word, move->word) == 0 ) {
          free(move);
          return;
        }
      }

     /* Keep track of the lowest scoring move in case we reach the max */
      if ( package->moves[i]->score < lowest_score ) {
        lowest_score = package->moves[i]->score;
        lowest_move = i;
      }
    }
  }

 /* Package was full, replace the lowest scoring move */
  if ( move->score > lowest_score ) {
    free(package->moves[lowest_move]);
    package->moves[lowest_move] = move;
    return;
  }

 /* There was no place for this move, go ahead and delete it */
  free(move);
}

void move_package_free(MovePackage *package) {
/* Free up the memory used by this package */
  int i;
  for (i=0; i<MAX_PACKAGE_SIZE; i++) {
    if ( package->moves[i] != NULL ) {
      free(package->moves[i]);
      package->moves[i] = NULL;
    }
  }

  free(package);
}

void move_print (Move *move) {
/* Print a single move */
  printf("  %3d pts (%3d) : %16s [%6s] @%2d,%2d %c\n",
         move->score, move->adjusted_score, move->word, move->leave, move->y + 1, move->x + 1, move->direction);
}

void move_package_print(MovePackage *package) {
/* Print every move inside the package */
  unsigned int i;

  move_package_sort(package);
  for (i=0; i<MAX_PACKAGE_SIZE; i++) {
    if ( package->moves[i] == NULL ) {
      break;
    }
    move_print(package->moves[i]);
  }
}

void move_package_sort (MovePackage *package) {
/* Sort the moves by size using qsort */
  qsort(package->moves, package->count, sizeof(Move *), move_cmp);
}

static int move_cmp (const void *m1, const void *m2) {
/* Return the value of the comparison */
  return (  (* (Move * const *) m1)->score - (* (Move * const *) m2)->score  );
}

void word_findall (char *letters, Node *node, MovePackage *package, Move move) {
/* Recursively find all words using the given letters, store the moves in the moves package */

 /* For each letter in the list, use it in the word and and recurse */
  unsigned int i;
  char new_letters[8];
  Move new_move;
  Node *next_node;
  for (i=0; i<strlen(letters); i++) {

   /* This letter has a place in our word */
    next_node = node_has_child(node, letters[i]);
    if ( next_node != NULL ) {
     /* Remove this letter from our rack */
      unsigned int j;
      strcpy(new_letters, letters);
      for (j=i; j<strlen(letters); j++) {
        new_letters[j] = new_letters[j+1];
      }
      memcpy(&new_move, &move, sizeof(Move));

     /* prepend the letter to the word */
      for (j=strlen(new_move.word)+1; j>0; j--) {
        new_move.word[j] = new_move.word[j-1];
      }
      new_move.word[0] = letters[i];

     /* Found a word, store it */
      if ( next_node->terminal ) {
        move_package_add(package, move_best_starting_position(new_move.word));
      }

     /* Recurse */
      word_findall(new_letters, next_node, package, new_move);

   /* Else, this is a blank tile */
    } else if ( letters[i] == '*' ) {
      next_node = node->child;

     /* Remove this blank from our rack */
      unsigned int j;
      strcpy(new_letters, letters);
      for (j=i; j<strlen(letters); j++) {
        new_letters[j] = new_letters[j+1];
      }

     /* Make the blank be anything that would work */
      char blank_value[1];
      while ( next_node != NULL ) {
        if ( next_node->value == '#' ) {
          next_node = next_node->next_sibling;
          continue;
        }
        blank_value[0] = tolower(next_node->value);
        memcpy(&new_move, &move, sizeof(Move));

       /* prepend the letter to the word */
        for (j=strlen(new_move.word)+1; j>0; j--) {
          new_move.word[j] = new_move.word[j-1];
        }
        new_move.word[0] = blank_value[0];

       /* Found a word, store it */
        if ( next_node->terminal ) {
          move_package_add(package, move_best_starting_position(new_move.word));
        }

       /* Recurse */
        word_findall(new_letters, next_node, package, new_move);
        next_node = next_node->next_sibling;
      }
    }
  }
}

void word_findall_board(Board *board, char *letters, MovePackage *package) {
/* Find words for every hook on the board */
  int y,x;
  Move move;
  move_clear(&move);
  for (y=0; y<14; y++) {
    for (x=0; x<14; x++) {
      unsigned int hook_type = word_is_hook_cell(board, y, x);
      if ( hook_type != NOT_HOOK ) {
        word_findall_hook(board, letters, y, x, hook_type, 0, move, Master_GADDAG, package);
      }
    }
  }
}

void word_findall_hook (
/* Find all plays at the position specified on the supplied board with the supplied letters */
  Board *board, char *letters, int y, int x, unsigned int hook_type,
  int position, Move move, Node *node, MovePackage *package
) {

 /* Hook can go vertical or horizontal, do both */
  if ( hook_type == MULTI_HOOK ) {
    move.direction = VERTICAL;
    word_findall_hook(board, letters, y, x, VERTICAL_HOOK, position, move, node, package);

    move.direction = HORIZONTAL;
    word_findall_hook(board, letters, y, x, HORIZONTAL_HOOK, position, move, node, package);

    return;
  }

 /* Vertical hook */
  if ( hook_type == VERTICAL_HOOK ) {
    int next_y = y + position;

   /* At the top, turn around and go the other direction */
    if ( next_y < 0 ) {

     /* Check if this is a word, and that we used at least one letter from our rack */
      if ( node->terminal ) {
        char *letters_used = word_letters_used(board, move.word, move.y, move.x, move.direction);
        if ( letters_used != NULL ) {

         /* Make sure the space below us is empty, or we are at the end of the board, add package if true */
          if ( (y < 14 && !board->cell[y+1][x]) || y == 14 ) {
            Move *new_move = move_new();
            memcpy(new_move, &move, sizeof(Move));

            new_move->score += board_score(board, new_move->word, new_move->y, new_move->x, new_move->direction);
            move_package_add(package, new_move);
          }
          free(letters_used);
        }
      }

     /* Turn around or stop recursing */
      Node *append_child = node_has_child(node, '#');
      if ( append_child != NULL ) {
        word_findall_hook(board, letters, y, x, hook_type, 1, move, append_child, package);
      }
      return;
    }

   /* At the bottom, check for a word and quit */
    if ( next_y > 14 ) {
     /* Check node to see if we made a word */
      if ( node->terminal ) {

       /* Make sure we actually used a word from our rack, add package if true */
        char *letters_used = word_letters_used(board, move.word, move.y, move.x, move.direction);
        if ( letters_used != NULL ) {
          Move *new_move = move_new();
          memcpy(new_move, &move, sizeof(Move));

          new_move->score += board_score(board, new_move->word, new_move->y, new_move->x, new_move->direction);
          move_package_add(package, new_move);

          free(letters_used);
        }
      }
      return;
    }

   /* Current cell has a value */
    if ( board->cell[next_y][x] ) {

     /* If the letter on the board could continue this word, add it and recurse */
      Node *child = node_has_child(node, toupper(board->cell[next_y][x]));
      if ( child != NULL ) {
        Move new_move;
        memcpy(&new_move, &move, sizeof(Move));

       /* If we are appending, add it to the end of the word */
        int length = strlen(new_move.word);
        if ( position > 0 ) {
          new_move.word[length] = board->cell[next_y][x];
          new_move.word[length+1] = 0x0;

       /* Else, prepend the letter */
        } else {
          int i;
          for (i=length; i>0; i--) {
            new_move.word[i] = new_move.word[i-1];
          }
          new_move.word[0] = board->cell[next_y][x];
        }

       /* Increment position, but keep moving in the same direction */
        if ( position > 0 ) {
          position++;
        } else {
          position--;
        }

       /* Update the starting y position if needed */
        if ( new_move.y > next_y ) {
          new_move.y = next_y;
        }

        word_findall_hook(board, letters, y, x, hook_type, position, new_move, child, package);
      }
      return;

   /* Current cell is empty */
    } else {
     /* Check to see if we've made a word already */
      if ( node->terminal ) {
       /* Add it to the package if we actually used a letter from our rack */
        char *letters_used = word_letters_used(board, move.word, move.y, move.x, move.direction);
        if ( letters_used != NULL ) {
          Move *new_move = move_new();
          memcpy(new_move, &move, sizeof(Move));

          new_move->score += board_score(board, new_move->word, new_move->y, new_move->x, new_move->direction);
          move_package_add(package, new_move);

          free(letters_used);
        }
      }

     /* Evaluate each letter in our rack to see if it could help make a word */
      int i;
      int letter_count = strlen(letters);
      for (i=0; i<letter_count; i++) {
        Node *child = node_has_child(node, letters[i]);
        if ( child != NULL ) {
          int crossword_score = word_valid_crossword(board, letters[i], next_y, x, move.direction);

         /* Crossword was illegal, move on */
          if ( crossword_score < 0 ) {
            continue;
          }

         /* Copy move for the recursive call */
          Move new_move;
          memcpy(&new_move, &move, sizeof(Move));
          new_move.score += crossword_score;

         /* If we are appending, add this letter to the end of the word */
          int length = strlen(new_move.word);
          if ( position > 0 ) {
            new_move.word[length] = board->cell[next_y][x];
            new_move.word[length+1] = 0x0;

         /* Else, prepend the letter */
          } else {
            int i;
            for (i=length; i>0; i--) {
              new_move.word[i] = new_move.word[i-1];
            }
            new_move.word[0] = board->cell[next_y][x];
          }

         /* Increment next position, but keep moving in the same direction */
          int next_position = position;
          if ( next_position > 0 ) {
            next_position++;
          } else {
            next_position--;
          }

         /* Copy letters and remove the current value */
          char new_letters[8];
          memcpy(&new_letters, letters, sizeof(char)*(strlen(letters)+1));

          unsigned int j;
          for (j=i; j<strlen(letters); j++) {
            new_letters[j] = new_letters[j+1];
          }

          word_findall_hook(board, new_letters, y, x, hook_type, next_position, new_move, child, package);

        } else if ( letters[i] == '*' ) {

        }
      }
    }

   /* Start building the suffix if we havnt already */
    Node *child = node_has_child(node, '#');
    if ( child != NULL ) {
      word_findall_hook(board, letters, y, x, hook_type, 1, move, child, package);
    }

 /* Horizontal hook */
  } else {

  }
}

char* word_letters_used(Board *board, char *word, int y, int x, char direction) {
/* Return the string of letters we need from our rack for this play, NULL if none were used */
  char *letters = malloc(sizeof(char)*(strlen(word)+1));
  int letter_count = 0;

  letters[0] = 0x0;
  int y_offset = 0;
  int x_offset = 0;

 /* Set the direction counters */
  if ( direction == HORIZONTAL ) {
    x_offset = 1;
  } else {
    y_offset = 1;
  }

  unsigned int i;
  for (i=0; i<strlen(word); i++) {
   /* Space on the board is empty, the letter must come from our rack */
    if ( ! board->cell[y][x] ) {
      letters[letter_count] = board->cell[y][x];
      letter_count++;
      letters[letter_count] = 0x0;
    }

    x += x_offset;
    y += y_offset;
  }

 /* Return list of letters if they exist, NULL otherwise */
  if ( letter_count ) {
    return letters;

  } else {
    free(letters);
    return NULL;
  }
}

int word_valid_crossword(Board *board, char letter, int y, int x, char direction) {
 /* Return 0 if there is no crossword created */
  if ( board->cell[y][x] ) {
    return 0;
  }

  char word[16] = {letter, 0x0};
  int word_letters = 1;
  int start_y = y;
  int start_x = x;

 /* Crossword is horizontal */
  if ( direction == HORIZONTAL ) {
    if ( x > 0 ) {
      int pos = x-1;
      while ( pos >= 0 && board->cell[y][pos] ) {
        int i;
        for (i=word_letters+1; i>0; i--) {
          word[i] = word[i-1];
        }
        word[0] = board->cell[y][pos];
        word_letters++;
        pos--;
      }
    }

    if ( x < 14 ) {
      int pos = x+1;
      while ( pos >= 0 && board->cell[y][pos] ) {
        word[word_letters] = board->cell[y][pos];
        word[word_letters+1] = 0x0;
        word_letters++;
        pos++;
      }
    }

 /* Crossword is vertical */
  } else {
    if ( y > 0 ) {
      int pos = y-1;
      while ( pos >= 0 && board->cell[pos][x] ) {
        int i;
        for (i=word_letters+1; i>0; i--) {
          word[i] = word[i-1];
        }
        word[0] = board->cell[pos][x];
        word_letters++;
        pos--;
      }
    }

    if ( y < 14 ) {
      int pos = y+1;
      while ( pos >= 0 && board->cell[pos][x] ) {
        word[word_letters] = board->cell[pos][x];
        word[word_letters+1] = 0x0;
        word_letters++;
        pos++;
      }
    }
  }

 /* There was no crossword, only our letter */
  if ( word_letters == 1 ) {
    return 0;
  }

 /* Return boardscore if word is valid, -1 if otherwise */
  if ( word_is_valid(word) ) {
    return board_score(board, word, start_y, start_x, direction);
  }
  return -1;
}

boolean word_is_valid(char *word) {
/* Return true if the word is valid, false if otherwise */
  Node *node = Master_GADDAG;

 /* Verify the word exists in the Master GADDAG structure */
  int i;
  for (i=strlen(word)-1; i>=0; i--) {
    node = node_has_child(node, word[i]);
    if ( node == NULL ) {
      return false;
    }
  }

 /* Since this is the last node it must be terminal */
  if ( node->terminal ) {
    return true;
  }

  return false;
}

boolean word_is_hook_cell (Board *board, int y, int x) {
/* Determine if this cell is a hook cell, and what type, on the supplied board */

 /* Flags array is {horizontal, vertical} */
  unsigned int flags[3] = {0, 0};

 /* This cell is filled, determine which vertici to search for words */
  if ( board->cell[y][x] ) {
   /* we are at the bottom of the board, or we are the last filled cell in this column */
    if ( y == 14 ) {
      flags[1] = 1;
    } else if ( board->cell[y+1][x] == 0 ) {
      flags[1] = 1;
    }

   /* We are at the far right of the board, or we are the last filled cell in this row */
    if ( x == 14 ) {
      flags[0] = 1;
    } else if ( board->cell[y][x+1] == 0 ) {
      flags[0] = 1;
    }

 /* This cell is empty, determine which vertici to search */
  } else {
   /* If we have an occupied cell above or below, allow vertical searches */
    if ( y == 14 ) {
      if ( !board->cell[y-1][x] ) {
        flags[1] = 1;
      }
    } else if ( y == 0 ) {
      if ( !board->cell[y+1][x] ) {
        flags[1] = 1;
      }
    } else if ( !board->cell[y-1][x] && !board->cell[y+1][x] ) {
      flags[1] = 1;
    }

   /* If we have an occupied cell above or below, allow horizontal searches */
    if ( x == 14 ) {
      if ( !board->cell[y][x-1] ) {
        flags[0] = 1;
      }
    } else if ( x == 0 ) {
      if ( !board->cell[y][x+1] ) {
        flags[0] = 1;
      }
    } else if ( !board->cell[y][x-1] && !board->cell[y][x+1] ) {
      flags[0] = 1;
    }

    if ( flags[0]+flags[1] != 1 ) {
      return NOT_HOOK;
    }
  }

  int hook_type = flags[0]*2 + flags[1];
  return hook_type;
}


