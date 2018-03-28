package Gui;

import Dictionary.*;
import Game.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

// Simply practice making words from within a single rack
public class GuiBoardPracticeWordplayGame extends GuiBoardGame implements ActionListener {
    private ArrayList dawgObjects;
    private ArrayList dawgDescriptions;

    private GuiHandpickTiles guiHandpickTiles;

    public GuiBoardPracticeWordplayGame(BoardPracticeWordplayGame initGame) {
        initGameObjects(initGame);
        initComponents();

        guiHandpickTiles = new GuiHandpickTiles((BoardPracticeWordplayGame) game());
        guiHandpickTiles.setVisible(false);
        guiHandpickTiles.getEventHandler().register(this);
    }
    public void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Store dictionaries
        dawgObjects = new ArrayList();
        dawgDescriptions = new ArrayList();

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/twl.dawg")));
        dawgDescriptions.add(new String("All Words"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/2letterwords.dawg")));
        dawgDescriptions.add(new String("Two Letter Words"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/3letterwords.dawg")));
        dawgDescriptions.add(new String("Three Letter Words"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/4letterwords.dawg")));
        dawgDescriptions.add(new String("Four Letter Words"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/7letterwords.dawg")));
        dawgDescriptions.add(new String("All Bingos"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/est.dawg")));
        ((Dawg)dawgObjects.get(dawgObjects.size()-1)).getConstraint().setContainsLetters("EST");
        dawgDescriptions.add(new String("Bingos ending in EST"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/ing.dawg")));
        ((Dawg)dawgObjects.get(dawgObjects.size()-1)).getConstraint().setContainsLetters("ING");
        dawgDescriptions.add(new String("Bingos ending in ING"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/un.dawg")));
        ((Dawg)dawgObjects.get(dawgObjects.size()-1)).getConstraint().setContainsLetters("UN");
        dawgDescriptions.add(new String("Bingos beginning with UN"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/out.dawg")));
        ((Dawg)dawgObjects.get(dawgObjects.size()-1)).getConstraint().setContainsLetters("OUT");
        dawgDescriptions.add(new String("Bingos beginning with OUT"));

        dawgObjects.add(new Dawg(getClass().getResourceAsStream("/Data/pre.dawg")));
        ((Dawg)dawgObjects.get(dawgObjects.size()-1)).getConstraint().setContainsLetters("PRE");
        dawgDescriptions.add(new String("Bingos beginning with PRE"));

        guiMovePackageList().updateList(game().moves(), 30, 15);
        guiBoard().hasHeadings(false);
        guiMovePackageList().eventHandler().unregister(this);

        // Panel for the rack
        JPanel rackPanel = new JPanel();
        rackPanel.setLayout(new BoxLayout(rackPanel, BoxLayout.PAGE_AXIS));
        rackPanel.add(guiRack());

        // Panel for setting constraints
        JPanel constraintsPanel = new JPanel();

        JLabel dictionaryLabel = new JLabel("Change Dictionary: ");
        JComboBox dictionaryList = new JComboBox(dawgDescriptions.toArray());
        dictionaryList.setActionCommand("changeWordList");
        dictionaryList.addActionListener(this);

        constraintsPanel.add(dictionaryLabel);
        constraintsPanel.add(dictionaryList);
        rackPanel.add(constraintsPanel);

        // Panel for the buttons
        JPanel buttonPanel = new JPanel();

        JButton newRack = new JButton("New Rack");
        newRack.setActionCommand("newRack");
        newRack.addActionListener(this);

        JButton addTile = new JButton("Add Tiles");
        addTile.setActionCommand("addTile");
        addTile.addActionListener(this);

        JButton showAll = new JButton("Show All");
        showAll.setActionCommand("showAll");
        showAll.addActionListener(this);

        buttonPanel.add(newRack);
        buttonPanel.add(addTile);
        buttonPanel.add(showAll);

        rackPanel.add(buttonPanel);

        this.getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.PAGE_END;
        c.gridx = 0;

        // Pack the main content pane with everything
        this.getContentPane().add(guiMovePackageList(), c);
        this.getContentPane().add(guiBoard(), c);
        this.getContentPane().add(rackPanel, c);

        this.pack();
        this.setTitle("Word Practice");
        this.setResizable(false);
    }
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GuiBoardPracticeWordplayGame(new BoardPracticeWordplayGame(new Dawg(getClass().getResourceAsStream("/Data/twl.dawg")))).setVisible(true);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if ( e.getActionCommand().equals("showAll") ) {
            guiMovePackageList().showMoves();

        } else if ( e.getActionCommand().equals("addTile") ) {

            this.setVisible(false);
            ((BoardPracticeWordplayGame)game()).emptyRack();

            guiHandpickTiles.setVisible(true);

        } else if ( e.getActionCommand().equals("newRack") ) {
            newRack();

        } else if ( e.getActionCommand().equals("changeWordList") ) {
            JComboBox wordList = (JComboBox) e.getSource();
            game().setCurrentDictionary((Dawg) dawgObjects.get(wordList.getSelectedIndex()));

            newRack();
        }
    }

}
