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

package org.mifos.application.customer.group.business;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import org.mifos.application.accounts.business.AccountActionDateEntity;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.loan.business.LoanBOTestUtils;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.accounts.savings.business.SavingBOTestUtils;
import org.mifos.application.accounts.savings.util.helpers.SavingsTestHelper;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.accounts.util.helpers.AccountStates;
import org.mifos.application.accounts.util.helpers.AccountTypes;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.CustomerBOTestUtils;
import org.mifos.application.customer.business.CustomerHierarchyEntity;
import org.mifos.application.customer.business.CustomerMovementEntity;
import org.mifos.application.customer.business.CustomerPositionView;
import org.mifos.application.customer.center.business.CenterBO;
import org.mifos.application.customer.client.business.ClientBO;
import org.mifos.application.customer.exceptions.CustomerException;
import org.mifos.application.customer.group.persistence.GroupPersistence;
import org.mifos.application.customer.persistence.CustomerPersistence;
import org.mifos.application.customer.util.helpers.CustomerConstants;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.customer.util.helpers.CustomerStatusFlag;
import org.mifos.application.fees.business.AmountFeeBO;
import org.mifos.application.fees.business.FeeView;
import org.mifos.application.fees.persistence.FeePersistence;
import org.mifos.application.fees.util.helpers.FeeCategory;
import org.mifos.application.fees.util.helpers.FeePayment;
import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.master.business.CustomFieldView;
import org.mifos.application.master.persistence.MasterPersistence;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.exceptions.MeetingException;
import org.mifos.application.meeting.persistence.MeetingPersistence;
import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.application.meeting.util.helpers.RankType;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.office.persistence.OfficePersistence;
import org.mifos.application.office.util.helpers.OfficeLevel;
import org.mifos.application.office.util.helpers.OfficeStatus;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.application.personnel.persistence.PersonnelPersistence;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.application.productdefinition.business.SavingsOfferingBO;
import org.mifos.application.productdefinition.util.helpers.ApplicableTo;
import org.mifos.application.productdefinition.util.helpers.InterestType;
import org.mifos.application.productdefinition.util.helpers.PrdStatus;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.components.audit.business.AuditLog;
import org.mifos.framework.components.audit.business.AuditLogRecord;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.TestDatabase;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class GroupBOIntegrationTest extends MifosIntegrationTestCase {

    public GroupBOIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    private AccountBO account1 = null;

    private AccountBO account2 = null;

    private CenterBO center;

    private CenterBO center1 = null;

    private GroupBO group;

    private GroupBO group1;

    private ClientBO client;

    private ClientBO client1 = null;

    private ClientBO client2 = null;

    private MeetingBO meeting;

    private OfficeBO officeBO;

    private SavingsTestHelper helper = new SavingsTestHelper();

    private SavingsOfferingBO savingsOffering;

    private Short officeId3 = 3;

    private Short officeId1 = 1;
    private OfficeBO officeBo1;

    private Short personnelId = 3;
    private PersonnelBO personnelBo;

    CustomerPersistence customerPersistence = new CustomerPersistence();
    PersonnelPersistence personnelPersistence = new PersonnelPersistence();
    MasterPersistence masterPersistence = new MasterPersistence();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        personnelBo = personnelPersistence.getPersonnel(personnelId);
        officeBo1 = new OfficePersistence().getOffice(officeId1);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            TestObjectFactory.cleanUp(account2);
            TestObjectFactory.cleanUp(account1);
            TestObjectFactory.cleanUp(client1);
            TestObjectFactory.cleanUp(client2);
            TestObjectFactory.cleanUp(client);
            TestObjectFactory.cleanUp(group);
            TestObjectFactory.cleanUp(group1);
            TestObjectFactory.cleanUp(center);
            TestObjectFactory.cleanUp(center1);
            TestObjectFactory.cleanUp(officeBO);
        } catch (Exception e) {
            // TODO Whoops, cleanup didnt work, reset db
            TestDatabase.resetMySQLDatabase();
        }
        StaticHibernateUtil.closeSession();
        super.tearDown();
    }

    public void testGeneratePortfolioAtRisk() throws Exception {
        createInitialObject();
        TestObjectFactory.flushandCloseSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        for (AccountBO account : group.getAccounts()) {
            if (account.getType() == AccountTypes.LOAN_ACCOUNT) {
                changeFirstInstallmentDate(account, 31);
            }
        }
        for (AccountBO account : client.getAccounts()) {
            if (account.getType() == AccountTypes.LOAN_ACCOUNT) {
                changeFirstInstallmentDate(account, 31);
            }
        }
        group.getGroupPerformanceHistory().generatePortfolioAtRisk();
       Assert.assertEquals(new Money("1.0"), group.getGroupPerformanceHistory().getPortfolioAtRisk());
        TestObjectFactory.flushandCloseSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        account1 = TestObjectFactory.getObject(AccountBO.class, account1.getAccountId());
        account2 = TestObjectFactory.getObject(AccountBO.class, account2.getAccountId());
    }

    public void testChangeUpdatedMeeting() throws Exception {
        String oldMeetingPlace = "Delhi";
        MeetingBO weeklyMeeting = new MeetingBO(WeekDay.FRIDAY, Short.valueOf("1"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, oldMeetingPlace);
        group = TestObjectFactory.createGroupUnderBranch("group1", CustomerStatus.GROUP_ACTIVE, officeId3,
                weeklyMeeting, personnelId);

        client1 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        client2 = createClient(group, CustomerStatus.CLIENT_ACTIVE);

        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        MeetingBO groupMeeting = group.getCustomerMeeting().getMeeting();

        String meetingPlace = "Bangalore";
        MeetingBO newMeeting = new MeetingBO(WeekDay.THURSDAY, groupMeeting.getMeetingDetails().getRecurAfter(),
                groupMeeting.getStartDate(), MeetingType.CUSTOMER_MEETING, meetingPlace);

        group.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());

       Assert.assertEquals(WeekDay.FRIDAY, group.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.FRIDAY, client1.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.FRIDAY, client2.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());

       Assert.assertEquals(WeekDay.THURSDAY, group.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client1.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails()
                .getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client2.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails()
                .getWeekDay());

        Integer updatedMeetingId = group.getCustomerMeeting().getUpdatedMeeting().getMeetingId();

        client1.changeUpdatedMeeting();
        group.changeUpdatedMeeting();

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());

       Assert.assertEquals(WeekDay.THURSDAY, group.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client1.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client2.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());

        Assert.assertNull(group.getCustomerMeeting().getUpdatedMeeting());
        Assert.assertNull(client1.getCustomerMeeting().getUpdatedMeeting());
        Assert.assertNull(client2.getCustomerMeeting().getUpdatedMeeting());

        MeetingBO meeting = new MeetingPersistence().getMeeting(updatedMeetingId);
        Assert.assertNull(meeting);
    }

    public void testChangeStatus_UpdatePendingClientToPartial_OnGroupCancelled() throws Exception {
        group = TestObjectFactory.createGroupUnderBranch("MyGroup", CustomerStatus.GROUP_PENDING, Short.valueOf("3"),
                meeting, personnelId, null);
        client1 = createClient(group, CustomerStatus.CLIENT_PENDING);
        client2 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        group.setUserContext(TestObjectFactory.getContext());
        group.changeStatus(CustomerStatus.GROUP_CANCELLED, null, "Group Cancelled");
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());

       Assert.assertEquals(CustomerStatus.GROUP_CANCELLED, group.getStatus());
       Assert.assertEquals(CustomerStatus.CLIENT_PARTIAL, client1.getStatus());
       Assert.assertEquals(CustomerStatus.CLIENT_PARTIAL, client2.getStatus());
    }

    public void testSuccessfulUpdate_Group_UnderBranchForLoggig() throws Exception {
        String name = "Group_underBranch";
        group = createGroupUnderBranch(name, CustomerStatus.GROUP_ACTIVE, getCustomFields());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
        client1 = TestObjectFactory.createClient("Client1", CustomerStatus.CLIENT_ACTIVE, group);
        StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.getInterceptor().createInitialValueMap(group);

        List<CustomerPositionView> customerPositionList = new ArrayList<CustomerPositionView>();
        CustomerBOTestUtils.setDisplayName(group, "changed group name");
        group.update(TestUtils.makeUser(), group.getDisplayName(), personnelId, "ABCD", Short.valueOf("1"), new Date(),
                TestObjectFactory.getAddressHelper(), getNewCustomFields(), customerPositionList);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());

        List<AuditLog> auditLogList = TestObjectFactory.getChangeLog(EntityType.GROUP, group.getCustomerId());
       Assert.assertEquals(1, auditLogList.size());
       Assert.assertEquals(EntityType.GROUP.getValue(), auditLogList.get(0).getEntityType());
       Assert.assertEquals(8, auditLogList.get(0).getAuditLogRecords().size());
        for (AuditLogRecord auditLogRecord : auditLogList.get(0).getAuditLogRecords()) {
            if (auditLogRecord.getFieldName().equalsIgnoreCase("City/District")) {
               Assert.assertEquals("-", auditLogRecord.getOldValue());
               Assert.assertEquals("city", auditLogRecord.getNewValue());
            } else if (auditLogRecord.getFieldName().equalsIgnoreCase("Trained")) {
               Assert.assertEquals("0", auditLogRecord.getOldValue());
               Assert.assertEquals("1", auditLogRecord.getNewValue());
            } else if (auditLogRecord.getFieldName().equalsIgnoreCase("Name")) {
               Assert.assertEquals("Group_underBranch", auditLogRecord.getOldValue());
               Assert.assertEquals("changed group name", auditLogRecord.getNewValue());
            }
        }
        TestObjectFactory.cleanUpChangeLog();

    }

    public void testSuccessfulTransferToCenterInSameBranchForLogging() throws Exception {
        createObjectsForTranferToCenterInSameBranch();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        group.setUserContext(center.getUserContext());
        StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.getInterceptor().createInitialValueMap(group);
        group.transferToCenter(center1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());
        center1 = TestObjectFactory.getCenter(center1.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());

        List<AuditLog> auditLogList = TestObjectFactory.getChangeLog(EntityType.GROUP, group.getCustomerId());
       Assert.assertEquals(1, auditLogList.size());
       Assert.assertEquals(EntityType.GROUP.getValue(), auditLogList.get(0).getEntityType());
        for (AuditLogRecord auditLogRecord : auditLogList.get(0).getAuditLogRecords()) {
            if (auditLogRecord.getFieldName().equalsIgnoreCase("Kendra Name")) {
               Assert.assertEquals("Center", auditLogRecord.getOldValue());
               Assert.assertEquals("toTransfer", auditLogRecord.getNewValue());
            } else {
                // TODO: Kendra versus Center?
                // Assert.fail();
            }
        }
        TestObjectFactory.cleanUpChangeLog();
    }

    public void testCreateWithoutName() throws Exception {
        try {
            group = new GroupBO(TestUtils.makeUser(), "", CustomerStatus.GROUP_PARTIAL, null, false, null, null, null,
                    null, personnelBo, officeBo1, meeting, personnelBo, new GroupPersistence(), new OfficePersistence());
            Assert.assertFalse("Group Created", true);
        } catch (CustomerException ce) {
            Assert.assertNull(group);
           Assert.assertEquals(CustomerConstants.INVALID_NAME, ce.getKey());
        }
    }

    public void testCreateWithoutStatus() throws Exception {
        try {
            group = new GroupBO(TestUtils.makeUser(), "GroupName", null, null, false, null, null, null, null,
                    personnelBo, officeBo1, meeting, personnelBo, new GroupPersistence(), new OfficePersistence());
            Assert.assertFalse("Group Created", true);
        } catch (CustomerException ce) {
            Assert.assertNull(group);
           Assert.assertEquals(CustomerConstants.INVALID_STATUS, ce.getKey());
        }
    }

    public void testCreateWithoutOffice_WithoutCenterHierarchy() throws Exception {
        try {
            group = new GroupBO(TestUtils.makeUser(), "GroupName", CustomerStatus.GROUP_PARTIAL, null, false, null,
                    null, null, null, personnelBo, null, meeting, personnelBo, new GroupPersistence(),
                    new OfficePersistence());
            Assert.assertFalse("Group Created", true);
        } catch (CustomerException ce) {
            Assert.assertNull(group);
           Assert.assertEquals(CustomerConstants.INVALID_OFFICE, ce.getKey());
        }
    }

    public void testCreateWithoutLO_InActiveState_WithoutCenterHierarchy() throws Exception {
        try {
            group = new GroupBO(TestUtils.makeUser(), "GroupName", CustomerStatus.GROUP_ACTIVE, null, false, null,
                    null, null, null, personnelBo, officeBo1, meeting, null, new GroupPersistence(),
                    new OfficePersistence());
            Assert.assertFalse("Group Created", true);
        } catch (CustomerException ce) {
            Assert.assertNull(group);
           Assert.assertEquals(CustomerConstants.INVALID_LOAN_OFFICER, ce.getKey());
        }
    }

    public void testCreateWithoutMeeting_InActiveState_WithoutCenterHierarchy() throws Exception {
        try {
            group = new GroupBO(TestUtils.makeUser(), "GroupName", CustomerStatus.GROUP_ACTIVE, null, false, null,
                    null, null, null, personnelBo, officeBo1, null, personnelBo, new GroupPersistence(),
                    new OfficePersistence());
            Assert.assertFalse("Group Created", true);
        } catch (CustomerException ce) {
            Assert.assertNull(group);
           Assert.assertEquals(CustomerConstants.INVALID_MEETING, ce.getKey());
        }
    }

    public void testCreateWithoutParent_WhenCenter_HierarchyExists() throws Exception {
        try {
            meeting = getMeeting();
            group = new GroupBO(TestUtils.makeUser(), "GroupName", CustomerStatus.GROUP_PARTIAL, null, false, null,
                    null, null, null, personnelBo, null, new GroupPersistence(), new OfficePersistence());
            Assert.assertFalse("Group Created", true);
        } catch (CustomerException ce) {
            Assert.assertNull(group);
           Assert.assertEquals(CustomerConstants.INVALID_PARENT, ce.getKey());
        }
    }

    public void testCreateWithoutFormedBy() throws Exception {
        try {
            createCenter();
            meeting = getMeeting();
            group = new GroupBO(TestUtils.makeUser(), "GroupName", CustomerStatus.GROUP_PARTIAL, null, false, null,
                    null, null, null, null, center, new GroupPersistence(), new OfficePersistence());
            Assert.fail();
        } catch (CustomerException ce) {
            Assert.assertNull(group);
           Assert.assertEquals(CustomerConstants.INVALID_FORMED_BY, ce.getKey());
        }
    }

    public void testCreateWithoutTrainedDate_WhenTrained() throws Exception {
        try {
            createCenter();
            meeting = getMeeting();
            group = new GroupBO(TestUtils.makeUser(), "GroupName", CustomerStatus.GROUP_PARTIAL, null, true, null,
                    null, null, null, personnelBo, center, new GroupPersistence(), new OfficePersistence());
            Assert.assertFalse("Group Created", true);
        } catch (CustomerException ce) {
            Assert.assertNull(group);
           Assert.assertEquals(CustomerConstants.INVALID_TRAINED_OR_TRAINEDDATE, ce.getKey());
        }
        TestObjectFactory.removeObject(meeting);
    }

    public void testFailureCreate_DuplicateName() throws Exception {
        String name = "GroupTest";
        createCenter();
        createGroup(name);
        StaticHibernateUtil.closeSession();

        List<FeeView> fees = getFees();
        try {
            group1 = new GroupBO(TestUtils.makeUser(), name, CustomerStatus.GROUP_ACTIVE, null, false, null, null,
                    null, fees, personnelBo, center, new GroupPersistence(), new OfficePersistence());
            Assert.assertFalse(true);
        } catch (CustomerException e) {
           Assert.assertTrue(true);
            Assert.assertNull(group1);
           Assert.assertEquals(CustomerConstants.ERRORS_DUPLICATE_CUSTOMER, e.getKey());
        }
        removeFees(fees);
    }

    public void testSuccessfulCreate_Group_UnderCenter() throws Exception {
        createCenter();
        String name = "GroupTest";
        Date trainedDate = getDate("11/12/2005");
        String externalId = "1234";
        StaticHibernateUtil.closeSession();
       Assert.assertEquals(0, center.getMaxChildCount().intValue());

        group = new GroupBO(TestUtils.makeUser(), name, CustomerStatus.GROUP_ACTIVE, externalId, true, trainedDate,
                getAddress(), getCustomFields(), getFees(), personnelBo, center, new GroupPersistence(),
                new OfficePersistence());
        new GroupPersistence().saveGroup(group);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());

       Assert.assertEquals(name, group.getDisplayName());
       Assert.assertEquals(externalId, group.getExternalId());
       Assert.assertTrue(group.isTrained());
       Assert.assertEquals(trainedDate, DateUtils.getDateWithoutTimeStamp(group.getTrainedDate().getTime()));
       Assert.assertEquals(CustomerStatus.GROUP_ACTIVE, group.getStatus());
        Address address = group.getCustomerAddressDetail().getAddress();
       Assert.assertEquals("Aditi", address.getLine1());
       Assert.assertEquals("Bangalore", address.getCity());
       Assert.assertEquals(getCustomFields().size(), group.getCustomFields().size());
       Assert.assertEquals(1, center.getMaxChildCount().intValue());
       Assert.assertEquals(center.getPersonnel().getPersonnelId(), group.getPersonnel().getPersonnelId());
       Assert.assertEquals("1.1.1", group.getSearchId());
       Assert.assertEquals(group.getCustomerId(), group.getGroupPerformanceHistory().getGroup().getCustomerId());
        client = TestObjectFactory
                .createClient("new client", CustomerStatus.CLIENT_ACTIVE, group, new java.util.Date());
       Assert.assertEquals(1, group.getGroupPerformanceHistory().getActiveClientCount().intValue());
    }

    public void testSuccessfulCreate_Group_UnderBranch() throws Exception {
        String name = "GroupTest";
        String externalId = "1234";
        group = new GroupBO(TestUtils.makeUser(), name, CustomerStatus.GROUP_ACTIVE, externalId, false, null,
                getAddress(), getCustomFields(), getFees(), personnelBo, officeBo1, getMeeting(), personnelBo,
                new GroupPersistence(), new OfficePersistence());
        new GroupPersistence().saveGroup(group);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());

       Assert.assertEquals(name, group.getDisplayName());
       Assert.assertEquals(externalId, group.getExternalId());
        Assert.assertFalse(group.isTrained());
       Assert.assertEquals(CustomerStatus.GROUP_ACTIVE, group.getStatus());
        Address address = group.getCustomerAddressDetail().getAddress();
       Assert.assertEquals("Aditi", address.getLine1());
       Assert.assertEquals("Bangalore", address.getCity());
       Assert.assertEquals(getCustomFields().size(), group.getCustomFields().size());

       Assert.assertEquals(personnelId, group.getCustomerFormedByPersonnel().getPersonnelId());
       Assert.assertEquals(personnelId, group.getPersonnel().getPersonnelId());
       Assert.assertEquals(officeId1, group.getOffice().getOfficeId());
        Assert.assertNotNull(group.getCustomerMeeting().getMeeting());
       Assert.assertEquals("1.1", group.getSearchId());
    }

    public void testSuccessfulUpdate_Group_UnderBranch() throws Exception {
        String name = "Group_underBranch";
        String newName = "Group_NameChanged";
        group = createGroupUnderBranch(name, CustomerStatus.GROUP_ACTIVE);
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(name, group.getDisplayName());
        group.update(TestUtils.makeUser(), newName, personnelId, " ", Short.valueOf("1"), new Date(), TestObjectFactory
                .getAddressHelper(), getCustomFields(), new ArrayList<CustomerPositionView>());
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(newName, group.getDisplayName());
       Assert.assertTrue(group.isTrained());

    }

    public void testSuccessfulUpdate_Group_UnderCenter() throws Exception {
        String name = "Group_underBranch";
        String newName = "Group_NameChanged";
        createCenter();
        createGroup(name);
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(name, group.getDisplayName());
        group.update(TestUtils.makeUser(), newName, personnelId, " ", Short.valueOf("1"), new Date(), TestObjectFactory
                .getAddressHelper(), getCustomFields(), new ArrayList<CustomerPositionView>());
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(newName, group.getDisplayName());
       Assert.assertTrue(group.isTrained());

    }

    public void testFailureUpdate_ActiveGroup_WithoutLoanOfficer() throws Exception {
        String name = "Group_underBranch";
        String newName = "Group_NameChanged";
        group = createGroupUnderBranch(name, CustomerStatus.GROUP_ACTIVE);
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(name, group.getDisplayName());
        try {
            group.update(TestUtils.makeUser(), newName, null, " ", Short.valueOf("1"), new Date(), TestObjectFactory
                    .getAddressHelper(), getCustomFields(), new ArrayList<CustomerPositionView>());
            Assert.assertFalse(true);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.INVALID_LOAN_OFFICER, ce.getKey());
        }

    }

    public void testFailureUpdate_OnHoldGroup_WithoutLoanOfficer() throws Exception {
        String name = "Group_underBranch";
        String newName = "Group_NameChanged";
        group = createGroupUnderBranch(name, CustomerStatus.GROUP_HOLD);
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(name, group.getDisplayName());
        try {
            group.update(TestUtils.makeUser(), newName, null, " ", Short.valueOf("1"), new Date(), TestObjectFactory
                    .getAddressHelper(), getCustomFields(), new ArrayList<CustomerPositionView>());
            Assert.assertFalse(true);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.INVALID_LOAN_OFFICER, ce.getKey());
        }

    }

    public void testFailureUpdate_Group_WithDuplicateName() throws Exception {
        String name = "Group_underBranch";
        String newName = "Group_NameChanged";
        group = createGroupUnderBranch(name, CustomerStatus.GROUP_ACTIVE);
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = createGroupUnderBranch(newName, CustomerStatus.GROUP_ACTIVE);
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());
       Assert.assertEquals(name, group.getDisplayName());
       Assert.assertEquals(newName, group1.getDisplayName());
        try {
            group1.update(TestUtils.makeUser(), name, personnelId, " ", Short.valueOf("1"), new Date(),
                    TestObjectFactory.getAddressHelper(), getCustomFields(), new ArrayList<CustomerPositionView>());
            Assert.assertFalse(true);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.ERRORS_DUPLICATE_CUSTOMER, ce.getKey());
        }

    }

    public void testGetTotalOutStandingLoanAmount() throws Exception {
        createInitialObject();
        TestObjectFactory.flushandCloseSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(new Money("600.0"), group.getGroupPerformanceHistory().getTotalOutStandingLoanAmount());
       Assert.assertEquals(new Money("600.0"), group.getGroupPerformanceHistory().getTotalOutStandingLoanAmount());
        TestObjectFactory.flushandCloseSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        account1 = TestObjectFactory.getObject(AccountBO.class, account1.getAccountId());
        account2 = TestObjectFactory.getObject(AccountBO.class, account2.getAccountId());
    }

    public void testGetAverageLoanAmount() throws Exception {
        createInitialObject();
        TestObjectFactory.flushandCloseSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(new Money("300.0"), group.getGroupPerformanceHistory().getAvgLoanAmountForMember());
        TestObjectFactory.flushandCloseSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        account1 = TestObjectFactory.getObject(AccountBO.class, account1.getAccountId());
        account2 = TestObjectFactory.getObject(AccountBO.class, account2.getAccountId());
    }

    public void testGetTotalSavingsBalance() throws Exception {
        createInitialObjects();
        SavingsBO savings1 = getSavingsAccount(group, "fsaf6", "ads6");
        SavingBOTestUtils.setBalance(savings1, new Money("1000"));

        savings1.update();
        SavingsBO savings2 = getSavingsAccount(client, "fsaf5", "ads5");
        SavingBOTestUtils.setBalance(savings2, new Money("2000"));
        savings1.update();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        savings1 = TestObjectFactory.getObject(SavingsBO.class, savings1.getAccountId());
        savings2 = TestObjectFactory.getObject(SavingsBO.class, savings2.getAccountId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
       Assert.assertEquals(new Money("1000.0"), savings1.getSavingsBalance());
       Assert.assertEquals(new Money("2000.0"), savings2.getSavingsBalance());
       Assert.assertEquals(new Money("2000.0"), client.getSavingsBalance());
       Assert.assertEquals(new Money("3000.0"), group.getGroupPerformanceHistory().getTotalSavingsAmount());
        TestObjectFactory.flushandCloseSession();
        center = TestObjectFactory.getCenter(center.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        savings1 = TestObjectFactory.getObject(SavingsBO.class, savings1.getAccountId());
        savings2 = TestObjectFactory.getObject(SavingsBO.class, savings2.getAccountId());
        TestObjectFactory.cleanUp(savings1);
        TestObjectFactory.cleanUp(savings2);
    }

    public void testGetActiveOnHoldChildrenOfGroup() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createCenter("Center_Active_test", meeting);
        group = TestObjectFactory.createGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("client1", CustomerStatus.CLIENT_ACTIVE, group);
        client1 = TestObjectFactory.createClient("client2", CustomerStatus.CLIENT_HOLD, group);
        client2 = TestObjectFactory.createClient("client3", CustomerStatus.CLIENT_CANCELLED, group);
       Assert.assertEquals(Integer.valueOf("2"), group.getGroupPerformanceHistory().getActiveClientCount());
    }

    public void testUpdateBranchFailure_OfficeNULL() throws Exception {
        StaticHibernateUtil.startTransaction();
        group = createGroupUnderBranch(CustomerStatus.GROUP_ACTIVE);
        try {
            group.transferToBranch(null);
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.INVALID_OFFICE, ce.getKey());
        }
    }

    public void testUpdateBranchFailure_TransferInSameOffice() throws Exception {
        StaticHibernateUtil.startTransaction();
        group = createGroupUnderBranch(CustomerStatus.GROUP_ACTIVE);
        try {
            group.transferToBranch(group.getOffice());
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.ERRORS_SAME_BRANCH_TRANSFER, ce.getKey());
        }
    }

    public void testUpdateBranchFailure_OfficeInactive() throws Exception {
        StaticHibernateUtil.startTransaction();
        group = createGroupUnderBranch(CustomerStatus.GROUP_ACTIVE);
        officeBO = createOffice();
        officeBO.update(officeBO.getOfficeName(), officeBO.getShortName(), OfficeStatus.INACTIVE, officeBO
                .getOfficeLevel(), officeBO.getParentOffice(), null, null);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        try {
            group.transferToBranch(officeBO);
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.ERRORS_TRANSFER_IN_INACTIVE_OFFICE, ce.getKey());
        }
    }

    public void testUpdateBranchFailure_DuplicateGroupName() throws Exception {
        StaticHibernateUtil.startTransaction();
        group = createGroupUnderBranch(CustomerStatus.GROUP_ACTIVE);
        officeBO = createOffice();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        StaticHibernateUtil.startTransaction();
        group1 = createGroupUnderBranch(CustomerStatus.GROUP_ACTIVE, officeBO.getOfficeId());
        try {
            group.transferToBranch(officeBO);
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.ERRORS_DUPLICATE_CUSTOMER, ce.getKey());
        }
    }

    public void testSuccessfulTransferToBranch() throws Exception {
        StaticHibernateUtil.startTransaction();
        group = createGroupUnderBranch(CustomerStatus.GROUP_ACTIVE);
        client = createClient(group, CustomerStatus.CLIENT_ACTIVE);
        client1 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        client2 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        officeBO = createOffice();
        client2.changeStatus(CustomerStatus.CLIENT_CLOSED, CustomerStatusFlag.CLIENT_CLOSED_TRANSFERRED, "comment");
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        group.setUserContext(TestUtils.makeUser());
        Assert.assertNull(client.getActiveCustomerMovement());

        group.transferToBranch(officeBO);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());
        officeBO = new OfficePersistence().getOffice(officeBO.getOfficeId());
        Assert.assertNotNull(group.getActiveCustomerMovement());
        Assert.assertNotNull(client.getActiveCustomerMovement());
        Assert.assertNotNull(client1.getActiveCustomerMovement());
        Assert.assertNotNull(client2.getActiveCustomerMovement());

       Assert.assertEquals(officeBO.getOfficeId(), group.getOffice().getOfficeId());
       Assert.assertEquals(officeBO.getOfficeId(), client.getOffice().getOfficeId());
       Assert.assertEquals(officeBO.getOfficeId(), client1.getOffice().getOfficeId());
       Assert.assertEquals(officeBO.getOfficeId(), client2.getOffice().getOfficeId());

       Assert.assertEquals(CustomerStatus.GROUP_HOLD, group.getStatus());
       Assert.assertEquals(CustomerStatus.CLIENT_HOLD, client.getStatus());
       Assert.assertEquals(CustomerStatus.CLIENT_PARTIAL, client1.getStatus());
       Assert.assertEquals(CustomerStatus.CLIENT_CLOSED, client2.getStatus());

        Assert.assertNull(group.getPersonnel());
        Assert.assertNull(client.getPersonnel());
        Assert.assertNull(client1.getPersonnel());
        Assert.assertNull(client2.getPersonnel());
    }

    public void testUpdateCenterFailure_CenterNULL() throws Exception {
        createInitialObjects();
        try {
            group.transferToCenter(null);
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.INVALID_PARENT, ce.getKey());
        }
    }

    public void testUpdateCenterFailure_TransferInSameCenter() throws Exception {
        createInitialObjects();
        try {
            group.transferToCenter((CenterBO) group.getParentCustomer());
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.ERRORS_SAME_PARENT_TRANSFER, ce.getKey());
        }
    }

    public void testUpdateCenterFailure_TransferInInactiveCenter() throws Exception {
        createInitialObjects();
        center1 = createCenter("newCenter");
        center1.changeStatus(CustomerStatus.CENTER_INACTIVE, null, "changeStatus");
        StaticHibernateUtil.commitTransaction();
        try {
            group.transferToCenter(center1);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.ERRORS_INTRANSFER_PARENT_INACTIVE, e.getKey());
        }
    }

    public void testUpdateCenterFailure_GroupHasActiveAccount() throws Exception {
        createInitialObjects();
        account1 = getSavingsAccount(group, "Savings Prod", "SAVP");
        center1 = createCenter("newCenter");
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        try {
            group.transferToCenter(center1);
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.ERRORS_HAS_ACTIVE_ACCOUNT, ce.getKey());
        }
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        account1 = TestObjectFactory.getObject(AccountBO.class, account1.getAccountId());
    }

    public void testUpdateCenterFailure_GroupChildrenHasActiveAccount() throws Exception {
        createInitialObjects();
        account1 = getSavingsAccount(client, "Savings Prod", "SAVP");
        center1 = createCenter("newCenter");
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        try {
            group.transferToCenter(center1);
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.ERRORS_CHILDREN_HAS_ACTIVE_ACCOUNT, ce.getKey());
        }
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        account1 = TestObjectFactory.getObject(AccountBO.class, account1.getAccountId());
    }

    public void testUpdateCenterFailure_MeetingFrequencyMismatch() throws Exception {
        createInitialObjects();
        center1 = createCenter("newCenter", createMonthlyMeetingOnWeekDay(WeekDay.MONDAY, RankType.FIRST, Short
                .valueOf("1"), new Date()));
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        try {
            group.transferToCenter(center1);
           Assert.assertTrue(false);
        } catch (CustomerException ce) {
           Assert.assertTrue(true);
           Assert.assertEquals(CustomerConstants.ERRORS_MEETING_FREQUENCY_MISMATCH, ce.getKey());
        }
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
    }

    public void testUpdateCenterFailure_MeetingFrequencyMonthly() throws Exception {
        center = createCenter("Centerold", createMonthlyMeetingOnDate(Short.valueOf("5"), Short.valueOf("1"),
                new Date()));
        group = createGroup("groupold", center);
        client = createClient(group, CustomerStatus.CLIENT_ACTIVE);
        center1 = createCenter("newCenter", createMonthlyMeetingOnWeekDay(WeekDay.MONDAY, RankType.FIRST, Short
                .valueOf("1"), new Date()));
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        group.setUserContext(TestObjectFactory.getContext());
        group.transferToCenter(center1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());

        Assert.assertNotNull(group.getCustomerMeeting().getUpdatedMeeting());
        Assert.assertNotNull(client.getCustomerMeeting().getUpdatedMeeting());
       Assert.assertEquals(WeekDay.MONDAY, group.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.MONDAY, client.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());

        group.changeUpdatedMeeting();
        client.changeUpdatedMeeting();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());

        Assert.assertNull(group.getCustomerMeeting().getUpdatedMeeting());
        Assert.assertNull(client.getCustomerMeeting().getUpdatedMeeting());

       Assert.assertEquals(WeekDay.MONDAY, group.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.MONDAY, client.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());

        center = TestObjectFactory.getCenter(center.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
    }

    public void testSuccessfulTransferToCenterInSameBranch() throws Exception {
        createObjectsForTranferToCenterInSameBranch();
        String newCenterSearchId = center1.getSearchId();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        group.setUserContext(center.getUserContext());
        group.transferToCenter(center1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        center = TestObjectFactory.getCenter(center.getCustomerId());
        center1 = TestObjectFactory.getCenter(center1.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());

        Assert.assertNotNull(group.getCustomerMeeting().getUpdatedMeeting());
        Assert.assertNotNull(client.getCustomerMeeting().getUpdatedMeeting());
        Assert.assertNotNull(client1.getCustomerMeeting().getUpdatedMeeting());
        Assert.assertNotNull(client2.getCustomerMeeting().getUpdatedMeeting());

       Assert.assertEquals(WeekDay.THURSDAY, group.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client1.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails()
                .getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client2.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails()
                .getWeekDay());

       Assert.assertEquals(center1.getCustomerId(), group.getParentCustomer().getCustomerId());
       Assert.assertEquals(0, center.getMaxChildCount().intValue());
       Assert.assertEquals(2, center1.getMaxChildCount().intValue());
       Assert.assertEquals(3, group.getMaxChildCount().intValue());

       Assert.assertEquals(newCenterSearchId + ".2", group.getSearchId());
       Assert.assertEquals(group.getSearchId() + ".1", client.getSearchId());
       Assert.assertEquals(group.getSearchId() + ".2", client1.getSearchId());
       Assert.assertEquals(group.getSearchId() + ".3", client2.getSearchId());

        Assert.assertNull(group.getActiveCustomerMovement());
        Assert.assertNull(client.getActiveCustomerMovement());
        Assert.assertNull(client1.getActiveCustomerMovement());
        Assert.assertNull(client2.getActiveCustomerMovement());

        CustomerHierarchyEntity currentHierarchy = group.getActiveCustomerHierarchy();
       Assert.assertEquals(center1.getCustomerId(), currentHierarchy.getParentCustomer().getCustomerId());
    }

    public void testSuccessfulTransferToCenterInDifferentBranch() throws Exception {
        createObjectsForTranferToCenterInDifferentBranch();
        String newCenterSearchId = center1.getSearchId();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        group.setUserContext(center.getUserContext());
        group.transferToCenter(center1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        center = TestObjectFactory.getCenter(center.getCustomerId());
        center1 = TestObjectFactory.getCenter(center1.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());
        officeBO = new OfficePersistence().getOffice(officeBO.getOfficeId());

       Assert.assertEquals(center1.getCustomerId(), group.getParentCustomer().getCustomerId());
       Assert.assertEquals(0, center.getMaxChildCount().intValue());
       Assert.assertEquals(2, center1.getMaxChildCount().intValue());
       Assert.assertEquals(3, group.getMaxChildCount().intValue());

       Assert.assertEquals(newCenterSearchId + ".2", group.getSearchId());
       Assert.assertEquals(group.getSearchId() + ".1", client.getSearchId());
       Assert.assertEquals(group.getSearchId() + ".2", client1.getSearchId());
       Assert.assertEquals(group.getSearchId() + ".3", client2.getSearchId());

       Assert.assertEquals(CustomerStatus.GROUP_HOLD.getValue(), group.getCustomerStatus().getId());
       Assert.assertEquals(CustomerStatus.CLIENT_HOLD.getValue(), client.getCustomerStatus().getId());
       Assert.assertEquals(CustomerStatus.CLIENT_PARTIAL.getValue(), client1.getCustomerStatus().getId());
       Assert.assertEquals(CustomerStatus.CLIENT_CANCELLED.getValue(), client2.getCustomerStatus().getId());

        CustomerHierarchyEntity currentHierarchy = group.getActiveCustomerHierarchy();
       Assert.assertEquals(center1.getCustomerId(), currentHierarchy.getParentCustomer().getCustomerId());

        Assert.assertNotNull(group.getActiveCustomerMovement());
        Assert.assertNotNull(client.getActiveCustomerMovement());
        Assert.assertNotNull(client1.getActiveCustomerMovement());
        Assert.assertNotNull(client2.getActiveCustomerMovement());

        CustomerMovementEntity customerMovement = group.getActiveCustomerMovement();
       Assert.assertEquals(officeBO.getOfficeId(), customerMovement.getOffice().getOfficeId());

       Assert.assertEquals(officeBO.getOfficeId(), center1.getOffice().getOfficeId());
       Assert.assertEquals(officeBO.getOfficeId(), group.getOffice().getOfficeId());
       Assert.assertEquals(officeBO.getOfficeId(), client.getOffice().getOfficeId());
       Assert.assertEquals(officeBO.getOfficeId(), client1.getOffice().getOfficeId());
       Assert.assertEquals(officeBO.getOfficeId(), client2.getOffice().getOfficeId());
    }

    public void testSuccessfulTransferToCenterInDifferentBranch_SecondTransfer() throws Exception {
        createObjectsForTranferToCenterInDifferentBranch();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        group.setUserContext(TestObjectFactory.getContext());
        group.transferToCenter(center1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());

        group.setUserContext(TestObjectFactory.getContext());
        group.transferToCenter(center);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        center = TestObjectFactory.getCenter(center.getCustomerId());
        center1 = TestObjectFactory.getCenter(center1.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());
        client = TestObjectFactory.getClient(client.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());
        officeBO = new OfficePersistence().getOffice(officeBO.getOfficeId());

       Assert.assertEquals(center.getCustomerId(), group.getParentCustomer().getCustomerId());
       Assert.assertEquals(1, center.getMaxChildCount().intValue());
       Assert.assertEquals(1, center1.getMaxChildCount().intValue());
       Assert.assertEquals(3, group.getMaxChildCount().intValue());
    }

    public void testUpdateMeeting_SavedToUpdateLater() throws Exception {
        String oldMeetingPlace = "Delhi";
        MeetingBO weeklyMeeting = new MeetingBO(WeekDay.FRIDAY, Short.valueOf("1"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, oldMeetingPlace);
        group = TestObjectFactory.createGroupUnderBranch("group1", CustomerStatus.GROUP_ACTIVE, officeId3,
                weeklyMeeting, personnelId);

        client1 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        client2 = createClient(group, CustomerStatus.CLIENT_ACTIVE);

        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        MeetingBO groupMeeting = group.getCustomerMeeting().getMeeting();

        String meetingPlace = "Bangalore";
        MeetingBO newMeeting = new MeetingBO(WeekDay.THURSDAY, groupMeeting.getMeetingDetails().getRecurAfter(),
                groupMeeting.getStartDate(), MeetingType.CUSTOMER_MEETING, meetingPlace);

        group.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());

       Assert.assertEquals(WeekDay.FRIDAY, group.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.FRIDAY, client1.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.FRIDAY, client2.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());

       Assert.assertEquals(WeekDay.THURSDAY, group.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client1.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails()
                .getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client2.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails()
                .getWeekDay());

       Assert.assertEquals(oldMeetingPlace, group.getCustomerMeeting().getMeeting().getMeetingPlace());
       Assert.assertEquals(oldMeetingPlace, client1.getCustomerMeeting().getMeeting().getMeetingPlace());
       Assert.assertEquals(oldMeetingPlace, client2.getCustomerMeeting().getMeeting().getMeetingPlace());

       Assert.assertEquals(meetingPlace, group.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
       Assert.assertEquals(meetingPlace, client1.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
       Assert.assertEquals(meetingPlace, client2.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());

       Assert.assertEquals(YesNoFlag.YES.getValue(), group.getCustomerMeeting().getUpdatedFlag());
       Assert.assertEquals(YesNoFlag.YES.getValue(), client1.getCustomerMeeting().getUpdatedFlag());
       Assert.assertEquals(YesNoFlag.YES.getValue(), client2.getCustomerMeeting().getUpdatedFlag());

        Integer groupUpdateMeetingId = group.getCustomerMeeting().getUpdatedMeeting().getMeetingId();
       Assert.assertEquals(groupUpdateMeetingId, client1.getCustomerMeeting().getUpdatedMeeting().getMeetingId());
       Assert.assertEquals(groupUpdateMeetingId, client2.getCustomerMeeting().getUpdatedMeeting().getMeetingId());
    }

    public void testUpdateMeeting() throws Exception {
        StaticHibernateUtil.startTransaction();
        group = createGroupUnderBranch(CustomerStatus.GROUP_PENDING);
        client1 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        client2 = createClient(group, CustomerStatus.CLIENT_PENDING);

        MeetingBO groupMeeting = group.getCustomerMeeting().getMeeting();
        String oldMeetingPlace = "Delhi";
        String meetingPlace = "Bangalore";
        MeetingBO newMeeting = new MeetingBO(WeekDay.THURSDAY, groupMeeting.getMeetingDetails().getRecurAfter(),
                groupMeeting.getStartDate(), MeetingType.CUSTOMER_MEETING, meetingPlace);
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        group.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());

       Assert.assertEquals(WeekDay.MONDAY, group.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.MONDAY, client1.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.MONDAY, client2.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());

       Assert.assertEquals(WeekDay.THURSDAY, group.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client1.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails()
                .getWeekDay());
       Assert.assertEquals(WeekDay.THURSDAY, client2.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails()
                .getWeekDay());

       Assert.assertEquals(oldMeetingPlace, group.getCustomerMeeting().getMeeting().getMeetingPlace());
       Assert.assertEquals(oldMeetingPlace, client1.getCustomerMeeting().getMeeting().getMeetingPlace());
       Assert.assertEquals(oldMeetingPlace, client2.getCustomerMeeting().getMeeting().getMeetingPlace());

       Assert.assertEquals(meetingPlace, group.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
       Assert.assertEquals(meetingPlace, client1.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
       Assert.assertEquals(meetingPlace, client2.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
    }

    public void testCreateMeeting() throws Exception {
        group = createGroupUnderBranchWithoutMeeting("MyGroup");
        client1 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        client2 = createClient(group, CustomerStatus.CLIENT_PENDING);
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group.setUserContext(TestObjectFactory.getContext());
        String meetingPlace = "newPlace";
        Short recurAfter = Short.valueOf("4");
        MeetingBO newMeeting = new MeetingBO(WeekDay.FRIDAY, recurAfter, new Date(), MeetingType.CUSTOMER_MEETING,
                meetingPlace);
        group.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        group = TestObjectFactory.getGroup(group.getCustomerId());
        client1 = TestObjectFactory.getClient(client1.getCustomerId());
        client2 = TestObjectFactory.getClient(client2.getCustomerId());

       Assert.assertEquals(WeekDay.FRIDAY, group.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.FRIDAY, client1.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(WeekDay.FRIDAY, client2.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());

       Assert.assertEquals(meetingPlace, group.getCustomerMeeting().getMeeting().getMeetingPlace());
       Assert.assertEquals(meetingPlace, client1.getCustomerMeeting().getMeeting().getMeetingPlace());
       Assert.assertEquals(meetingPlace, client2.getCustomerMeeting().getMeeting().getMeetingPlace());

       Assert.assertEquals(recurAfter, group.getCustomerMeeting().getMeeting().getMeetingDetails().getRecurAfter());
       Assert.assertEquals(recurAfter, client1.getCustomerMeeting().getMeeting().getMeetingDetails().getRecurAfter());
       Assert.assertEquals(recurAfter, client2.getCustomerMeeting().getMeeting().getMeetingDetails().getRecurAfter());
    }

    public void testFailureCreate_Group_UnderCenter() throws Exception {
        createCenter();
        String name = "GroupTest";
        Date trainedDate = getDate("11/12/2005");
        String externalId = "1234";
        StaticHibernateUtil.closeSession();

        try {
            group = new GroupBO(TestUtils.makeUser(), name, CustomerStatus.GROUP_ACTIVE, externalId, true, trainedDate,
                    getAddress(), null, null, personnelBo, center, new GroupPersistence(), new OfficePersistence());
            TestObjectFactory.simulateInvalidConnection();
            new GroupPersistence().saveGroup(group);
            Assert.fail();
        } catch (CustomerException ce) {
           Assert.assertEquals("Customer.CreateFailed", ce.getKey());
        } finally {
            group = null;
            StaticHibernateUtil.closeSession();
        }
    }

    private GroupBO createGroupUnderBranchWithoutMeeting(String name) {
        return TestObjectFactory.createGroupUnderBranch(name, CustomerStatus.GROUP_PENDING, officeId1, null,
                personnelId);
    }

    private void createCenter() {
        meeting = getMeeting();
        center = TestObjectFactory.createCenter("Center", meeting);
    }

    private CenterBO createCenter(String name) throws Exception {
        return createCenter(name, officeId3, WeekDay.MONDAY);
    }

    private CenterBO createCenter(String name, Short officeId, WeekDay weekDay) throws Exception {
        meeting = new MeetingBO(weekDay, Short.valueOf("1"), new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        return TestObjectFactory.createCenter(name, meeting, officeId, personnelId);
    }

    private void createGroup(String name) {
        group = TestObjectFactory.createGroupUnderCenter(name, CustomerStatus.GROUP_ACTIVE, center);
    }

    private GroupBO createGroupUnderBranch(String name, CustomerStatus customerStatus) {
        meeting = getMeeting();
        return TestObjectFactory.createGroupUnderBranch(name, customerStatus, officeId1, meeting, personnelId);
    }

    private GroupBO createGroupUnderBranch(String name, CustomerStatus customerStatus,
            List<CustomFieldView> customFieldView) {
        meeting = getMeeting();
        return TestObjectFactory.createGroupUnderBranch(name, customerStatus, officeId1, meeting, personnelId,
                customFieldView);
    }

    private GroupBO createGroupUnderBranch(CustomerStatus groupStatus) throws Exception {
        return createGroupUnderBranch(groupStatus, officeId3);
    }

    private GroupBO createGroupUnderBranch(CustomerStatus groupStatus, Short officeId) throws Exception {
        meeting = new MeetingBO(WeekDay.MONDAY, Short.valueOf("1"), new Date(), MeetingType.CUSTOMER_MEETING, "Delhi");
        return TestObjectFactory.createGroupUnderBranchWithMakeUser("group1", groupStatus, officeId, meeting,
                personnelId);
    }

    private MeetingBO getMeeting() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        return meeting;
    }

    private void removeFees(List<FeeView> feesToRemove) {
        for (FeeView fee : feesToRemove) {
            TestObjectFactory.cleanUp(new FeePersistence().getFee(fee.getFeeIdValue()));
        }
    }

    private List<CustomFieldView> getCustomFields() {
        List<CustomFieldView> fields = new ArrayList<CustomFieldView>();
        fields.add(new CustomFieldView(Short.valueOf("4"), "value1", CustomFieldType.ALPHA_NUMERIC));
        fields.add(new CustomFieldView(Short.valueOf("3"), "value2", CustomFieldType.NUMERIC));
        return fields;
    }

    private List<CustomFieldView> getNewCustomFields() {
        List<CustomFieldView> fields = new ArrayList<CustomFieldView>();
        fields.add(new CustomFieldView(Short.valueOf("4"), "value3", CustomFieldType.ALPHA_NUMERIC));
        fields.add(new CustomFieldView(Short.valueOf("3"), "value4", CustomFieldType.NUMERIC));
        return fields;
    }

    private Address getAddress() {
        Address address = new Address();
        address.setLine1("Aditi");
        address.setCity("Bangalore");
        return address;
    }

    private List<FeeView> getFees() {
        List<FeeView> fees = new ArrayList<FeeView>();
        AmountFeeBO fee1 = (AmountFeeBO) TestObjectFactory.createPeriodicAmountFee("PeriodicAmountFee",
                FeeCategory.GROUP, "200", RecurrenceType.WEEKLY, Short.valueOf("2"));
        AmountFeeBO fee2 = (AmountFeeBO) TestObjectFactory.createOneTimeAmountFee("OneTimeAmountFee",
                FeeCategory.ALLCUSTOMERS, "100", FeePayment.UPFRONT);
        fees.add(new FeeView(TestObjectFactory.getContext(), fee1));
        fees.add(new FeeView(TestObjectFactory.getContext(), fee2));
        StaticHibernateUtil.commitTransaction();
        return fees;
    }

    private void createObjectsForTranferToCenterInSameBranch() throws Exception {
        createInitialObjects();
        client1 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        client2 = createClient(group, CustomerStatus.CLIENT_CANCELLED);
        center1 = createCenter("toTransfer", officeId3, WeekDay.THURSDAY);
        group1 = createGroup("newGroup", center1);
    }

    private void createObjectsForTranferToCenterInDifferentBranch() throws Exception {
        createInitialObjects();
        client1 = createClient(group, CustomerStatus.CLIENT_PARTIAL);
        client2 = createClient(group, CustomerStatus.CLIENT_CANCELLED);
        officeBO = createOffice();
        center1 = createCenter("toTransfer", officeBO.getOfficeId(), WeekDay.FRIDAY);
        group1 = createGroup("newGroup", center1);
    }

    private ClientBO createClient(GroupBO group, CustomerStatus clientStatus) {
        return TestObjectFactory.createClient("client1", clientStatus, group, new Date());
    }

    private GroupBO createGroup(String name, CenterBO center) {
        return TestObjectFactory.createGroupUnderCenter(name, CustomerStatus.GROUP_ACTIVE, center);
    }

    private OfficeBO createOffice() throws Exception {
        return TestObjectFactory.createOffice(OfficeLevel.BRANCHOFFICE, TestObjectFactory
                .getOffice(TestObjectFactory.HEAD_OFFICE), "customer_office", "cust");
    }

    private void createInitialObjects() throws Exception {
        center = createCenter("Center");
        group = createGroup("Group", center);
        client = createClient(group, CustomerStatus.CLIENT_ACTIVE);
    }

    private SavingsBO getSavingsAccount(CustomerBO customerBO, String offeringName, String shortName) throws Exception {
        savingsOffering = helper.createSavingsOffering(offeringName, shortName);
        return TestObjectFactory.createSavingsAccount("000100000000017", customerBO,
                AccountStates.SAVINGS_ACC_APPROVED, new Date(System.currentTimeMillis()), savingsOffering);
    }

    private void createInitialObject() {
        Date startDate = new Date(System.currentTimeMillis());

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createCenter("Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loandsdasd", "fsad", startDate, meeting);
        account1 = TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);
        loanOffering = TestObjectFactory.createLoanOffering("Loandfas", "dsvd", ApplicableTo.CLIENTS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, meeting);
        account2 = TestObjectFactory.createLoanAccount("42427777341", client,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, startDate, loanOffering);
    }

    private void changeFirstInstallmentDate(AccountBO accountBO, int numberOfDays) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - numberOfDays);
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanBOTestUtils.setActionDate(accountActionDateEntity, new java.sql.Date(currentDateCalendar
                    .getTimeInMillis()));
            break;
        }
    }

    private CenterBO createCenter(String name, MeetingBO meeting) {
        return TestObjectFactory.createCenter(name, meeting, officeId3, personnelId);
    }

    private MeetingBO createMonthlyMeetingOnDate(Short dayNumber, Short recurAfer, Date startDate)
            throws MeetingException {
        return new MeetingBO(dayNumber, recurAfer, startDate, MeetingType.CUSTOMER_MEETING, "MeetingPlace");
    }

    private MeetingBO createMonthlyMeetingOnWeekDay(WeekDay weekDay, RankType rank, Short recurAfer, Date startDate)
            throws MeetingException {
        return new MeetingBO(weekDay, rank, recurAfer, startDate, MeetingType.CUSTOMER_MEETING, "MeetingPlace");
    }
}
