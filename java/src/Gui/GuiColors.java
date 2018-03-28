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

import java.awt.*;

// Common colors everyone should use
public class GuiColors extends Color {
    public static final Color tileColor = new Color(255, 215, 0);
    public static final Color headingColor = new Color(240, 230, 140);  // Khaki

    // Taken from Quackle graphicalboard.cpp
    public static final Color[] bonusColors = {
        new Color(220,220,220),     // Normal square        : gainsboro
        new Color(100, 149, 237),   // Double Letter Score  : cornflower blue
        new Color(219, 112, 147),   // Double Word Score    : paleviolet red
        new Color(127, 0, 255),     // Tripple Letter Score : medium slate blue
        new Color(178, 34, 34)};    // Tripple Word Score   : firebrick red

    // Default constructor needed to extend Color
    public GuiColors(int rgb) {
        super(rgb);
    }
}
