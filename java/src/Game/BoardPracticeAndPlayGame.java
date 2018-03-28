/*  This file is part of Wordplay Assistant.
 *
 *  Copyright 2012 Bryan Bueter
 *
 *  Wordplay Assistant is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Wordplay Assistant is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Wordplay Assistant.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package Game;

import Dictionary.*;

public class BoardPracticeAndPlayGame extends BoardPracticeGame {
    private Move lastCommittedMove;

    // Game states
    int state;
    public static final int PLAYING = 0;
    public static final int GAMEOVER = 1;

    // Super constructor
    public BoardPracticeAndPlayGame(Dawg initDawg) {
        super(initDawg);

        lastCommittedMove = null;
        state = PLAYING;
    }

    // Public access to private variables
    public Move lastCommittedMove() {return lastCommittedMove; }
    public int state() { return state; }

    // Start the game all over
    @Override
    public void reset() {
        emptyBoard();
        drawNewTiles();
        
        moves = Assistant.WordPlay.boardFindall(board, rack, masterDictionary());
    }

    // Commit the staged move currently on the board
    public void commitMove() {
        // Nothing currently staged
        if ( board.currentState() == Board.NORMAL ) {
            // Dilemma - nulling this now helps the gui know that nothing was committed
            // However, it prevents us from referencing the previous move if an errant commit occurs
            lastCommittedMove = null;
            return;
        }

        lastCommittedMove = Assistant.WordPlay.boardMakeMove(board, this.masterDictionary());
        board.finishPlayingTiles();

        // Move was invalid
        if ( lastCommittedMove == null ) { return; }
        
        // Commit the move and re-draw tiles
        board.add(lastCommittedMove);
        drawNewTiles(lastCommittedMove);

        if ( rack.size() == 0 ) {
            state = GAMEOVER;
            moves = new MovePackage();
        } else {
            moves = Assistant.WordPlay.boardFindall(board, rack, masterDictionary());
        }
    }

    public void drawNewTiles(Move move) {
        for (int index=0; index<move.tilesPlayed().size(); index++) {
            rack.remove(move.tilesPlayed().tile(index));
        }

        while ( rack.size() < Rack.MAX_TILES && bag.tilesLeft() > 0 ) {
            rack.add(bag.draw());
        }
    }
}
