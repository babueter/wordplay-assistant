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

package Events;

import java.util.*;

// Event handlers for any entity that need to callback after an update
public class ObjectHandler {
    private Object entity;

    // List of registered objects
    private Vector registeredObjects;
    private int registeredCount;

    // Constructor
    public ObjectHandler(Object entityInit) {
        entity = entityInit;

        registeredObjects = new Vector();
        registeredCount = 0;
    }
    public Object getEntity() {
        return entity;
    }

    // Registration methods
    public void register(Object registrant) {
        // Dont re-register the same objects
        for (int index=0; index<registeredCount; index++) {
            if ( registrant == registeredObjects.get(index) ) { return; }
        }
        registeredObjects.add(registrant);
        registeredCount++;
    }
    public void unregister(Object registrant) {
        for (int index=0; index<registeredCount; index++) {
            if ( registeredObjects.get(index) == registrant ) {
                registeredObjects.remove(index);
                registeredCount--;
            }
        }
    }

    // Call back everything that has registered itself with us
    public void callback() {
        int index;
        for (index=0; index<registeredCount; index++) {
            ((ObjectListener)registeredObjects.get(index)).objectUpdated(this);
        }
    }
}
