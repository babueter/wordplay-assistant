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

package Dictionary;

import Assistant.util.*;
import Data.*;
import java.io.*;
import java.util.logging.*;
import javax.swing.*;

// Dawg structure stores our dictionary
public class Dawg extends Node<Dawg> {
    private DictionaryConstraint constraint;

    public Dawg() { super(); }
    public Dawg(final InputStream is) {
        constraint = new DictionaryConstraint();

        init(is);
    }

    // This could be less elaborate, but when loading large files its helpfull to
    // have a progress bar
    protected void init(final InputStream is) {

        // Not really sure how else to access myself from inside the thread
        final Dawg thisDawg = this;
        Thread thread = new Thread() {
            @Override
            public void run() {
                // Show a progress bar while we load the file
                Object message[] = { "Reading Dictionary" };
                ProgressMonitorInputStream pm = new ProgressMonitorInputStream(null, message, is);
                final LittleEndianInputStream in = new LittleEndianInputStream(pm);

                // Read in the first integer, which should be the count of nodes in the file
                int count = 0;
                try {
                    count = in.readInt();
                } catch (IOException ex) {
                    Logger.getLogger(Dawg.class.getName()).log(Level.SEVERE, null, ex);
                    Thread.currentThread().interrupt();
                    return;
                }

                // Create a list of nodes to reference later
                Dawg[] nodeList = new Dawg[count + 1];
                nodeList[0] = null;
                nodeList[1] = thisDawg;
                int i;
                for (i = 2; i <= count; i++) {
                    nodeList[i] = new Dawg();
                    
                    // Force everyone to use the same constraint
                    nodeList[i].constraint = thisDawg.constraint;
                }

                // Read in each node from the file, exit on failure
                for (i = 1; i <= count; i++) {
                    try {
                        nodeList[i].value(((char) in.readByte()));
                        nodeList[i].sibling(nodeList[in.readInt()]);
                        nodeList[i].child(nodeList[in.readInt()]);
                        int bool = in.readInt();
                        if (bool > 0) {
                            nodeList[i].terminal(true);
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Dawg.class.getName()).log(Level.SEVERE, null, ex);
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        };
        try {
            thread.start();
            thread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Dawg.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Be carefull what you wish for...
    public void printTree() {
        printLevel(this, 0);
    }
    private void printLevel(Dawg node, int level) {
        int i;
        for (i=0; i<level; i++) {
            System.out.print("|");
        }

        node.print();
        Dawg child = node.child();
        while ( child != null ) {
            printLevel(child, level+1);
            child = child.sibling();
        }
    }

    public DictionaryConstraint getConstraint() { return constraint; }

    // Determine if a word exists in this structure
    public boolean validWord (String word) {
        if ( ! constraint.validate(word) ) { return false; }

        return validWordRecurse(word);
    }
    public boolean validWordRecurse (String word) {
        // Recursively determine if this word is valid

        // Out of letters, determine if this node is terminal
        if ( word.length() == 0 ) {
            return this.isTerminal();
        }

        // Stop right here if this node is childless
        char childValue = word.charAt(0);
        if ( ! this.hasChild(childValue) ) { return false; }

        // Drop the first letter of the string as that was this nodes value
        String nextWord = word.substring(1);
        return ((Dawg)this.child(childValue)).validWordRecurse(nextWord);
    }
}
