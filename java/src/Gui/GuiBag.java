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
import java.awt.image.*;
import javax.swing.*;

// Gui representation of a Bag
public class GuiBag extends JPanel implements MouseListener, ObjectListener  {
    private Bag bag;

    public char letterPicked;

    private final int preferredHeight = 380;
    private final int preferredWidth = 450;

    private final int preferredCols = 6;
    private final int preferredRows = 5;

    private ObjectHandler eventHandler;

    // We need a bag to initialize ourself
    public GuiBag (Bag initBag) {
        bag = initBag;
        eventHandler = new ObjectHandler(this);

        letterPicked = 0x0;

        this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        this.setVisible(true);

        addMouseListener(this);
        bag.getEventHandler().register(this);
    }
    public ObjectHandler getEventHandler() { return eventHandler; }

    // Determine which letter was selected
    public char getLetterPicked() { return letterPicked; }

    // Draw the canvas
    @Override
    public void paintComponent(Graphics g) {
        // Paint a 50x310 buffered image to be scaled to current window
        java.awt.Image bufferImage = createImage(preferredWidth, preferredHeight);
        paintPreferredSize(bufferImage.getGraphics());

        // Scale the current window size
        int width = getWidth();
        int height = getHeight();
        ReplicateScaleFilter scale = new ReplicateScaleFilter(width, height);
        FilteredImageSource fis = new FilteredImageSource(bufferImage.getSource(), scale);
        Image scaledImage = createImage(fis);

        g.drawImage(scaledImage, 0, 0, null);
    }
    private void paintPreferredSize(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Calculate the spacing
        int widthSpacing = (preferredWidth - preferredCols*50) / (preferredCols+1) ;
        int heightSpacing = (preferredHeight - preferredRows*50) / (preferredRows+1) ;

        int column = 0;
        int row = 0;
        for (char letter : Bag.LETTERS ) {
            int x = widthSpacing + (widthSpacing*column) + (column*50);
            int y = heightSpacing + (heightSpacing*row) + (row*50);

            if ( bag.tilesLeft(letter) > 0 ) {
                g2.setColor(GuiColors.tileColor);
            } else {
                g2.setColor(GuiColors.LIGHT_GRAY);
            }
            paintTile(g, letter, Bag.points(letter), x, y);

            column++;
            if ( column == preferredCols ) {
                row++;
                column = 0;
            }
        }
    }

    @Override
    public void update(Graphics g) { paint(g); }

    // Paint an individual tile at coordinate x,y
    private void paintTile(Graphics g, char tile, int points, int x, int y) {
        // You must set the background color first
        Graphics2D g2 = (Graphics2D) g;
        g2.fill3DRect(x, y, 50, 50, true);

        FontMetrics fm;
        Font letterFont = new Font("Arial", Font.BOLD, 36);
        Font scoreFont = new Font("Serif", Font.PLAIN, 14);

        // Set the font color to black
        g2.setColor(GuiColors.BLACK);

        // Draw the points on the tile
        g2.setFont(scoreFont);
        fm = g2.getFontMetrics(scoreFont);

        Integer pointsInteger = new Integer(points);
        g.drawString(pointsInteger.toString(), x+49-fm.stringWidth(pointsInteger.toString()), y+50-(fm.getAscent()/2)+1);

        // Draw the letter on the tile
        g2.setFont(letterFont);
        fm = g2.getFontMetrics(letterFont);

        g.drawString(Character.toString(tile), x+25-(fm.stringWidth(Character.toString(tile))/2)-2, y+25+(fm.getAscent()/2)-2);

    }

    // Return the tile selected at coordinate x,y
    private char tileByCoordinate(int x, int y) {
        // Calculate the spacing
        int widthSpacing = (preferredWidth - preferredCols*50) / (preferredCols+1) ;
        int heightSpacing = (preferredHeight - preferredRows*50) / (preferredRows+1) ;

        int column = 0;
        int row = 0;
        for (char letter : Bag.LETTERS ) {
            int xCoordinate = widthSpacing + (widthSpacing*column) + (column*50);
            int yCoordinate = heightSpacing + (heightSpacing*row) + (row*50);

            column++;
            if ( column == preferredCols ) {
                row++;
                column = 0;
            }

            // Cant select this letter because there are none left
            if ( bag.tilesLeft(letter) == 0 ) { continue; }

            // We moved past the actuall x,y coordinates so we couldnt have clicked anything
            if ( xCoordinate > x && yCoordinate > y ) { return 0x0; }

            // We just clicked a letter, return the value
            if ( x <= xCoordinate+50 && y <= yCoordinate+50 ) { return letter; }
        }

        return 0x0;
    }

    public void mouseClicked(MouseEvent e) {
        // Translate x and y coordinates to scale
        int x = e.getX();
        int y = e.getY();

        x /= (float)this.getWidth()/preferredWidth;
        y /= (float)this.getHeight()/preferredHeight;

        // Something actually happened, call back registered listeners
        letterPicked = tileByCoordinate(x, y);
        if ( letterPicked != 0x0 ) {
            eventHandler.callback();
        }
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void objectUpdated(ObjectHandler o) {
        repaint();
    }

}
