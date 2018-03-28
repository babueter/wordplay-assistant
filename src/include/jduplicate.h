/*
*/

#include <board.h>
#include <wordplay.h>

#ifndef FILE_JDUPLICATE
#define FILE_JDUPLICATE

#define SCRABBLE 1
#define DUPLICATE 2

typedef struct jdup_player {
	char name[25];
	char hand[8];
	Move last_play;
} Jdup_Player;

typedef struct jdup_gamepack {
	unsigned int gametype;
	Jdup_Player *player[8];
	int players;
	Board board;
} Jdup_Gamepack;

extern void jdup_analyse_game(char*);
extern Jdup_Gamepack* jdup_gamepack_new();
extern Jdup_Player* jdup_gamepack_has_player(Jdup_Gamepack*, char*);
extern void jdup_gamepack_add_player(Jdup_Gamepack*, Jdup_Player*);
extern Jdup_Player* jdup_player_new(char*);
extern void jdup_gamepack_free(Jdup_Gamepack*);

#endif	/* FILE_JDUPLICATE */
