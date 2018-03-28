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

package Assistant;

import Dictionary.*;
import Game.*;

// Routines to find words, manipulate game objects, etc...
public class WordPlay {

    ////////////////////////////////////////////////////////////////////////////
    //  These routines find words within a rack, user supplies the dictionary //
    ////////////////////////////////////////////////////////////////////////////

    // Find all the words within this rack
    public static MovePackage wordFindall(Rack rack, Dawg dawg) {
        MovePackage wordList = new MovePackage();

        AssistantRack findallRack = new AssistantRack(rack);
        findallRack.sort();
        wordFindallSubsets(new AssistantRack(findallRack), new AssistantRack(), 0, wordList, dawg);

        return wordList;
    }

    // Find all subsets of this rack, then check for valid words, update wordList
    private static void wordFindallSubsets(AssistantRack rack, AssistantRack subset, int position, MovePackage wordList, Dawg dawg) {
        while (position < rack.size()) {
            AssistantRack nextRack = new AssistantRack(rack);
            AssistantRack nextSubset = new AssistantRack(subset);
            wordFindallSubsets(nextRack, nextSubset, position + 1, wordList, dawg);

            subset.add(rack.remove(position));
            subset.sort();

            // Test for wordRack, add if its good
            subset.sort();
            do {
                wordFindallValidWord(new AssistantRack(subset), 0, dawg, wordList);
            } while (subset.permutate());
        }
    }

    // Recursively determine if this rack has formed a valid wordRack, compensating for blanks
    private static void wordFindallValidWord(AssistantRack rack, int position, Dawg dawg, MovePackage wordList) {
        // Past the end of the rack, check for wordRack and store results
        if ( position == rack.size() ) {

            // Add the wordRack if it is valid
            if ( dawg.isTerminal() ) {
                // Check that the wordRack doesnt violate the dictionary constraints
                if ( dawg.getConstraint().validate(rack.toValueString()) ) {
                    Boolean isBingo = false;
                    if ( rack.size() == Rack.MAX_TILES ) { isBingo = true; }

                    Move move;
                    if ( isBingo ) {
                        move = new Move(rack.toValueString(), rack, null, 7, 7, Move.HORIZONTAL, rack.points()+50, true);
                    } else {
                        move = new Move(rack.toValueString(), rack, null, 7, 7, Move.HORIZONTAL, rack.points(), false);
                    }
                    wordList.add(move);

                }
            }
            return;
        }

        // Current tile is a blank, treat it as such
        if ( rack.tile(position).isBlank() ) {
            Dawg nextNode = dawg.child();
            while ( nextNode != null ) {
                rack.tile(position).selectLetter(nextNode.value());
                wordFindallValidWord(rack, position+1, nextNode, wordList);

                nextNode = nextNode.sibling();
            }
        } else {
            Dawg child = dawg.child(rack.tile(position).value());
            if ( child != null ) {
                wordFindallValidWord(rack, position+1, child, wordList);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////
    //  These routines find words on the board, user supplies the dictionary  //
    ////////////////////////////////////////////////////////////////////////////

    // Find all moves on the board
    public static MovePackage boardFindall(Board board, Rack rack, Dawg dawg) {
        MovePackage wordList = new MovePackage();

        // If the board is emtpy, only find horizontal moves hooking onto 7,7
        if ( board.empty() ) {
            boardFindallHorizontalMoves(board, rack, dawg, wordList, 7, 7);
            return wordList;
        }

        // Otherwise, find moves for every position
        for (int row=0; row<board.Rows(); row++) {
            for (int col=0; col<board.Columns(); col++) {
                boardFindallVerticalMoves(board, rack, dawg, wordList, row, col);
                boardFindallHorizontalMoves(board, rack, dawg, wordList, row, col);
            }
        }
        return wordList;
    }
    
    // Find every move that could be made at this positon on the board,
    // at this position, and return the list of words
    public static MovePackage boardFindallAtPosition(Board board, Rack rack, Dawg dawg, int row, int col) {
        MovePackage wordList = new MovePackage();

        boardFindallVerticalMoves(board, rack, dawg, wordList, row, col);
        boardFindallHorizontalMoves(board, rack, dawg, wordList, row, col);

        return wordList;
    }

    // Find all possible starting positions and set up the recursive call for horizontal and vertical moves
    public static void boardFindallVerticalMoves(Board board, Rack rack, Dawg dawg, MovePackage moveList, int row, int col) {
        // Dont bother looking if this position isnt a hook
        if ( ! boardPositionIsValidHook(board, row, col) ) { return; }

        // Find the furthest NORTH starting postion
        int startingRow = row;
        int tilesNeeded = 1;
        while ( startingRow > 0 && tilesNeeded < Rack.MAX_TILES ) {
            if ( boardPositionIsValidHook(board, startingRow-1, col) ) { break; }

            startingRow--;
            if ( board.tile(startingRow, col) == null ) {
                tilesNeeded++;
            }
        }

        // Find the furthest SOUTH this move could have
        while ( row < board.Rows() && board.tile(row+1, col) != null ) {
            row++;
        }

        // Special case: We hooked on the bottom, move row up to the last occupied cell
        if ( row > 0 && board.tile(row, col) == null && board.tile(row-1, col) != null ) {
            row--;
        }

        // Call recursive function for each cell from startingRow to row
        while ( startingRow <= row ) {
            // Stop if we run into an occupied tile
            if ( board.tile(startingRow, col) != null && startingRow != row ) { break; }

            AssistantRack findallRack = new AssistantRack(rack);
            AssistantRack findallWordRack = new AssistantRack();
            Move findallMove = new Move("", findallWordRack, findallRack, startingRow, col, Move.VERTICAL, 0, false);

            boardFindallVerticalMovesAtPosition(board, findallRack, findallWordRack, dawg, dawg, row, moveList, findallMove);

            startingRow++;
        }
    }
    public static void boardFindallHorizontalMoves(Board board, Rack rack, Dawg dawg, MovePackage moveList, int row, int col) {
        // Dont bother looking if this position isnt a hook
        if ( ! boardPositionIsValidHook(board, row, col) ) { return; }

        // Find the furthest WEST starting postion
        int startingCol = col;
        int tilesNeeded = 1;
        while ( startingCol > 0 && tilesNeeded < Rack.MAX_TILES ) {
            if ( boardPositionIsValidHook(board, row, startingCol-1) ) { break; }

            startingCol--;
            if ( board.tile(row, startingCol) == null ) {
                tilesNeeded++;
            }
        }

        // Find furthest EAST of this move
        while ( col < board.Columns() && board.tile(row, col+1) != null ) {
            col++;
        }

        // Special case: We hooked on the right, move left to the last occupied cell
        if ( col > 0 && board.tile(row, col) == null && board.tile(row, col-1) != null ) {
            col--;
        }

        // Call recursive function for each cell from startingRow to row
        while ( startingCol <= col ) {
            // Stop if we run into an occupied tile
            if ( board.tile(row, startingCol) != null && startingCol != col) { break; }

            AssistantRack findallRack = new AssistantRack(rack);
            AssistantRack findallWordRack = new AssistantRack();
            Move findallMove = new Move("", findallWordRack, findallRack, row, startingCol, Move.HORIZONTAL, 0, false);

            boardFindallHorizontalMovesAtPosition(board, findallRack, findallWordRack, dawg, dawg, col, moveList, findallMove);

            startingCol++;
        }
    }

    // Recursively find words from row,col to hookCol,hookCol and beyond
    // These routine are only intended to be used by boardFindallVerticalMoves and boardFindallHorizontalMoves
    private static void boardFindallVerticalMovesAtPosition(Board board, AssistantRack rack, AssistantRack wordRack, Dawg dawg, Dawg node, int hookRow, MovePackage moveList, Move move) {
        // Build a wordRack with our rack and this board
        if ( wordRack == null ) { wordRack = new AssistantRack(); }

        int currentRow = move.row()+move.wordPlayed().length();

        // Current location on the board is not empty, advance vertically adding the values to the move
        while( board.tile(currentRow, move.col()) != null ) {
            node = node.child(board.tile(currentRow, move.col()).value());
            if ( node == null ) { return; }

            move = new Move(move.wordPlayed().concat(Character.toString(board.tile(currentRow, move.col()).value())), move.tilesPlayed(), move.tilesRemaining(), move.row(), move.col(), move.direction(), move.score(), false);
            currentRow++;
        }

        // Dont go past the end of the board
        if ( currentRow > Board.MAX_ROWS ) { return; }

        // Determine if this position and word makes a valid word
        if ( currentRow > hookRow ) {

            // We actually have a play on this board, add it to the moveList
            if ( node.isTerminal() ) {
                // Create the final move and add it to the list
                int boardScore = board.score(move.wordPlayed(), move.tilesPlayed(), move.row(), move.col(), move.direction());
                Move finalMove;
                if ( move.tilesPlayed().size() == Rack.MAX_TILES ) {
                    finalMove = new Move(move.wordPlayed(), move.tilesPlayed(), move.tilesRemaining(), move.row(), move.col(), move.direction(), move.score()+boardScore, true);
                } else {
                    finalMove = new Move(move.wordPlayed(), move.tilesPlayed(), move.tilesRemaining(), move.row(), move.col(), move.direction(), move.score()+boardScore, false);
                }
                moveList.add(finalMove);
            }
        }

        // Walk through the rack and build a word with the remaining tiles
        for (int index=0; index<rack.size(); index++) {
            if (rack.tile(index).isBlank()) {
                // Tile is a blank, make it be every possible value
                Dawg nextNode = node.child();
                while ( nextNode != null ) {
                    rack.tile(index).selectLetter(nextNode.value());

                    int crosswordScore = boardValidCrossword(board, rack.tile(index), dawg, currentRow, move.col(), move.direction());

                    // This didnt create a valid crossword, move to the next option
                    if (crosswordScore < 0) {
                        nextNode = nextNode.sibling();
                        continue;
                    }

                    // Set up the recursive call here
                    AssistantRack nextRack = new AssistantRack(rack);
                    AssistantRack nextWordRack = new AssistantRack(wordRack);

                    String nextWord = move.wordPlayed() + nextRack.tile(index).value();
                    nextWordRack.add(nextRack.remove(index));

                    // Only recurse if we dont move past the end of the board
                    if (move.row() + nextWord.length() < Board.MAX_ROWS) {
                        Move nextMove = new Move(nextWord, nextWordRack, nextRack, move.row(), move.col(), move.direction(), move.score() + crosswordScore, false);
                        boardFindallVerticalMovesAtPosition(board, nextRack, nextWordRack, dawg, nextNode, hookRow, moveList, nextMove);
                    }

                    nextNode = nextNode.sibling();
                }

            } else {
                // Tile is not a blank, simply add it to the move and see if it works
                Dawg nextNode = node.child(rack.tile(index).value());
                if (nextNode == null) {
                    // Not a valid move, continue to the next tile
                    continue;
                }

                int crosswordScore = boardValidCrossword(board, rack.tile(index), dawg, currentRow, move.col(), move.direction());

                // This didnt create a valid crossword, move to the next option
                if (crosswordScore < 0) {
                    continue;
                }

                // Set up the recursive call here
                AssistantRack nextRack = new AssistantRack(rack);
                AssistantRack nextWordRack = new AssistantRack(wordRack);

                String nextWord = move.wordPlayed() + nextRack.tile(index).value();
                nextWordRack.add(nextRack.remove(index));

                Move nextMove = new Move(nextWord, nextWordRack, nextRack, move.row(), move.col(), move.direction(), move.score() + crosswordScore, false);
                boardFindallVerticalMovesAtPosition(board, nextRack, nextWordRack, dawg, nextNode, hookRow, moveList, nextMove);
            }
        }
    }
    private static void boardFindallHorizontalMovesAtPosition(Board board, AssistantRack rack, AssistantRack wordRack, Dawg dawg, Dawg node, int hookCol, MovePackage moveList, Move move) {
        // Build a wordRack with our rack and this board
        if ( wordRack == null ) { wordRack = new AssistantRack(); }

        int currentCol = move.col()+move.wordPlayed().length();

        // Current location on the board is not empty, advance horizontally adding the values to the move
        while( board.tile(move.row(), currentCol) != null ) {
            node = node.child(board.tile(move.row(), currentCol).value());
            if ( node == null ) { return; }

            move = new Move(move.wordPlayed().concat(Character.toString(board.tile(move.row(), currentCol).value())), move.tilesPlayed(), move.tilesRemaining(), move.row(), move.col(), move.direction(), move.score(), false);
            currentCol++;
        }

        // Dont go past the end of the board
        if ( currentCol > Board.MAX_COLUMNS ) { return; }

        // Determine if this position and word makes a valid word
        if ( currentCol > hookCol ) {
            // We actually have a play on this board, add it to the moveList
            if ( node.isTerminal() ) {
                // Create the final move and add it to the list
                int boardScore = board.score(move.wordPlayed(), move.tilesPlayed(), move.row(), move.col(), move.direction());
                Move finalMove;
                if ( move.tilesPlayed().size() == Rack.MAX_TILES ) {
                    finalMove = new Move(move.wordPlayed(), move.tilesPlayed(), move.tilesRemaining(), move.row(), move.col(), move.direction(), move.score()+boardScore, true);
                } else {
                    finalMove = new Move(move.wordPlayed(), move.tilesPlayed(), move.tilesRemaining(), move.row(), move.col(), move.direction(), move.score()+boardScore, false);
                }
                moveList.add(finalMove);
            }
        }

        // Walk through the rack and build a word with the remaining tiles
        for (int index=0; index<rack.size(); index++) {
            if (rack.tile(index).isBlank()) {
                // Tile is a blank, make it be every possible value
                Dawg nextNode = node.child();
                while (nextNode != null) {
                    rack.tile(index).selectLetter(nextNode.value());

                    int crosswordScore = boardValidCrossword(board, rack.tile(index), dawg, move.row(), currentCol, move.direction());

                    // This didnt create a valid crossword, move to the next option
                    if (crosswordScore < 0) {
                        nextNode = nextNode.sibling();
                        continue;
                    }

                    // Set up the recursive call here
                    AssistantRack nextRack = new AssistantRack(rack);
                    AssistantRack nextWordRack = new AssistantRack(wordRack);

                    String nextWord = move.wordPlayed() + nextRack.tile(index).value();
                    nextWordRack.add(nextRack.remove(index));

                    // Only recurse if we dont move past the end of the board
                    if (move.col() + nextWord.length() < Board.MAX_COLUMNS) {
                        Move nextMove = new Move(nextWord, nextWordRack, nextRack, move.row(), move.col(), move.direction(), move.score() + crosswordScore, false);
                        boardFindallHorizontalMovesAtPosition(board, nextRack, nextWordRack, dawg, nextNode, hookCol, moveList, nextMove);
                    }

                    nextNode = nextNode.sibling();
                }
            } else {
                // Tile is not a blank, simply add it to the move and see if it works
                Dawg nextNode = node.child(rack.tile(index).value());
                if (nextNode == null) {
                    continue;
                }

                int crosswordScore = boardValidCrossword(board, rack.tile(index), dawg, move.row(), currentCol, move.direction());

                // This didnt create a valid crossword, move to the next option
                if (crosswordScore < 0) {
                    continue;
                }

                // Set up the recursive call here
                AssistantRack nextRack = new AssistantRack(rack);
                AssistantRack nextWordRack = new AssistantRack(wordRack);

                String nextWord = move.wordPlayed() + nextRack.tile(index).value();
                nextWordRack.add(nextRack.remove(index));

                Move nextMove = new Move(nextWord, nextWordRack, nextRack, move.row(), move.col(), move.direction(), move.score() + crosswordScore, false);
                boardFindallHorizontalMovesAtPosition(board, nextRack, nextWordRack, dawg, nextNode, hookCol, moveList, nextMove);
            }
        }
    }

    // Determine if this position is a hook of any type
    public static boolean boardPositionIsValidHook(Board board, int row, int col) {
        // If the board is empty, the only hook is 7,7
        if ( board.empty() ) {
            if ( row == 7 && col == 7 ) {
                return true;
            } else {
                return false;
            }
        }

        // Cant be a hook if the position is filled
        if ( board.tile(row, col) != null ) {
            return false;
        }

        // Hooking to a wordRack on the right side
        if ( col < board.Columns()-1 && board.tile(row, col+1) != null ) {
            return true;
        }

        // Hooking to a wordRack on the left side
        if ( col > 0 && board.tile(row, col-1) != null ) {
            return true;
        }

        // Hooking to a word on the bottom
        if ( row < board.Rows() && board.tile(row+1, col) != null ) {
            return true;
        }

        // Hooking to a word on the top
        if ( row > 0 && board.tile(row-1, col) != null ) {
            return true;
        }

        return false;
    }

    // Determine if this letter at this position makes a valid crossword, return the score of the crossword
    private static int boardValidCrossword(Board board, Tile tile, Dawg dawg, int row, int col, int direction) {
        // It doesnt make a crossword if this isnt a hook
        if ( ! boardPositionIsValidHook(board, row, col) ) { return 0; }

        String word = new String();
        int score = 0;

        // Horizontal moves only make vertical crosswords
        if (direction == Move.HORIZONTAL) {
            // Determine if there are any vertical words created
            int startingRow = row;
            int startingCol = col;

            // Start by finding the highest tile
            while (startingRow > 0 && board.tile(startingRow - 1, col) != null) {
                startingRow--;
            }
            int endingRow = startingRow;

            // Then walk down the board
            word = "";
            while (board.tile(endingRow, col) != null) {
                word += board.tile(endingRow, col).value();
                endingRow++;
            }

            // Append the letter to the word we've creaed so far, then keep going down
            word += tile.value();
            while (board.tile(endingRow + 1, col) != null) {
                endingRow++;
                word += board.tile(endingRow, col).value();
            }

            // Determine if we created a valid word or not
            if (word.length() > 1) {
                if (!dawg.validWord(word)) {
                    return -1;
                }
                AssistantRack wordRack = new AssistantRack();
                wordRack.add(tile);
                score += board.score(word, wordRack, startingRow, startingCol, Move.VERTICAL);
            }
        }


        // Vertical moves only make horizontal crosswords
        if (direction == Move.VERTICAL) {
            // Determine if there are any horizontal words created
            int startingRow = row;
            int startingCol = col;

            // Start by finding the furthest left tile
            while (startingCol > 0 && board.tile(row, startingCol - 1) != null) {
                startingCol--;
            }
            int endingCol = startingCol;

            // Then walk the board right
            word = "";
            while (board.tile(row, endingCol) != null) {
                word += board.tile(row, endingCol).value();
                endingCol++;
            }

            // Append the letter to the word we've created so far, then keep going right
            word += tile.value();
            while (board.tile(row, endingCol + 1) != null) {
                endingCol++;
                word += board.tile(row, endingCol).value();
            }

            // Determine if we created a valid word or not
            if (word.length() > 1) {
                if (!dawg.validWord(word)) {
                    return -1;
                }
                AssistantRack wordRack = new AssistantRack();
                wordRack.add(tile);
                score += board.score(word, wordRack, startingRow, startingCol, Move.HORIZONTAL);
            }
        }
        return score;
    }

    // This makes a move from a staged play on the board
    public static Move boardMakeMove(Board board, Dawg dawg) {
        // If this is a rackboard call the special routine instead
        if ( board instanceof RackBoard ) {
            return rackBoardMakeMove(board);
        }

        int realStartingRow = board.startingRow();
        int realStartingCol = board.startingCol();

        // Determine which direction we're going in
        int rowVector = 0;
        int colVector = 0;
        if ( board.directionPlaying() == Move.HORIZONTAL ) {
            colVector = 1;
        } else if ( board.directionPlaying() == Move.VERTICAL ) {
            rowVector = 1;
        } else {
            // Nothing was actually staged
            return null;
        }

        // Find real starting position
        while ( board.tile(realStartingRow-rowVector, realStartingCol-colVector) != null ) {
            realStartingRow -= rowVector;
            realStartingCol -= colVector;
        }

        // I cannot remember why this was here, leaving it in case my memory is jogged
        //if ( board.tile(realStartingRow, realStartingCol) != null ) {
        //    realStartingRow -= rowVector;
        //    realStartingCol -= colVector;
        //}

        // Something went wrong finding the starting position
        if ( realStartingRow < 0 || realStartingCol < 0 ) { return null; }

        // Build the word and boardscore here
        int endingRow = realStartingRow;
        int endingCol = realStartingCol;

        // Make sure we have a hook
        boolean hasHook = false;

        String word = new String();
        int score = 0;
        int tilesPlayedIndex = 0;
        while ( tilesPlayedIndex < board.tilesPlayed().size() || board.tile(endingRow, endingCol) != null ) {
            if ( boardPositionIsValidHook(board, endingRow, endingCol) ) {
                hasHook = true;
            }

            if ( board.tile(endingRow, endingCol) == null ) {
                if ( tilesPlayedIndex >= board.tilesPlayed().size() ) { return null; }

                // Validate any crosswords created, return null on failure
                int boardScore = boardValidCrossword(board, board.tilesPlayed().tile(tilesPlayedIndex), dawg, endingRow, endingCol, board.directionPlaying());
                if ( boardScore < 0 ) { return null; }
                score += boardScore;

                word += board.tilesPlayed().tile(tilesPlayedIndex).value();
                tilesPlayedIndex++;
            } else {
                word += board.tile(endingRow, endingCol);
            }

            endingRow += rowVector;
            endingCol += colVector;
        }

        // No position was a hook, move is not valid
        if ( ! hasHook ) {
            return null;
        }

        // Only build a move if the word created is valid
        if ( dawg.validWord(word) ) {
            score += board.score(word, board.tilesPlayed(), realStartingRow, realStartingCol, board.directionPlaying());
            if ( board.tilesPlayed().size() == Rack.MAX_TILES ) {
                return new Move(word, board.tilesPlayed(), null, realStartingRow, realStartingCol, board.directionPlaying(), score, true);
            }
            return new Move(word, board.tilesPlayed(), null, realStartingRow, realStartingCol, board.directionPlaying(), score, false);
        }

        // Word created was bogus
        return null;
    }

    // For a rackBoard we just need to generate the score for our new move
    private static Move rackBoardMakeMove(Board board) {
        if ( board.currentState() == Board.NORMAL ) { return null; }

        if ( board.tilesPlayed().size() == Rack.MAX_TILES ) {
            return new Move(board.tilesPlayed().toValueString(), board.tilesPlayed(), null, 7, 7, Move.HORIZONTAL, board.score(board.tilesPlayed().toValueString(), board.tilesPlayed(), board.startingRow(), board.startingCol(), board.directionPlaying()), true);
        } else {
            return new Move(board.tilesPlayed().toValueString(), board.tilesPlayed(), null, 7, 7, Move.HORIZONTAL, board.score(board.tilesPlayed().toValueString(), board.tilesPlayed(), board.startingRow(), board.startingCol(), board.directionPlaying()), false);
        }
    }
}
