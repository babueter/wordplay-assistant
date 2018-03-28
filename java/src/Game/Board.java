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

import Events.*;

// The board a game can be played on
public class Board {
    protected Tile[][] tiles;
    protected Rack tilesPlayed;

    protected int rows;
    protected int columns;

    // These variables allow us to keep track of any staged moves
    public static final int NORMAL = 0;
    public static final int PLAYING_HORIZONTAL = Move.HORIZONTAL;
    public static final int PLAYING_VERTICAL = Move.VERTICAL;

    protected int currentState;
    protected int currentPlayRow;
    protected int currentPlayCol;

    protected int startPlayRow;
    protected int startPlayCol;

    // To callback events after an update
    protected ObjectHandler eventHandler;

    // Constructor
    public Board() {
        tilesPlayed = new Rack();
        eventHandler = new ObjectHandler(this);

        currentState = NORMAL;

        // Set up spaces and clear the values
        tiles = new Tile[MAX_ROWS][MAX_COLUMNS];
        rows = MAX_ROWS;
        columns = MAX_COLUMNS;

        clear();
    }

    // Anyone can inquire about what is on the board
    public Tile tile(int row, int col) {
        // Return the tile on the board at row, col

        if ( row < 0 || row >= MAX_ROWS || col < 0 || col >= MAX_COLUMNS ) {
            // Out of bounds, return null
            return null;
        }
        return tiles[row][col];
    }
    public boolean empty() {
        for (int row=0; row< Rows(); row++) {
            for (int col=0; col<Columns(); col++) {
                if ( tile(row, col) != null ) { return false; }
            }
        }
        return true;
    }
    public ObjectHandler getEventHandler() { return eventHandler; }
    public int Rows() { return rows; }
    public int Columns() { return columns; }

    // Interface used for staging a move.
    // Anyone can read the current state, only class members can stage a move
    public Rack tilesPlayed() { return tilesPlayed; }
    public int currentState() { return currentState; }
    public int directionPlaying() { return currentState; }
    public int startingRow() { return startPlayRow; }
    public int startingCol() { return startPlayCol; }
    public int currentRow() { return currentPlayRow; }
    public int currentCol() { return currentPlayCol; }

    protected void stageMove(Move move) {
        startPlayingTiles(move.row(), move.col(), move.direction());
        int rowVector = 0;
        int colVector = 0;

        if ( move.direction() == Move.HORIZONTAL ) {
            colVector = 1;
        } else if ( move.direction() == Move.VERTICAL ) {
            rowVector = 1;
        } else {
            return;
        }

        int playingRow = move.row();
        int playingCol = move.col();
        int position = 0;
        int wordPosition = 0;
        while ( position < move.tilesPlayed().size() ) {
            while ( tile(playingRow, playingCol) != null ) {
                playingRow += rowVector;
                playingCol += colVector;
                wordPosition++;
            }

            playTile(move.tilesPlayed().tile(position));
            if ( move.tilesPlayed().tile(position).isBlank() ) {
                move.tilesPlayed().tile(position).selectLetter(move.wordPlayed().charAt(wordPosition));
            }
            playingRow += rowVector;
            playingCol += colVector;
            position++;
            wordPosition++;
        }
    }
    protected void startPlayingTiles(int row, int col, int direction) {
        finishPlayingTiles();
        while ( tile(row, col) != null ) {
            if ( direction == Move.HORIZONTAL ) {
                col++;
            } else if ( direction == Move.VERTICAL ) {
                row++;
            } else {
                return;
            }
        }
        if ( row >= Board.MAX_ROWS || col >= Board.MAX_COLUMNS ) { return; }

        startPlayRow = row;
        startPlayCol = col;
        currentPlayRow = startPlayRow;
        currentPlayCol = startPlayCol;

        if ( direction == Move.HORIZONTAL ) {
            currentState = PLAYING_HORIZONTAL;
        } else if ( direction == Move.VERTICAL ) {
            currentState = PLAYING_VERTICAL;
        } else {
            currentState = NORMAL;
        }

        tilesPlayed.clear();
        eventHandler.callback();
    }
    protected void playTile(Tile tile) {
        if ( tile == null ) { return; }

        // We're not staging a move
        if ( currentState == NORMAL ) { return; }

        // We're already past the end of the board
        if ( currentPlayCol >= MAX_COLUMNS || currentPlayRow >= MAX_ROWS ) { return; }

        // We've already played this tile
        if ( tilesPlayed.contains(tile) ) { return; }

        tilesPlayed.add(tile);

        // Advance to the next unoccupied cell
        do {
            if ( currentState == PLAYING_HORIZONTAL ) {
                currentPlayCol++;
            } else {
                currentPlayRow++;
            }
        } while (tile(currentPlayRow, currentPlayCol) != null ) ;
        eventHandler.callback();
    }
    protected void removeLastTilePlayed() {
        if ( currentState == NORMAL ) { return; }
        if ( tilesPlayed.size() == 0 ) { return; }

        tilesPlayed.remove(tilesPlayed.size() - 1);
        do {
            if (currentState == PLAYING_HORIZONTAL) {
                currentPlayCol--;
            } else {
                currentPlayRow--;
            }
        } while (tile(currentPlayRow, currentPlayCol) != null);

        eventHandler.callback();
    }
    protected void finishPlayingTiles() {
        currentState = NORMAL;
        tilesPlayed.clear();
        eventHandler.callback();
    }
    
    // Deal with scoring
    // usedLetters must be in the order they are used - sorry :(
    public int score(String word, Rack usedLetters, int row, int col, int direction) {
        // Cant score anything if you didnt use any letters
        if ( usedLetters == null ) { return 0; }
        if ( usedLetters.size() == 0 ) { return 0; }

        int row_vector = 0;
        int col_vector = 0;

        // Set the direction we are moving
        if ( direction == Move.HORIZONTAL ) {
            col_vector = 1;
        } else if ( direction == Move.VERTICAL ) {
            row_vector = 1;
        } else {
            // Direction unkown, should probably throw something here
            return 0;
        }

        // Word goes past the end of the board, should probably throw something here
        if ( col_vector*(usedLetters.size()-1)+col > 14 || row_vector*(usedLetters.size()-1)+row > 14 ) {
            return 0;
        }

        int usedLettersIndex = 0;
        int wordIndex = 0;
        int score = 0;
        int tripple_bonus = 0;
        int double_bonus = 0;

        // Walk through the usedLetters and calculate the score
        // It is expected that crosswords are calculated elsewhere (we dont have access to the DAWG)
        while ( wordIndex < word.length() ) {
            // Cell is occupied, count only face value of the tile
            if ( tile(row, col) != null ) {
                score += tile(row, col).points();

            } else {
                // Factor in any space bonus
                if ( bonus(row, col) == DLS ) {
                    score += usedLetters.tile(usedLettersIndex).points()*2;
                } else if ( bonus(row, col) == DWS ) {
                    score += usedLetters.tile(usedLettersIndex).points();
                    double_bonus++;
                } else if ( bonus(row, col) == TLS ) {
                    score += usedLetters.tile(usedLettersIndex).points()*3;
                } else if ( bonus(row, col) == TWS ) {
                    score += usedLetters.tile(usedLettersIndex).points();
                    tripple_bonus++;
                } else {
                    score += usedLetters.tile(usedLettersIndex).points();
                }
                usedLettersIndex++;

            }
            wordIndex++;

            row += row_vector;
            col += col_vector;
        }

        // Add usedLetters score bonus
        while ( double_bonus > 0 ) {
            score *= 2;
            double_bonus--;
        }
        while ( tripple_bonus > 0 ) {
            score *= 3;
            tripple_bonus--;
        }

        // Add bingo bonus
        if ( usedLetters.size() == Rack.MAX_TILES ) {
            score += 50;
        }

        return score;
    }
    public int bonus(int row, int col) {
        // Return the value of the bonus at row, col

        if ( row < 0 || row >= MAX_ROWS || col < 0 || col >= MAX_COLUMNS ) {
            // Out of bounds, return NORMAL
            return Board.NORMAL;
        }
        return bonus[row][col];
    }

    // Only class members can manipulate the tiles on the board
    protected void clear() {
        // Remove all tiles from the board, its up to class members to keep track of the tiles
        int row, col;
        for (row=0; row<rows; row++) {
            for (col=0; col<columns; col++) {
                tiles[row][col] = null;
            }
        }
        eventHandler.callback();
    }
    protected Tile remove(int row, int col) {
        // Remove the tile, return the value of the space

        if ( row < 0 || row >= MAX_ROWS || col < 0 || col >= MAX_COLUMNS ) {
            // Out of bounds, return null
            return null;
        }

        Tile removed = tiles[row][col];
        tiles[row][col] = null;

        eventHandler.callback();
        return removed;
    }

    protected void add(Move move) {
        int rowVector = 0;
        int colVector = 0;

        if ( move.direction() == Move.HORIZONTAL ) {
            colVector = 1;
        } else if ( move.direction() == Move.VERTICAL ) {
            rowVector = 1;
        } else {
            return;
        }

        int playingRow = move.row();
        int playingCol = move.col();
        int position = 0;
        int wordPosition = 0;
        while ( position < move.tilesPlayed().size() ) {
            while ( tile(playingRow, playingCol) != null ) {
                playingRow += rowVector;
                playingCol += colVector;
                wordPosition++;
            }

            if ( move.tilesPlayed().tile(position).isBlank() ) {
                move.tilesPlayed().tile(position).selectLetter(move.wordPlayed().charAt(wordPosition));
            }
            add(move.tilesPlayed().tile(position), playingRow, playingCol);
            playingRow += rowVector;
            playingCol += colVector;
            position++;
            wordPosition++;
        }
    }
    private void add(Tile tile, int row, int col) {
        // Add the tile to row, col
        if ( tile == null ) { return; }

        if ( row < 0 || row >= MAX_ROWS || col < 0 || col >= MAX_COLUMNS ) {
            // Out of bounds, do nothing
            return;
        }

        // Dont add the tile if its a blank without a value
        if ( tile.value() == Tile.BLANK ) {
            return;
        }
        tiles[row][col] = tile;

        eventHandler.callback();
    }

    public void print() {
        System.out.println("      A B C D E F G H I J K L M N O");
        System.out.println("     -------------------------------");
        for (int row=0; row<MAX_ROWS; row++) {
            System.out.printf(" %2d | ", row);
            for (int col=0; col<MAX_COLUMNS; col++)  {
                if ( tile(row, col) == null ) {
                    System.out.print("- ");
                } else {
                    if ( tile(row, col).isBlank() ) {
                        System.out.print(Character.toLowerCase(tile(row, col).value())+" ");
                    } else {
                        System.out.print(tile(row, col).value()+" ");
                    }
                }
            }
            System.out.println("|");
        }
        System.out.println("     -------------------------------");
    }

    // The boundries of this object
    public static final int MAX_ROWS = 15;
    public static final int MAX_COLUMNS = 15;

    // Bonus values: None, Double Letter Score, Double Word Score, Tripple LS, Tripple WS, respectively
    public static final int DLS = 1;
    public static final int DWS = 2;
    public static final int TLS = 3;
    public static final int TWS = 4;

    // The column values by usedLettersIndex
    public static final char[] columnHeaders = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'L', 'K', 'M', 'N', 'O' };

    // The actual locations of the bonus on this board, formatted for your viewing pleasure
    private final int[][] bonus = {
        { TWS,  0,  0,DLS,  0,  0,  0,TWS,  0,  0,  0,DLS,  0,  0,TWS,},
        {   0,DWS,  0,  0,  0,TLS,  0,  0,  0,TLS,  0,  0,  0,DWS,  0,},
        {   0,  0,DWS,  0,  0,  0,DLS,  0,DLS,  0,  0,  0,DWS,  0,  0,},
        { DLS,  0,  0,DWS,  0,  0,  0,DLS,  0,  0,  0,DWS,  0,  0,DLS,},
        {   0,  0,  0,  0,DWS,  0,  0,  0,  0,  0,DWS,  0,  0,  0,  0,},
        {   0,TLS,  0,  0,  0,TLS,  0,  0,  0,TLS,  0,  0,  0,TLS,  0,},
        {   0,  0,DLS,  0,  0,  0,DLS,  0,DLS,  0,  0,  0,DLS,  0,  0,},
        { TWS,  0,  0,DLS,  0,  0,  0,DWS,  0,  0,  0,DLS,  0,  0,TWS,},
        {   0,  0,DLS,  0,  0,  0,DLS,  0,DLS,  0,  0,  0,DLS,  0,  0,},
        {   0,TLS,  0,  0,  0,TLS,  0,  0,  0,TLS,  0,  0,  0,TLS,  0,},
        {   0,  0,  0,  0,DWS,  0,  0,  0,  0,  0,DWS,  0,  0,  0,  0,},
        { DLS,  0,  0,DWS,  0,  0,  0,DLS,  0,  0,  0,DWS,  0,  0,DLS,},
        {   0,  0,DWS,  0,  0,  0,DLS,  0,DLS,  0,  0,  0,DWS,  0,  0,},
        {   0,DWS,  0,  0,  0,TLS,  0,  0,  0,TLS,  0,  0,  0,DWS,  0,},
        { TWS,  0,  0,DLS,  0,  0,  0,TWS,  0,  0,  0,DLS,  0,  0,TWS,},
    };
}
