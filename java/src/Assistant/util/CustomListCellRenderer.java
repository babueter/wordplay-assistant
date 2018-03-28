/*  This file is part of Wordplay Trainer.
 *
 *  Copyright 2012 Bryan Bueter
 *
 *  Wordplay Trainer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Wordplay Trainer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Wordplay Trainer.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package Assistant.util;

import java.awt.*;
import javax.swing.*;

// Class to allow alternative color and font
public class CustomListCellRenderer extends JLabel implements ListCellRenderer {
    private Color fontColor;
    private Color backgroundColor;
    private Color selectedFontColor;
    private Color selectedBackgroundColor;

    private Font fontType;

    public CustomListCellRenderer() {
        setOpaque(true);

        fontColor = Color.BLACK;
        backgroundColor = Color.WHITE;

        selectedFontColor = Color.WHITE;
        selectedBackgroundColor = Color.BLUE;

    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.toString());
        setBackground(isSelected ? selectedBackgroundColor : Color.white);
        setForeground(isSelected ? selectedFontColor : fontColor);

        setFont(list.getFont());
        return this;
    }

    public void setFontColor(Color color) {
        fontColor = color;
    }
    public void setSelectedFontColor(Color color) {
        selectedFontColor = color;
    }
    public void setBackgroundColor(Color color) {
        backgroundColor = color;
    }
    public void setSelectedBackgroundColor(Color color) {
        selectedBackgroundColor = color;
    }

}
