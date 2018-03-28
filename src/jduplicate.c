/*
*/

#include <stdio.h>
#include <sys/types.h>
#include <regex.h>

#include <dawg.h>
#include <wordplay.h>
#include <jduplicate.h>

void jdup_analyse_game(char *filename) {
  FILE *fin;
  Jdup_Gamepack *Game = jdup_gamepack_new();

  regex_t *re = malloc(sizeof(regex_t));
  unsigned int i, semicolon_count;
  unsigned int j;
  unsigned int k;

  int y, x;

 /* Open file, exit on failure */
  fin = fopen(filename, "rb");
  if ( fin == NULL ) {
    printf("Error: unable to open file %s\n", filename);

    fclose(fin);

    free(re);
    jdup_gamepack_free(Game);
    return;
  }

 /* Read file and analyse game */
  char buffer[255];
  while ( fgets(buffer, 255, fin) ) {

   /* Make sure this is an english scrabble or duplicate game */
    regcomp(re, "receivedCreateRoom: creating game args = .*;.*;.*;.*$", REG_EXTENDED|REG_NOSUB);
    if ( regexec(re, buffer, (size_t)0, NULL, 0) == 0 ) {
     /* Find the game type position in the buffer */
      semicolon_count = 0;
      for (i=0; i<strlen(buffer); i++) {
        if ( buffer[i] == ';' ) {
          semicolon_count++;
        }

        if ( semicolon_count == 2 ) {
          break;
        }
      }

     /* Determine if its scrabble, duplicate, or unknown */
      if ( strncmp(buffer+i+1, "ScrEN", 5) == 0 ) {
        Game->gametype = SCRABBLE;

      } else if ( strncmp(buffer+i+1, "DupEN", 5) == 0 ) {
        Game->gametype = DUPLICATE;

      } else {
        printf("Error: This program only supports games of type 'ScrEN' and 'DupEN'\n");

        fclose(fin);
        regfree(re);

        free(re);
        jdup_gamepack_free(Game);
        return;
      }

      continue;
    }
    regfree(re);

   /* Capture each players rack in a scrabble game */
    regcomp(re, " .GS. Hands: \\{", REG_EXTENDED|REG_NOSUB);
    if ( regexec(re, buffer, (size_t)0, NULL, 0) == 0 ) {
     /* Find the hands position */
      j=0;
      while ( buffer[j] != '{' ) {
        j++;
      }
      j++;

      char player_name[25];
      k=j;
      while ( k < strlen(buffer) ) {
       /* Store the player name */
        if ( buffer[k] == '=' ) {
          strncpy(player_name, buffer+j, k-j);
          player_name[k-j] = 0x0;

          k++;
          j = k;
          continue;
        }

       /* Store the player hand */
        if ( buffer[k] == ',' || buffer[k] == '}' ) {
          Jdup_Player *player = jdup_gamepack_has_player(Game, player_name);
          if ( player == NULL ) {
            player = jdup_player_new(player_name);
            jdup_gamepack_add_player(Game, player);
          }

          strncpy(player->hand, buffer+j, k-j);
          player->hand[k-j] = 0x0;

          k += 2;
          j = k;
          continue;
        }

        k++;
      }
    }
    regfree(re);

   /* Capture the board */
    regcomp(re, "^.[0-9]+:[0-9]+:[0-9]+. .Bo. ", REG_EXTENDED|REG_NOSUB);
    y=0;
    x=0;
    if ( regexec(re, buffer, (size_t)0, NULL, 0) == 0 ) {
      /* Unexpected end of file, just return as normal */
      if ( ! fgets(buffer, 255, fin) ) {
        fclose(fin);
        regfree(re);

        jdup_gamepack_free(Game);
        free(re);
        return;
      }

     /* Read in the board and store it in the game */
      board_clear(&Game->board);
      for (i=0; i<15; i++) {
        if ( ! fgets(buffer, 255, fin) ) {
          fclose(fin);
          regfree(re);

          jdup_gamepack_free(Game);
          free(re);
          return;
        }

       /* Start at position 5 and read every other character until position 33 */
        for (j=5; j<=33; j+=2) {
          if ( buffer[j] != '.' ) {
            Game->board.cell[y][x] = buffer[j];
          }
          x++;
        }
        y++;
        x=0;
      }

      board_print(&Game->board);
      printf("\n  Player %s with rack [%7s]:\n",Game->player[0]->name, Game->player[0]->hand);
      MovePackage *package = move_package_new();
      word_findall_board(&Game->board, Game->player[0]->hand, package);
      move_package_print(package);
      char dummy[25];
      scanf("%s", dummy);

      continue;
    }
    regfree(re);


  }
  fclose(fin);
  jdup_gamepack_free(Game);
  free(re);

  return;
}

Jdup_Gamepack* jdup_gamepack_new() {
/* Return an empty and initialized gamepack */
  Jdup_Gamepack *new = malloc(sizeof(Jdup_Gamepack));

  int i;
  for (i=0; i<8; i++) {
    new->player[i] = NULL;
  }

  new->gametype = 0;
  new->players  = 0;

  board_clear(&new->board);

  return new;
}

Jdup_Player* jdup_gamepack_has_player(Jdup_Gamepack *game, char *player) {
/* Return the player object if they are a part of this game, NULL otherwise */
  int i;
  for (i=0; i<8; i++) {
    if ( game->player[i] == NULL ) {
      return NULL;
    }

    if ( strcmp(game->player[i]->name, player) == 0 ) {
      return game->player[i];
    }
  }

  return NULL;
}

void jdup_gamepack_add_player(Jdup_Gamepack *game, Jdup_Player *player) {
/* Add a player to the end of the list */
  int i;
  for (i=0; i<8; i++) {
    if ( game->player[i] == NULL ) {
      game->player[i] = player;

      if ( game->players != i ) {
        printf("Error: player count should be %d but its %d\n", i, game->players);
      }
      game->players++;
      return;
    }
  }
}

Jdup_Player* jdup_player_new(char *name) {
/* Return an new initialized player */
  Jdup_Player *new = malloc(sizeof(Jdup_Player));

  strncpy(new->name, name, 25);
  new->hand[0] = 0x0;

  new->last_play.y = 0;
  new->last_play.x = 0;
  new->last_play.score = 0;
  new->last_play.adjusted_score = 0;

  new->last_play.direction = 'h';
  new->last_play.word[0] = 0x0;
  new->last_play.leave[0] = 0x0;

  return new;
}

void jdup_gamepack_free(Jdup_Gamepack *game) {
/* Free up the memory used by this gamepack and its player */
  int i;
  for (i=0; i<game->players; i++) {
    free(game->player[i]);
    game->player[i] = NULL;
  }

  free(game);
}
