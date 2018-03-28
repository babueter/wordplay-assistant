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
import Assistant.*;
import java.util.*;

// Set up a board, bag, and rack then put a random word on the board.
// Player is challenged to find the best play on the board with a random rack
public class BoardPracticeGame {
    // Allow inheritors to override these objects
    protected Board board;
    protected Bag bag;
    protected Rack rack;
    protected MovePackage moves;

    // Master dictionary used for finding connections on the board
    private Dawg masterDictionary;

    // Current dictionary used for finding words within the rack
    private Dawg currentDictionary;

    // The starting coordinates of each word
    protected static int startingRow = 7;
    protected static int startingCol = 7;

    // Must at least have a dictionary to set this up
    public BoardPracticeGame(Dawg initMasterDictionary) {
        board = new Board();
        rack = new Rack();
        moves = new MovePackage();
        bag = new Bag();

        masterDictionary = initMasterDictionary;
        setCurrentDictionary(initMasterDictionary);
    }
    protected BoardPracticeGame() { }

    // Allow anyone to change the dictionary we use
    public Dawg currentDictionary() { return currentDictionary; }
    public void setCurrentDictionary(Dawg initDictionary) {
        currentDictionary = initDictionary;
        reset();
    }

    // Only we can see the master dictionary
    protected Dawg masterDictionary() { return masterDictionary; }
    protected void setMasterDictionary(Dawg initMasterDictionary) {
        masterDictionary = initMasterDictionary;
    }

    // Set up a new game
    public void reset() {
        emptyBoard();

        // Find something to put on the board
        drawNewTiles();

        MovePackage boardWords = Assistant.WordPlay.wordFindall(rack, masterDictionary);
        while ( boardWords.isEmpty() ) {
            drawNewTiles();
            boardWords = Assistant.WordPlay.wordFindall(rack, masterDictionary);
        }

        // Pick a random move from the list and put it on the board
        Random rand = new Random();
        int wordIndex = rand.nextInt(boardWords.size());

        Move randomMove = (Move) boardWords.get(wordIndex);
        board.add(randomMove);
        rack.remove(randomMove.tilesPlayed());

        // Finally grab new letters for our rack and find words that hook on the ends
        drawNewTiles();

        moves = Assistant.WordPlay.boardFindall(board, rack, masterDictionary);
    }

    // Keep track of tiles played
    public void startPlayingTiles(int row, int col) {
        if ( board.tilesPlayed().size() == 0 && row == board.startingRow() && col == board.startingCol() ) {
            if ( board.directionPlaying() == Move.HORIZONTAL ) {
                board.startPlayingTiles(row, col, Move.VERTICAL);
            } else {
                board.startPlayingTiles(row, col, Move.HORIZONTAL);
            }
            return;
        }

        board.startPlayingTiles(row, col, Move.HORIZONTAL);
    }
    public void stopPlayingTiles() {
        board.finishPlayingTiles();
    }
    public Move finishPlayingTiles() {
        Move move = WordPlay.boardMakeMove(board, masterDictionary);
        board.finishPlayingTiles();

        return move;
    }

    public void playTile(char letter, boolean forceBlank) {
        if (!forceBlank) {
            for (int index = 0; index < rack.size(); index++) {
                // Exclude blanks the first time around
                if ( rack.tile(index).isBlank() ) { continue; }

                if (rack.tile(index).value() == letter && !board.tilesPlayed().contains(rack.tile(index))) {
                    board.playTile(rack.tile(index));
                    return;
                }
            }
        }

        // Couldnt find the real tile, or excluded it, now find a blank tile instead
        for (int index=0; index<rack.size(); index++) {
            if ( rack.tile(index).isBlank() && !board.tilesPlayed().contains(rack.tile(index)) ) {
                rack.tile(index).selectLetter(letter);
                board.playTile(rack.tile(index));
                rack.tile(index).selectLetter(letter);
                return;
            }
        }
    }
    public void removeLastTilePlayed() {
        board.removeLastTilePlayed();
    }
    public void stageMove(Move move) {
        board.stageMove(move);
    }

    // Redraw tiles
    protected void drawNewTiles() {
        emptyRack();

        // Fill the rack back up
        while ( rack.size() < Rack.MAX_TILES ) {
            rack.add(bag.draw());
        }
        rack.shuffle();
        rack.shuffle();
        rack.shuffle();
    }

    // Remove all the tiles from the board
    protected void emptyBoard() {
        for (int row = 0; row < board.Rows(); row++) {
            for (int col = 0; col < board.Columns(); col++) {
                bag.replace(board.remove(row, col));
            }
        }
        board.finishPlayingTiles();
    }

    // Remove all the tiles from the rack
    protected void emptyRack() {
        // Remove anything in the rack
        while ( rack.size() > 0 ) {
            bag.replace(rack.remove(0));
        }
    }

    // Allow anyone to see the board, bag, rack, and moves
    public Board board() { return board; }
    public Bag bag() { return bag; }
    public Rack rack() { return rack; }
    public MovePackage moves() { return moves; }
    
}
