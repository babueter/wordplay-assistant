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

import java.io.InputStream;

// Same structure as a DAWG but different contents
public class Gaddag extends Dawg {

    public Gaddag(final InputStream is) {
        init(is);
    }

    // Gaddag words are stored rotated by at least one character, separated with '#'
    @Override
    public boolean validWord(String word) {
        return super.validWord(word.charAt(0)+"#"+word.substring(1));
    }
}
