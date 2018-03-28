/*
*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <tiles.h>
#include <board.h>

Board Master;

Board* board_new () {
/* Create a new board, blank board and return a pointer to it */
  Board *new = malloc(sizeof(Board));
  board_clear(new);
  return new;
}

void board_clear(Board *board) {
/* Erase the board */
  int y,x;
  for (y=0; y<15; y++) {
    for (x=0; x<15; x++) {
      board->cell[y][x] = 0;
    }
  }
}

void board_addword(Board *board, char *word, int y, int x, char direction) {
/* add the word supplied to the board with the given vectors */
  int x_vector = 0;
  int y_vector = 0;

 /* Set the direction we are moving */
  if ( direction == 'h' ) {
    x_vector = 1;
  } else if ( direction == 'v' ) {
    y_vector = 1;
  }

 /* Direction error, exit failure */
  if ( ! (x_vector+y_vector) ) {
    printf("Error: incorrect direction supplied: %c\n", direction);
    exit(1);
  }

 /* Word is too long, exit failure */
  if ( (x_vector*strlen(word)+x) > 14 || (y_vector*strlen(word)+y) > 14 ) {
    printf("Error: adding word goes off the board: %s @(%d,%d)\n", word, y, x);
    exit(1);
  }

 /* Actually update the board */
  int position = 0;
  while ( word[position] != 0x0 ) {
    board->cell[y][x] = word[position];
    x += x_vector;
    y += y_vector;
    position++;
  }

  return;
}

int board_score(Board *board, char *word, int y, int x, char direction) {
/* Return the score of the word if it were played on this board */
  int x_vector = 0;
  int y_vector = 0;
  int letters_used = 0;

 /* Set the direction we are moving */
  if ( direction == 'h' ) {
    x_vector = 1;
  } else if ( direction == 'v' ) {
    y_vector = 1;
  }

 /* Direction error, exit failure */
  if ( ! (x_vector+y_vector) ) {
    printf("Error: incorrect direction supplied: %c\n", direction);
    exit(1);
  }

 /* Word is too long, exit failure */
  if ( (x_vector*strlen(word)+x) > 14 || (y_vector*strlen(word)+y) > 14 ) {
    printf("Error: adding word goes off the board: %s @(%d,%d)\n", word, y, x);
    exit(1);
  }

  int position = 0;
  int score = 0;
  int triple_bonus = 0;
  int double_bonus = 0;
  while ( word[position] != 0x0 ) {
    if ( board->cell[y][x] ) {
     /* cell is occupied, count only the face value of the tile */
      score += tile_score(board->cell[y][x]);

    } else {
     /* factor in any space bonus */
     if ( Master.cell[x][y] == 0 ) {
       score += tile_score(word[position]);

     } else if ( Master.cell[x][y] == DLS ) {
       score += tile_score(word[position])*2;

     } else if ( Master.cell[x][y] == TLS ) {
       score += tile_score(word[position])*3;

     } else if ( Master.cell[x][y] == DWS ) {
       score += tile_score(word[position]);
       double_bonus++;

     } else if ( Master.cell[x][y] == TWS ) {
       score += tile_score(word[position]);
       triple_bonus++;
     }
     letters_used++;
    }

   /* Advance the position */
    x += x_vector;
    y += y_vector;
    position++;
  }

 /* Add word score bonuses */
  while ( double_bonus-- ) {
    score *= 2;
  }
  while ( triple_bonus-- ) {
    score *= 3;
  }

 /* Add bingo bonus */
  if ( letters_used == 7 ) {
    score += 50;
  }

  return score;
}

void board_print(Board *board) {
/* Print the board in a standard format */
  int y,x;

  printf("   1  2  3  4  5  6  7  8  9 10 11 12 13 14 15\n");
  for (y=0; y<15; y++) {
    printf("%2d ", y+1);
    for (x=0; x<15; x++) {
      if ( board->cell[y][x] ) {
        printf("%c  ", board->cell[y][x]);
      } else {
        printf("-  ");
      }
    }
    printf("\n");
  }
  printf("\n");
}

void board_init() {
/* Initialize master objects */
  board_clear(&Master);

  Master.cell[3][0] = DLS;
  Master.cell[11][0] = DLS;
  Master.cell[0][0] = TWS;
  Master.cell[7][0] = TWS;
  Master.cell[14][0] = TWS;
  Master.cell[5][1] = TLS;
  Master.cell[9][1] = TLS;
  Master.cell[1][1] = DWS;
  Master.cell[13][1] = DWS;
  Master.cell[6][2] = DLS;
  Master.cell[8][2] = DLS;
  Master.cell[2][2] = DWS;
  Master.cell[12][2] = DWS;
  Master.cell[0][3] = DLS;
  Master.cell[7][3] = DLS;
  Master.cell[14][3] = DLS;
  Master.cell[3][3] = DWS;
  Master.cell[11][3] = DWS;
  Master.cell[4][4] = DWS;
  Master.cell[10][4] = DWS;
  Master.cell[1][5] = TLS;
  Master.cell[5][5] = TLS;
  Master.cell[9][5] = TLS;
  Master.cell[13][5] = TLS;
  Master.cell[2][6] = DLS;
  Master.cell[6][6] = DLS;
  Master.cell[8][6] = DLS;
  Master.cell[12][6] = DLS;
  Master.cell[3][7] = DLS;
  Master.cell[11][7] = DLS;
  Master.cell[7][7] = DWS;
  Master.cell[7][7] = DWS;
  Master.cell[0][7] = TWS;
  Master.cell[14][7] = TWS;
  Master.cell[2][8] = DLS;
  Master.cell[6][8] = DLS;
  Master.cell[8][8] = DLS;
  Master.cell[12][8] = DLS;
  Master.cell[1][9] = TLS;
  Master.cell[5][9] = TLS;
  Master.cell[9][9] = TLS;
  Master.cell[13][9] = TLS;
  Master.cell[4][10] = DWS;
  Master.cell[10][10] = DWS;
  Master.cell[0][11] = DLS;
  Master.cell[7][11] = DLS;
  Master.cell[14][11] = DLS;
  Master.cell[3][11] = DWS;
  Master.cell[11][11] = DWS;
  Master.cell[6][12] = DLS;
  Master.cell[8][12] = DLS;
  Master.cell[2][12] = DWS;
  Master.cell[12][12] = DWS;
  Master.cell[5][13] = TLS;
  Master.cell[9][13] = TLS;
  Master.cell[1][13] = DWS;
  Master.cell[13][13] = DWS;
  Master.cell[3][14] = DLS;
  Master.cell[11][14] = DLS;
  Master.cell[0][14] = TWS;
  Master.cell[7][14] = TWS;
  Master.cell[14][14] = TWS;
}

