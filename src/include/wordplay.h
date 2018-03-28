/*
*/
#include <stdlib.h>

#include <dawg.h>
#include <board.h>

/* Prevent double loading */
#ifndef FILE_WORDPLAY
#define FILE_WORDPLAY

#define VERTICAL 'v'
#define HORIZONTAL 'h'

Node *Master_GADDAG;

typedef struct move {
	int y;
	int x;
	char direction;
	char word[16];
	char leave[8];
	int score;
	int adjusted_score;
} Move;

#define MAX_PACKAGE_SIZE 250
typedef struct movepackage {
	Move *moves[MAX_PACKAGE_SIZE];
	int count;
} MovePackage;


extern Move* move_best_starting_position(char*);
extern Move* move_new(void);
extern void move_clear(Move*);
extern void move_print (Move*);
extern MovePackage* move_package_new(void);
extern void move_package_add(MovePackage*, Move*);
extern void move_package_free(MovePackage*);
extern void move_package_print (MovePackage*);
extern void move_package_sort(MovePackage*);

extern void word_findall(char*, Node*, MovePackage*, Move);
extern void word_findall_board(Board*, char*, MovePackage*);

#endif	/* FILE_WORDPLAY */
