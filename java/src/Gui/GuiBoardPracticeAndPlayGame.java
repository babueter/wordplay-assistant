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

import Dictionary.*;
import Game.*;
import java.awt.event.*;
import javax.swing.*;

// Gui for Game.BoardPracticeAndPlay
public class GuiBoardPracticeAndPlayGame extends GuiBoardPracticeGame {
    private int currentScore;

    private JLabel currentScoreLabel;
    private JLabel tilesLeftLabel;

    public GuiBoardPracticeAndPlayGame(BoardPracticeGame initGame) {
        initComponents(initGame);
        newRack();
        updateLabels();
    }

    @Override
    public void initComponents(BoardPracticeGame initGame) {
        // Display a running score
        currentScoreLabel = new JLabel("Score: 0");
        currentScoreLabel.setHorizontalAlignment(JLabel.CENTER);
        currentScore = 0;

        // Commit the move on the board
        JButton commitButton = new JButton("Commit");
        commitButton.addActionListener(this);
        commitButton.setActionCommand("commit");

        // Show all the best moves in the moves list window
        JButton showMovesButton = new JButton("Show Moves");
        showMovesButton.addActionListener(this);
        showMovesButton.setActionCommand("showmoves");

        // Start the game over
        JButton resetBoardButton = new JButton("Start Over");
        resetBoardButton.addActionListener(this);
        resetBoardButton.setActionCommand("reset");

        // Show how many tiles are left in the bag
        tilesLeftLabel = new JLabel("Tiles Left: 100");
        tilesLeftLabel.setHorizontalAlignment(JLabel.CENTER);

        JComponent[] components = {currentScoreLabel, commitButton, showMovesButton, resetBoardButton, tilesLeftLabel};
        initComponents(initGame, components);
    }

    // Run in a new thread
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new GuiBoardPracticeAndPlayGame(new BoardPracticeAndPlayGame(new Dawg(getClass().getResourceAsStream("/Data/twl.dawg")))).setVisible(true);
            }
        });
    }

    // Update the text and moves list
    private void updateLabels() {
        guiMovePackageList().updateList(game().moves());
        currentScoreLabel.setText("Score: "+currentScore);
        tilesLeftLabel.setText("Tiles Left: "+game().bag().tilesLeft());
    }

    // Respond to a button being clicked
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("commit")) {
            ((BoardPracticeAndPlayGame)game()).commitMove();

            if ( ((BoardPracticeAndPlayGame)game()).lastCommittedMove() != null ) {
                currentScore += ((BoardPracticeAndPlayGame)game()).lastCommittedMove().score();
                updateLabels();
            }

        } else if (e.getActionCommand().equals("showmoves")) {
            guiMovePackageList().showMoves();
            
        } else if (e.getActionCommand().equals("reset")) {
            game().reset();
            currentScore = 0;

            updateLabels();

        }
    }
}
