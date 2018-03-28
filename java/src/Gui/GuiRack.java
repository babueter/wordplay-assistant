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

// Canvas that displays a rack and allows sorting by mouse click and drag
public class GuiRack  extends JPanel implements MouseListener, MouseMotionListener, ObjectListener {
    private Rack rack;

    // Preferred dimensions
    public static final int preferredWidth = 450;
    public static final int preferredHeight = 90;
    
    // Preferred tile sizes
    private static final int preferredTileSize = 50;
    private static final int preferredTileSpacing = 10;
    private static final int preferredTileBorder = 20;

    // Keeping track of a tile that was picked up
    private Tile selectedTile;
    private int selectedXlocation;
    private int selectedYlocation;

    // Must supply a rack to create this object
    public GuiRack(Rack initRack) {
        rack = initRack;
        selectedTile = null;
        
        //this.setSize(preferredWidth, preferredHeight);
        this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        this.setVisible(true);

        addMouseListener(this);
        addMouseMotionListener(this);

        rack.eventHandler().register(this);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        // Paint a preferred size image to be scaled to current window
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

        // Paint each tile in the rack
        int tile;
        for (tile=0; tile<rack.size(); tile++) {
            int x_offset = tile*preferredTileSpacing + preferredTileBorder + tile*preferredTileSize;
            int y_offset = preferredTileBorder;

            if ( rack.tile(tile) == selectedTile ) {
                g2.setColor(GuiColors.LIGHT_GRAY);
            } else {
                g2.setColor(GuiColors.tileColor);
            }
            paintTile(g2, rack.tile(tile), x_offset, y_offset);
        }

        // Paint the tile wherever we tracked the mouse last
        if ( selectedTile != null ) {
            g2.setColor(GuiColors.tileColor);
            paintTile(g2, selectedTile, selectedXlocation, selectedYlocation);
        }
    }
    private void paintTile(Graphics g, Tile tile, int x, int y) {
        // You must set the background color first
        Graphics2D g2 = (Graphics2D) g;
        g2.fill3DRect(x, y, preferredTileSize, preferredTileSize, true);

        FontMetrics fm;
        Font letterFont = new Font("Arial", Font.BOLD, 36);
        Font scoreFont = new Font("Serif", Font.PLAIN, 14);

        // Set the font color to black
        g2.setColor(GuiColors.BLACK);

        // Draw the points on the tile
        g2.setFont(scoreFont);
        fm = g2.getFontMetrics(scoreFont);

        Integer points = tile.points();
        g.drawString(points.toString(), x+(preferredTileSize-1)-fm.stringWidth(points.toString()), y+preferredTileSize-(fm.getAscent()/2)+1);

        // Draw the letter on the tile
        g2.setFont(letterFont);
        fm = g2.getFontMetrics(letterFont);

        // Never draw a selected character on the rack
        if ( tile.isBlank() ) {
            g.drawString(Character.toString(Tile.BLANK), x+(preferredTileSize/2)-(fm.stringWidth(Character.toString(Tile.BLANK))/2)-2, y+(preferredTileSize/2)+(fm.getAscent()/2)-2);
        } else {
            g.drawString(tile.toString(), x+(preferredTileSize/2)-(fm.stringWidth(tile.toString())/2)-2, y+(preferredTileSize/2)+(fm.getAscent()/2)-2);
        }

    }

    @Override
    public void update(Graphics g) { paint(g); }

    // Return the tile selected using the coordinates given.
    // Coordinates are assumed to be translated to preferred Size
    private Tile tileByCoordinate(int x, int y) {
        // Return null if we clicked in the preferredTileBorder area
        if ( y < preferredTileBorder || y > (preferredTileBorder+preferredTileSize) ) { return null; }
        if ( x < preferredTileBorder || x > preferredWidth-preferredTileBorder ) { return null; }

        // Calculate x range
        x -= preferredTileBorder;
        if ( x < 0 ) { return null; }
        int tileIndex = x/(preferredTileSize+preferredTileSpacing);

        // Ignore the white spacing between tiles
        if ( tileIndex*(preferredTileSize+preferredTileSpacing)+preferredTileSize < x ) { return null; }

        return rack.tile(tileIndex);
    }

    public void mouseClicked(MouseEvent e) {
    }

    // Grab a tile
    public void mousePressed(MouseEvent e) {
        // Translate x and y coordinates to preferred size
        int x = e.getX();
        int y = e.getY();

        x /= (float)this.getWidth()/preferredWidth;
        y /= (float)this.getHeight()/preferredHeight;

        selectedTile = tileByCoordinate(x, y);
        mouseDragged(e);
        repaint();
    }

    // Drop a tile and deal with the results
    public void mouseReleased(MouseEvent e) {
        // Translate x and y coordinates to preferred size
        int x = e.getX();
        int y = e.getY();

        x /= (float)this.getWidth()/preferredWidth;
        y /= (float)this.getHeight()/preferredHeight;

        if ( selectedTile != null ) {
            Tile releasedTile = tileByCoordinate(x, y);
            if ( releasedTile != selectedTile && releasedTile != null ) {
                int from = rack.indexByTile(selectedTile);
                int to = rack.indexByTile(releasedTile);

                rack.swap(from, to);
            }
        }
        selectedTile = null;
        repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    // Set the location of the tile to be painted
    public void mouseDragged(MouseEvent e) {
        if ( selectedTile == null ) { return; }

        // Translate x and y coordinates to preferred size
        int x = e.getX();
        int y = e.getY();

        x /= (float)this.getWidth()/preferredWidth;
        y /= (float)this.getHeight()/preferredHeight;

        // Put the tile directly under the mouse
        x -= preferredTileSize/2;
        y -= preferredTileSize/2;

        selectedXlocation = x;
        selectedYlocation = y;
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void objectUpdated(ObjectHandler o) {
        repaint();
    }


}
