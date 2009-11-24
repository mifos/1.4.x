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

package org.mifos.application.meeting.business;

import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.framework.business.PersistentObject;

/**
 * This class encapsulate the type of the meeting e.g loan meeting
 * 
 * Also see {@link MeetingType}.
 */
public class MeetingTypeEntity extends PersistentObject {

    private Short meetingTypeId;

    private String meetingPurpose;

    private String description;

    public MeetingTypeEntity(MeetingType meetingType) {
        this.meetingTypeId = meetingType.getValue();
    }

    protected MeetingTypeEntity() {
    }

    public String getDescription() {
        return description;
    }

    public String getMeetingPurpose() {
        return meetingPurpose;
    }

    public Short getMeetingTypeId() {
        return meetingTypeId;
    }

    public MeetingType asEnum() {
        return MeetingType.fromInt(meetingTypeId);
    }
}
