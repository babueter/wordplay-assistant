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

// Gui representatino of a board object
public class GuiBoard extends JPanel implements MouseListener, ObjectListener {
    private Board board;

    // Preferred tile size
    private int preferredTileSize = 30;

    // Preferred heading sizes
    private int preferredRowHeadingSize = 20;
    private int preferredColHeadingSize = 20;

    // Preferred dimentions are set after creation
    private int preferredHeight;
    private int preferredWidth;

    // Keep track of the previous play
    public static final int NORMAL = 0;
    public static final int PLAYING_HORIZONTAL = 1;
    public static final int PLAYING_VERTICAL = 2;

    ObjectHandler eventHandler;
    private int clickedRow;
    private int clickedCol;


    // We need a board to initialize ourselves
    public GuiBoard(Board initBoard) {
        initComponents(initBoard);
    }
    public GuiBoard(Board initBoard, boolean hasHeadings) {
        hasHeadings(hasHeadings);
        initComponents(initBoard);
    }
    public ObjectHandler eventHandler() { return eventHandler; }

    public void hasHeadings(Boolean hasHeadings) {
        if ( ! hasHeadings ) {
            preferredRowHeadingSize = 0;
            preferredColHeadingSize = 0;
        } else {
            preferredRowHeadingSize = 20;
            preferredColHeadingSize = 20;
        }
        initComponents(board);
    }
    private void initComponents(Board initBoard) {
        board = initBoard;

        // Set the preferred sizes here:
        preferredHeight = preferredRowHeadingSize+preferredTileSize*board.Rows();
        preferredWidth = preferredColHeadingSize+preferredTileSize*board.Columns();
        this.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        this.setVisible(true);

        eventHandler = new ObjectHandler(this);

        addMouseListener(this);
        board.getEventHandler().register(this);
    }

    public int clickedRow() { return clickedRow; }
    public int clickedCol() { return clickedCol; }

    // Draw the canvas
    @Override
    public void paintComponent(Graphics g) {
        // Paint a buffered image to be scaled to current window
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

        // Paint the row and column labels
        g2.setColor(GuiColors.headingColor);
        g2.fill3DRect(0, 0, preferredColHeadingSize, preferredRowHeadingSize, true);

        FontMetrics fm;
        Font headingFont = new Font("Arial", Font.PLAIN, 12);
        for (Integer headingLabel=0; headingLabel<board.Columns(); headingLabel++) {
            g2.setColor(GuiColors.headingColor);

            g2.fill3DRect(0, yByHeadingRow(headingLabel), preferredRowHeadingSize, preferredTileSize, true);
            g2.fill3DRect(xByHeadingCol(headingLabel), 0, preferredTileSize, preferredColHeadingSize, true);

            g2.setColor(GuiColors.BLACK);
            g2.setFont(headingFont);
            fm = g2.getFontMetrics(headingFont);

            Integer columnInt = headingLabel+1;
            g.drawString(columnInt.toString(), (preferredColHeadingSize/2)-(fm.stringWidth(columnInt.toString())/2),
                    yByHeadingRow(headingLabel)+(preferredTileSize/2)+(fm.getAscent()/2)-2);

            String rowText = Character.toString(Move.columnToChar(headingLabel));
            g.drawString(rowText, xByHeadingCol(headingLabel)+(preferredTileSize/2)-(fm.stringWidth(rowText)/2),
                    (preferredTileSize/2)+(fm.getAscent()/2)-6);
        }
        
        // Paint each tile individually
        int row, col;
        for (row=0; row<board.Rows(); row++) {
            for (col=0; col<board.Columns(); col++) {
                if ( board.tile(row, col) == null ) {
                    g2.setColor(GuiColors.bonusColors[board.bonus(row, col)]);
                    g2.fill3DRect(xByCol(col), yByRow(row), preferredTileSize, preferredTileSize, true);
                } else {
                    paintTile(g2, board.tile(row, col), yByRow(row), xByCol(col));
                }
            }
        }

        // Paint any tiles that have been played
        int drawingRow = board.startingRow();
        int drawingCol = board.startingCol();
        for (int index = 0; index < board.tilesPlayed().size(); index++) {
            // Advance to the first empty square
            while (board.tile(drawingRow, drawingCol) != null) {
                if (board.directionPlaying() == Move.HORIZONTAL) {
                    drawingCol++;
                } else if (board.directionPlaying() == Move.VERTICAL) {
                    drawingRow++;
                }
            }

            // paint the played tile
            Tile tile = (Tile) board.tilesPlayed().tile(index);
            paintTile(g2, tile, yByRow(drawingRow), xByCol(drawingCol));

            // Advance to the next empty square
            do {
                if (board.directionPlaying() == Move.HORIZONTAL) {
                    drawingCol++;
                } else if (board.directionPlaying() == Move.VERTICAL) {
                    drawingRow++;
                }
            }while (board.tile(drawingRow, drawingCol) != null);
        }

        // Paint the arrow
        Font arrowFont = new Font("SansSerif", Font.BOLD, 18);
        fm = g2.getFontMetrics(arrowFont);

        g2.setColor(GuiColors.BLACK);
        g2.setFont(arrowFont);
        if (board.directionPlaying() == Move.HORIZONTAL ) {
            g.drawString("\u21e8", xByCol(board.currentCol())+(preferredTileSize/2)-(fm.stringWidth("\u21e8")/2),
                    yByRow(board.currentRow())+(preferredTileSize/2)+(fm.getAscent()/2)-2);
        } else if ( board.directionPlaying() == Move.VERTICAL ) {
            g.drawString("\u21e9", xByCol(board.currentCol())+(preferredTileSize/2)-(fm.stringWidth("\u21e9")/2),
                    yByRow(board.currentRow())+(preferredTileSize/2)+(fm.getAscent()/2)-2);
        }
    }
    private void paintTile(Graphics g, Tile tile, int row, int col) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(GuiColors.tileColor);
        g2.fill3DRect(col, row, preferredTileSize, preferredTileSize, true);

        FontMetrics fm;
        Font letterFont = new Font("Arial", Font.BOLD, 20);
        Font scoreFont = new Font("Serif", Font.PLAIN, 10);

        // Set the font color to black
        g2.setColor(GuiColors.BLACK);
        if ( tile.isBlank() ) {
            g2.setColor(GuiColors.BLUE);
        }

        // Draw the points on the tile
        g2.setFont(scoreFont);
        fm = g2.getFontMetrics(scoreFont);

        Integer points = tile.points();
        g.drawString(points.toString(), col+(preferredTileSize-1)-fm.stringWidth(points.toString()), row+preferredTileSize-(fm.getAscent()/2)+1);

        g2.setFont(letterFont);
        fm = g2.getFontMetrics(letterFont);

        g.drawString(tile.toString(), col+(preferredTileSize/2)-(fm.stringWidth(tile.toString())/2)-2, row+(preferredTileSize/2)+(fm.getAscent()/2)-2);
    }

    @Override
    public void update(Graphics g) { paint(g); }

    // Calculate the starting coordinates by row and column
    private int xByCol(int col) {
        return preferredColHeadingSize+col*preferredTileSize;
    }
    private int yByRow(int row) {
        return preferredRowHeadingSize+row*preferredTileSize;
    }
    private int xByHeadingCol(int col) {
        return preferredColHeadingSize+col*preferredTileSize;
    }
    private int yByHeadingRow(int row) {
        return preferredRowHeadingSize+row*preferredTileSize;
    }

    // Calculate the tile selected by click
    private int colByX(int x) {
        if ( x < preferredColHeadingSize ) { return 0; }

        return ((x-preferredColHeadingSize)/preferredTileSize);
    }
    private int rowByY(int y) {
        if ( y < preferredRowHeadingSize ) { return 0; }

        return ((y-preferredRowHeadingSize)/preferredTileSize);
    }

    public void mouseClicked(MouseEvent e) {
        // Translate x and y coordinates to scale
        int x = e.getX();
        int y = e.getY();

        x /= (float)this.getWidth()/preferredWidth;
        y /= (float)this.getHeight()/preferredHeight;

        clickedCol = colByX(x);
        clickedRow = rowByY(y);
        // Let listening objects know a tile was clicked
        eventHandler.callback();

        repaint();
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
