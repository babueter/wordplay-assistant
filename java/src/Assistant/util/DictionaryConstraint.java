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

import Game.*;

// Set of constraints used by Dawg and Gaddag objects to filter out words
public class DictionaryConstraint {
    private String regexExpression;
    private String containsLetters;

    private int exactLetters = -1;
    private int maxLetters = -1;
    private int minLetters = -1;

    public DictionaryConstraint() {
        regexExpression = new String();
        containsLetters = new String();
    }

    public void reset() {
        regexExpression = "";
        containsLetters = "";

        exactLetters = -1;
        maxLetters = -1;
        minLetters = -1;
    }

    // Create a regex that each word must match
    public void setRegex (String initRegexExpression) {
        regexExpression = initRegexExpression;
    }

    // Each word must contaion at least these letters
    public void setContainsLetters (String initContainsLetters) {
        containsLetters = initContainsLetters;
    }

    // Each word must have exactly these letters
    public void setExactLetters (int initExactLetters) {
        exactLetters = initExactLetters;
    }

    // Set an upper and lower limit to the words we'll allow
    public void setMaxLetters (int initMaxLetters) {
        maxLetters = initMaxLetters;
        if ( maxLetters < minLetters ) { minLetters = maxLetters; }
    }
    public void setMinLetters (int initMinLetters) {
        minLetters = initMinLetters;
    }

    public String getRegex() { return regexExpression; }
    public String getContainsLetters() { return containsLetters; }
    public int getExactLetters() { return exactLetters; }
    public int getMaxLetters() { return maxLetters; }
    public int getMinLetters() { return minLetters; }

    // Validate should be called by the Dawg or Gaddag object validWord() routines
    public boolean validate(String word) {
        if ( ! validateRegexExpression(word) || ! validateContainsLetters(word) ) {
            return false;
        }

        if ( exactLetters > 0 ) {
            if ( word.length() != exactLetters ) { return false; }
        }
        if ( maxLetters > 0 && word.length() > maxLetters ) {
            return false;
        }
        if ( minLetters > 0 && word.length() < minLetters ) {
            return false;
        }
        return true;
    }
    private boolean validateRegexExpression(String word) {
        if ( regexExpression.isEmpty() ) {
            return true;
        }

        return word.matches(regexExpression);
    }
    private boolean validateContainsLetters(String word) {
        if ( word.length() < containsLetters.length() ) { return false; }

        for (char letter : containsLetters.toCharArray() ) {
            if ( ! word.contains(Character.toString(letter)) && letter != Tile.BLANK ) {
                return false;
            }
        }
        return true;
    }
}
