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
 * It stores menuitems for a menugroup.
 */
public class MenuItem {
    /**
     * displayName is the display name for MenuItem.
     */
    private String displayName[];
    
    /**
     * linkId is the id attribute for the link tag that will
     * be displayed by this menu item.
     */
    private String linkId;

    /**
     * linkValue is the page, which has to be shown when user clicks this
     * menuitem.
     */
    private String linkValue;

    /**
     * Method to get the value of linkValue member variable.
     * 
     * @return String that holds linkValue.
     */
    public String getLinkValue() {
        return linkValue;
    }

    /**
     * Method to set the value of linkValue member variable.
     * 
     * @param linkValue
     *            is the link for this menuitem.
     */
    public void setLinkValue(String linkValue) {
        this.linkValue = linkValue;
    }
    
    /**
     * Method to get the value of linkId member variable.
     * 
     * @return String that holds linkId.
     */
    public String getLinkId() {
        return linkId;
    }

    /**
     * Method to set the value of linkId member variable.
     * 
     * @param linkId
     *            is the id for the link for this menuitem.
     */
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    /**
     * Method to get the value of displayName member variable. If it is crude
     * menu displayName length will be >=1 otherwise it will be 1 only.
     * 
     * @return an array of String holding displayNames.
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
