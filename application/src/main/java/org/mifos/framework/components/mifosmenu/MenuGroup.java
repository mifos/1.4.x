/*
 * Copyright (c) 2005-2009 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.framework.components.mifosmenu;

/**
 * It stores menugroups for a left menu.
 */
public class MenuGroup {
    /**
     * displayName is the display name for MenuGroup.
     */
    private String[] displayName = null;

    /**
     * menuItems is an array of MenuItem objects for this menugroup.
     */
    private MenuItem[] menuItems = null;

    /**
     * Method to get the value of menuItems member variable.
     * 
     * @return an array of MenuItem objects
     */
    public MenuItem[] getMenuItems() {
        return menuItems;
    }

    /**
     * Method to set the value of menuItems member variable.
     * 
     * @param menuItems
     *            an array of MenuItem objects
     */
    public void setMenuItems(MenuItem[] menuItems) {
        this.menuItems = menuItems;
    }

    /**
     * Method to get the value of displayName member variable. If it is crude
     * menu displayName length will be >=1 otherwise it will be 1 only.
     * 
     * @return an array of String holding displayNames
     */
    public String[] getDisplayName() {
        return displayName;
    }

    /**
     * Method to set the value of displayName member variable. If it is crude
     * menu displayName length will be >=1 otherwise it will be 1 only.
     * 
     * @param displayName
     *            an array of displaynames.
     */
    public void setDisplayName(String[] displayName) {
        this.displayName = displayName;
    }
}
