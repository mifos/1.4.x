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

package org.mifos.application.meeting.business.service;

import java.util.List;

import org.mifos.application.customer.exceptions.CustomerException;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.persistence.MeetingPersistence;
import org.mifos.framework.business.BusinessObject;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.security.util.ActivityMapper;
import org.mifos.framework.security.util.SecurityConstants;
import org.mifos.framework.security.util.UserContext;
import org.mifos.application.meeting.util.helpers.WeekDay;

public class MeetingBusinessService implements BusinessService {
    @Override
    public BusinessObject getBusinessObject(UserContext userContext) {
        return null;
    }

    public List<WeekDay> getWorkingDays() throws RuntimeException {
        return new MeetingPersistence().getWorkingDays();

    }

    public MeetingBO getMeeting(Integer meetingId) throws ServiceException {
        try {
            return new MeetingPersistence().getMeeting(meetingId);
        } catch (PersistenceException pe) {
            throw new ServiceException(pe);
        }
    }

    public void checkPermissionForEditMeetingSchedule(CustomerLevel customerLevel, UserContext userContext,
            Short recordOfficeId, Short recordLoanOfficerId) throws ApplicationException {
        if (!isPermissionAllowed(customerLevel, userContext, recordOfficeId, recordLoanOfficerId))
            throw new CustomerException(SecurityConstants.KEY_ACTIVITY_NOT_ALLOWED);
    }

    private boolean isPermissionAllowed(CustomerLevel customerLevel, UserContext userContext, Short recordOfficeId,
            Short recordLoanOfficerId) {
        return ActivityMapper.getInstance().isEditMeetingSchedulePermittedForCustomers(customerLevel, userContext,
                recordOfficeId, recordLoanOfficerId);
    }
}
