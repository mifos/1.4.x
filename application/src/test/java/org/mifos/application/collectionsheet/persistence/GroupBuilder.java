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
package org.mifos.application.collectionsheet.persistence;

import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.CustomerMeetingEntity;
import org.mifos.application.customer.group.business.GroupBO;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.fees.business.AmountFeeBO;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.framework.util.helpers.Constants;

/**
 *
 */
public class GroupBuilder {
    
    private GroupBO group;
    private final CustomerAccountBuilder customerAccountBuilder = new CustomerAccountBuilder();
    private final CustomerLevel customerLevel = CustomerLevel.GROUP;
    private String name = "Test Group";
    private MeetingBO meeting = new MeetingBuilder().customerMeeting().weekly().every(1).startingToday().build();
    private OfficeBO office;
    private PersonnelBO loanOfficer;
    private final String searchId = "1.1.1";
    private final Short updatedFlag = Constants.NO;
    private final CustomerStatus customerStatus = CustomerStatus.GROUP_ACTIVE;
    private CustomerBO parentCustomer;
    
    public GroupBO build() {

        final CustomerMeetingEntity customerMeeting = new CustomerMeetingEntity(meeting, updatedFlag);
        group = new GroupBO(customerLevel, customerStatus, name, office, loanOfficer, customerMeeting, searchId,
                parentCustomer);

        // add relationship between customer account and group.
        customerAccountBuilder.withCustomer(group).withOffice(office).withLoanOfficer(loanOfficer).buildForIntegrationTests();

        return group;
    }
    
    public GroupBuilder withName(final String withName) {
        this.name = withName;
        return this;
    }

    public GroupBuilder withMeeting(final MeetingBO withMeeting) {
        this.meeting = withMeeting;
        return this;
    }

    public GroupBuilder withOffice(final OfficeBO withOffice) {
        this.office = withOffice;
        return this;
    }

    public GroupBuilder withLoanOfficer(final PersonnelBO withLoanOfficer) {
        this.loanOfficer = withLoanOfficer;
        return this;
    }
    
    public GroupBuilder withFee(final AmountFeeBO withFee) {
        customerAccountBuilder.withFee(withFee);
        return this;
    }
    
    public GroupBuilder withParentCustomer(final CustomerBO withParentCustomer) {
        this.parentCustomer = withParentCustomer;
        return this;
    }
}
