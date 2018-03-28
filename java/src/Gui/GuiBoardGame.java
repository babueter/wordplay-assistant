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

package Gui;

import Events.*;
import Game.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Generic game with a board, rack, and move list, designed to be inherited
// This sets up the gui objects and listeners along with keyboard interaction
// Its up to the inheriting classes to actually set up the gui
public class GuiBoardGame extends JFrame implements ObjectListener {
    private BoardPracticeGame game;

    private GuiRack guiRack;
    private GuiBoard guiBoard;
    private GuiMovePackageList guiMovePackageList;

    // Methods inheritors need access to
    protected BoardPracticeGame game() { return game; }
    protected GuiRack guiRack() { return guiRack; }
    protected GuiBoard guiBoard() { return guiBoard; }
    protected GuiMovePackageList guiMovePackageList() { return guiMovePackageList; }

    protected void initGameObjects(BoardPracticeGame initGame) {
        // Only things common to any board type game should go in here
        //
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // The game itself
        game = initGame;

        // Gui objects that represent components of the game
        guiRack = new GuiRack(game.rack());
        guiBoard = new GuiBoard(game.board());
        guiMovePackageList = new GuiMovePackageList(game.moves());

        // We want to listen to any key pressed, forward to our KeyListener
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if ( e.getID() == KeyEvent.KEY_RELEASED ) {
                    keyReleased(e);
                }
                return true;
            }
        });

        // Listen for updates made to the guiBoard (on by default)
        guiBoard.eventHandler().register(this);
        guiMovePackageList.eventHandler().register(this);
    }
    protected void newRack() {
        game.reset();

        int count = 0;
        while ( count < 50 && game.moves().isEmpty() ) {
            game.reset();
            count++;
        }

        if ( game.moves().isEmpty() ) {
            JOptionPane.showMessageDialog(this, "Unable to find a playable rack.\n" +
                    "Click New Rack to try again or\n" +
                    "try adding/removing tiles.", "Inane error", JOptionPane.ERROR_MESSAGE);
        }

        guiMovePackageList.updateList(game.moves());
    }
    protected void setMovesListClickable(boolean enabled) {
        if ( enabled ) {
            guiMovePackageList.eventHandler().register(this);
        } else {
            guiMovePackageList.eventHandler().unregister(this);
        }
    }
    protected void setBoardClickable(boolean enabled) {
        if ( enabled ) {
            guiBoard.eventHandler().register(this);
        } else {
            guiBoard.eventHandler().unregister(this);
        }
    }

    public void objectUpdated(ObjectHandler o) {
        // Decide which object was updated and act accordingly
        if ( o.getEntity() instanceof GuiBoard ) {
            game.startPlayingTiles(guiBoard.clickedRow(), guiBoard.clickedCol());

        } else if ( o.getEntity() instanceof GuiMovePackageList ) {
            game.stageMove(guiMovePackageList.moveSelected());

        } else if ( o.getEntity() instanceof GuiHandpickTiles ) {
            ((GuiHandpickTiles)o.getEntity()).setVisible(false);

            game.currentDictionary().getConstraint().setContainsLetters(game.rack().toString());
            newRack();
            this.setVisible(true);
        }
    }

    public void keyReleased(KeyEvent e) {
        // Deal with special characters
        if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
            game.stopPlayingTiles();
            return;
        }
        if ( e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) {
            game.removeLastTilePlayed();
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            Move move = game.finishPlayingTiles();
            guiMovePackageList.guesMove(move);
            return;
        }

        // Actually play a character typed
        if ( ! Bag.isValidLetter(Character.toUpperCase(e.getKeyChar())) ) { return; }

        if ( e.isShiftDown() ) {
            game.playTile(Character.toUpperCase(e.getKeyChar()), true);
        } else {
            game.playTile(Character.toUpperCase(e.getKeyChar()), false);
        }
    }

}
