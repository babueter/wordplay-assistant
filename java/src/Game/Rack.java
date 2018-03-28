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

import Assistant.util.*;
import Events.*;
import java.util.*;

// A rack containing tiles
public class Rack {
    protected ArrayList tiles;

    public final static int MAX_TILES = 7;

    private ObjectHandler eventHandler;

    // Only class members should be able to construct us
    protected Rack() {
        initComponents();
    }
    protected Rack(Rack initRack) {
        initComponents();
        if ( initRack == null ) { return; }

        for (int index=0; index<initRack.size(); index++) {
            tiles.add((Object)initRack.tile(index));
        }
    }
    private void initComponents() {
        tiles = new ArrayList();
        eventHandler = new ObjectHandler(this);
    }

    // Allow anyone to register for updates
    public ObjectHandler eventHandler() { return eventHandler; }

    // Allow anyone to order and/or shuffle the rack
    public void shuffle() {
        Collections.shuffle(tiles);
        eventHandler.callback();
    }
    public void swap(int indexA, int indexB) {
        if ( indexA < 0 || indexB < 0 || indexA > tiles.size() || indexB > tiles.size() ) {
            // Out of bounds, do nothing
            return;
        }

        // Change the order of the tiles
        Tile tileA = (Tile) tiles.get(indexA);
        tiles.set(indexA, tiles.get(indexB));
        tiles.set(indexB, tileA);

        eventHandler.callback();
    }
    public void sort() {
        // Sort this rack
        Collections.sort(tiles, new TileComparator());
        eventHandler.callback();
    }

    // Only allow class members to add or remove tiles
    protected void add(Tile tile) {
        if ( tile == null ) { return ; }

        if ( tiles.contains(tile) ) { return ; }
        
        if ( tiles.size() < MAX_TILES ) {
            tiles.add(tile);
        }
        eventHandler.callback();
    }
    protected Tile remove(int index) {
        Tile removedTile = null;
        if ( index >= 0 && index < tiles.size() ) {
            removedTile = (Tile) tiles.remove(index);
            eventHandler.callback();
            return removedTile;
        }
        return null;
    }
    protected void remove(Tile tile) {
        tiles.remove(tile);
        eventHandler.callback();
    }
    protected void remove(Rack playedRack) {
        for (int index=0; index<playedRack.size(); index++) {
            remove(playedRack.tile(index));
        }
    }
    protected void clear() {
        tiles.clear();
    }

    // Public access to information about this rack
    public Tile tile(int index) {
        if ( index >= 0 && index < tiles.size() ) {
            return (Tile) tiles.get(index);
        }
        return null;
    }
    public Tile tile(char letter) {
        // Look for this tile exactly
        for (int index=0; index<tiles.size(); index++) {
            if ( ((Tile)tiles.get(index)).value() == letter ) {
                return (Tile)tiles.get(index);
            }
        }

        // Didnt find the right tile, lets look for a blank
        for (int index = 0; index < tiles.size(); index++) {
            if ( ((Tile)tiles.get(index)).isBlank() ) {
                return (Tile)tiles.get(index);
            }
        }

        // We dont have a tile by that letter
        return null;
    }
    public int indexByTile(Tile tile) {
        int index;
        for (index=0; index<tiles.size(); index++) {
            if ( tile == tiles.get(index) ) { return index; }
        }
        return -1;
    }
    public boolean contains(Tile tile) {
        return tiles.contains(tile);
    }
    public int size() { return tiles.size(); }

    public void print() {
        System.out.println(toString());
    }

    @Override
    public String toString() {
        String tostring = "";

        int index = 0;
        for (index=0; index<size(); index++) {
            if ( tile(index).isBlank() ) {
                tostring += Tile.BLANK;
            } else {
                tostring += tile(index).value();
            }
        }
        return tostring;
    }
    public String toValueString() {
        String tostring = "";

        for (int index=0; index<size(); index++) {
            tostring += tile(index).value();
        }

        return tostring;
    }
}
