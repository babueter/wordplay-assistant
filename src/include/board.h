/*
*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <tiles.h>

/* Prevent double loading */
#ifndef FILE_BOARD
#define FILE_BOARD

typedef struct board {
	char cell[15][15];
} Board;

#define DLS 1
#define TLS 2
#define DWS 3
#define TWS 4

extern Board* board_new(void);
extern void   board_clear(Board*);
extern void   board_print(Board*);
extern void   board_init(void);
extern int    board_score(Board*, char*, int, int, char);

#endif	/* FILE_BOARD */
