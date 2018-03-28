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

// A board only the size of a rack
public class RackBoard extends Board {

    // Constructor
    public RackBoard() {
        tilesPlayed = new Rack();
        eventHandler = new ObjectHandler(this);

        // Set up spaces and clear the values
        tiles = new Tile[1][Rack.MAX_TILES+1];
        rows = 1;
        columns = Rack.MAX_TILES;

        // We're always playing tiles on this board
        currentState = Board.PLAYING_HORIZONTAL;
        startPlayingTiles(0, 0, Move.HORIZONTAL);

        clear();
    }

    // No bonuses on this board
    @Override
    public int bonus(int row, int col) {
        return Board.NORMAL;
    }

    // We're always playing a word on this type of board
    @Override
    protected void startPlayingTiles(int row, int col, int direction) {
        tilesPlayed.clear();
        eventHandler.callback();

        startPlayRow = 0;
        startPlayCol = 0;
        currentPlayRow = 0;
        currentPlayCol = 0;
    }
    @Override
    public void finishPlayingTiles() {
        tilesPlayed.clear();
        eventHandler.callback();

        startPlayRow = 0;
        startPlayCol = 0;
        currentPlayRow = 0;
        currentPlayCol = 0;
    }

}
