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

// A tile containing a letter and value
public class Tile {
    private char value;        // Character value of the tile
    private int points;        // Points this tile is worth
    private boolean blank;     // Is this currently a malleable blank
    private boolean wasBlank;  // Was at one time a blank

    public final static char BLANK = '?';

    // Only class members can create a tile
    protected Tile(char initValue, int initPoints) {
        // Create giving value and points
        value = initValue;
        points = initPoints;

        // Special case for creating a blank tile
        if ( initValue == BLANK && initPoints == 0 ) {
            blank = true;
            wasBlank = true;
        } else {
            blank = false;
            wasBlank = false;
        }
    }

    // Any one can look at the tile
    public char value() { return value; }
    public int points() { return points; }

    // Anyone can pick a letter for blank, only class members can set it permanently
    public void selectLetter(char setValue) {
        // Select a temporary value for this blank
        if ( ! blank ) { return; }

        value = setValue;
    }

    // Determine the blankness of this tile
    public boolean isSet() {
        // Has a temporary value been set
        if ( value == BLANK ) { return false; }
        return true;
    }
    public boolean isBlank() {
        // Is this tile actually a blan (regardless of its current state)
        return wasBlank;
    }

    @Override
    public String toString() {
        return Character.toString(value);
    }
}
