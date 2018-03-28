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

import Assistant.util.*;
import Events.*;
import Game.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Represent a MovePackage as a JList
public class GuiMovePackageList extends JPanel {
    private MovePackage moves;

    private boolean[] movesGuessed;
    private boolean doneGuessing;

    private JList list;
    private DefaultListModel  listModel;
    private CustomListCellRenderer guessingCellRenderer;
    private CustomListCellRenderer showingCellRenderer;

    private ObjectHandler eventHandler;
    private Move moveSelected;

    private int MOVES = 20;
    private int ROWS = MOVES;

    // Constructors
    public GuiMovePackageList(MovePackage initMoves) {
        super();

        initComponents(initMoves);
    }
    public GuiMovePackageList(MovePackage initMoves, int movesCount, int rowsCount) {
        super();

        MOVES = movesCount;
        ROWS = rowsCount;
        initComponents(initMoves);
    }
    private void initComponents(MovePackage initMoves) {

        movesGuessed = new boolean[1024];
        doneGuessing = false;

        listModel = new DefaultListModel();
        guessingCellRenderer = new CustomListCellRenderer();
        showingCellRenderer = new CustomListCellRenderer();

        showingCellRenderer.setSelectedBackgroundColor(Color.PINK);
        showingCellRenderer.setSelectedFontColor(Color.BLACK);

        guessingCellRenderer.setSelectedBackgroundColor(Color.GREEN);
        guessingCellRenderer.setSelectedFontColor(Color.BLACK);

        list = new JList(listModel);
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        list.setVisibleRowCount(ROWS);
        list.setCellRenderer(showingCellRenderer);
        list.setFont(new Font("Monospaced", Font.PLAIN, 14));

        this.setSize(314, 250);
        this.add(list);

        updateList(initMoves);
        setVisible(true);

        list.setEnabled(false);

        // Set up the mouse listener to load moves on the board
        eventHandler = new ObjectHandler(this);
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ( e.getClickCount() == 2 ) {
                    int moveIndex = list.locationToIndex(e.getPoint());

                    if (doneGuessing || movesGuessed[moveIndex]) {
                        if (moveIndex < moves.size()) {
                            moveSelected = (Move) moves.get(moveIndex);
                            eventHandler.callback();
                        }
                    }
                }
            }
        };
        list.addMouseListener(mouseListener);
        moveSelected = null;
    }

    public ObjectHandler eventHandler() { return eventHandler; }
    public Move moveSelected() { return moveSelected; }

    // Change the MovePackage moves
    public void updateList(MovePackage initMoves) {
        moves = initMoves;
        moves.sort();
        listModel.clear();

        list.setCellRenderer(guessingCellRenderer);
        list.clearSelection();
        int count;
        for (count=0; count<MOVES && count<moves.size(); count++) {
            String formattedString = new Integer(count+1).toString()+".   ";
            formattedString = formattedString.substring(0, 4);
            listModel.addElement(formattedString+"                       ");

            movesGuessed[count] = false;
        }
        while ( count < MOVES ) {
            listModel.addElement("                           ");
            count++;
        }
        doneGuessing = false;
    }
    public void updateList(MovePackage initMoves, int movesCount, int rowsCount) {
        MOVES = movesCount;
        ROWS = rowsCount;
        list.setVisibleRowCount(ROWS);

        updateList(initMoves);
    }

    // Highlight a move if it matches the one guessed
    public void guesMove(Move move) {
        if ( doneGuessing ) { return; }

        list.setCellRenderer(guessingCellRenderer);
        list.clearSelection();
        if ( move == null ) { return; }

        for (int count=0; count<MOVES && count<moves.size(); count++) {
            Move compareMove = (Move)moves.get(count);
            if ( compareMove.equals(move) ) {
                String formattedString = new Integer(count+1).toString()+".   ";
                formattedString = formattedString.substring(0, 4);
                formattedString += compareMove.toString();
                formattedString += "                           ";
                listModel.setElementAt(formattedString.substring(0, 27), count);
                list.setSelectedIndex(count);

                movesGuessed[count] = true;
                return;
            }
        }
    }
    public void guesMove(String move) {
        if ( move == null ) { return; }
        if ( doneGuessing ) { return; }

        list.setCellRenderer(guessingCellRenderer);
        list.clearSelection();
        int count = MOVES;
        while ( count >= moves.size() ) { count--; }
        while ( count >= 0 ) {
            Move compareMove = (Move)moves.get(count);
            if ( compareMove.wordPlayed().equals(move) ) {
                String formattedString = new Integer(count+1).toString()+".   ";
                formattedString = formattedString.substring(0, 4);
                formattedString += compareMove.toString();
                formattedString += "                           ";
                listModel.setElementAt(formattedString.substring(0, 27), count);
                list.setSelectedIndex(count);

                movesGuessed[count] = true;
            }
            count--;
        }
    }

    // Show all moves, highlight the ones not guessed
    public void showMoves() {
        list.setCellRenderer(showingCellRenderer);
        list.clearSelection();
        for (int count = 0; count<MOVES && count<moves.size(); count++) {
            Move move = (Move) moves.get(count);
            String formattedString = new Integer(count + 1).toString() + ".   ";
            formattedString = formattedString.substring(0, 4);
            formattedString += move.toString();
            formattedString += "                           ";
            listModel.setElementAt(formattedString.substring(0, 27), count);

            if ( ! movesGuessed[count] ) {
                list.addSelectionInterval(count, count);
            }
        }
        doneGuessing = true;
    }
}
