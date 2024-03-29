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

package org.mifos.accounts.api;


/**
 * The Class UserReferenceDto is a Data Transfer Object to hold 
 * a reference to a Mifos user. A Mifos user is currently represented as 
 * a PersonnelBO.
 */
public class UserReferenceDto {
    
    /** The user id. */
    private final short userId;

    /**
     * Instantiates a new user reference dto.
     * 
     * @param userId the user id
     */
    public UserReferenceDto(short userId) {
        this.userId = userId;
    }

    /**
     * Gets the user id.
     * 
     * @return the user id
     */
    public short getUserId() {
        return this.userId;
    }

}
