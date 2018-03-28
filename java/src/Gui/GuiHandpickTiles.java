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

// A gui popup window that selects tiles for a rack, designed for BoardPracticeWordplayGame
public class GuiHandpickTiles extends JFrame implements ObjectListener, ActionListener {
    private Bag bag;
    private Rack rack;
    private BoardPracticeWordplayGame game;

    private ObjectHandler eventHandler;

    // Must have a game already setup to create this
    public GuiHandpickTiles (BoardPracticeWordplayGame initGame) {
        bag = initGame.bag();
        rack = initGame.rack();
        game = initGame;

        eventHandler = new ObjectHandler(this);

        initComponents();
    }
    public ObjectHandler getEventHandler() {
        return eventHandler;
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Configure GUI components
        GuiBag guiBag = new GuiBag(bag);
        GuiRack guiRack = new GuiRack(rack);

        // Button panel
        JPanel buttonBox = new JPanel();
        JButton clearButton = new   JButton("Clear");
        JButton doneButton = new JButton("Done");
        buttonBox.add(clearButton);
        buttonBox.add(doneButton);

        clearButton.setActionCommand("clearRack");
        doneButton.setActionCommand("done");

        clearButton.addActionListener(this);
        doneButton.addActionListener(this);

        // Pack our items
        this.getContentPane().add(guiBag, BorderLayout.PAGE_START);
        this.getContentPane().add(guiRack, BorderLayout.CENTER);
        this.getContentPane().add(buttonBox, BorderLayout.PAGE_END);
        this.pack();

        guiBag.getEventHandler().register(this);
    }

    public void objectUpdated(ObjectHandler o) {
        GuiBag guiBag = (GuiBag) o.getEntity();

        game.addTile(guiBag.getLetterPicked());
    }

    public void actionPerformed(ActionEvent e) {
        if ( e.getActionCommand().equals("clearRack") ) {
            game.setRack("");
            
        } else if ( e.getActionCommand().equals("done") ) {
            this.dispose();
            eventHandler.callback();
        }
    }

}
