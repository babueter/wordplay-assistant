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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Gui interface to Game.BoardPracticeGame
public class GuiBoardPracticeGame extends GuiBoardGame implements ActionListener {
    private JPanel buttonPanel;

    // Constructor, initiator, main routine
    public GuiBoardPracticeGame() { };
    public GuiBoardPracticeGame(BoardPracticeGame initGame) {
        initComponents(initGame);
        newRack();
    }
    // Create two generic buttons that draw a new rack and show the moves
    public void initComponents(BoardPracticeGame initGame) {
        // Button that generates a new rack
        JButton newRack = new JButton("New Rack");
        newRack.setActionCommand("newRack");
        newRack.addActionListener(this);

        // Button that shows the moves list
        JButton showMoves = new JButton("Show Moves");
        showMoves.setActionCommand("showMoves");
        showMoves.addActionListener(this);

        JComponent[] components = {newRack, showMoves};

        initComponents(initGame, components);
    }
    // Compile the main interface and stuff each comonent into the button panel
    public void initComponents(BoardPracticeGame initGame, JComponent[] components) {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Set up the game and basic objects
        initGameObjects(initGame);

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new BoxLayout(boardPanel, BoxLayout.PAGE_AXIS));
        boardPanel.add(guiBoard());
        boardPanel.add(guiRack());

        // Allow inheritors to add items to this
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridx = 0;

        // Add each object in the components to the button panel
        for (int index=0; index<components.length; index++) {
            buttonPanel.add(components[index], c);
        }

        JPanel answersPanel = new JPanel();
        answersPanel.setLayout(new BoxLayout(answersPanel, BoxLayout.PAGE_AXIS));
        answersPanel.add(guiMovePackageList(), BorderLayout.PAGE_START);
        answersPanel.add(buttonPanel, BorderLayout.PAGE_END);

        this.getContentPane().add(boardPanel, BorderLayout.LINE_START);
        this.getContentPane().add(answersPanel, BorderLayout.LINE_END);

        this.setTitle("Board Play");
        this.pack();
        this.setResizable(false);
    }
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GuiBoardPracticeGame(new BoardPracticeGame(new Dawg(getClass().getResourceAsStream("/Data/twl.dawg")))).setVisible(true);
            }
        });
    }

    protected JPanel buttonPanel() { return buttonPanel; }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("newRack")) {
            newRack();

        }else if (e.getActionCommand().equals("showMoves")) {
            guiMovePackageList().showMoves();
        }
    }
}
