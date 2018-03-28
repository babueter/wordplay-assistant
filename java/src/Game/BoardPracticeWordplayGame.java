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

public class BoardPracticeWordplayGame extends BoardPracticeGame {
    public BoardPracticeWordplayGame(Dawg initMasterDictionary) {
        board = new RackBoard();
        rack = new Rack();
        moves = new MovePackage();

        bag = new Bag();
        bag.draw(Tile.BLANK);
        bag.draw(Tile.BLANK);

        setMasterDictionary(initMasterDictionary);
        setCurrentDictionary(initMasterDictionary);
    }
    
    @Override
    public void reset() {
        emptyRack();

        // Fill the rack back up
        while ( rack.size() < Rack.MAX_TILES ) {
            rack.add(bag.draw());
        }
        rack.shuffle();
        rack.shuffle();
        rack.shuffle();

        // Find any moves that can be made with this rack
        moves = Assistant.WordPlay.wordFindall(rack, currentDictionary());
        moves.sort();

        board.finishPlayingTiles();
    }

    // For this game we allow the public to empty the rack
    @Override
    public void emptyRack() {
        // Remove anything in the rack
        while ( rack.size() > 0 ) {
            bag.replace(rack.remove(0));
        }

        // Add those tiles that we need
        String addTiles = currentDictionary().getConstraint().getContainsLetters();
        for (int index=0; index<addTiles.length(); index++) {
            rack.add(bag.draw(addTiles.charAt(index)));
        }

    }

    // Allow anyone to add tiles to the rack
    public void addTile(char tile) {
        rack.add(bag.draw(tile));
        currentDictionary().getConstraint().setContainsLetters(rack.toString());
    }
    public void setRack(String tiles) {
        currentDictionary().getConstraint().setContainsLetters(tiles);
        emptyRack();
    }

}
