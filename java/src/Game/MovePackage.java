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
import java.util.*;

// An ArrayList of Moves
public class MovePackage extends ArrayList {
    public static final int MAX_MOVES = 50;

    // Constructor is inherited
    
    // Add a move to the list, maintaining only MAX_MOVES
    public void add(Move move) {
        if ( this.size() >= MAX_MOVES ) {
            // Our package is full, determine if this move is worthy of inclusion
            sort();

            // Determine where on the list this move would end up based on score
            int index=0;
            while ( index < MAX_MOVES && move.score() <= ((Move)this.get(index)).score() ) {
                if ( ((Move)this.get(index)).equals(move) ) {
                    return;
                }
                index++;
            }

            // This move is less then or equal to everything already on the list, dont add
            if ( index == MAX_MOVES ) {
                return;
            }

            // Finally add the move at the proper position
            this.add(index, move);
            this.remove(this.size() - 1);

        } else {
            // Package is not full, add the move if it doesnt already exist
            for (int index=0; index<this.size(); index++) {
                if ( ((Move)this.get(index)).equals(move) ) {
                    return;
                }
            }
            this.add((Object)move);
        }
    }

    // Sort this list by score, highest to lowest
    public void sort() {
        Collections.sort(this, Collections.reverseOrder(new MoveComparator()));
    }

    // Print the list
    public void print() {
        for (int index=0; index<this.size(); index++) {
            ((Move)this.get(index)).print();
        }
    }
}
