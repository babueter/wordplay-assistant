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

import Game.*;

// Extend a normal rack and add features needed by WordPlay routines
public class AssistantRack extends Rack {
    protected AssistantRack() {
        super();
    }
    protected AssistantRack(Rack rack) {
        // Clone the rack specified
        super();

        for (int index=0; index<rack.size(); index++) {
            this.add(rack.tile(index));
        }
    }

    public boolean permutate() {
    /*
     * Permutate using lexigraphical ordering
     *
     * 1. Find largest k such that tile[k] < tile[k+1], if none we are done permutating
     * 2. Find largest l such that tile[k] < tile[l], must exist and must satisfy k < l
     * 3. Swap tile[k] < tile[l]
     * 4. Reverse order of tile[k+1]...tile[n], where n is the last index of tile[]
     *
     */

        int largestK = -1;
        int largestL = -1;

        // Find largest k such that tile[k] < tile[k+1]
        for (int index=0; index<this.size()-1; index++) {
            if ( this.tile(index).value() < this.tile(index+1).value() ) {
                largestK = index;
                largestL = index+1;
            }
        }

        // Done permutating
        if ( largestK < 0 ) { return false; }

        // Find largest l such that tile[k] < tile[l]
        for (int index=largestK+1; index<this.size(); index++) {
            if ( this.tile(largestK).value() < this.tile(index).value() ) {
                largestL = index;
            }
        }

        // Swap tile[k] and tile[l]
        this.swap(largestK, largestL);

        // Reverse the sequence of tile[k+1] ... tile[n]
        int start = largestK+1;
        int end = this.size()-1;

        while ( start < end ) {
            this.swap(start, end);
            start++;
            end--;
        }
        return true;
    }

    @Override
    protected void add(Tile tile) {
        if ( tiles.size() < MAX_TILES ) {
            tiles.add(tile);
        }
    }

    @Override
    protected Tile remove(int index) {
        Tile removedTile = null;
        if ( index >= 0 && index < tiles.size() ) {
            removedTile = (Tile) tiles.remove(index);
            return removedTile;
        }
        return null;
    }

    // Tally the points of all tiles in the rack
    public int points() {
        int points = 0;

        for (int index=0; index<this.size(); index++) {
            points += ((Tile)tiles.get(index)).points();
        }

        return points;
    }


}
