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

// A set of tiles to play the game
import Events.*;
import java.util.*;

public class Bag {
    private ArrayList tiles;
    ObjectHandler eventHandler;

    // Constructor
    public Bag() {
        // Populate a new bag with tiles based on the value and counts defined below
        tiles = new ArrayList();
        eventHandler = new ObjectHandler(this);

        for (char letter : LETTERS ) {
            int count;
            for (count=0; count<count(letter); count++) {
                tiles.add(new Tile(letter, points(letter)));
            }
        }

        // Shuffle thrice to mix the bag nice
        this.shuffle();
        this.shuffle();
        this.shuffle();
    }
    public ObjectHandler getEventHandler() {
        return eventHandler;
    }

    // Allow anyone to shuffle us
    public void shuffle() {
        Collections.shuffle(tiles);
    }

    // Only allow class memebers to actually manipulate the tiles within us
    protected Tile draw() {
        // Return a single tile from the bag, draw it from the list

        // Nothing to return, send null
        if ( tiles.isEmpty() ) { return null; }

        // Only one tile left, return that
        if ( tiles.size() == 1 ) {
            Tile tile = (Tile)tiles.remove(0);
            return tile;
        }

        // Randomly pick a tile to return
        Random rand = new Random();

        this.shuffle();
        Tile tile = (Tile)tiles.remove(rand.nextInt(tiles.size()-1));

        eventHandler.callback();
        return tile;
    }
    protected Tile draw(char letter) {
        if ( tilesLeft(letter) == 0 ) { return null; }

        int i;
        for (i=0; i<tiles.size(); i++) {
            if ( ((Tile)tiles.get(i)).value() == letter || (letter == Tile.BLANK && ((Tile)tiles.get(i)).isBlank() )) {
                Tile tile = (Tile)tiles.remove(i);

                eventHandler.callback();
                return tile;
            }
        }

        return null;
    }
    protected void replace(Tile tile) {
        // Do nothing if the tile is actually null
        if ( tile == null ) { return; }

        // Dont add the tile if it already exists
        if ( tiles.contains(tile) ) { return; }

        // Reset blank tiles
        if ( tile.isBlank() ) {
            tile.selectLetter(Tile.BLANK);
        }
        
        // Put a tile back into the list, shuffle for good measure
        tiles.add(tile);
        this.shuffle();

        eventHandler.callback();
    }

    // Anyone can see how many tiles and what kind we have left
    public int tilesLeft() {
        return tiles.size();
    }
    public int tilesLeft(char value) {
        // Count how many tiles of this value there are
        int count = 0;
        
        int i;
        for (i=0; i<tiles.size(); i++) {
            if ( ((Tile)tiles.get(i)).value() == value ) {
                count++;
            }
        }
        return count;
    }
    public int size() {
        return tiles.size();
    }

    // Return some defaults about what went into this bag
    public static int points(char value) {
        // Determine how many points this letter is
        if ( value == '?' ) { return LETTER_VALUES[26]; }

        if ( value < 'A' || value > 'Z' ) { return 0 ; }

        return LETTER_VALUES[value-'A'];
    }
    public static int count(char value) {
        // Determine how many tiles a full back contains
        if ( value == '?' ) { return LETTER_COUNTS[26]; }

        if ( value < 'A' || value > 'Z' ) { return 0 ; }

        return LETTER_COUNTS[value-'A'];
    }
    public static boolean isValidLetter(char letter) {
        for (int index=0; index<LETTERS.length-1; index++) {
            if ( LETTERS[index] == Character.toUpperCase(letter) ) {
                return true;
            }
        }

        return false;
    }

    // For debugging purposes we allow printing the tiles left
    public void print() {
        System.out.print("Bag: ");
        int index;
        for (index=0; index<tiles.size(); index++) {
            System.out.print(((Tile)tiles.get(index)).value()+" ");
        }
        System.out.println();
    }

    // Available for external routines to iterate through all options
    public static final char[] LETTERS  = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '?'};

    // Interanally we use these to assist creating the bag of tiles
    // Externally these can be accessed using points() and count()
    private final static int[] LETTER_VALUES = {1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10, 0};
    private final static int[] LETTER_COUNTS = {9, 2, 2, 4, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1, 2};
}
