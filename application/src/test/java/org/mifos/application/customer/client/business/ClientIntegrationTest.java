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

package org.mifos.application.customer.client.business;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;

import org.mifos.application.accounts.business.AccountActionDateEntity;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.accounts.savings.util.helpers.SavingsTestHelper;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.accounts.util.helpers.AccountStates;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.CustomerHierarchyEntity;
import org.mifos.application.customer.business.CustomerMovementEntity;
import org.mifos.application.customer.business.CustomerPositionEntity;
import org.mifos.application.customer.business.PositionEntity;
import org.mifos.application.customer.center.business.CenterBO;
import org.mifos.application.customer.client.persistence.ClientPersistence;
import org.mifos.application.customer.client.util.helpers.ClientConstants;
import org.mifos.application.customer.exceptions.CustomerException;
import org.mifos.application.customer.group.business.GroupBO;
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
import org.mifos.application.meeting.util.helpers.MeetingType;
import org.mifos.application.meeting.util.helpers.RankType;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.office.persistence.OfficePersistence;
import org.mifos.application.office.util.helpers.OfficeLevel;
import org.mifos.application.office.util.helpers.OfficeStatus;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.application.productdefinition.business.SavingsOfferingBO;
import org.mifos.application.productdefinition.util.helpers.ApplicableTo;
import org.mifos.application.productdefinition.util.helpers.SavingsType;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.TestDatabase;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class ClientIntegrationTest extends MifosIntegrationTestCase {
    public ClientIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    private AccountBO accountBO;
    private CustomerBO center;
    private CenterBO center1;
    private CustomerBO group;
    private GroupBO group1;
    private SavingsOfferingBO savingsOffering1;
    private SavingsOfferingBO savingsOffering2;
    private ClientBO client;

    private MeetingBO meeting;

    PersonnelBO personnel;
    private Short officeId = 1;

    private OfficeBO office;
    private OfficeBO officeBo;
    private CustomerPersistence customerPersistence = new CustomerPersistence();
    private MasterPersistence masterPersistence = new MasterPersistence();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        personnel = getTestUser();
        officeBo = getHeadOffice();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            TestObjectFactory.cleanUp(accountBO);
            TestObjectFactory.cleanUp(client);
            TestObjectFactory.cleanUp(group);
            TestObjectFactory.cleanUp(group1);
            TestObjectFactory.cleanUp(center);
            TestObjectFactory.cleanUp(center1);
            TestObjectFactory.cleanUp(office);
            TestObjectFactory.removeObject(savingsOffering1);
            TestObjectFactory.removeObject(savingsOffering2);
        } catch (Exception e) {
            // TODO Whoops, cleanup didnt work, reset db
            TestDatabase.resetMySQLDatabase();
        }
        StaticHibernateUtil.closeSession();
        super.tearDown();
    }

    public void testPovertyLikelihoodHibernateMapping() throws Exception {
        createInitialObjects();
        Double pct = new Double(55.0);
        String hd = "**FOO**";
        client.setPovertyLikelihoodPercent(pct);
        client.getCustomerDetail().setHandicappedDetails(hd);
        new ClientPersistence().createOrUpdate(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        Integer clientId = client.getCustomerId();
        ClientBO retrievedClient = (ClientBO) StaticHibernateUtil.getSessionTL().get(ClientBO.class,
                client.getCustomerId());
       Assert.assertEquals(hd, retrievedClient.getCustomerDetail().getHandicappedDetails());
       Assert.assertEquals(pct, retrievedClient.getPovertyLikelihoodPercent());

    }

    public void testRemoveClientFromGroup() throws Exception {
        createInitialObjects();
        client.updateClientFlag();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());

    }

    public void testSuccessfulAddClientToGroupWithMeeting() throws Exception {
        createObjectsForTransferToGroup_WithMeeting();
       Assert.assertEquals(0, group1.getMaxChildCount().intValue());

        client.addClientToGroup(group1);

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        Assert.assertNotNull(client.getParentCustomer());
       Assert.assertEquals(group1.getCustomerId(), client.getParentCustomer().getCustomerId());

        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());

       Assert.assertEquals(1, group1.getMaxChildCount().intValue());
       Assert.assertEquals(group1.getCustomerId(), client.getParentCustomer().getCustomerId());
       Assert.assertEquals(true, client.isClientUnderGroup());
    }

    public void testSuccessfulValidateBeforeAddingClientToGroup_Client() throws Exception {
        String oldMeetingPlace = "Tunisia";
        MeetingBO weeklyMeeting = new MeetingBO(WeekDay.FRIDAY, Short.valueOf("1"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, oldMeetingPlace);
        client = TestObjectFactory.createClient("clientname", weeklyMeeting, CustomerStatus.CLIENT_CANCELLED);
        try {
            client.validateBeforeAddingClientToGroup();
        } catch (CustomerException expected) {
           Assert.assertEquals(CustomerConstants.CLIENT_IS_CLOSED_OR_CANCELLED_EXCEPTION, expected.getKey());
           Assert.assertTrue(true);
        }

    }

    public void testSuccessfulValidateBeforeAddingClientToGroup_Amount() throws Exception {
        String oldMeetingPlace = "Tunis";
        MeetingBO weeklyMeeting = new MeetingBO(WeekDay.FRIDAY, Short.valueOf("1"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, oldMeetingPlace);
        client = TestObjectFactory.createClient("clientname", weeklyMeeting, CustomerStatus.CLIENT_CANCELLED);

        try {
            client.validateBeforeAddingClientToGroup();
            Assert.fail();
        } catch (CustomerException expected) {
            assertNotSame(CustomerConstants.CLIENT_HAVE_OPEN_LOAN_ACCOUNT_EXCEPTION, expected.getKey());
           Assert.assertTrue(true);
        }

    }

    public void testUpdateWeeklyMeeting_SavedToUpdateLater() throws Exception {
        String oldMeetingPlace = "Delhi";
        MeetingBO weeklyMeeting = new MeetingBO(WeekDay.FRIDAY, Short.valueOf("1"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, oldMeetingPlace);
        client = TestObjectFactory.createClient("clientname", weeklyMeeting, CustomerStatus.CLIENT_ACTIVE);
        MeetingBO clientMeeting = client.getCustomerMeeting().getMeeting();
        String meetingPlace = "Bangalore";
        MeetingBO newMeeting = new MeetingBO(WeekDay.THURSDAY, clientMeeting.getMeetingDetails().getRecurAfter(),
                clientMeeting.getStartDate(), MeetingType.CUSTOMER_MEETING, meetingPlace);
        client.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());

       Assert.assertEquals(WeekDay.FRIDAY, client.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(oldMeetingPlace, client.getCustomerMeeting().getMeeting().getMeetingPlace());

       Assert.assertEquals(YesNoFlag.YES.getValue(), client.getCustomerMeeting().getUpdatedFlag());
       Assert.assertEquals(WeekDay.THURSDAY, client.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(meetingPlace, client.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
    }

    public void testGenerateScheduleForClient_CenterSavingsAccount_OnChangeStatus() throws Exception {
        SavingsOfferingBO savingsOffering = TestObjectFactory.createSavingsProduct("Offering1", "s1",
                SavingsType.MANDATORY, ApplicableTo.CENTERS, new Date(System.currentTimeMillis()));
        createParentObjects(CustomerStatus.GROUP_ACTIVE);
        accountBO = TestObjectFactory.createSavingsAccount("globalNum", center, AccountState.SAVINGS_ACTIVE,
                new java.util.Date(), savingsOffering, TestObjectFactory.getContext());
        client = createClient(CustomerStatus.CLIENT_PENDING);
        StaticHibernateUtil.closeSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(0, accountBO.getAccountActionDates().size());
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        client.setUserContext(TestObjectFactory.getContext());
        client.changeStatus(CustomerStatus.CLIENT_ACTIVE, null, "clientActive");
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        Assert.assertNotNull(accountBO.getAccountActionDates());

       Assert.assertEquals(10, accountBO.getAccountActionDates().size());
       Assert.assertEquals(1, accountBO.getAccountCustomFields().size());
        for (AccountActionDateEntity actionDate : accountBO.getAccountActionDates()) {
           Assert.assertEquals(client.getCustomerId(), actionDate.getCustomer().getCustomerId());
           Assert.assertTrue(true);
        }

        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    public void testGenerateScheduleForClient_GroupSavingsAccount_OnChangeStatus() throws Exception {
        SavingsOfferingBO savingsOffering = TestObjectFactory.createSavingsProduct("Offering1", "s1",
                SavingsType.MANDATORY, ApplicableTo.GROUPS, new Date(System.currentTimeMillis()));
        createParentObjects(CustomerStatus.GROUP_ACTIVE);
        accountBO = TestObjectFactory.createSavingsAccount("globalNum", group, AccountState.SAVINGS_ACTIVE,
                new java.util.Date(), savingsOffering, TestObjectFactory.getContext());
        client = createClient(CustomerStatus.CLIENT_PENDING);
        StaticHibernateUtil.closeSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(0, accountBO.getAccountActionDates().size());
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        client.setUserContext(TestObjectFactory.getContext());
        client.changeStatus(CustomerStatus.CLIENT_ACTIVE, null, "clientActive");
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        Assert.assertNotNull(accountBO.getAccountActionDates());
       Assert.assertEquals(1, accountBO.getAccountCustomFields().size());
       Assert.assertEquals(10, accountBO.getAccountActionDates().size());
        for (AccountActionDateEntity actionDate : accountBO.getAccountActionDates()) {
           Assert.assertEquals(client.getCustomerId(), actionDate.getCustomer().getCustomerId());
           Assert.assertTrue(true);
        }

        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    public void testGenerateScheduleForClient_OnClientCreate() throws Exception {
        SavingsOfferingBO savingsOffering = TestObjectFactory.createSavingsProduct("Offering1", "s1",
                SavingsType.MANDATORY, ApplicableTo.GROUPS, new Date(System.currentTimeMillis()));
        createParentObjects(CustomerStatus.GROUP_ACTIVE);
        accountBO = TestObjectFactory.createSavingsAccount("globalNum", center, AccountState.SAVINGS_ACTIVE,
                new java.util.Date(), savingsOffering, TestObjectFactory.getContext());
       Assert.assertEquals(0, accountBO.getAccountActionDates().size());
        client = createClient(CustomerStatus.CLIENT_ACTIVE);
        StaticHibernateUtil.closeSession();

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(1, accountBO.getAccountCustomFields().size());
       Assert.assertEquals(10, accountBO.getAccountActionDates().size());
        for (AccountActionDateEntity actionDate : accountBO.getAccountActionDates()) {
           Assert.assertEquals(client.getCustomerId(), actionDate.getCustomer().getCustomerId());
           Assert.assertTrue(true);
        }

        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());
    }

    public void testFailure_InitialSavingsOfferingAtCreate() throws Exception {
        savingsOffering1 = TestObjectFactory.createSavingsProduct("Offering1", "s1", SavingsType.MANDATORY,
                ApplicableTo.CLIENTS, new Date(System.currentTimeMillis()));
        ClientNameDetailView clientView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
        ClientNameDetailView spouseView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), Short.valueOf("41"));
        List<SavingsOfferingBO> offerings = new ArrayList<SavingsOfferingBO>();
        offerings.add(savingsOffering1);
        offerings.add(savingsOffering1);
        try {
            client = new ClientBO(TestObjectFactory.getContext(), clientView.getDisplayName(),
                    CustomerStatus.CLIENT_PARTIAL, null, null, null, null, null, offerings, personnel, officeBo, null,
                    null, null, null, null, YesNoFlag.NO.getValue(), clientView, spouseView, clientDetailView, null);
        } catch (CustomerException ce) {
           Assert.assertEquals(ClientConstants.ERRORS_DUPLICATE_OFFERING_SELECTED, ce.getKey());
           Assert.assertTrue(true);
        }
        savingsOffering1 = (SavingsOfferingBO) TestObjectFactory.getObject(SavingsOfferingBO.class, savingsOffering1
                .getPrdOfferingId());
    }

    public void testInitialSavingsOfferingAtCreate() throws Exception {
        savingsOffering1 = TestObjectFactory.createSavingsProduct("Offering1", "s1", SavingsType.MANDATORY,
                ApplicableTo.CLIENTS, new Date(System.currentTimeMillis()));
        savingsOffering2 = TestObjectFactory.createSavingsProduct("Offering2", "s2", SavingsType.VOLUNTARY,
                ApplicableTo.CLIENTS, new Date(System.currentTimeMillis()));
        ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
        ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), Short.valueOf("41"));
        List<SavingsOfferingBO> offerings = new ArrayList<SavingsOfferingBO>();
        offerings.add(savingsOffering1);
        offerings.add(savingsOffering2);
        client = new ClientBO(TestObjectFactory.getContext(), clientNameDetailView.getDisplayName(),
                CustomerStatus.CLIENT_PARTIAL, null, null, null, null, null, offerings, personnel, officeBo, null,
                null, null, null, null, YesNoFlag.YES.getValue(), clientNameDetailView, spouseNameDetailView,
                clientDetailView, null);
        new ClientPersistence().saveClient(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = new ClientPersistence().getClient(client.getCustomerId());
       Assert.assertEquals(offerings.size(), client.getOfferingsAssociatedInCreate().size());
        for (ClientInitialSavingsOfferingEntity clientOffering : client.getOfferingsAssociatedInCreate()) {
            if (clientOffering.getSavingsOffering().getPrdOfferingId().equals(savingsOffering1.getPrdOfferingId()))
               Assert.assertTrue(true);
            if (clientOffering.getSavingsOffering().getPrdOfferingId().equals(savingsOffering2.getPrdOfferingId()))
               Assert.assertTrue(true);
        }
        savingsOffering1 = (SavingsOfferingBO) TestObjectFactory.getObject(SavingsOfferingBO.class, savingsOffering1
                .getPrdOfferingId());
        savingsOffering2 = (SavingsOfferingBO) TestObjectFactory.getObject(SavingsOfferingBO.class, savingsOffering2
                .getPrdOfferingId());
    }

    public void testCreateClientWithoutName() throws Exception {
        try {
            ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                    TestObjectFactory.SAMPLE_SALUTATION, "", "", "", "");
            ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
            ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                    .valueOf("1"), Short.valueOf("41"));
            client = new ClientBO(TestUtils.makeUser(), "", CustomerStatus.fromInt(new Short("1")), null, null, null,
                    null, null, null, personnel, officeBo, null, null, null, null, null, YesNoFlag.YES.getValue(),
                    clientNameDetailView, spouseNameDetailView, clientDetailView, null);
            Assert.fail("Client Created");
        } catch (CustomerException ce) {
            Assert.assertNull(client);
           Assert.assertEquals(CustomerConstants.INVALID_NAME, ce.getKey());
        }
    }

    public void testCreateClientWithoutOffice() throws Exception {
        try {
            ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "", "last", "");
            ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
            ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                    .valueOf("1"), Short.valueOf("41"));
            client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus
                    .fromInt(new Short("1")), null, null, null, null, null, null, personnel, null, null, null, null,
                    null, null, YesNoFlag.YES.getValue(), clientNameDetailView, spouseNameDetailView, clientDetailView,
                    null);
            Assert.fail("Client Created");
        } catch (CustomerException ce) {
            Assert.assertNull(client);
           Assert.assertEquals(ce.getKey(), CustomerConstants.INVALID_OFFICE);
        }
    }

    public void testSuccessfulCreateWithoutFeeAndCustomField() throws Exception {
        String name = "Client 1";
        Short povertyStatus = Short.valueOf("41");
        ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
        ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), povertyStatus);
        client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus
                .fromInt(new Short("1")), null, null, null, null, null, null, personnel, officeBo, null, null, null,
                null, null, YesNoFlag.YES.getValue(), clientNameDetailView, spouseNameDetailView, clientDetailView,
                null);
        new ClientPersistence().saveClient(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
       Assert.assertEquals(name, client.getDisplayName());
       Assert.assertEquals(povertyStatus, client.getCustomerDetail().getPovertyStatus());
       Assert.assertEquals(officeId, client.getOffice().getOfficeId());
    }

    public void testSuccessfulCreateInActiveState_WithAssociatedSavingsOffering() throws Exception {
        savingsOffering1 = TestObjectFactory.createSavingsProduct("offering1", "s1", SavingsType.MANDATORY,
                ApplicableTo.CLIENTS, new Date(System.currentTimeMillis()));
        StaticHibernateUtil.closeSession();
        List<SavingsOfferingBO> selectedOfferings = new ArrayList<SavingsOfferingBO>();
        selectedOfferings.add(savingsOffering1);

        String name = "Client 1";
        Short povertyStatus = Short.valueOf("41");
        ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
        ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), povertyStatus);
        client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(),
                CustomerStatus.CLIENT_ACTIVE, null, null, null, null, null, selectedOfferings, personnel,
                new OfficePersistence().getOffice(TestObjectFactory.SAMPLE_BRANCH_OFFICE), getMeeting(), personnel,
                null, null, null, null, YesNoFlag.NO.getValue(), clientNameDetailView, spouseNameDetailView,
                clientDetailView, null);
        new ClientPersistence().saveClient(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
       Assert.assertEquals(name, client.getDisplayName());
       Assert.assertEquals(1, client.getOfferingsAssociatedInCreate().size());
       Assert.assertEquals(2, client.getAccounts().size());
        for (AccountBO account : client.getAccounts()) {
            if (account instanceof SavingsBO) {
               Assert.assertEquals(savingsOffering1.getPrdOfferingId(), ((SavingsBO) account).getSavingsOffering()
                        .getPrdOfferingId());
                Assert.assertNotNull(account.getGlobalAccountNum());
               Assert.assertTrue(true);
            }
        }
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        savingsOffering1 = null;
    }

    public void testSavingsAccountOnChangeStatusToActive() throws Exception {
        savingsOffering1 = TestObjectFactory.createSavingsProduct("offering1", "s1", SavingsType.MANDATORY,
                ApplicableTo.CLIENTS, new Date(System.currentTimeMillis()));
        savingsOffering2 = TestObjectFactory.createSavingsProduct("offering2", "s2", SavingsType.VOLUNTARY,
                ApplicableTo.CLIENTS, new Date(System.currentTimeMillis()));
        StaticHibernateUtil.closeSession();
        List<SavingsOfferingBO> selectedOfferings = new ArrayList<SavingsOfferingBO>();
        selectedOfferings.add(savingsOffering1);
        selectedOfferings.add(savingsOffering2);

        Short povertyStatus = Short.valueOf("41");
        ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
        ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), povertyStatus);
        client = new ClientBO(TestObjectFactory.getContext(), clientNameDetailView.getDisplayName(),
                CustomerStatus.CLIENT_PENDING, null, null, null, null, null, selectedOfferings, personnel,
                getBranchOffice(), getMeeting(), personnel, null, null, null, null, YesNoFlag.YES.getValue(),
                clientNameDetailView, spouseNameDetailView, clientDetailView, null);
        new ClientPersistence().saveClient(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());
       Assert.assertEquals(2, client.getOfferingsAssociatedInCreate().size());
       Assert.assertEquals(1, client.getAccounts().size());

        client.setUserContext(TestObjectFactory.getContext());
        client.changeStatus(CustomerStatus.CLIENT_ACTIVE, null, "Client Made Active");
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());
       Assert.assertEquals(3, client.getAccounts().size());

        for (AccountBO account : client.getAccounts()) {
            if (account instanceof SavingsBO) {
                Assert.assertNotNull(account.getGlobalAccountNum());
               Assert.assertTrue(true);
            }
        }
        savingsOffering1 = null;
        savingsOffering2 = null;
    }

    public void testSuccessfulCreateWithParentGroup() throws Exception {
        String name = "Client 1";
        ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
        ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), Short.valueOf("41"));
        createParentObjects(CustomerStatus.GROUP_PARTIAL);
        client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus
                .fromInt(new Short("1")), null, null, null, null, null, null, personnel, group.getOffice(), group,
                null, null, null, null, YesNoFlag.YES.getValue(), clientNameDetailView, spouseNameDetailView,
                clientDetailView, null);
        new ClientPersistence().saveClient(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
       Assert.assertEquals(name, client.getDisplayName());
       Assert.assertEquals(client.getOffice().getOfficeId(), group.getOffice().getOfficeId());
    }

    public void testFailureCreatePendingClientWithParentGroupInLowerStatus() throws Exception {
        try {
            ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                    TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
            ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
            ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                    .valueOf("1"), Short.valueOf("41"));
            createParentObjects(CustomerStatus.GROUP_PARTIAL);
            client = new ClientBO(TestUtils.makeUserWithLocales(), clientNameDetailView.getDisplayName(),
                    CustomerStatus.CLIENT_PENDING, null, null, null, null, null, null, personnel, group.getOffice(),
                    group, null, null, null, null, YesNoFlag.YES.getValue(), clientNameDetailView,
                    spouseNameDetailView, clientDetailView, null);
            Assert.fail();
        } catch (CustomerException e) {
            Assert.assertNull(client);
           Assert.assertEquals(ClientConstants.INVALID_CLIENT_STATUS_EXCEPTION, e.getKey());
        }
    }

    public void testFailureCreateActiveClientWithParentGroupInLowerStatus() throws Exception {
        try {
            ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                    TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
            ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
            ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                    .valueOf("1"), Short.valueOf("41"));
            createParentObjects(CustomerStatus.GROUP_PARTIAL);
            client = new ClientBO(TestUtils.makeUserWithLocales(), clientNameDetailView.getDisplayName(),
                    CustomerStatus.CLIENT_ACTIVE, null, null, null, null, null, null, personnel, group.getOffice(),
                    group, null, null, null, null, YesNoFlag.YES.getValue(), clientNameDetailView,
                    spouseNameDetailView, clientDetailView, null);
            Assert.fail();
        } catch (CustomerException e) {
            Assert.assertNull(client);
           Assert.assertEquals(ClientConstants.INVALID_CLIENT_STATUS_EXCEPTION, e.getKey());
        }
    }

    public void testFailureCreateActiveClientWithoutLO() throws Exception {
        List<FeeView> fees = getFees();
        try {
            meeting = getMeeting();
            ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "", "last", "");
            ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
            ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                    .valueOf("1"), Short.valueOf("41"));
            client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus
                    .fromInt(new Short("3")), null, null, null, null, fees, null, personnel, officeBo, meeting, null,
                    null, null, null, null, YesNoFlag.NO.getValue(), clientNameDetailView, spouseNameDetailView,
                    clientDetailView, null);
            Assert.fail();
        } catch (CustomerException e) {
            Assert.assertNull(client);
           Assert.assertEquals(CustomerConstants.INVALID_LOAN_OFFICER, e.getKey());
        }
        removeFees(fees);
    }

    public void testFailureCreateActiveClientWithoutMeeting() throws Exception {
        try {
            ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "", "last", "");
            ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                    TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
            ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                    .valueOf("1"), Short.valueOf("41"));
            client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus
                    .fromInt(new Short("3")), null, null, null, null, null, null, personnel, officeBo, null, personnel,
                    null, null, null, null, YesNoFlag.NO.getValue(), clientNameDetailView, spouseNameDetailView,
                    clientDetailView, null);
            Assert.fail();
        } catch (CustomerException ce) {
            Assert.assertNull(client);
           Assert.assertEquals(CustomerConstants.INVALID_MEETING, ce.getKey());
        }

    }

    public void testFailureCreateClientWithDuplicateNameAndDOB() throws Exception {
        ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
        ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), Short.valueOf("41"));
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus
                .fromInt(new Short("1")), null, null, null, null, null, null, personnel, officeBo, null, personnel,
                date, null, null, null, YesNoFlag.YES.getValue(), clientNameDetailView, spouseNameDetailView,
                clientDetailView, null);
        new ClientPersistence().saveClient(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        try {
            new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus.fromInt(new Short(
                    "1")), null, null, null, null, null, null, personnel, officeBo, null, personnel, date, null, null,
                    null, YesNoFlag.NO.getValue(), clientNameDetailView, spouseNameDetailView, clientDetailView, null);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.CUSTOMER_DUPLICATE_CUSTOMERNAME_EXCEPTION, e.getKey());
        }

    }

    public void testFailureCreateClientWithDuplicateGovtId() throws Exception {
        ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, "Client", "", "1", "");
        ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), Short.valueOf("41"));
        client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus
                .fromInt(new Short("1")), null, null, null, null, null, null, personnel, officeBo, null, personnel,
                new java.util.Date(), "1", null, null, YesNoFlag.YES.getValue(), clientNameDetailView,
                spouseNameDetailView, clientDetailView, null);
        new ClientPersistence().saveClient(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getObject(ClientBO.class, client.getCustomerId());
        try {
            client = new ClientBO(TestUtils.makeUserWithLocales(), clientNameDetailView.getDisplayName(),
                    CustomerStatus.CLIENT_PARTIAL, null, null, null, null, null, null, personnel, officeBo, null,
                    personnel, new java.util.Date(), "1", null, null, YesNoFlag.NO.getValue(), clientNameDetailView,
                    spouseNameDetailView, clientDetailView, null);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.DUPLICATE_GOVT_ID_EXCEPTION, e.getKey());
        }

    }

    public void testSuccessfulCreateClientInBranch() throws Exception {
        OfficeBO office = new OfficePersistence().getOffice(TestObjectFactory.HEAD_OFFICE);
        String firstName = "Client";
        String lastName = "Last";
        String displayName = "Client Last";
        ClientNameDetailView clientNameDetailView = new ClientNameDetailView(NameType.CLIENT,
                TestObjectFactory.SAMPLE_SALUTATION, firstName, "", lastName, "");
        ClientNameDetailView spouseNameDetailView = new ClientNameDetailView(NameType.SPOUSE,
                TestObjectFactory.SAMPLE_SALUTATION, "first", "middle", "last", "secondLast");
        ClientDetailView clientDetailView = new ClientDetailView(1, 1, 1, 1, 1, 1, Short.valueOf("1"), Short
                .valueOf("1"), Short.valueOf("41"));
        client = new ClientBO(TestUtils.makeUser(), clientNameDetailView.getDisplayName(), CustomerStatus
                .fromInt(new Short("1")), null, null, null, getCustomFields(), null, null, personnel, office, meeting,
                personnel, null, null, null, null, YesNoFlag.YES.getValue(), clientNameDetailView,
                spouseNameDetailView, clientDetailView, null);
        new ClientPersistence().saveClient(client);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
       Assert.assertEquals(displayName, client.getDisplayName());
       Assert.assertEquals(firstName, client.getFirstName());
       Assert.assertEquals(lastName, client.getLastName());
       Assert.assertEquals(officeId, client.getOffice().getOfficeId());
    }

    public void testUpdateGroupFailure_GroupNULL() throws Exception {
        createInitialObjects();
        try {
            client.transferToGroup(null);
            Assert.fail();
        } catch (CustomerException ce) {
           Assert.assertEquals(CustomerConstants.INVALID_PARENT, ce.getKey());
        }
    }

    public void testUpdateGroupFailure_TransferInSameGroup() throws Exception {
        createInitialObjects();
        try {
            client.transferToGroup((GroupBO) client.getParentCustomer());
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.ERRORS_SAME_PARENT_TRANSFER, e.getKey());
        }
    }

    public void testUpdateGroupFailure_GroupCancelled() throws Exception {
        createObjectsForTransferToGroup_SameBranch(CustomerStatus.GROUP_ACTIVE);
        refetchGroup();
        group1.changeStatus(CustomerStatus.GROUP_CANCELLED, CustomerStatusFlag.GROUP_CANCEL_WITHDRAW, "Status Changed");
        StaticHibernateUtil.commitTransaction();
        try {
            client.transferToGroup(group1);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.ERRORS_INTRANSFER_PARENT_INACTIVE, e.getKey());
        }
    }

    private void refetchGroup() throws PersistenceException {
        group1 = (GroupBO) customerPersistence.getCustomer(group1.getCustomerId());
        group1.setUserContext(TestUtils.makeUserWithLocales());
    }

    public void testUpdateGroupFailure_GroupClosed() throws Exception {
        createObjectsForTransferToGroup_SameBranch(CustomerStatus.GROUP_ACTIVE);
        refetchGroup();
        group1.changeStatus(CustomerStatus.GROUP_CLOSED, CustomerStatusFlag.GROUP_CLOSED_TRANSFERRED, "Status Changed");
        StaticHibernateUtil.commitTransaction();
        try {
            client.transferToGroup(group1);
            Assert.fail();
        } catch (CustomerException expected) {
           Assert.assertEquals(CustomerConstants.ERRORS_INTRANSFER_PARENT_INACTIVE, expected.getKey());
        }
    }

    public void testUpdateGroupFailure_GroupStatusLower() throws Exception {
        createObjectsForTransferToGroup_SameBranch(CustomerStatus.GROUP_PARTIAL);
        try {
            client.transferToGroup(group1);
            Assert.fail();
        } catch (CustomerException expected) {
           Assert.assertEquals(ClientConstants.ERRORS_LOWER_GROUP_STATUS, expected.getKey());
        }
    }

    public void testUpdateGroupFailure_GroupStatusLower_Client_OnHold() throws Exception {
        createObjectsForTransferToGroup_SameBranch(CustomerStatus.GROUP_PARTIAL);
        client = (ClientBO) customerPersistence.getCustomer(client.getCustomerId());
        client.setUserContext(TestUtils.makeUserWithLocales());
        client.changeStatus(CustomerStatus.CLIENT_HOLD, null, "client on hold");
        try {
            client.transferToGroup(group1);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(ClientConstants.ERRORS_LOWER_GROUP_STATUS, e.getKey());
        }
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
    }

    public void testUpdateGroupFailure_ClientHasActiveAccounts() throws Exception {
        createObjectsForTransferToGroup_SameBranch(CustomerStatus.GROUP_ACTIVE);
        accountBO = createSavingsAccount(client, "fsaf6", "ads6");
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        client.setUserContext(TestUtils.makeUserWithLocales());
        try {
            client.transferToGroup(group1);
            Assert.fail();
        } catch (CustomerException ce) {
           Assert.assertEquals(ClientConstants.ERRORS_ACTIVE_ACCOUNTS_PRESENT, ce.getKey());
        }
        StaticHibernateUtil.closeSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        client = TestObjectFactory.getClient(client.getCustomerId());
    }

    public void testUpdateGroupFailure_MeetingFrequencyMismatch() throws Exception {
        createInitialObjects();
        MeetingBO meeting = new MeetingBO(Short.valueOf("2"), Short.valueOf("1"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, "Bangalore");
        center1 = TestObjectFactory.createCenter("Center1", meeting);
        group1 = TestObjectFactory.createGroupUnderCenter("Group2", CustomerStatus.GROUP_ACTIVE, center1);
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        client.setUserContext(TestUtils.makeUser());
        try {
            client.transferToGroup(group1);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.ERRORS_MEETING_FREQUENCY_MISMATCH, e.getKey());
        }
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
    }

    public void testSuccessfulTransferToGroupInSameBranch() throws Exception {
        createObjectsForTransferToGroup_SameBranch(CustomerStatus.GROUP_ACTIVE);
        PositionEntity position = (PositionEntity) new MasterPersistence().retrieveMasterEntities(PositionEntity.class,
                Short.valueOf("1")).get(0);
        group.addCustomerPosition(new CustomerPositionEntity(position, client, group));
        center.addCustomerPosition(new CustomerPositionEntity(position, client, group));
        client.transferToGroup(group1);

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());
       Assert.assertEquals(group1.getCustomerMeeting().getMeeting().getMeetingId(), client.getCustomerMeeting()
                .getUpdatedMeeting().getMeetingId());
       Assert.assertEquals(group1.getCustomerId(), client.getParentCustomer().getCustomerId());
       Assert.assertEquals(0, group.getMaxChildCount().intValue());
       Assert.assertEquals(1, group1.getMaxChildCount().intValue());
       Assert.assertEquals(center1.getSearchId() + ".1.1", client.getSearchId());
        CustomerHierarchyEntity currentHierarchy = client.getActiveCustomerHierarchy();
       Assert.assertEquals(group1.getCustomerId(), currentHierarchy.getParentCustomer().getCustomerId());
    }

    public void testSuccessfulTransferToGroup_WithMeeting() throws Exception {
        createObjectsForTransferToGroup_WithMeeting();

        client.transferToGroup(group1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

       Assert.assertEquals(WeekDay.MONDAY, client.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
        Assert.assertNull(client.getCustomerMeeting().getUpdatedMeeting());

        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());

       Assert.assertEquals(group1.getCustomerId(), client.getParentCustomer().getCustomerId());
       Assert.assertEquals(0, group.getMaxChildCount().intValue());
       Assert.assertEquals(1, group1.getMaxChildCount().intValue());

        CustomerHierarchyEntity currentHierarchy = client.getActiveCustomerHierarchy();
       Assert.assertEquals(group1.getCustomerId(), currentHierarchy.getParentCustomer().getCustomerId());

    }

    /**
     * Transfer a client created outside a group to a group. This was originally
     * created to address <a
     * href="https://mifos.dev.java.net/issues/show_bug.cgi?id=2184">issue
     * 2184</a>.
     * <p>
     * In the UI, the second transfer is what causes the the null pointer
     * exception to be thrown.
     */
    public void testSuccessfulTransferToGroupFromOutsideGroup() throws Exception {
        createObjectsForTransferToGroup_OutsideGroup();

        client.addClientToGroup((GroupBO) group); // FIXME: this method should
        // be adding hierarchy
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

       Assert.assertEquals(1, group.getMaxChildCount().intValue());

        CustomerHierarchyEntity currentHierarchy = client.getActiveCustomerHierarchy();
        Assert.assertNotNull("Adding client to group should also create hierarchy entities.", currentHierarchy);
       Assert.assertEquals(group.getCustomerId(), currentHierarchy.getParentCustomer().getCustomerId());

        // this "normal" group transfer is tested elsewhere, but I added this
        // specifically because
        // issue 2184 reproduced after *two* transfers, one into a group from
        // outside any group,
        // the 2nd from group to group.
        StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.startTransaction();
        client.transferToGroup(group1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        currentHierarchy = client.getActiveCustomerHierarchy();
        Assert.assertNotNull(currentHierarchy);
       Assert.assertEquals(group1.getCustomerId(), currentHierarchy.getParentCustomer().getCustomerId());
    }

    public void testSuccessfulTransferToGroup_WithOutMeeting() throws Exception {
        createObjectsForTransferToGroup_WithoutMeeting();
        Assert.assertNotNull(client.getCustomerMeeting());

        client.transferToGroup(group1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());

        Assert.assertNull(client.getCustomerMeeting());
       Assert.assertEquals(group1.getCustomerId(), client.getParentCustomer().getCustomerId());
    }

    public void testSuccessfulTransferToGroupInDifferentBranch() throws Exception {
        createObjectsForTransferToGroup_DifferentBranch();
        PositionEntity position = (PositionEntity) new MasterPersistence().retrieveMasterEntities(PositionEntity.class,
                Short.valueOf("1")).get(0);
        group.addCustomerPosition(new CustomerPositionEntity(position, client, group));
        center.addCustomerPosition(new CustomerPositionEntity(position, client, group));

        client.transferToGroup(group1);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());
        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());
       Assert.assertEquals(office.getOfficeId(), client.getOffice().getOfficeId());
       Assert.assertEquals(group1.getCustomerId(), client.getParentCustomer().getCustomerId());
       Assert.assertEquals(0, group.getMaxChildCount().intValue());
       Assert.assertEquals(1, group1.getMaxChildCount().intValue());
       Assert.assertEquals(center1.getSearchId() + ".1.1", client.getSearchId());
        CustomerHierarchyEntity currentHierarchy = client.getActiveCustomerHierarchy();
       Assert.assertEquals(group1.getCustomerId(), currentHierarchy.getParentCustomer().getCustomerId());
        CustomerMovementEntity customerMovementEntity = client.getActiveCustomerMovement();
       Assert.assertEquals(office.getOfficeId(), customerMovementEntity.getOffice().getOfficeId());

        client.setUserContext(TestObjectFactory.getContext());
        client.transferToGroup((GroupBO) group);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());

        group = TestObjectFactory.getGroup(group.getCustomerId());
        group1 = TestObjectFactory.getGroup(group1.getCustomerId());
        center = TestObjectFactory.getCenter(center.getCustomerId());
        center1 = TestObjectFactory.getCenter(center1.getCustomerId());
        office = new OfficePersistence().getOffice(office.getOfficeId());
    }

    public void testUpdateBranchFailure_OfficeNULL() throws Exception {
        createInitialObjects();
        try {
            client.transferToBranch(null);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.INVALID_OFFICE, e.getKey());
        }
    }

    public void testUpdateBranchFailure_TransferInSameOffice() throws Exception {
        createInitialObjects();
        try {
            client.transferToBranch(client.getOffice());
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.ERRORS_SAME_BRANCH_TRANSFER, e.getKey());
        }
    }

    public void testUpdateBranchFailure_OfficeInactive() throws Exception {
        createObjectsForClientTransfer();
        office.update(office.getOfficeName(), office.getShortName(), OfficeStatus.INACTIVE, office.getOfficeLevel(),
                office.getParentOffice(), null, null);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        try {
            client.transferToBranch(office);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.ERRORS_TRANSFER_IN_INACTIVE_OFFICE, e.getKey());
        }
    }

    public void testUpdateBranchFirstTime() throws Exception {
        createObjectsForClientTransfer();
        Assert.assertNull(client.getActiveCustomerMovement());

        client.transferToBranch(office);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());
        Assert.assertNotNull(client.getActiveCustomerMovement());
       Assert.assertEquals(office.getOfficeId(), client.getOffice().getOfficeId());
       Assert.assertEquals(CustomerStatus.CLIENT_HOLD, client.getStatus());
        office = client.getOffice();
    }

    public void testUpdateBranchSecondTime() throws Exception {
        createObjectsForClientTransfer();
        Assert.assertNull(client.getActiveCustomerMovement());
        OfficeBO oldOffice = client.getOffice();

        client.transferToBranch(office);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());
        client.setUserContext(TestUtils.makeUser());
        CustomerMovementEntity currentMovement = client.getActiveCustomerMovement();
        Assert.assertNotNull(currentMovement);
       Assert.assertEquals(office.getOfficeId(), currentMovement.getOffice().getOfficeId());
       Assert.assertEquals(office.getOfficeId(), client.getOffice().getOfficeId());

        client.transferToBranch(oldOffice);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());
        currentMovement = client.getActiveCustomerMovement();
        Assert.assertNotNull(currentMovement);
       Assert.assertEquals(oldOffice.getOfficeId(), currentMovement.getOffice().getOfficeId());
       Assert.assertEquals(oldOffice.getOfficeId(), client.getOffice().getOfficeId());

        office = new OfficePersistence().getOffice(office.getOfficeId());
    }

    public void testGetClientAndSpouseName() throws Exception {
        createObjectsForClient("Client 1", CustomerStatus.CLIENT_ACTIVE);
       Assert.assertEquals(client.getClientName().getName().getFirstName(), "Client 1");
       Assert.assertEquals(client.getSpouseName().getName().getFirstName(), "Client 1");
        client = TestObjectFactory.getClient(client.getCustomerId());
        office = new OfficePersistence().getOffice(office.getOfficeId());
    }

    public void testUpdateClientDetails() throws Exception {
        createObjectsForClient("Client 1", CustomerStatus.CLIENT_ACTIVE);
        Short povertyStatus = Short.valueOf("41");
       Assert.assertEquals(1, client.getCustomerDetail().getEthinicity().intValue());
       Assert.assertEquals(1, client.getCustomerDetail().getCitizenship().intValue());
       Assert.assertEquals(1, client.getCustomerDetail().getHandicapped().intValue());
        ClientDetailView clientDetailView = new ClientDetailView(2, 2, 2, 2, 2, 2, Short.valueOf("1"), Short
                .valueOf("1"), povertyStatus);
        client.updateClientDetails(clientDetailView);
       Assert.assertEquals(2, client.getCustomerDetail().getEthinicity().intValue());
       Assert.assertEquals(2, client.getCustomerDetail().getCitizenship().intValue());
       Assert.assertEquals(2, client.getCustomerDetail().getHandicapped().intValue());
       Assert.assertEquals(povertyStatus, client.getCustomerDetail().getPovertyStatus());
        client = TestObjectFactory.getClient(client.getCustomerId());
        office = new OfficePersistence().getOffice(office.getOfficeId());
    }

    public void testUpdateFailureIfLoanOffcierNotThereInActiveState() throws Exception {
        createObjectsForClient("Client 1", CustomerStatus.CLIENT_ACTIVE);
        try {
            client.updateMfiInfo(null);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.INVALID_LOAN_OFFICER, e.getKey());
        }
        client = TestObjectFactory.getClient(client.getCustomerId());
    }

    public void testUpdateFailureIfLoanOffcierNotThereInHoldState() throws Exception {
        createObjectsForClient("Client 1", CustomerStatus.CLIENT_HOLD);
        try {
            client.updateMfiInfo(null);
            Assert.fail();
        } catch (CustomerException e) {
           Assert.assertEquals(CustomerConstants.INVALID_LOAN_OFFICER, e.getKey());
        }
        client = TestObjectFactory.getClient(client.getCustomerId());
    }

    public void testUpdateWeeklyMeeting() throws Exception {
        client = TestObjectFactory.createClient("clientname", getMeeting(), CustomerStatus.CLIENT_PENDING);
        MeetingBO clientMeeting = client.getCustomerMeeting().getMeeting();
        String meetingPlace = "Bangalore";
        MeetingBO newMeeting = new MeetingBO(WeekDay.THURSDAY, clientMeeting.getMeetingDetails().getRecurAfter(),
                clientMeeting.getStartDate(), MeetingType.CUSTOMER_MEETING, meetingPlace);
        client.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());

       Assert.assertEquals(WeekDay.THURSDAY, client.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(meetingPlace, client.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
    }

    public void testUpdateMonthlyMeeting() throws Exception {
        String meetingPlace = "Bangalore";
        MeetingBO monthlyMeeting = new MeetingBO(WeekDay.MONDAY, RankType.FIRST, Short.valueOf("2"),
                new java.util.Date(), MeetingType.CUSTOMER_MEETING, "delhi");
        client = TestObjectFactory.createClient("clientname", monthlyMeeting, CustomerStatus.CLIENT_PENDING);
        MeetingBO clientMeeting = client.getCustomerMeeting().getMeeting();
        MeetingBO newMeeting = new MeetingBO(WeekDay.THURSDAY, RankType.FIRST, clientMeeting.getMeetingDetails()
                .getRecurAfter(), clientMeeting.getStartDate(), MeetingType.CUSTOMER_MEETING, meetingPlace);
        client.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
       Assert.assertEquals(WeekDay.THURSDAY, client.getCustomerMeeting().getUpdatedMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(meetingPlace, client.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
    }

    public void testUpdateMonthlyMeetingOnDate() throws Exception {
        MeetingBO monthlyMeetingOnDate = new MeetingBO(Short.valueOf("5"), Short.valueOf("2"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, "delhi");
        client = TestObjectFactory.createClient("clientname", monthlyMeetingOnDate, CustomerStatus.CLIENT_PENDING);
        MeetingBO clientMeeting = client.getCustomerMeeting().getMeeting();
        String meetingPlace = "Bangalore";
        MeetingBO newMeeting = new MeetingBO(WeekDay.THURSDAY, clientMeeting.getMeetingDetails().getRecurAfter(),
                clientMeeting.getStartDate(), MeetingType.CUSTOMER_MEETING, meetingPlace);
        client.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getClient(client.getCustomerId());
       Assert.assertEquals(meetingPlace, client.getCustomerMeeting().getUpdatedMeeting().getMeetingPlace());
    }

    public void testCreateMeeting() throws Exception {
        client = TestObjectFactory.createClient("clientname", null, CustomerStatus.CLIENT_PENDING);
        String meetingPlace = "newPlace";
        Short recurAfter = Short.valueOf("4");
        MeetingBO newMeeting = new MeetingBO(WeekDay.FRIDAY, recurAfter, new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, meetingPlace);
        client.updateMeeting(newMeeting);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        client = TestObjectFactory.getClient(client.getCustomerId());

       Assert.assertEquals(WeekDay.FRIDAY, client.getCustomerMeeting().getMeeting().getMeetingDetails().getWeekDay());
       Assert.assertEquals(meetingPlace, client.getCustomerMeeting().getMeeting().getMeetingPlace());
       Assert.assertEquals(recurAfter, client.getCustomerMeeting().getMeeting().getMeetingDetails().getRecurAfter());
    }

    private void createObjectsForClientTransfer() throws Exception {
        office = TestObjectFactory.createOffice(OfficeLevel.BRANCHOFFICE, TestObjectFactory
                .getOffice(TestObjectFactory.HEAD_OFFICE), "customer_office", "cust");
        client = TestObjectFactory.createClient("client_to_transfer", getMeeting(), CustomerStatus.CLIENT_ACTIVE);
        StaticHibernateUtil.closeSession();
    }

    private SavingsBO createSavingsAccount(CustomerBO customer, String offeringName, String shortName) throws Exception {
        SavingsOfferingBO savingsOffering = new SavingsTestHelper().createSavingsOffering(offeringName, shortName);
        return TestObjectFactory.createSavingsAccount("000100000000017", customer, AccountStates.SAVINGS_ACC_APPROVED,
                new Date(System.currentTimeMillis()), savingsOffering);
    }

    private void createObjectsForTransferToGroup_WithMeeting() throws Exception {
        Short officeId = new Short("3");
        Short personnel = new Short("1");
        group = TestObjectFactory.createGroupUnderBranch("Group", CustomerStatus.GROUP_PENDING, officeId, null,
                personnel);
        group1 = TestObjectFactory.createGroupUnderBranch("Group2", CustomerStatus.GROUP_PENDING, officeId,
                getMeeting(), personnel);
        client = TestObjectFactory.createClient("new client", CustomerStatus.CLIENT_PARTIAL, group,
                new java.util.Date());
        StaticHibernateUtil.closeSession();
    }

    private void createObjectsForTransferToGroup_WithoutMeeting() throws Exception {
        Short officeId = new Short("3");
        Short personnel = new Short("1");
        group = TestObjectFactory.createGroupUnderBranch("Group", CustomerStatus.GROUP_PENDING, officeId, getMeeting(),
                personnel);
        group1 = TestObjectFactory.createGroupUnderBranch("Group2", CustomerStatus.GROUP_PENDING, officeId, null,
                personnel);
        client = TestObjectFactory.createClient("new client", CustomerStatus.CLIENT_PARTIAL, group,
                new java.util.Date());
        StaticHibernateUtil.closeSession();
    }

    private void createObjectsForTransferToGroup_OutsideGroup() throws Exception {
        Short officeId = new Short("3");
        Short personnel = new Short("1");
        group = TestObjectFactory.createGroupUnderBranch("Group", CustomerStatus.GROUP_PENDING, officeId, getMeeting(),
                personnel);
        group1 = TestObjectFactory.createGroupUnderBranch("Group2", CustomerStatus.GROUP_PENDING, officeId,
                getMeeting(), personnel);
        client = TestObjectFactory
                .createClient("new client", CustomerStatus.CLIENT_PARTIAL, null, new java.util.Date());
        StaticHibernateUtil.closeSession();
    }

    private void createObjectsForTransferToGroup_SameBranch(CustomerStatus groupStatus) throws Exception {
        createInitialObjects();
        MeetingBO meeting = new MeetingBO(WeekDay.THURSDAY, Short.valueOf("1"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, "Bangalore");
        center1 = TestObjectFactory.createCenter("Center1", meeting);
        group1 = TestObjectFactory.createGroupUnderCenter("Group2", groupStatus, center1);
        StaticHibernateUtil.closeSession();
    }

    private void createObjectsForTransferToGroup_DifferentBranch() throws Exception {
        createInitialObjects();
        office = TestObjectFactory.createOffice(OfficeLevel.BRANCHOFFICE, TestObjectFactory
                .getOffice(TestObjectFactory.HEAD_OFFICE), "customer_office", "cust");
        StaticHibernateUtil.closeSession();
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center1 = TestObjectFactory.createCenter("Center1", meeting, office.getOfficeId(),
                PersonnelConstants.SYSTEM_USER);
        group1 = TestObjectFactory.createGroupUnderCenter("Group2", CustomerStatus.GROUP_ACTIVE, center1);
        StaticHibernateUtil.closeSession();
    }

    private void createObjectsForClient(String name, CustomerStatus status) throws Exception {
        office = TestObjectFactory.createOffice(OfficeLevel.BRANCHOFFICE, TestObjectFactory
                .getOffice(TestObjectFactory.HEAD_OFFICE), "customer_office", "cust");
        client = TestObjectFactory.createClient(name, getMeeting(), status);
        StaticHibernateUtil.closeSession();
    }

    private List<FeeView> getFees() {
        List<FeeView> fees = new ArrayList<FeeView>();
        AmountFeeBO fee1 = (AmountFeeBO) TestObjectFactory.createPeriodicAmountFee("PeriodicAmountFee",
                FeeCategory.CENTER, "200", RecurrenceType.WEEKLY, Short.valueOf("2"));
        AmountFeeBO fee2 = (AmountFeeBO) TestObjectFactory.createOneTimeAmountFee("OneTimeAmountFee",
                FeeCategory.ALLCUSTOMERS, "100", FeePayment.UPFRONT);
        fees.add(new FeeView(TestObjectFactory.getContext(), fee1));
        fees.add(new FeeView(TestObjectFactory.getContext(), fee2));
        StaticHibernateUtil.commitTransaction();
        return fees;
    }

    private void removeFees(List<FeeView> feesToRemove) {
        for (FeeView fee : feesToRemove) {
            TestObjectFactory.cleanUp(new FeePersistence().getFee(fee.getFeeIdValue()));
        }
    }

    private List<CustomFieldView> getCustomFields() {
        List<CustomFieldView> fields = new ArrayList<CustomFieldView>();
        fields.add(new CustomFieldView(Short.valueOf("5"), "value1", CustomFieldType.ALPHA_NUMERIC));
        fields.add(new CustomFieldView(Short.valueOf("6"), "value2", CustomFieldType.ALPHA_NUMERIC));
        return fields;
    }

    private void createInitialObjects() throws Exception {
        MeetingBO meeting = new MeetingBO(WeekDay.MONDAY, Short.valueOf("1"), new java.util.Date(),
                MeetingType.CUSTOMER_MEETING, "Delhi");
        center = TestObjectFactory.createCenter("Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = createClient(CustomerStatus.CLIENT_ACTIVE);
        StaticHibernateUtil.closeSession();
    }

    private ClientBO createClient(CustomerStatus clientStatus) {
        return TestObjectFactory.createClient("Client", clientStatus, group);
    }

    private void createParentObjects(CustomerStatus groupStatus) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createCenter("Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter("Group", groupStatus, center);
        StaticHibernateUtil.closeSession();
    }

    private ClientAttendanceBO getClientAttendance(java.util.Date meetingDate) {
        ClientAttendanceBO clientAttendance = new ClientAttendanceBO();
        clientAttendance.setAttendance(AttendanceType.PRESENT);
        clientAttendance.setMeetingDate(meetingDate);
        return clientAttendance;
    }

    private Date getDateOffset(int numberOfDays) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, (day - numberOfDays));
        return new Date(currentDateCalendar.getTimeInMillis());
    }

    private MeetingBO getMeeting() throws Exception {
        return new MeetingBO(WeekDay.MONDAY, Short.valueOf("1"), new java.util.Date(), MeetingType.CUSTOMER_MEETING,
                "Delhi");
    }
}
