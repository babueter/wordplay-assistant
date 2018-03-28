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

// A move containing the main word formed, optional tiles used, and the location on the board
public class Move {
    private String wordPlayed;

    // We can actually track the tiles used for reference within the same class
    private Rack tilesPlayed;
    private Rack tilesRemaining;

    // Vertical positions are numbered, horizontal positions are lettered
    private int row;
    private int col;
    private int direction;
    private static final char[] colChars = {0x0, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O'};

    // Direction values
    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 2;

    // Scoring information
    private int score;
    private boolean isBingo;

    // Protected constructor allows us to do whatever we want
    protected Move() {
        tilesPlayed = new Rack();
        tilesRemaining = new Rack();

        col = 8;
        row = 8;

        score = 0;
        isBingo = false;
    }

    // Constructor we expect everyone to use
    public Move(String initWordPlayed, Rack initTilesPlayed, Rack initTilesRemaining, int initRow, int initCol, int initDirection, int initScore, boolean initIsBingo) {
        wordPlayed = initWordPlayed;

        tilesPlayed = new Rack(initTilesPlayed);
        tilesRemaining = new Rack(initTilesRemaining);

        row = initRow;
        col = initCol;
        direction = initDirection;

        score = initScore;
        isBingo = initIsBingo;
    }

    // Public access to private variables
    public String wordPlayed() { return wordPlayed; }
    public int row() { return row; }
    public int col() { return col; }
    public int direction() { return direction; }
    public int score() { return score; }
    public boolean isBingo() { return isBingo; }

    // Anyone can see the tiles played and remaining
    public Rack tilesPlayed() { return tilesPlayed; }
    public Rack tilesRemaining() { return tilesRemaining; }

    // Only class members can update our internal values
    protected void row(int setvPosition) {
        row = setvPosition;
    }
    protected void col(int sethPosition) {
        col = sethPosition;
    }
    protected void direction(int setDirection) {
        direction = setDirection;
    }

    // Anyone can change the score or set the bingo
    public void score(int setScore) {
        score = setScore;
    }
    public void isBingo(boolean value) {
        if ( ! isBingo && value ) { score+= 50; }
        isBingo = value;
    }

    // Test if this move is the same as another move (primarily for MovePackage)
    public boolean equals(Move move) {
        if ( ! wordPlayed.equals(move.wordPlayed()) ) { return false; }
        if ( col != move.col() || row != move.row() ) { return false; }
        if ( score != move.score() ) { return false; }
        if ( isBingo != move.isBingo() ) { return false; }

        return true;
    }

    // Print the move
    @Override
    public String toString() {
        int rowIndex = row+1;
        int colIndex = col+1;

        String coordinate = new String();
        if ( direction == VERTICAL ) {
            coordinate = colChars[colIndex]+""+rowIndex;
        } else {
            coordinate = rowIndex+""+colChars[colIndex];
        }

        if ( tilesRemaining != null ) {
            return coordinate+" "+wordPlayed+" ["+tilesRemaining.toString()+"] "+score+" pts";
        } else {
            return coordinate+" "+wordPlayed+" "+score+" pts";
        }
    }
    public void print() {
        System.out.println(toString());
    }

    // Convert column index to character
    public static char columnToChar(int col) {
        col++;
        if ( col < 1 || col > colChars.length ) { return 0x0; }

        return colChars[col];
    }
}
