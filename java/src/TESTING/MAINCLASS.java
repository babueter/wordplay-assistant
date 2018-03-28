package TESTING;

import Dictionary.*;

public class MAINCLASS {
    public MAINCLASS() {
        Dawg dawg = new Dawg(getClass().getResourceAsStream("/Data/twl.dawg"));

        Dawg nextNode = dawg.child('T');
        nextNode = nextNode.child('E');
        nextNode = nextNode.child('A');
        nextNode = nextNode.child('M');
        nextNode = nextNode.child('E');

        nextNode.printTree();

        nextNode = nextNode.child();
        nextNode.printTree();

        nextNode = nextNode.sibling();
        if ( nextNode == null ) {
            System.out.println("Oh yeah, its null");
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MAINCLASS();
            }
        });
    }
}
