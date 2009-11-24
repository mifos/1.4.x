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

package org.mifos.application.accounts.loan.business;

import static org.mifos.application.meeting.util.helpers.MeetingType.CUSTOMER_MEETING;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.MONTHLY;
import static org.mifos.application.meeting.util.helpers.RecurrenceType.WEEKLY;
import static org.mifos.application.meeting.util.helpers.WeekDay.MONDAY;
import static org.mifos.framework.util.helpers.DateUtils.currentDate;
import static org.mifos.framework.util.helpers.DateUtils.getCurrentDateWithoutTimeStamp;
import static org.mifos.framework.util.helpers.DateUtils.getDateFromToday;
import static org.mifos.framework.util.helpers.NumberUtils.DOUBLE_ZERO;
import static org.mifos.framework.util.helpers.NumberUtils.SHORT_ZERO;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_MONTH;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_SECOND_MONTH;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_SECOND_WEEK;
import static org.mifos.framework.util.helpers.TestObjectFactory.EVERY_WEEK;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.hibernate.Session;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.mifos.application.accounts.business.AccountActionDateEntity;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.business.AccountFeesActionDetailEntity;
import org.mifos.application.accounts.business.AccountFeesEntity;
import org.mifos.application.accounts.business.AccountPaymentEntity;
import org.mifos.application.accounts.business.AccountTrxnEntity;
import org.mifos.application.accounts.business.FeesTrxnDetailEntity;
import org.mifos.application.accounts.exceptions.AccountException;
import org.mifos.application.accounts.loan.persistance.LoanDao;
import org.mifos.application.accounts.loan.util.helpers.LoanConstants;
import org.mifos.application.accounts.persistence.AccountPersistence;
import org.mifos.application.accounts.util.helpers.AccountActionTypes;
import org.mifos.application.accounts.util.helpers.AccountConstants;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.accounts.util.helpers.AccountStates;
import org.mifos.application.accounts.util.helpers.PaymentData;
import org.mifos.application.accounts.util.helpers.PaymentStatus;
import org.mifos.application.accounts.util.helpers.WaiveEnum;
import org.mifos.application.collectionsheet.business.CollSheetCustBO;
import org.mifos.application.collectionsheet.business.CollectionSheetBO;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.CustomerBOTestUtils;
import org.mifos.application.customer.business.CustomerStatusEntity;
import org.mifos.application.customer.client.business.ClientBO;
import org.mifos.application.customer.client.business.ClientPerformanceHistoryEntity;
import org.mifos.application.customer.group.business.GroupPerformanceHistoryEntity;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.fees.business.AmountFeeBO;
import org.mifos.application.fees.business.FeeBO;
import org.mifos.application.fees.business.FeeView;
import org.mifos.application.fees.business.RateFeeBO;
import org.mifos.application.fees.util.helpers.FeeCategory;
import org.mifos.application.fees.util.helpers.FeeFormula;
import org.mifos.application.fees.util.helpers.FeePayment;
import org.mifos.application.fees.util.helpers.FeeStatus;
import org.mifos.application.fund.business.FundBO;
import org.mifos.application.holiday.business.HolidayBO;
import org.mifos.application.holiday.business.HolidayPK;
import org.mifos.application.holiday.business.RepaymentRuleEntity;
import org.mifos.application.holiday.persistence.HolidayPersistence;
import org.mifos.application.holiday.util.helpers.RepaymentRuleTypes;
import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.master.business.CustomFieldView;
import org.mifos.application.master.business.InterestTypesEntity;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.business.MeetingRecurrenceEntity;
import org.mifos.application.meeting.exceptions.MeetingException;
import org.mifos.application.meeting.util.helpers.RecurrenceType;
import org.mifos.application.meeting.util.helpers.WeekDay;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.application.productdefinition.business.LoanOfferingInstallmentRange;
import org.mifos.application.productdefinition.util.helpers.ApplicableTo;
import org.mifos.application.productdefinition.util.helpers.GraceType;
import org.mifos.application.productdefinition.util.helpers.InterestType;
import org.mifos.application.productdefinition.util.helpers.PrdStatus;
import org.mifos.application.productsmix.business.service.ProductMixBusinessService;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.config.AccountingRules;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.TestUtils;
import org.mifos.framework.components.audit.business.AuditLog;
import org.mifos.framework.components.audit.business.AuditLogRecord;
import org.mifos.framework.components.configuration.business.Configuration;
import org.mifos.framework.components.configuration.persistence.ConfigurationPersistence;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.InvalidDateException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.TestDatabase;
import org.mifos.framework.persistence.TestObjectPersistence;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.DateTimeService;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class LoanBOIntegrationTest extends MifosIntegrationTestCase {

    public LoanBOIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    private static final double DELTA = 0.00000001;

    private static final double DEFAULT_LOAN_AMOUNT = 300.0;

    private LoanOfferingBO loanOffering = null;

    // TODO: probably should be of type LoanBO
    private AccountBO accountBO = null;

    private AccountBO badAccountBO = null;

    private CustomerBO center = null;

    private CustomerBO group = null;

    private CustomerBO client = null;

    private AccountPersistence accountPersistence = null;

    private MeetingBO meeting;

    private UserContext userContext = null;

    private final List<FeeView> feeViews = new ArrayList<FeeView>();

    private BigDecimal savedInitialRoundOffMultiple = null;
    private BigDecimal savedFinalRoundOffMultiple = null;
    private RoundingMode savedCurrencyRoundingMode = null;
    private RoundingMode savedInitialRoundingMode = null;
    private RoundingMode savedFinalRoundingMode = null;
    private Short savedDigitAfterDecimal;

    private LoanDao loanDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        userContext = TestObjectFactory.getContext();
        accountPersistence = new AccountPersistence();

        savedInitialRoundOffMultiple = AccountingRules.getInitialRoundOffMultiple();
        savedFinalRoundOffMultiple = AccountingRules.getFinalRoundOffMultiple();
        savedCurrencyRoundingMode = AccountingRules.getCurrencyRoundingMode();
        savedDigitAfterDecimal = AccountingRules.getDigitsAfterDecimal();
        savedInitialRoundingMode = AccountingRules.getInitialRoundingMode();
        savedFinalRoundingMode = AccountingRules.getFinalRoundingMode();

        AccountingRules.setInitialRoundOffMultiple(new BigDecimal("1"));
        AccountingRules.setFinalRoundOffMultiple(new BigDecimal("1"));
        AccountingRules.setCurrencyRoundingMode(RoundingMode.CEILING);
        AccountingRules.setInitialRoundingMode(RoundingMode.CEILING);
        AccountingRules.setFinalRoundingMode(RoundingMode.CEILING);
        AccountingRules.setDigitsAfterDecimal((short) 1);

        loanDao = new LoanDao();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            TestObjectFactory.removeObject(loanOffering);
            if (accountBO != null) {
                accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class,
                        accountBO.getAccountId());
            }
            if (badAccountBO != null) {
                badAccountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class,
                        badAccountBO.getAccountId());
            }
            if (group != null) {
                group = (CustomerBO) StaticHibernateUtil.getSessionTL().get(CustomerBO.class, group.getCustomerId());
            }
            if (center != null) {
                center = (CustomerBO) StaticHibernateUtil.getSessionTL().get(CustomerBO.class, center.getCustomerId());
            }
            TestObjectFactory.cleanUp(accountBO);
            TestObjectFactory.cleanUp(badAccountBO);
            TestObjectFactory.cleanUp(client);
            TestObjectFactory.cleanUp(group);
            TestObjectFactory.cleanUp(center);
        } catch (Exception e) {
            // TODO Whoops, cleanup didnt work, reset db
            TestDatabase.resetMySQLDatabase();
        }
        AccountingRules.setInitialRoundOffMultiple(savedInitialRoundOffMultiple);
        AccountingRules.setFinalRoundOffMultiple(savedFinalRoundOffMultiple);
        AccountingRules.setCurrencyRoundingMode(savedCurrencyRoundingMode);
        AccountingRules.setDigitsAfterDecimal(savedDigitAfterDecimal);
        AccountingRules.setInitialRoundingMode(savedInitialRoundingMode);
        AccountingRules.setFinalRoundingMode(savedFinalRoundingMode);

        StaticHibernateUtil.closeSession();
        super.tearDown();
    }

    public void testApplyPeriodicFee() throws Exception {
        accountBO = getBasicLoanAccount();
        Money intialTotalFeeAmount = ((LoanBO) accountBO).getLoanSummary().getOriginalFees();
        TestObjectFactory.flushandCloseSession();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "200",
                RecurrenceType.WEEKLY, Short.valueOf("2"));
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        UserContext uc = TestUtils.makeUser();
        accountBO.setUserContext(uc);
        accountBO
                .applyCharge(periodicFee.getFeeId(), ((AmountFeeBO) periodicFee).getFeeAmount().getAmountDoubleValue());
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        Date lastAppliedDate = null;

        Map<String, String> fees1 = new HashMap<String, String>();
        fees1.put("Periodic Fee", "200.0");// missing an entry
        fees1.put("Mainatnence Fee", "100.0");

        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(((LoanBO) accountBO)
                .getAccountActionDates());
       Assert.assertEquals(6, paymentsArray.length);

        checkFees(fees1, paymentsArray[0], false);
        checkFees(fees1, paymentsArray[2], false);
        checkFees(fees1, paymentsArray[4], false);

        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;

            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("5"))) {
               Assert.assertEquals(2, loanScheduleEntity.getAccountFeesActionDetails().size());
                lastAppliedDate = loanScheduleEntity.getActionDate();

            }
        }

       Assert.assertEquals(intialTotalFeeAmount.add(new Money("600.0")), ((LoanBO) accountBO).getLoanSummary()
                .getOriginalFees());
        LoanActivityEntity loanActivityEntity = (LoanActivityEntity) ((LoanBO) accountBO).getLoanActivityDetails()
                .toArray()[0];
       Assert.assertEquals(periodicFee.getFeeName() + " applied", loanActivityEntity.getComments());
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getOriginalFees(), loanActivityEntity.getFeeOutstanding());
        AccountFeesEntity accountFeesEntity = accountBO.getAccountFees(periodicFee.getFeeId());
       Assert.assertEquals(FeeStatus.ACTIVE, accountFeesEntity.getFeeStatusAsEnum());
       Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(lastAppliedDate.getTime()), DateUtils
                .getDateWithoutTimeStamp(accountFeesEntity.getLastAppliedDate().getTime()));
    }

    public void testApplyPeriodicFeeWithHoliday() throws Exception {
        Date startDate = DateUtils.getDate(2008, Calendar.MAY, 23);
        new DateTimeService().setCurrentDateTime(new DateTime(startDate));

        accountBO = getLoanAccount(startDate, AccountState.LOAN_APPROVED);

        Money intialTotalFeeAmount = ((LoanBO) accountBO).getLoanSummary().getOriginalFees();
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());

        // create holiday on first installment date
        HolidayBO holiday = createHoliday(DateUtils.getDate(2008, Calendar.MAY, 30));

        try {
            LoanBO loanBO = (LoanBO) accountBO;
            loanBO.updateLoan(loanBO.isInterestDeductedAtDisbursement(), loanBO.getLoanAmount(), loanBO
                    .getInterestRate(), loanBO.getNoOfInstallments(), loanBO.getDisbursementDate(), loanBO
                    .getGracePeriodDuration(), loanBO.getBusinessActivityId(), "Loan account updated", null, null,
                    false, null);

            FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "25",
                    RecurrenceType.WEEKLY, EVERY_WEEK);

            UserContext uc = TestUtils.makeUser();
            loanBO.setUserContext(uc);
            loanBO.applyCharge(periodicFee.getFeeId(), ((AmountFeeBO) periodicFee).getFeeAmount()
                    .getAmountDoubleValue());
            StaticHibernateUtil.commitTransaction();
            StaticHibernateUtil.closeSession();

            Map<String, String> fees1 = new HashMap<String, String>();
            fees1.put("Mainatnence Fee", "100.0");

            Map<String, String> fees2 = new HashMap<String, String>();
            fees2.put("Mainatnence Fee", "100.0");
            fees2.put("Periodic Fee", "25.0");

            LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(loanBO.getAccountActionDates());

            List<Date> installmentDates = new ArrayList<Date>();
            List<Date> feeDates = new ArrayList<Date>();
            for (LoanScheduleEntity loanScheduleEntity : paymentsArray) {
                installmentDates.add(loanScheduleEntity.getActionDate());
                for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : loanScheduleEntity
                        .getAccountFeesActionDetails()) {
                    Date feeDate = accountFeesActionDetailEntity.getAccountActionDate().getActionDate();
                    feeDates.add(feeDate);
                }
            }

           Assert.assertEquals(6, paymentsArray.length);

            checkFees(fees2, paymentsArray[0], false);
            checkFees(fees2, paymentsArray[1], false);
            checkFees(fees2, paymentsArray[2], false);
            checkFees(fees2, paymentsArray[3], false);
            checkFees(fees2, paymentsArray[4], false);
            checkFees(fees2, paymentsArray[5], false);

            List<Date> expectedDates = new ArrayList<Date>();
            expectedDates.add(DateUtils.getDate(2008, Calendar.MAY, 31));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JUNE, 06));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JUNE, 13));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JUNE, 20));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JUNE, 27));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JULY, 04));

            int i = 0;
            for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
                LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
               Assert.assertEquals(expectedDates.get(i++), loanScheduleEntity.getActionDate());
               Assert.assertEquals(new Money("125"), loanScheduleEntity.getTotalFees());
            }

           Assert.assertEquals(intialTotalFeeAmount.add(new Money("750.0")), loanBO.getLoanSummary().getOriginalFees());
        } finally {
            // make sure that we don't leave any persistent changes that could
            // affect subsequent tests
            new DateTimeService().resetToCurrentSystemDateTime();
            deleteHoliday(holiday);
        }
    }

    public void testPeriodicFeeWithHoliday() throws Exception {
        Date startDate = DateUtils.getDate(2008, Calendar.MAY, 23);
        new DateTimeService().setCurrentDateTime(new DateTime(startDate));

        accountBO = getLoanAccount(startDate, AccountState.LOAN_APPROVED);

        Money intialTotalFeeAmount = ((LoanBO) accountBO).getLoanSummary().getOriginalFees();
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());

        // create holiday on first installment date
        HolidayBO holiday = createHoliday(DateUtils.getDate(2008, Calendar.MAY, 30));

        try {
            LoanBO loanBO = (LoanBO) accountBO;
            loanBO.updateLoan(loanBO.isInterestDeductedAtDisbursement(), loanBO.getLoanAmount(), loanBO
                    .getInterestRate(), loanBO.getNoOfInstallments(), loanBO.getDisbursementDate(), loanBO
                    .getGracePeriodDuration(), loanBO.getBusinessActivityId(), "Loan account updated", null, null,
                    false, null);

            Map<String, String> fees1 = new HashMap<String, String>();
            fees1.put("Mainatnence Fee", "100.0");

            LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(loanBO.getAccountActionDates());

            List<Date> installmentDates = new ArrayList<Date>();
            List<Date> feeDates = new ArrayList<Date>();
            for (LoanScheduleEntity loanScheduleEntity : paymentsArray) {
                installmentDates.add(loanScheduleEntity.getActionDate());
                for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : loanScheduleEntity
                        .getAccountFeesActionDetails()) {
                    Date feeDate = accountFeesActionDetailEntity.getAccountActionDate().getActionDate();
                    feeDates.add(feeDate);
                }
            }
            System.out.println(installmentDates);
            System.out.println(feeDates);

           Assert.assertEquals(6, paymentsArray.length);

            checkFees(fees1, paymentsArray[0], false);
            checkFees(fees1, paymentsArray[1], false);
            checkFees(fees1, paymentsArray[2], false);
            checkFees(fees1, paymentsArray[3], false);
            checkFees(fees1, paymentsArray[4], false);
            checkFees(fees1, paymentsArray[5], false);

            List<Date> expectedDates = new ArrayList<Date>();
            expectedDates.add(DateUtils.getDate(2008, Calendar.MAY, 31));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JUNE, 06));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JUNE, 13));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JUNE, 20));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JUNE, 27));
            expectedDates.add(DateUtils.getDate(2008, Calendar.JULY, 04));

            int i = 0;
            for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
                LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
               Assert.assertEquals(expectedDates.get(i++), loanScheduleEntity.getActionDate());
               Assert.assertEquals(new Money("100"), loanScheduleEntity.getTotalFees());
            }

           Assert.assertEquals(intialTotalFeeAmount.add(new Money("600.0")), loanBO.getLoanSummary().getOriginalFees());
        } finally {
            // make sure that we don't leave any persistent changes that could
            // affect subsequent tests
            new DateTimeService().resetToCurrentSystemDateTime();
            deleteHoliday(holiday);
        }
    }

    private AccountBO getLoanAccount(final Date startDate, final AccountState state) throws MeetingException {
        createInitialCustomers(startDate);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, center.getCustomerMeeting()
                .getMeeting());
        return TestObjectFactory.createLoanAccount("42423142341", group, state, startDate, loanOffering);
    }

    private void createInitialCustomers(final Date meetingStartDate) throws MeetingException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(meetingStartDate);
        // create a meeting based on meetingStartDate rather than the
        // current system date
        MeetingBO meeting = TestObjectFactory.createMeeting(new MeetingBO(WeekDay.getWeekDay(cal
                .get(Calendar.DAY_OF_WEEK)), EVERY_WEEK, meetingStartDate, CUSTOMER_MEETING, "place"));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
    }

    private HolidayBO createHoliday(final Date holidayDate) throws PersistenceException, ApplicationException {
        // next working day repayment rule
        RepaymentRuleEntity entity = new HolidayPersistence().getRepaymentRule(RepaymentRuleTypes.NEXT_WORKING_DAY
                .getValue());
        HolidayBO holiday = new HolidayBO(new HolidayPK((short) 1, holidayDate), holidayDate, "a holiday", entity);
        holiday.setValidationEnabled(false);
        holiday.save();
        StaticHibernateUtil.commitTransaction();
        return holiday;
    }

    private void deleteHoliday(final HolidayBO holiday) throws PersistenceException {
        new HolidayPersistence().delete(holiday);
        StaticHibernateUtil.commitTransaction();
    }

    public void testCreateIndividualLoan() throws Exception {
        createInitialCustomers();
        MeetingBO meeting = TestObjectFactory.createLoanMeeting(group.getCustomerMeeting().getMeeting());

        Date startDate = new Date(System.currentTimeMillis());
        loanOffering = TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 1, InterestType.FLAT, meeting);
        List<Date> meetingDates = TestObjectFactory.getMeetingDates(meeting, 1);
        MifosCurrency currency = TestObjectFactory.getCurrency();
        try {

            LoanBO.createIndividualLoan(userContext, loanOffering, group, AccountState.LOAN_PARTIAL_APPLICATION,
                    new Money(currency, "300.0"), Short.valueOf("1"), meetingDates.get(0), true, false, 10.0,
                    (short) 0, new FundBO(), new ArrayList<FeeView>(), null);

            Assert.fail();
        } catch (AccountException e) {
           Assert.assertTrue(true);
        }
    }

    public void testWaiveMiscFeeAfterPayment() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        List<FeeView> feeViews = new ArrayList<FeeView>();
        boolean isInterestDedAtDisb = false;
        Short noOfinstallments = (short) 6;
        LoanBO loan = createAndRetrieveLoanAccount(loanOffering, isInterestDedAtDisb, feeViews, noOfinstallments);
        loan.setUserContext(TestUtils.makeUser());
        loan.applyCharge(Short.valueOf("-1"), Double.valueOf("200"));
        for (AccountActionDateEntity accountActionDateEntity : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(loanScheduleEntity.getMiscFee(), new Money("200"));
               Assert.assertEquals(loanScheduleEntity.getMiscFeePaid(), new Money());
            }
        }
       Assert.assertEquals(loan.getLoanSummary().getOriginalFees(), new Money("200"));
       Assert.assertEquals(loan.getLoanSummary().getFeesPaid(), new Money());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        Date currentDate = new Date(System.currentTimeMillis());
        PaymentData paymentData = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestObjectFactory
                .getMoneyForMFICurrency(200), null, accountBO.getPersonnel(), "receiptNum", Short.valueOf("1"),
                currentDate, currentDate);
        loan.applyPaymentWithPersist(paymentData);
        TestObjectFactory.updateObject(loan);
        for (AccountActionDateEntity accountActionDateEntity : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(loanScheduleEntity.getMiscFee(), new Money("200"));
               Assert.assertEquals(loanScheduleEntity.getMiscFeePaid(), new Money("200"));
            }
        }
       Assert.assertEquals(loan.getLoanSummary().getOriginalFees(), new Money("200"));
       Assert.assertEquals(loan.getLoanSummary().getFeesPaid(), new Money("200"));
        loan.applyCharge(Short.valueOf("-1"), Double.valueOf("300"));
        TestObjectFactory.updateObject(loan);
        for (AccountActionDateEntity accountActionDateEntity : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(loanScheduleEntity.getMiscFee(), new Money("500"));
               Assert.assertEquals(loanScheduleEntity.getMiscFeePaid(), new Money("200"));
            }
        }
       Assert.assertEquals(loan.getLoanSummary().getOriginalFees(), new Money("500"));
       Assert.assertEquals(loan.getLoanSummary().getFeesPaid(), new Money("200"));
        loan.waiveAmountDue(WaiveEnum.FEES);
        for (AccountActionDateEntity accountActionDateEntity : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(loanScheduleEntity.getMiscFee(), new Money("200"));
               Assert.assertEquals(loanScheduleEntity.getMiscFeePaid(), new Money("200"));
            }
        }
       Assert.assertEquals(loan.getLoanSummary().getOriginalFees(), new Money("200"));
       Assert.assertEquals(loan.getLoanSummary().getFeesPaid(), new Money("200"));
    }

    public void testWaiveMiscPenaltyAfterPayment() throws Exception {
        accountBO = getLoanAccount();
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
        accountBO.setUserContext(TestUtils.makeUser());
        Date currentDate = new Date(System.currentTimeMillis());
        LoanBO loan = (LoanBO) accountBO;
        loan.applyCharge(Short.valueOf("-2"), Double.valueOf("200"));
        TestObjectFactory.updateObject(loan);
        for (AccountActionDateEntity accountActionDateEntity : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(loanScheduleEntity.getMiscPenalty(), new Money("200"));
               Assert.assertEquals(loanScheduleEntity.getMiscPenaltyPaid(), new Money());
            }
        }
       Assert.assertEquals(loan.getLoanSummary().getOriginalPenalty(), new Money("200"));
       Assert.assertEquals(loan.getLoanSummary().getPenaltyPaid(), new Money());
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PaymentData paymentData = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestObjectFactory
                .getMoneyForMFICurrency(200), null, accountBO.getPersonnel(), "receiptNum", Short.valueOf("1"),
                currentDate, currentDate);
        loan.applyPaymentWithPersist(paymentData);
        TestObjectFactory.updateObject(loan);
        for (AccountActionDateEntity accountActionDateEntity : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(loanScheduleEntity.getMiscPenalty(), new Money("200"));
               Assert.assertEquals(loanScheduleEntity.getMiscPenaltyPaid(), new Money("200"));
            }
        }
       Assert.assertEquals(loan.getLoanSummary().getOriginalPenalty(), new Money("200"));
       Assert.assertEquals(loan.getLoanSummary().getPenaltyPaid(), new Money("200"));
        loan.applyCharge(Short.valueOf("-2"), Double.valueOf("300"));
        TestObjectFactory.updateObject(loan);
        for (AccountActionDateEntity accountActionDateEntity : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(loanScheduleEntity.getMiscPenalty(), new Money("500"));
               Assert.assertEquals(loanScheduleEntity.getMiscPenaltyPaid(), new Money("200"));
            }
        }
       Assert.assertEquals(loan.getLoanSummary().getOriginalPenalty(), new Money("500"));
       Assert.assertEquals(loan.getLoanSummary().getPenaltyPaid(), new Money("200"));
        loan.waiveAmountDue(WaiveEnum.PENALTY);
        for (AccountActionDateEntity accountActionDateEntity : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(loanScheduleEntity.getMiscPenalty(), new Money("200"));
               Assert.assertEquals(loanScheduleEntity.getMiscPenaltyPaid(), new Money("200"));
            }
        }
       Assert.assertEquals(loan.getLoanSummary().getOriginalPenalty(), new Money("200"));
       Assert.assertEquals(loan.getLoanSummary().getPenaltyPaid(), new Money("200"));
    }

    public void testAddLoanDetailsForDisbursal() {
        LoanBO loan = (LoanBO) createLoanAccount();
        loan.setLoanAmount(TestObjectFactory.getMoneyForMFICurrency(100));
        loan.setNoOfInstallments(Short.valueOf("5"));
        InterestTypesEntity interestType = new InterestTypesEntity(InterestType.FLAT);
        loan.setInterestType(interestType);
        List<LoanBO> loanWithDisbursalDate = new ArrayList<LoanBO>();
        loanWithDisbursalDate.add(loan);
        CollectionSheetBO collSheet = new CollectionSheetBO();
        collSheet.addLoanDetailsForDisbursal(loanWithDisbursalDate);
        CollSheetCustBO collectionSheetCustomer = collSheet.getCollectionSheetCustomerForCustomerId(group
                .getCustomerId());
        Assert.assertNotNull(collectionSheetCustomer);
       Assert.assertEquals(collectionSheetCustomer.getLoanDetailsForAccntId(loan.getAccountId()).getTotalNoOfInstallments(),
                Short.valueOf("5"));
    }

    public void testPrdOfferingsCanCoexist() throws PersistenceException {
        LoanBO loan = (LoanBO) createLoanAccount();
       Assert.assertTrue(new ProductMixBusinessService().canProductsCoExist(loan.getLoanOffering(), loan.getLoanOffering()));
    }

    public void testLoanPerfObject() throws PersistenceException {
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        createInitialObjects();
        accountBO = getLoanAccount(client, meeting);
        LoanBO loanBO = (LoanBO) accountBO;
        LoanPerformanceHistoryEntity loanPerformanceHistory = loanBO.getPerformanceHistory();
        loanPerformanceHistory.setNoOfPayments(Integer.valueOf("3"));
        loanPerformanceHistory.setLoanMaturityDate(currentDate);
        TestObjectFactory.updateObject(loanBO);
        loanBO = (LoanBO) new AccountPersistence().getAccount(loanBO.getAccountId());
       Assert.assertEquals(Integer.valueOf("3"), loanBO.getPerformanceHistory().getNoOfPayments());
       Assert.assertEquals(currentDate, loanBO.getPerformanceHistory().getLoanMaturityDate());
    }

    /**
     * This test has been disabled until code is able to handle removing fees.
     * TODO: re-enable this test when loan schedule adjustments correctly handle
     * removing fees.
     */
    public void xtestSuccessRemoveFees() throws Exception {
        accountBO = getLoanAccount();
        UserContext uc = TestUtils.makeUser();
        Set<AccountFeesEntity> accountFeesEntitySet = accountBO.getAccountFees();
        ((LoanBO) accountBO).getLoanOffering().setPrinDueLastInst(false);
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                ((LoanScheduleEntity) accountActionDateEntity).setMiscFee(new Money("20.3"));
            }
        }
        Iterator<AccountFeesEntity> itr = accountFeesEntitySet.iterator();
        while (itr.hasNext()) {
            accountBO.removeFees(itr.next().getFees().getFeeId(), uc.getId());
        }

        StaticHibernateUtil.getTransaction().commit();
        for (AccountFeesEntity accountFeesEntity : accountFeesEntitySet) {
           Assert.assertEquals(accountFeesEntity.getFeeStatusAsEnum(), FeeStatus.INACTIVE);
        }
        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
        for (LoanActivityEntity accountNonTrxnEntity : ((LoanBO) accountBO).getLoanActivityDetails()) {
           Assert.assertEquals(loanSummaryEntity.getOriginalFees().subtract(loanSummaryEntity.getFeesPaid()),
                    accountNonTrxnEntity.getFeeOutstanding());
           Assert.assertEquals(loanSummaryEntity.getOriginalPrincipal().subtract(loanSummaryEntity.getPrincipalPaid()),
                    accountNonTrxnEntity.getPrincipalOutstanding());
           Assert.assertEquals(loanSummaryEntity.getOriginalInterest().subtract(loanSummaryEntity.getInterestPaid()),
                    accountNonTrxnEntity.getInterestOutstanding());
           Assert.assertEquals(loanSummaryEntity.getOriginalPenalty().subtract(loanSummaryEntity.getPenaltyPaid()),
                    accountNonTrxnEntity.getPenaltyOutstanding());
            break;
        }
        for (AccountActionDateEntity accountActionDate : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDate;
            if (accountActionDate.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(new Money("212.0"), loanScheduleEntity.getTotalDueWithFees());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("2"))) {
               Assert.assertEquals(new Money("133.0"), loanScheduleEntity.getTotalDueWithFees());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("6"))) {
               Assert.assertEquals(new Money("111.3"), loanScheduleEntity.getTotalDueWithFees());
            } else {
               Assert.assertEquals(new Money("112.0"), loanScheduleEntity.getTotalDueWithFees());
            }
        }

    }

    /*
     * TODO: turn back on when IntersetDeductedAtDisbursement is re-enabled
     * 
     * public void
     * testCreateLoanAccountWithIntersetDeductedAtDisbursementFailure() throws
     * Exception { createInitialCustomers(); MeetingBO meeting =
     * TestObjectFactory.createLoanMeeting(group
     * .getCustomerMeeting().getMeeting());
     * 
     * Date startDate = new Date(System.currentTimeMillis()); loanOffering =
     * TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS,
     * startDate, PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 1, InterestType.FLAT, true,
     * true, meeting); List<Date> meetingDates =
     * TestObjectFactory.getMeetingDates(meeting, 1); MifosCurrency currency =
     * TestObjectFactory.getCurrency(); try { LoanOfferingInstallmentRange
     * eligibleInstallmentRange =
     * loanOffering.getEligibleInstallmentSameForAllLoan();
     * LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_PARTIAL_APPLICATION, new Money(currency, "300.0"),
     * Short.valueOf("1"), meetingDates.get(0), true, 10.0, (short) 0, new
     * FundBO(), new ArrayList<FeeView>(), null, DEFAULT_LOAN_AMOUNT,
     * DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange.getMaxNoOfInstall(),
     * eligibleInstallmentRange.getMinNoOfInstall(), false, null);
     * 
     * Assert.fail(); } catch (AccountException e) {Assert.assertTrue(true); } }
     */

    public void testHandleArrearsAging_Create() throws Exception {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(DateUtils.getCurrentDateWithoutTimeStamp());
        calendar.add(calendar.WEEK_OF_MONTH, -2);
        java.sql.Date secondWeekDate = new java.sql.Date(calendar.getTimeInMillis());

        accountBO = getLoanAccount();
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(secondWeekDate);
            }
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        Assert.assertNull(((LoanBO) accountBO).getLoanArrearsAgingEntity());

        ((LoanBO) accountBO).handleArrearsAging();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        LoanArrearsAgingEntity agingEntity = ((LoanBO) accountBO).getLoanArrearsAgingEntity();

        Assert.assertNotNull(agingEntity);
       Assert.assertEquals(new Money("100"), agingEntity.getOverduePrincipal());
       Assert.assertEquals(new Money("12"), agingEntity.getOverdueInterest());
       Assert.assertEquals(new Money("112"), agingEntity.getOverdueBalance());
       Assert.assertEquals(Short.valueOf("14"), agingEntity.getDaysInArrears());

       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getPrincipalDue(), agingEntity.getUnpaidPrincipal());
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getInterestDue(), agingEntity.getUnpaidInterest());
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getPrincipalDue().add(
                ((LoanBO) accountBO).getLoanSummary().getInterestDue()), agingEntity.getUnpaidBalance());

       Assert.assertEquals(((LoanBO) accountBO).getTotalPrincipalAmountInArrears(), agingEntity.getOverduePrincipal());
       Assert.assertEquals(((LoanBO) accountBO).getTotalInterestAmountInArrears(), agingEntity.getOverdueInterest());
       Assert.assertEquals(((LoanBO) accountBO).getTotalPrincipalAmountInArrears().add(
                ((LoanBO) accountBO).getTotalInterestAmountInArrears()), agingEntity.getOverdueBalance());
    }

    public void testHandleArrearsAging_Update() throws Exception {
        testHandleArrearsAging_Create();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(DateUtils.getCurrentDateWithoutTimeStamp());
        calendar.add(calendar.WEEK_OF_MONTH, -1);
        java.sql.Date lastWeekDate = new java.sql.Date(calendar.getTimeInMillis());

        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 2) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
            }
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());

        Assert.assertNotNull(((LoanBO) accountBO).getLoanArrearsAgingEntity());

        ((LoanBO) accountBO).handleArrearsAging();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        LoanArrearsAgingEntity agingEntity = ((LoanBO) accountBO).getLoanArrearsAgingEntity();

       Assert.assertEquals(new Money("200"), agingEntity.getOverduePrincipal());
       Assert.assertEquals(new Money("24"), agingEntity.getOverdueInterest());
       Assert.assertEquals(new Money("224"), agingEntity.getOverdueBalance());
       Assert.assertEquals(Short.valueOf("14"), agingEntity.getDaysInArrears());

       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getPrincipalDue(), agingEntity.getUnpaidPrincipal());
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getInterestDue(), agingEntity.getUnpaidInterest());
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getPrincipalDue().add(
                ((LoanBO) accountBO).getLoanSummary().getInterestDue()), agingEntity.getUnpaidBalance());

       Assert.assertEquals(((LoanBO) accountBO).getTotalPrincipalAmountInArrears(), agingEntity.getOverduePrincipal());
       Assert.assertEquals(((LoanBO) accountBO).getTotalInterestAmountInArrears(), agingEntity.getOverdueInterest());
       Assert.assertEquals(((LoanBO) accountBO).getTotalPrincipalAmountInArrears().add(
                ((LoanBO) accountBO).getTotalInterestAmountInArrears()), agingEntity.getOverdueBalance());
    }

    public void testUpdateLoanForLogging() throws Exception {
        Date newDate = incrementCurrentDate(14);
        accountBO = getLoanAccount();
        accountBO.setUserContext(TestUtils.makeUser());
        StaticHibernateUtil.getInterceptor().createInitialValueMap(accountBO);

        LoanBO loanBO = (LoanBO) accountBO;
        ((LoanBO) accountBO).updateLoan(true, loanBO.getLoanAmount(), loanBO.getInterestRate(), loanBO
                .getNoOfInstallments(), newDate, (short) 2, TestObjectFactory.SAMPLE_BUSINESS_ACTIVITY_2, "Added note",
                null, null, false, null);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());

        List<AuditLog> auditLogList = TestObjectFactory.getChangeLog(EntityType.LOAN, accountBO.getAccountId());
        // TODO restore this
        //Assert.assertEquals(1, auditLogList.size());
        for (int auditLogListIndex = 0; auditLogListIndex < auditLogList.size(); auditLogListIndex++) {
            auditLogList.get(auditLogListIndex).getAuditLogRecords();
        }
       Assert.assertEquals(EntityType.LOAN, auditLogList.get(0).getEntityTypeAsEnum());
       Assert.assertEquals(3, auditLogList.get(0).getAuditLogRecords().size());
        for (AuditLogRecord auditLogRecord : auditLogList.get(0).getAuditLogRecords()) {
            if (auditLogRecord.getFieldName().equalsIgnoreCase("Collateral Notes")) {
               Assert.assertEquals("-", auditLogRecord.getOldValue());
               Assert.assertEquals("Added note", auditLogRecord.getNewValue());
            } else if (auditLogRecord.getFieldName().equalsIgnoreCase("Service Charge deducted At Disbursement")) {
               Assert.assertEquals("1", auditLogRecord.getOldValue());
               Assert.assertEquals("0", auditLogRecord.getNewValue());
            }
        }
        TestObjectFactory.cleanUpChangeLog();
    }

    public void testGetTotalRepayAmountForCurrentDateBeforeFirstInstallment() {
        accountBO = getLoanAccount();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        changeFirstInstallmentDateToNextDate(accountBO);
        Money totalRepaymentAmount = new Money();
        for (AccountActionDateEntity accountAction : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                totalRepaymentAmount = totalRepaymentAmount.add(accountActionDateEntity.getTotalDueWithFees());
            } else {
                totalRepaymentAmount = totalRepaymentAmount.add(accountActionDateEntity.getPrincipal());
            }
        }
       Assert.assertEquals(totalRepaymentAmount, ((LoanBO) accountBO).getTotalEarlyRepayAmount());
    }

    public void testGetTotalRepayAmountForCurrentDateSameAsInstallmentDate() {
        accountBO = getLoanAccount();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        Money totalRepaymentAmount = new Money();
        for (AccountActionDateEntity accountAction : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                totalRepaymentAmount = totalRepaymentAmount.add(accountActionDateEntity.getTotalDueWithFees());
            } else {
                totalRepaymentAmount = totalRepaymentAmount.add(accountActionDateEntity.getPrincipal());
            }
        }
       Assert.assertEquals(totalRepaymentAmount, ((LoanBO) accountBO).getTotalEarlyRepayAmount());
    }

    public void testGetTotalRepayAmountForCurrentDateLiesBetweenInstallmentDates() {
        accountBO = getLoanAccount();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        changeFirstInstallmentDate(accountBO);
        Money totalRepaymentAmount = new Money();
        for (AccountActionDateEntity accountAction : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))
                    || accountActionDateEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                totalRepaymentAmount = totalRepaymentAmount.add(accountActionDateEntity.getTotalDueWithFees());
            } else {
                totalRepaymentAmount = totalRepaymentAmount.add(accountActionDateEntity.getPrincipal());
            }
        }
       Assert.assertEquals(totalRepaymentAmount, ((LoanBO) accountBO).getTotalEarlyRepayAmount());
    }

    public void testMakeEarlyRepaymentForCurrentDateLiesBetweenInstallmentDates() throws Exception {
        accountBO = getLoanAccount();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        changeFirstInstallmentDate(accountBO);
        LoanBO loanBO = (LoanBO) accountBO;
        UserContext uc = TestUtils.makeUser();
        Money totalRepaymentAmount = loanBO.getTotalEarlyRepayAmount();
        loanBO.makeEarlyRepayment(loanBO.getTotalEarlyRepayAmount(), null, null, "1", uc.getId());

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());

       Assert.assertEquals(accountBO.getAccountState().getId(), Short.valueOf(AccountStates.LOANACC_OBLIGATIONSMET));
        LoanSummaryEntity loanSummaryEntity = loanBO.getLoanSummary();
       Assert.assertEquals(totalRepaymentAmount, loanSummaryEntity.getPrincipalPaid().add(loanSummaryEntity.getFeesPaid())
                .add(loanSummaryEntity.getInterestPaid()).add(loanSummaryEntity.getPenaltyPaid()));
       Assert.assertEquals(1, accountBO.getAccountPayments().size());
        // / Change this to more clearly separate what we are testing for from
        // the
        // machinery needed to get that data?
        //
        for (AccountPaymentEntity accountPaymentEntity : accountBO.getAccountPayments()) {
           Assert.assertEquals(6, accountPaymentEntity.getAccountTrxns().size());
            for (AccountTrxnEntity accountTrxnEntity : accountPaymentEntity.getAccountTrxns()) {
                if (accountTrxnEntity.getInstallmentId().equals(Short.valueOf("1"))
                        || accountTrxnEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                   Assert.assertEquals(new Money("212.0"), accountTrxnEntity.getAmount());
                    LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
                   Assert.assertEquals(new Money("100.0"), loanTrxnDetailEntity.getPrincipalAmount());
                   Assert.assertEquals(new Money("12.0"), loanTrxnDetailEntity.getInterestAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
                   Assert.assertEquals(6, accountTrxnEntity.getFinancialTransactions().size());
                   Assert.assertEquals(1, loanTrxnDetailEntity.getFeesTrxnDetails().size());
                    for (FeesTrxnDetailEntity feesTrxnDetailEntity : loanTrxnDetailEntity.getFeesTrxnDetails()) {
                       Assert.assertEquals(new Money("100.0"), feesTrxnDetailEntity.getFeeAmount());
                    }
                } else {
                    LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
                   Assert.assertEquals(new Money("100.0"), accountTrxnEntity.getAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getInterestAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
                   Assert.assertEquals(2, accountTrxnEntity.getFinancialTransactions().size());
                   Assert.assertEquals(0, loanTrxnDetailEntity.getFeesTrxnDetails().size());
                }
            }
        }
    }

    public void testMakeEarlyRepaymentForCurrentDateSameAsInstallmentDate() throws Exception {
        accountBO = getLoanAccount();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        LoanBO loanBO = (LoanBO) accountBO;
        UserContext uc = TestUtils.makeUser();
        Money totalRepaymentAmount = loanBO.getTotalEarlyRepayAmount();
        loanBO.makeEarlyRepayment(loanBO.getTotalEarlyRepayAmount(), null, null, "1", uc.getId());

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());

       Assert.assertEquals(accountBO.getAccountState().getId(), Short.valueOf(AccountStates.LOANACC_OBLIGATIONSMET));
        LoanSummaryEntity loanSummaryEntity = loanBO.getLoanSummary();
       Assert.assertEquals(totalRepaymentAmount, loanSummaryEntity.getPrincipalPaid().add(loanSummaryEntity.getFeesPaid())
                .add(loanSummaryEntity.getInterestPaid()).add(loanSummaryEntity.getPenaltyPaid()));
       Assert.assertEquals(1, accountBO.getAccountPayments().size());

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountPaymentEntity accountPaymentEntity : accountBO.getAccountPayments()) {
           Assert.assertEquals(6, accountPaymentEntity.getAccountTrxns().size());
            for (AccountTrxnEntity accountTrxnEntity : accountPaymentEntity.getAccountTrxns()) {
                if (accountTrxnEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                   Assert.assertEquals(new Money("212.0"), accountTrxnEntity.getAmount());
                    LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
                   Assert.assertEquals(new Money("100.0"), loanTrxnDetailEntity.getPrincipalAmount());
                   Assert.assertEquals(new Money("12.0"), loanTrxnDetailEntity.getInterestAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
                   Assert.assertEquals(6, accountTrxnEntity.getFinancialTransactions().size());
                   Assert.assertEquals(1, loanTrxnDetailEntity.getFeesTrxnDetails().size());
                    for (FeesTrxnDetailEntity feesTrxnDetailEntity : loanTrxnDetailEntity.getFeesTrxnDetails()) {
                       Assert.assertEquals(new Money("100.0"), feesTrxnDetailEntity.getFeeAmount());
                    }
                } else {
                    LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
                   Assert.assertEquals(new Money("100.0"), accountTrxnEntity.getAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getInterestAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
                   Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
                   Assert.assertEquals(2, accountTrxnEntity.getFinancialTransactions().size());
                   Assert.assertEquals(0, loanTrxnDetailEntity.getFeesTrxnDetails().size());
                }
            }
        }
    }

    public void testMakeEarlyRepaymentOnPartiallyPaidAccount() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = createOfferingNoPrincipalInLastInstallment(startDate, center.getCustomerMeeting()
                .getMeeting());
        UserContext userContext = TestUtils.makeUser();
        userContext.setLocaleId(null);
        List<FeeView> feeViewList = new ArrayList<FeeView>();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "100",
                RecurrenceType.WEEKLY, Short.valueOf("1"));
        feeViewList.add(new FeeView(userContext, periodicFee));
        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT);
        feeViewList.add(new FeeView(userContext, upfrontFee));
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        accountBO = LoanBO
                .createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                        new Money("300.0"), Short.valueOf("6"), startDate, false, 1.2, (short) 0, new FundBO(),
                        feeViewList, getCustomFields(), DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT,
                        eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(),
                        false, null);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());
       Assert.assertEquals(1, accountBO.getAccountCustomFields().size());
        StaticHibernateUtil.closeSession();

        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("160"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, startDate, startDate));

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(new Money(), ((LoanBO) accountBO).getTotalPaymentDue());
        LoanBO loanBO = (LoanBO) accountBO;
        UserContext uc = TestUtils.makeUser();
        Money totalRepaymentAmount = loanBO.getTotalEarlyRepayAmount();
       Assert.assertEquals(new Money("300.2"), totalRepaymentAmount);
        loanBO.makeEarlyRepayment(loanBO.getTotalEarlyRepayAmount(), null, null, "1", uc.getId());

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());

       Assert.assertEquals(accountBO.getAccountState().getId(), Short.valueOf(AccountStates.LOANACC_OBLIGATIONSMET));
        LoanSummaryEntity loanSummaryEntity = loanBO.getLoanSummary();
       Assert.assertEquals(new Money(), loanSummaryEntity.getPrincipalDue().add(loanSummaryEntity.getFeesDue()).add(
                loanSummaryEntity.getInterestDue()).add(loanSummaryEntity.getPenaltyDue()));
       Assert.assertEquals(2, accountBO.getAccountPayments().size());
        AccountPaymentEntity accountPaymentEntity = (AccountPaymentEntity) Arrays.asList(
                accountBO.getAccountPayments().toArray()).get(0);

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountTrxnEntity accountTrxnEntity : accountPaymentEntity.getAccountTrxns()) {
            if (accountTrxnEntity.getInstallmentId().equals(Short.valueOf("6"))) {
                LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
               Assert.assertEquals(new Money("46.0"), accountTrxnEntity.getAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getInterestAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
               Assert.assertEquals(2, accountTrxnEntity.getFinancialTransactions().size());
               Assert.assertEquals(0, loanTrxnDetailEntity.getFeesTrxnDetails().size());
            } else if (accountTrxnEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
               Assert.assertEquals(new Money("51.0"), accountTrxnEntity.getAmount());
               Assert.assertEquals(new Money("0.2"), loanTrxnDetailEntity.getInterestAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
               Assert.assertEquals(4, accountTrxnEntity.getFinancialTransactions().size());
               Assert.assertEquals(0, loanTrxnDetailEntity.getFeesTrxnDetails().size());
            } else {
                LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
               Assert.assertEquals(new Money("50.8"), accountTrxnEntity.getAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getInterestAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
               Assert.assertEquals(2, accountTrxnEntity.getFinancialTransactions().size());
               Assert.assertEquals(0, loanTrxnDetailEntity.getFeesTrxnDetails().size());
            }
        }
    }

    private LoanOfferingBO createOfferingNoPrincipalInLastInstallment(final Date startDate, final MeetingBO meeting) {
        return TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, startDate, PrdStatus.LOAN_ACTIVE,
                DEFAULT_LOAN_AMOUNT, 1.2, 3, InterestType.FLAT, meeting);
    }

    public void testApplyRoundingAfterAddMiscFee() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = createOfferingNoPrincipalInLastInstallment(startDate, center.getCustomerMeeting()
                .getMeeting());
        UserContext userContext = TestUtils.makeUser();
        userContext.setLocaleId(null);
        List<FeeView> feeViewList = new ArrayList<FeeView>();

        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "33",
                RecurrenceType.WEEKLY, Short.valueOf("1"));
        feeViewList.add(new FeeView(userContext, periodicFee));

        // 20% of 300 = 60.0, applied to first installment
        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT);
        feeViewList.add(new FeeView(userContext, upfrontFee));

        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        accountBO = LoanBO
                .createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                        new Money("300.0"), Short.valueOf("6"), startDate, false, 0.0, (short) 0, new FundBO(),
                        feeViewList, null, DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange
                                .getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(), false, null);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());

        /*
         * StaticHibernateUtil.closeSession();
         * 
         * accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(
         * AccountBO.class, accountBO.getAccountId());
         * accountBO.applyPaymentWithPersist(TestObjectFactory
         * .getLoanAccountPaymentData(null, new Money("180.1"), accountBO
         * .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1,
         * startDate, startDate));
         * 
         * StaticHibernateUtil.commitTransaction();
         * StaticHibernateUtil.closeSession(); accountBO = (AccountBO)
         * StaticHibernateUtil.getSessionTL().get( AccountBO.class,
         * accountBO.getAccountId());Assert.assertEquals(new Money(), ((LoanBO)
         * accountBO).getTotalPaymentDue());
         */
        LoanBO loanBO = (LoanBO) accountBO;
       Assert.assertEquals(new Money("300.0"), loanBO.getLoanAmount());
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipal());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("93.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                // periodic fee applies every 2nd week so no payments in even
                // weeks
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("3"))) {
                // pay periodic fee in 1st, 3rd, and 5th week
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("4"))) {
                // pay periodic fee in 1st, 3rd, and 5th week
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("5"))) {
                // pay periodic fee in 1st, 3rd, and 5th week
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("6"))) {
                // pay periodic fee in 1st, 3rd, and 5th week
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            }
        }

        accountBO.applyCharge(Short.valueOf(AccountConstants.MISC_FEES), new Double("10.0"));
        accountBO.applyCharge(Short.valueOf(AccountConstants.MISC_PENALTY), new Double("2"));

        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipal());
               Assert.assertEquals(new Money("10.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("93.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
               Assert.assertEquals(new Money("2.0"), loanScheduleEntity.getMiscPenaltyDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                // periodic fee applies every week so double fee payment (66)
                // due
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("3"))) {
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("4"))) {
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("5"))) {
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            } else if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("6"))) {
                // pay periodic fee in 1st, 3rd, and 5th week
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getPrincipalDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getMiscFeeDue());
               Assert.assertEquals(new Money("66.0"), loanScheduleEntity.getTotalFeeDue());
               Assert.assertEquals(new Money("0.0"), loanScheduleEntity.getInterestDue());
            }
        }

    }

    public void testMakeEarlyRepaymentOnPartiallyPaidPricipal() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = createOfferingNoPrincipalInLastInstallment(startDate, center.getCustomerMeeting()
                .getMeeting());
        UserContext userContext = TestUtils.makeUser();
        userContext.setLocaleId(null);
        List<FeeView> feeViewList = new ArrayList<FeeView>();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "100",
                RecurrenceType.WEEKLY, Short.valueOf("1"));
        feeViewList.add(new FeeView(userContext, periodicFee));
        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT);
        feeViewList.add(new FeeView(userContext, upfrontFee));
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        accountBO = LoanBO
                .createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                        new Money("300.0"), Short.valueOf("6"), startDate, false, 1.2, (short) 0, new FundBO(),
                        feeViewList, null, DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange
                                .getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(), false, null);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());

        StaticHibernateUtil.closeSession();

        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("180.1"),
                accountBO.getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, startDate, startDate));

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(new Money(), ((LoanBO) accountBO).getTotalPaymentDue());
        LoanBO loanBO = (LoanBO) accountBO;
        UserContext uc = TestUtils.makeUser();
        Money totalRepaymentAmount = loanBO.getTotalEarlyRepayAmount();
       Assert.assertEquals(new Money("280.1"), totalRepaymentAmount);
        loanBO.makeEarlyRepayment(loanBO.getTotalEarlyRepayAmount(), null, null, "1", uc.getId());

        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());

       Assert.assertEquals(accountBO.getAccountState().getId(), Short.valueOf(AccountStates.LOANACC_OBLIGATIONSMET));
        LoanSummaryEntity loanSummaryEntity = loanBO.getLoanSummary();
       Assert.assertEquals(new Money(), loanSummaryEntity.getPrincipalDue().add(loanSummaryEntity.getFeesDue()).add(
                loanSummaryEntity.getInterestDue()).add(loanSummaryEntity.getPenaltyDue()));
       Assert.assertEquals(2, accountBO.getAccountPayments().size());
        AccountPaymentEntity accountPaymentEntity = (AccountPaymentEntity) Arrays.asList(
                accountBO.getAccountPayments().toArray()).get(0);
       Assert.assertEquals(6, accountPaymentEntity.getAccountTrxns().size());

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountTrxnEntity accountTrxnEntity : accountPaymentEntity.getAccountTrxns()) {
            if (accountTrxnEntity.getInstallmentId().equals(Short.valueOf("6"))) {
                LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
               Assert.assertEquals(new Money("46.0"), accountTrxnEntity.getAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getInterestAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
               Assert.assertEquals(2, accountTrxnEntity.getFinancialTransactions().size());
               Assert.assertEquals(0, loanTrxnDetailEntity.getFeesTrxnDetails().size());
            } else if (accountTrxnEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
               Assert.assertEquals(new Money("30.9"), accountTrxnEntity.getAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getInterestAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
               Assert.assertEquals(2, accountTrxnEntity.getFinancialTransactions().size());
               Assert.assertEquals(0, loanTrxnDetailEntity.getFeesTrxnDetails().size());
            } else {
                LoanTrxnDetailEntity loanTrxnDetailEntity = (LoanTrxnDetailEntity) accountTrxnEntity;
               Assert.assertEquals(new Money("50.8"), accountTrxnEntity.getAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getInterestAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscFeeAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getMiscPenaltyAmount());
               Assert.assertEquals(new Money(), loanTrxnDetailEntity.getPenaltyAmount());
               Assert.assertEquals(2, accountTrxnEntity.getFinancialTransactions().size());
               Assert.assertEquals(0, loanTrxnDetailEntity.getFeesTrxnDetails().size());
            }
        }
    }

    public void testUpdateTotalFeeAmount() {
        Date startDate = new Date(System.currentTimeMillis());
        accountBO = getLoanAccount(AccountState.LOAN_APPROVED, startDate, 1);
        LoanBO loanBO = (LoanBO) accountBO;
        LoanSummaryEntity loanSummaryEntity = loanBO.getLoanSummary();
        Money orignalFeesAmount = loanSummaryEntity.getOriginalFees();
        loanBO.updateTotalFeeAmount(new Money("20"));
       Assert.assertEquals(loanSummaryEntity.getOriginalFees(), orignalFeesAmount.subtract(new Money("20")));
    }

    public void testDisburseLoanWithFeeAtDisbursement() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        Money miscFee = new Money("20");
        Money miscPenalty = new Money("50");
        accountBO = getLoanAccountWithMiscFeeAndPenalty(AccountState.LOAN_APPROVED, startDate, 1, miscFee, miscPenalty);

        // disburse loan

        ((LoanBO) accountBO).disburseLoan("1234", startDate, Short.valueOf("1"), accountBO.getPersonnel(), startDate,
                Short.valueOf("1"));
        Session session = StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.startTransaction();
        session.save(accountBO);
        StaticHibernateUtil.getTransaction().commit();

        for (AccountActionDateEntity accountAction : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(miscPenalty, accountActionDateEntity.getMiscPenalty());
               Assert.assertEquals(miscFee, accountActionDateEntity.getMiscFee());
                break;
            }
        }

        Set<AccountPaymentEntity> accountpayments = accountBO.getAccountPayments();
       Assert.assertEquals(1, accountpayments.size());
        for (AccountPaymentEntity entity : accountpayments) {

           Assert.assertEquals("1234", entity.getReceiptNumber());

            // asssert loan trxns

            Set<AccountTrxnEntity> accountTrxns = entity.getAccountTrxns();
           Assert.assertEquals(2, accountTrxns.size());
            for (AccountTrxnEntity transaction : accountTrxns) {

                if (transaction.getAccountAction() == AccountActionTypes.FEE_REPAYMENT) {
                   Assert.assertEquals(30.0, transaction.getAmount().getAmountDoubleValue(), DELTA);
                    // it should have two feetrxn's
                    Set<FeesTrxnDetailEntity> feesTrxnDetails = ((LoanTrxnDetailEntity) transaction)
                            .getFeesTrxnDetails();
                   Assert.assertEquals(2, feesTrxnDetails.size());
                }
                if (transaction.getAccountAction() == AccountActionTypes.DISBURSAL) {
                   Assert.assertEquals(300.0, transaction.getAmount().getAmountDoubleValue(), DELTA);
                }

               Assert.assertEquals(accountBO.getAccountId(), transaction.getAccount().getAccountId());

            }

            // check loan summary fee paid should be updated
            LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
           Assert.assertEquals(30.0, loanSummaryEntity.getFeesPaid().getAmountDoubleValue(), DELTA);
            // check the loan object for the disbursal date
           Assert.assertEquals(startDate, ((LoanBO) accountBO).getDisbursementDate());
            // check new account state
           Assert.assertEquals(AccountStates.LOANACC_ACTIVEINGOODSTANDING, ((LoanBO) accountBO).getAccountState().getId()
                    .shortValue());
        }
        LoanSummaryEntity loanSummary = ((LoanBO) accountBO).getLoanSummary();
        for (LoanActivityEntity loanActivityEntity : ((LoanBO) accountBO).getLoanActivityDetails()) {
            if (loanActivityEntity.getComments().equalsIgnoreCase("Loan Disbursal")) {
               Assert.assertEquals(loanSummary.getOriginalPrincipal(), loanActivityEntity.getPrincipalOutstanding());
               Assert.assertEquals(loanSummary.getOriginalFees(), loanActivityEntity.getFeeOutstanding());
                break;
            }
        }
        accountBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
    }

    public void testDisbursalLoanNoFeeOrInterestAtDisbursal() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        accountBO = getLoanAccount(AccountState.LOAN_APPROVED, startDate, 3);

        // disburse loan

        ((LoanBO) accountBO).disburseLoan("1234", startDate, Short.valueOf("1"), accountBO.getPersonnel(), startDate,
                Short.valueOf("1"));
        Session session = StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.startTransaction();
        session.save(accountBO);
        StaticHibernateUtil.getTransaction().commit();
        ((LoanBO) accountBO).setLoanMeeting(null);
        Set<AccountPaymentEntity> accountpayments = accountBO.getAccountPayments();
       Assert.assertEquals(1, accountpayments.size());
        for (AccountPaymentEntity entity : accountpayments) {

           Assert.assertEquals("1234", entity.getReceiptNumber());
            // asssert loan trxns
            Set<AccountTrxnEntity> accountTrxns = entity.getAccountTrxns();
           Assert.assertEquals(1, accountTrxns.size());
            AccountTrxnEntity accountTrxn = accountTrxns.iterator().next();
           Assert.assertEquals(AccountActionTypes.DISBURSAL, accountTrxn.getAccountAction());
           Assert.assertEquals(300.0, accountTrxn.getAmount().getAmountDoubleValue(), DELTA);
           Assert.assertEquals(accountBO.getAccountId(), accountTrxn.getAccount().getAccountId());
        }
    }

    /*
     * TODO: turn back on when IntersetDeductedAtDisbursement is re-enabled
     * 
     * 
     * public void testDisbursalLoanWithInterestDeductedAtDisbursal() throws
     * Exception { Date startDate = new Date(System.currentTimeMillis());
     * accountBO = getLoanAccount(AccountState.LOAN_APPROVED, startDate, 2); int
     * statusChangeHistorySize = accountBO.getAccountStatusChangeHistory()
     * .size();
     * 
     * ((LoanBO) accountBO).disburseLoan("1234", startDate, Short.valueOf("1"),
     * accountBO.getPersonnel(), startDate, Short .valueOf("1")); Session
     * session = StaticHibernateUtil.getSessionTL();
     * StaticHibernateUtil.startTransaction(); session.save(accountBO);
     * StaticHibernateUtil.getTransaction().commit(); ((LoanBO)
     * accountBO).setLoanMeeting(null); Set<AccountPaymentEntity>
     * accountpayments = accountBO .getAccountPayments();Assert.assertEquals(1,
     * accountpayments.size());
     * 
     * AccountPaymentEntity entity = accountpayments.iterator().next();
     *Assert.assertEquals("1234", entity.getReceiptNumber()); // asssert loan trxns
     * Set<AccountTrxnEntity> accountTrxns = entity.getAccountTrxns();
     *Assert.assertEquals(2, accountTrxns.size()); int finTransaction = 0; for
     * (AccountTrxnEntity transaction : accountTrxns) { finTransaction +=
     * transaction.getFinancialTransactions().size(); AccountActionTypes
     * accountAction = transaction.getAccountAction(); if (accountAction ==
     * AccountActionTypes.FEE_REPAYMENT) {Assert.assertEquals(40.0,
     * transaction.getAmount() .getAmountDoubleValue(), DELTA); // it should
     * have two feetrxn's Set<FeesTrxnDetailEntity> feesTrxnDetails =
     * ((LoanTrxnDetailEntity) transaction) .getFeesTrxnDetails();
     *Assert.assertEquals(2, feesTrxnDetails.size());
     * Assert.fail("This doesn't actually happen.  " +
     * "Is it a copy-paste from a test which doesn't apply here?"); } else if
     * (accountAction == AccountActionTypes.DISBURSAL) {Assert.assertEquals(300.0,
     * transaction.getAmount() .getAmountDoubleValue(), DELTA); } else if
     * (accountAction == AccountActionTypes.LOAN_REPAYMENT) {Assert.assertEquals(52.0,
     * transaction.getAmount() .getAmountDoubleValue(), DELTA); } else {
     * Assert.fail("unexpected account action " + accountAction.name()); }
     * 
     *Assert.assertEquals(accountBO.getAccountId(), transaction.getAccount()
     * .getAccountId()); }Assert.assertEquals(10, finTransaction); // check loan
     * summary fee paid should be updated
     * 
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();Assert.assertEquals(40.0, loanSummaryEntity.getFeesPaid()
     * .getAmountDoubleValue(), DELTA);
     * 
     * // check new account state
     *Assert.assertEquals(AccountStates.LOANACC_ACTIVEINGOODSTANDING, ((LoanBO)
     * accountBO).getAccountState().getId().shortValue());
     *Assert.assertEquals(statusChangeHistorySize + 1, accountBO
     * .getAccountStatusChangeHistory().size()); }
     */

    public void testDisbursalLoanRegeneteRepaymentScheduleWithInvalidDisbDate() throws Exception {

        // Allowing loan disbursal dates to become independent of meeting
        // schedules.
        Date startDate = new Date(System.currentTimeMillis());
        accountBO = getLoanAccount(AccountState.LOAN_APPROVED, startDate, 3);

        // disburse loan
        Calendar disbDate = new GregorianCalendar();
        disbDate.setTimeInMillis(startDate.getTime());
        disbDate.roll(Calendar.DATE, 1);
        Date disbursalDate = new Date(disbDate.getTimeInMillis());
        try {
            ((LoanBO) accountBO).disburseLoan("1234", disbursalDate, Short.valueOf("1"), accountBO.getPersonnel(),
                    startDate, Short.valueOf("1"));

           Assert.assertEquals(true, true);
        } catch (ApplicationException rse) {
           Assert.assertEquals(true, false);
        } finally {
            Session session = StaticHibernateUtil.getSessionTL();
            StaticHibernateUtil.startTransaction();
            ((LoanBO) accountBO).setLoanMeeting(null);
            session.update(accountBO);
            StaticHibernateUtil.getTransaction().commit();
        }
    }

    public void testDisbursalLoanRegeneteRepaymentSchedule() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        accountBO = getLoanAccountWithMiscFeeAndPenalty(AccountState.LOAN_APPROVED, startDate, 3, new Money("20"),
                new Money("30"));

        Set<AccountActionDateEntity> intallments = accountBO.getAccountActionDates();
        AccountActionDateEntity firstInstallment = null;
        for (AccountActionDateEntity entity : intallments) {
            if (entity.getInstallmentId().intValue() == 1) {
                firstInstallment = entity;
                break;
            }
        }
        Calendar disbursalDate = new GregorianCalendar();
        disbursalDate.setTimeInMillis(firstInstallment.getActionDate().getTime());
        Calendar cal = new GregorianCalendar(disbursalDate.get(Calendar.YEAR), disbursalDate.get(Calendar.MONTH),
                disbursalDate.get(Calendar.DATE), 0, 0);
        ((LoanBO) accountBO).disburseLoan("1234", cal.getTime(), Short.valueOf("1"), accountBO.getPersonnel(),
                startDate, Short.valueOf("1"));

        Set<AccountActionDateEntity> actionDateEntities = accountBO.getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkLoanScheduleEntity(null, null, null, "20", "30", null, null, null, null, null, paymentsArray[0]);

        Session session = StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.startTransaction();
        ((LoanBO) accountBO).setLoanMeeting(null);
        session.update(accountBO);
        StaticHibernateUtil.getTransaction().commit();
       Assert.assertEquals(true, true);
    }

    public void testGetTotalAmountDueForCurrentDateMeeting() {
        accountBO = getLoanAccount();
       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountDue().getAmountDoubleValue(), 212.0);
    }

    public void testGetTotalAmountDueForSingleInstallment() throws Exception {
        accountBO = getLoanAccount();

        AccountActionDateEntity accountActionDateEntity = accountBO.getAccountActionDate((short) 1);
        ((LoanScheduleEntity) accountActionDateEntity).setActionDate(offSetCurrentDate(1));

        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountDue().getAmountDoubleValue(), 424.0);
    }

    public void testGetTotalAmountDueWithPayment() throws Exception {
        accountBO = getLoanAccount();

        LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountBO.getAccountActionDate((short) 1);

        accountActionDateEntity.setPrincipalPaid(new Money("20.0"));
        accountActionDateEntity.setInterestPaid(new Money("10.0"));
        accountActionDateEntity.setPenaltyPaid(new Money("5.0"));
        accountActionDateEntity.setActionDate(offSetCurrentDate(1));
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountDue().getAmountDoubleValue(), 389.0);
    }

    public void testGetTotalAmountDueWithPaymentDone() throws Exception {
        accountBO = getLoanAccount();

        AccountActionDateEntity accountActionDateEntity = accountBO.getAccountActionDate((short) 1);
        ((LoanScheduleEntity) accountActionDateEntity).setActionDate(offSetCurrentDate(1));
        ((LoanScheduleEntity) accountActionDateEntity).setPaymentStatus(PaymentStatus.PAID);

        accountBO = saveAndFetch(accountBO);

       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountDue().getAmountDoubleValue(), 212.0);
    }

    public void testGetTotalAmountDueForTwoInstallments() throws Exception {
        accountBO = getLoanAccount();
        AccountActionDateEntity accountActionDateEntity = accountBO.getAccountActionDate((short) 1);
        ((LoanScheduleEntity) accountActionDateEntity).setActionDate(offSetCurrentDate(2));
        AccountActionDateEntity accountActionDateEntity2 = accountBO.getAccountActionDate((short) 2);

        ((LoanScheduleEntity) accountActionDateEntity2).setActionDate(offSetCurrentDate(1));

        accountBO = saveAndFetch(accountBO);

       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountDue().getAmountDoubleValue(), 636.0);
    }

    public void testGetOustandingBalance() {
        accountBO = getLoanAccount();
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getOustandingBalance().getAmountDoubleValue(), 336.0);
    }

    public void testGetOustandingBalancewithPayment() throws Exception {
        accountBO = getLoanAccount();
        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();

        loanSummaryEntity.setPrincipalPaid(new Money("20.0"));
        loanSummaryEntity.setInterestPaid(new Money("10.0"));

        accountBO = saveAndFetch(accountBO);

       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getOustandingBalance().getAmountDoubleValue(), 306.0);
    }

    public void testGetNextMeetingDateAsCurrentDate() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        accountBO = TestObjectFactory.createLoanAccount("42423142341", group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Date(System.currentTimeMillis()), loanOffering);
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day);
        accountBO = accountPersistence.getAccount(accountBO.getAccountId());
       Assert.assertEquals(((LoanBO) accountBO).getNextMeetingDate().toString(), new java.sql.Date(currentDateCalendar
                .getTimeInMillis()).toString());
    }

    public void testGetNextMeetingDateAsFutureDate() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        accountBO = TestObjectFactory.createLoanAccount("42423142341", group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Date(System.currentTimeMillis()), loanOffering);
        AccountActionDateEntity accountActionDateEntity = accountBO.getAccountActionDate(Short.valueOf("1"));
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - 1);
        ((LoanScheduleEntity) accountActionDateEntity).setActionDate(new java.sql.Date(currentDateCalendar
                .getTimeInMillis()));
        TestObjectFactory.updateObject(accountBO);

        accountBO = accountPersistence.getAccount(accountBO.getAccountId());
       Assert.assertEquals(((LoanBO) accountBO).getNextMeetingDate().toString(), accountBO.getAccountActionDate(
                Short.valueOf("2")).getActionDate().toString());
    }

    public void testGetTotalAmountInArrearsForCurrentDateMeeting() {
        accountBO = getLoanAccount();
       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountInArrears().getAmountDoubleValue(), 0.0);
    }

    public void testGetTotalAmountInArrearsForSingleInstallmentDue() throws Exception {
        accountBO = getLoanAccount();
        AccountActionDateEntity accountActionDateEntity = accountBO.getAccountActionDate((short) 1);
        ((LoanScheduleEntity) accountActionDateEntity).setActionDate(offSetCurrentDate(1));
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountInArrears().getAmountDoubleValue(), 212.0);
    }

    public void testGetTotalAmountInArrearsWithPayment() throws Exception {
        accountBO = getLoanAccount();

        LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountBO.getAccountActionDate((short) 1);
        accountActionDateEntity.setPrincipalPaid(new Money("20.0"));
        accountActionDateEntity.setInterestPaid(new Money("10.0"));
        accountActionDateEntity.setPenaltyPaid(new Money("5.0"));
        accountActionDateEntity.setActionDate(offSetCurrentDate(1));

        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountInArrears().getAmountDoubleValue(), 177.0);
    }

    public void testGetTotalAmountInArrearsWithPaymentDone() throws Exception {
        accountBO = getLoanAccount();

        AccountActionDateEntity accountActionDateEntity = accountBO.getAccountActionDate(Short.valueOf("1"));
        ((LoanScheduleEntity) accountActionDateEntity).setActionDate(offSetCurrentDate(1));
        ((LoanScheduleEntity) accountActionDateEntity).setPaymentStatus(PaymentStatus.PAID);
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountInArrears().getAmountDoubleValue(), 0.0);
    }

    public void testGetTotalAmountDueForTwoInstallmentsDue() throws Exception {
        accountBO = getLoanAccount();

        AccountActionDateEntity accountActionDateEntity = accountBO.getAccountActionDate((short) 1);
        ((LoanScheduleEntity) accountActionDateEntity).setActionDate(offSetCurrentDate(2));
        AccountActionDateEntity accountActionDateEntity2 = accountBO.getAccountActionDate((short) 2);
        ((LoanScheduleEntity) accountActionDateEntity2).setActionDate(offSetCurrentDate(1));

        accountBO = saveAndFetch(accountBO);

       Assert.assertEquals(((LoanBO) accountBO).getTotalAmountInArrears().getAmountDoubleValue(), 424.0);
    }

    public void testChangedStatusForLastInstallmentPaid() throws Exception {
        accountBO = getLoanAccount();
        Date startDate = new Date(System.currentTimeMillis());
        java.sql.Date offSetDate = offSetCurrentDate(1);
        for (AccountActionDateEntity accountAction : accountBO.getAccountActionDates()) {
            ((LoanScheduleEntity) accountAction).setActionDate(offSetDate);
        }
        accountBO = saveAndFetch(accountBO);

        List<AccountActionDateEntity> accountActions = new ArrayList<AccountActionDateEntity>();
        accountActions.addAll(accountBO.getAccountActionDates());

        PaymentData paymentData = TestObjectFactory.getLoanAccountPaymentData(accountActions, new Money(Configuration
                .getInstance().getSystemConfig().getCurrency(), "1272.0"), null, accountBO.getPersonnel(), "5435345",
                Short.valueOf("1"), startDate, startDate);

        accountBO.applyPaymentWithPersist(paymentData);
       Assert.assertEquals("When Last installment is paid the status has been changed to closed", ((LoanBO) accountBO)
                .getAccountState().getId().toString(), String.valueOf(AccountStates.LOANACC_OBLIGATIONSMET));
        accountBO.getAccountPayments().clear();
    }

    public void testHandleArrears() throws AccountException {
        accountBO = getLoanAccount();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        int statusChangeHistorySize = accountBO.getAccountStatusChangeHistory().size();

        ((LoanBO) accountBO).handleArrears();
       Assert.assertEquals(Short.valueOf(AccountStates.LOANACC_BADSTANDING), accountBO.getAccountState().getId());
       Assert.assertEquals(statusChangeHistorySize + 1, accountBO.getAccountStatusChangeHistory().size());
    }

    public void testChangedStatusOnPayment() throws Exception {
        accountBO = getLoanAccount();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        ((LoanBO) accountBO).handleArrears();
       Assert.assertEquals("The status of account before payment should be active in bad standing", Short
                .valueOf(AccountStates.LOANACC_BADSTANDING), accountBO.getAccountState().getId());
        accountBO = applyPaymentandRetrieveAccount();
       Assert.assertEquals("The status of account after payment should be active in good standing", Short
                .valueOf(AccountStates.LOANACC_ACTIVEINGOODSTANDING), accountBO.getAccountState().getId());
    }

    public void testIsInterestDeductedAtDisbursement() {
        accountBO = getLoanAccount();
        LoanBO loan = (LoanBO) accountBO;
       Assert.assertEquals(false, loan.isInterestDeductedAtDisbursement());
        loan.setInterestDeductedAtDisbursement(true);
       Assert.assertEquals(true, loan.isInterestDeductedAtDisbursement());

    }

    public void testWtiteOff() throws Exception {
        accountBO = getLoanAccount();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        LoanBO loanBO = (LoanBO) accountBO;
        UserContext uc = TestUtils.makeUser();
        loanBO.setUserContext(uc);
        loanBO.writeOff();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        LoanBO loan = (LoanBO) accountBO;
        for (LoanActivityEntity loanActivityEntity : loan.getLoanActivityDetails()) {
           Assert.assertEquals(loanActivityEntity.getComments(), "Loan Written Off");
           Assert.assertEquals(new Timestamp(DateUtils.getCurrentDateWithoutTimeStamp().getTime()), loanActivityEntity
                    .getTrxnCreatedDate());
            break;
        }
    }

    /*
     * TODO: it seems unclear what this test is testing and whether or not the
     * loan is being set up correctly. Originally it was created with the
     * InterestDueAtDisbursement flag on, but now with that flag turned off, the
     * result returned is 102.5 According to the setup method, the disbursal
     * type parameter which was originally "2" should probably be "1" now.
     * 
     * 
     * public void testGetAmountTobePaidAtdisburtail() throws Exception { Date
     * startDate = new Date(System.currentTimeMillis()); accountBO =
     * getLoanAccount(AccountState.LOAN_APPROVED, startDate, 2);
     *Assert.assertEquals(new Money("52"), ((LoanBO) accountBO)
     * .getAmountTobePaidAtdisburtail(startDate)); }
     */

    public void testWaivePenaltyChargeDue() throws Exception {
        accountBO = getLoanAccount();
        for (AccountActionDateEntity accountAction : ((LoanBO) accountBO).getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                accountActionDateEntity.setMiscPenalty(new Money("100"));
            }
        }
        ((LoanBO) accountBO).getLoanSummary().setOriginalPenalty(new Money("100"));
        TestObjectFactory.updateObject(accountBO);

        StaticHibernateUtil.closeSession();
        LoanBO loanBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());

        UserContext userContext = TestUtils.makeUser();
        loanBO.setUserContext(userContext);
        loanBO.waiveAmountDue(WaiveEnum.PENALTY);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        loanBO = TestObjectFactory.getObject(LoanBO.class, loanBO.getAccountId());
        for (AccountActionDateEntity accountAction : loanBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(new Money(), accountActionDateEntity.getMiscPenalty());
            }
        }

        Set<LoanActivityEntity> loanActivityDetailsSet = loanBO.getLoanActivityDetails();
        List<Object> objectList = Arrays.asList(loanActivityDetailsSet.toArray());
        LoanActivityEntity loanActivityEntity = (LoanActivityEntity) objectList.get(0);
       Assert.assertEquals(LoanConstants.PENALTY_WAIVED, loanActivityEntity.getComments());
       Assert.assertEquals(new Money("100"), loanActivityEntity.getPenalty());
       Assert.assertEquals(new Timestamp(DateUtils.getCurrentDateWithoutTimeStamp().getTime()), loanActivityEntity
                .getTrxnCreatedDate());
       Assert.assertEquals(loanBO.getLoanSummary().getOriginalPenalty().subtract(loanBO.getLoanSummary().getPenaltyPaid()),
                loanActivityEntity.getPenaltyOutstanding());
    }

    public void testWaivePenaltyOverDue() throws Exception {
        accountBO = getLoanAccount();

        for (AccountActionDateEntity installment : ((LoanBO) accountBO).getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) installment;
            if (installment.getInstallmentId().intValue() == 1) {
                loanScheduleEntity.setMiscPenalty(new Money("100"));
            } else if (installment.getInstallmentId().intValue() == 2) {
                loanScheduleEntity.setMiscPenalty(new Money("100"));
            }
        }

        ((LoanBO) accountBO).getLoanSummary().setOriginalPenalty(new Money("200"));
        TestObjectFactory.updateObject(accountBO);

        StaticHibernateUtil.closeSession();
        LoanBO loanBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(DateUtils.getCurrentDateWithoutTimeStamp());
        calendar.add(calendar.WEEK_OF_MONTH, -1);
        java.sql.Date lastWeekDate = new java.sql.Date(calendar.getTimeInMillis());

        Calendar date = new GregorianCalendar();
        date.setTime(DateUtils.getCurrentDateWithoutTimeStamp());
        date.add(date.WEEK_OF_MONTH, -2);
        java.sql.Date twoWeeksBeforeDate = new java.sql.Date(date.getTimeInMillis());

        for (AccountActionDateEntity installment : loanBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
            } else if (installment.getInstallmentId().intValue() == 2) {
                ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
            }
        }
        TestObjectFactory.updateObject(loanBO);
        StaticHibernateUtil.closeSession();
        loanBO = TestObjectFactory.getObject(LoanBO.class, loanBO.getAccountId());
        UserContext userContext = TestUtils.makeUser();
        loanBO.setUserContext(userContext);
        loanBO.waiveAmountOverDue(WaiveEnum.PENALTY);
        TestObjectFactory.flushandCloseSession();
        loanBO = TestObjectFactory.getObject(LoanBO.class, loanBO.getAccountId());
        for (AccountActionDateEntity accountAction : loanBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
           Assert.assertEquals(new Money(), accountActionDateEntity.getMiscPenalty());
        }

        Set<LoanActivityEntity> loanActivityDetailsSet = loanBO.getLoanActivityDetails();
        List<Object> objectList = Arrays.asList(loanActivityDetailsSet.toArray());
        LoanActivityEntity loanActivityEntity = (LoanActivityEntity) objectList.get(0);
       Assert.assertEquals(new Timestamp(DateUtils.getCurrentDateWithoutTimeStamp().getTime()), loanActivityEntity
                .getTrxnCreatedDate());
       Assert.assertEquals(new Money("200"), loanActivityEntity.getPenalty());
       Assert.assertEquals(loanBO.getLoanSummary().getOriginalPenalty().subtract(loanBO.getLoanSummary().getPenaltyPaid()),
                loanActivityEntity.getPenaltyOutstanding());
    }

    public void testWaiveFeeChargeDue() throws Exception {
        accountBO = getLoanAccount();
        StaticHibernateUtil.closeSession();
        LoanBO loanBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
        UserContext userContext = TestUtils.makeUser();
        loanBO.setUserContext(userContext);
        loanBO.waiveAmountDue(WaiveEnum.FEES);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        loanBO = TestObjectFactory.getObject(LoanBO.class, loanBO.getAccountId());

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountActionDateEntity accountAction : loanBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : accountActionDateEntity
                    .getAccountFeesActionDetails()) {

                if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                   Assert.assertEquals(new Money(), accountFeesActionDetailEntity.getFeeAmount());
                } else {
                   Assert.assertEquals(new Money("100"), accountFeesActionDetailEntity.getFeeAmount());
                }
            }
        }

        Set<LoanActivityEntity> loanActivityDetailsSet = loanBO.getLoanActivityDetails();
        List<Object> objectList = Arrays.asList(loanActivityDetailsSet.toArray());
        LoanActivityEntity loanActivityEntity = (LoanActivityEntity) objectList.get(0);
       Assert.assertEquals(LoanConstants.FEE_WAIVED, loanActivityEntity.getComments());
       Assert.assertEquals(new Money("100"), loanActivityEntity.getFee());
       Assert.assertEquals(new Timestamp(DateUtils.getCurrentDateWithoutTimeStamp().getTime()), loanActivityEntity
                .getTrxnCreatedDate());
       Assert.assertEquals(loanBO.getLoanSummary().getOriginalFees().subtract(loanBO.getLoanSummary().getFeesPaid()),
                loanActivityEntity.getFeeOutstanding());

    }

    public void testWaiveFeeChargeOverDue() throws Exception {
        accountBO = getLoanAccount();
        StaticHibernateUtil.closeSession();
        LoanBO loanBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(DateUtils.getCurrentDateWithoutTimeStamp());
        calendar.add(calendar.WEEK_OF_MONTH, -1);
        java.sql.Date lastWeekDate = new java.sql.Date(calendar.getTimeInMillis());

        Calendar date = new GregorianCalendar();
        date.setTime(DateUtils.getCurrentDateWithoutTimeStamp());
        date.add(date.WEEK_OF_MONTH, -2);
        java.sql.Date twoWeeksBeforeDate = new java.sql.Date(date.getTimeInMillis());

        for (AccountActionDateEntity installment : loanBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
            } else if (installment.getInstallmentId().intValue() == 2) {
                ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
            }
        }
        TestObjectFactory.updateObject(loanBO);
        StaticHibernateUtil.closeSession();
        loanBO = TestObjectFactory.getObject(LoanBO.class, loanBO.getAccountId());
        UserContext userContext = TestUtils.makeUser();
        loanBO.setUserContext(userContext);
        loanBO.waiveAmountOverDue(WaiveEnum.FEES);
        TestObjectFactory.flushandCloseSession();
        loanBO = TestObjectFactory.getObject(LoanBO.class, loanBO.getAccountId());

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountActionDateEntity accountAction : loanBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : accountActionDateEntity
                    .getAccountFeesActionDetails()) {
                if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))
                        || accountActionDateEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                   Assert.assertEquals(new Money(), accountFeesActionDetailEntity.getFeeAmount());
                } else {
                   Assert.assertEquals(new Money("100"), accountFeesActionDetailEntity.getFeeAmount());
                }
            }
        }

        Set<LoanActivityEntity> loanActivityDetailsSet = loanBO.getLoanActivityDetails();
        List<Object> objectList = Arrays.asList(loanActivityDetailsSet.toArray());
        LoanActivityEntity loanActivityEntity = (LoanActivityEntity) objectList.get(0);
       Assert.assertEquals(new Money("200"), loanActivityEntity.getFee());
       Assert.assertEquals(new Timestamp(DateUtils.getCurrentDateWithoutTimeStamp().getTime()), loanActivityEntity
                .getTrxnCreatedDate());
       Assert.assertEquals(loanBO.getLoanSummary().getOriginalFees().subtract(loanBO.getLoanSummary().getFeesPaid()),
                loanActivityEntity.getFeeOutstanding());
    }

    public void testRegenerateFutureInstallments() throws Exception {
        accountBO = getLoanAccount();
        accountBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
        AccountActionDateEntity accountActionDateEntity = accountBO.getDetailsOfNextInstallment();
        MeetingBO meeting = accountBO.getCustomer().getCustomerMeeting().getMeeting();
        meeting.getMeetingDetails().getMeetingRecurrence().setWeekDay(WeekDay.THURSDAY);
        meeting.setMeetingStartDate(accountActionDateEntity.getActionDate());
        List<java.util.Date> meetingDates = meeting.getAllDates(6);
        ((LoanBO) accountBO).regenerateFutureInstallments(Short.valueOf("3"));
        ((LoanBO) accountBO).update();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(LoanBO.class, accountBO.getAccountId());

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountActionDateEntity actionDateEntity : accountBO.getAccountActionDates()) {
            if (actionDateEntity.getInstallmentId() < 3) {
               Assert.assertEquals(
                        DateUtils.getCurrentPlusWeeksDateWithoutTimeStamp(actionDateEntity.getInstallmentId() - 1),
                        DateUtils.getDateWithoutTimeStamp(actionDateEntity.getActionDate()));
            } else {
               Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(meetingDates
                        .get(actionDateEntity.getInstallmentId() - 1)), DateUtils
                        .getDateWithoutTimeStamp(actionDateEntity.getActionDate()));
            }
        }
    }

    /*
     * This test was failing if run on a Saturday because it used meetings based
     * on the current system time. By ensuring the meetings occur on a Monday
     * the problem is resolved.
     */
    public void testRegenerateFutureWhenDayScheduleChanges() throws Exception {
        // create customers with meetings on Monday
        MeetingBO customerMeeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeeting(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING, WeekDay.MONDAY));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", customerMeeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);

        MeetingBO loanOfferingMeeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeeting(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING, WeekDay.MONDAY));

        Date startDate = TestUtils.generateNearestMondayOnOrAfterToday();

        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, loanOfferingMeeting);
        accountBO = TestObjectFactory.createLoanAccount("42423142341", group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, startDate, loanOffering);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
        AccountActionDateEntity accountActionDateEntity = accountBO.getDetailsOfNextInstallment();
        MeetingBO meeting = accountBO.getCustomer().getCustomerMeeting().getMeeting();

        MeetingRecurrenceEntity recurrence = meeting.getMeetingDetails().getMeetingRecurrence();
        recurrence.setWeekDay(recurrence.getWeekDayValue().next());

        meeting.setMeetingStartDate(accountActionDateEntity.getActionDate());
        ((LoanBO) accountBO).regenerateFutureInstallments((short) (accountActionDateEntity.getInstallmentId() + 1));
        ((LoanBO) accountBO).update();
        StaticHibernateUtil.commitTransaction();
        TestObjectFactory.flushandCloseSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(LoanBO.class, accountBO.getAccountId());
       Assert.assertEquals(Short.valueOf("1"), accountBO.getCustomer().getCustomerMeeting().getMeeting().getMeetingDetails()
                .getRecurAfter());
       Assert.assertEquals(Short.valueOf("2"), ((LoanBO) accountBO).getLoanOffering().getLoanOfferingMeeting().getMeeting()
                .getMeetingDetails().getRecurAfter());
        for (AccountActionDateEntity actionDateEntity : accountBO.getAccountActionDates()) {
            if (actionDateEntity.getInstallmentId() == 1) {
               Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(startDate), DateUtils
                        .getDateWithoutTimeStamp(actionDateEntity.getActionDate()));
            }
        }
    }

    public void testRegenerateFutureInstallmentsWithCancelState() throws Exception {
        accountBO = getLoanAccount();
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(LoanBO.class, accountBO.getAccountId());
        java.sql.Date intallment2ndDate = null;
        java.sql.Date intallment3ndDate = null;
        for (AccountActionDateEntity actionDateEntity : accountBO.getAccountActionDates()) {
            if (actionDateEntity.getInstallmentId().equals(Short.valueOf("2"))) {
                intallment2ndDate = actionDateEntity.getActionDate();
            } else if (actionDateEntity.getInstallmentId().equals(Short.valueOf("3"))) {
                intallment3ndDate = actionDateEntity.getActionDate();
            }
        }
        AccountActionDateEntity accountActionDateEntity = accountBO.getDetailsOfNextInstallment();
        MeetingBO meeting = accountBO.getCustomer().getCustomerMeeting().getMeeting();
        meeting.getMeetingDetails().setRecurAfter(Short.valueOf("2"));
        meeting.setMeetingStartDate(accountActionDateEntity.getActionDate());
        accountBO.setUserContext(TestObjectFactory.getContext());
        accountBO.changeStatus(AccountState.LOAN_CANCELLED, null, "");
        ((LoanBO) accountBO).regenerateFutureInstallments((short) (accountActionDateEntity.getInstallmentId()
                .intValue() + 1));
        StaticHibernateUtil.commitTransaction();
        TestObjectFactory.flushandCloseSession();
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(LoanBO.class, accountBO.getAccountId());

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountActionDateEntity actionDateEntity : accountBO.getAccountActionDates()) {
            if (actionDateEntity.getInstallmentId().equals(Short.valueOf("2"))) {
               Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(intallment2ndDate.getTime()), DateUtils
                        .getDateWithoutTimeStamp(actionDateEntity.getActionDate().getTime()));
            } else if (actionDateEntity.getInstallmentId().equals(Short.valueOf("3"))) {
               Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(intallment3ndDate.getTime()), DateUtils
                        .getDateWithoutTimeStamp(actionDateEntity.getActionDate().getTime()));
            }
        }
    }

    public void testHasPortfolioAtRisk() {
        accountBO = getLoanAccount();
        Assert.assertFalse(((LoanBO) accountBO).hasPortfolioAtRisk());
        changeFirstInstallmentDate(accountBO, 31);
       Assert.assertTrue(((LoanBO) accountBO).hasPortfolioAtRisk());
    }

    public void testGetRemainingPrincipalAmount() throws AccountException, SystemException {
        accountBO = getLoanAccount();
        Date currentDate = new Date(System.currentTimeMillis());
        LoanBO loan = (LoanBO) accountBO;
        List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
        accntActionDates.addAll(loan.getAccountActionDates());
        PaymentData paymentData = TestObjectFactory.getLoanAccountPaymentData(accntActionDates, TestObjectFactory
                .getMoneyForMFICurrency(700), null, accountBO.getPersonnel(), "receiptNum", Short.valueOf("1"),
                currentDate, currentDate);
        loan.applyPaymentWithPersist(paymentData);

        TestObjectFactory.updateObject(loan);
        TestObjectFactory.flushandCloseSession();
       Assert.assertEquals("The amount returned for the payment should have been 700", 700.0, loan.getLastPmntAmnt());
        accountBO = TestObjectFactory.getObject(AccountBO.class, loan.getAccountId());
        loan = (LoanBO) accountBO;
        loan.getLoanSummary().getOriginalPrincipal().subtract(loan.getLoanSummary().getPrincipalPaid());
       Assert.assertEquals(loan.getRemainingPrincipalAmount(), loan.getLoanSummary().getOriginalPrincipal().subtract(
                loan.getLoanSummary().getPrincipalPaid()));
    }

    public void testIsAccountActive() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        accountBO = getLoanAccount(AccountState.LOAN_APPROVED, startDate, 3);
        Assert.assertFalse(((LoanBO) accountBO).isAccountActive());

        // disburse loan

        ((LoanBO) accountBO).disburseLoan("1234", startDate, Short.valueOf("1"), accountBO.getPersonnel(), startDate,
                Short.valueOf("1"));
        Session session = StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.startTransaction();
        session.save(accountBO);
        StaticHibernateUtil.getTransaction().commit();
        ((LoanBO) accountBO).setLoanMeeting(null);
        Set<AccountPaymentEntity> accountpayments = accountBO.getAccountPayments();
       Assert.assertEquals(1, accountpayments.size());
        for (AccountPaymentEntity entity : accountpayments) {
           Assert.assertEquals("1234", entity.getReceiptNumber());
            // asssert loan trxns
            Set<AccountTrxnEntity> accountTrxns = entity.getAccountTrxns();
           Assert.assertEquals(1, accountTrxns.size());
            AccountTrxnEntity transaction = accountTrxns.iterator().next();
           Assert.assertEquals(AccountActionTypes.DISBURSAL, transaction.getAccountAction());
           Assert.assertEquals(300.0, transaction.getAmount().getAmountDoubleValue(), DELTA);
           Assert.assertEquals(accountBO.getAccountId(), transaction.getAccount().getAccountId());
        }
       Assert.assertTrue(((LoanBO) accountBO).isAccountActive());
    }

    public void testGetNoOfBackDatedPayments() throws AccountException, SystemException {
        accountBO = getLoanAccount();
        accountBO.setUserContext(TestObjectFactory.getContext());
        accountBO.changeStatus(AccountState.LOAN_ACTIVE_IN_BAD_STANDING, null, "");
        changeInstallmentDate(accountBO, 14, Short.valueOf("1"));
        changeInstallmentDate(accountBO, 14, Short.valueOf("2"));
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());

        Date startDate = new Date(System.currentTimeMillis());
        PaymentData paymentData = PaymentData.createPaymentData(new Money(Configuration.getInstance().getSystemConfig()
                .getCurrency(), "212.0"), accountBO.getPersonnel(), Short.valueOf("1"), startDate);
        paymentData.setReceiptDate(startDate);
        paymentData.setReceiptNum("5435345");

        accountBO.applyPaymentWithPersist(paymentData);
        TestObjectFactory.flushandCloseSession();

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());

       Assert.assertEquals(Integer.valueOf("2"), ((LoanBO) accountBO).getPerformanceHistory().getTotalNoOfMissedPayments());
    }

    public void testGetTotalRepayAmountForCustomerPerfHistory() throws Exception {
        accountBO = getLoanAccountWithPerformanceHistory();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());

        changeFirstInstallmentDate(accountBO);
        LoanBO loanBO = (LoanBO) accountBO;
        UserContext uc = TestUtils.makeUser();
        Money totalRepaymentAmount = loanBO.getTotalEarlyRepayAmount();

        Integer noOfActiveLoans = ((ClientPerformanceHistoryEntity) ((LoanBO) accountBO).getCustomer()
                .getPerformanceHistory()).getNoOfActiveLoans();

        LoanPerformanceHistoryEntity loanPerfHistory = ((LoanBO) accountBO).getPerformanceHistory();
        Integer noOfPayments = loanPerfHistory.getNoOfPayments();
        loanBO.makeEarlyRepayment(loanBO.getTotalEarlyRepayAmount(), null, null, "1", uc.getId());
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        loanPerfHistory = ((LoanBO) accountBO).getPerformanceHistory();
       Assert.assertEquals(noOfPayments + 1, loanPerfHistory.getNoOfPayments().intValue());

        ClientPerformanceHistoryEntity clientPerfHistory = (ClientPerformanceHistoryEntity) ((LoanBO) accountBO)
                .getCustomer().getPerformanceHistory();

        /*
         * customerPerformanceHistory = ((LoanBO)
         * accountBO).getCustomer().getPerformanceHistory(); // TODO: The
         * following checks and 'hacks' aren't perceived to be normal, something
         * sometimes goes astray here? if (null == customerPerformanceHistory) {
         * customerPerformanceHistory = (CustomerPerformanceHistory)
         * StaticHibernateUtil.getSessionTL().createQuery(
         * "from org.mifos.application.customer.client.business.ClientPerformanceHistoryEntity e where e.client.customerId = "
         * + ((LoanBO) accountBO).getCustomer().getCustomerId()).uniqueResult();
         * }
         */

       Assert.assertEquals(Integer.valueOf(1), clientPerfHistory.getLoanCycleNumber());
       Assert.assertEquals(noOfActiveLoans - 1, clientPerfHistory.getNoOfActiveLoans().intValue());
       Assert.assertEquals(totalRepaymentAmount, clientPerfHistory.getLastLoanAmount());
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
    }

    public void testWtiteOffForCustomerPerfHistory() throws Exception {
        accountBO = getLoanAccountWithPerformanceHistory();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        LoanBO loanBO = (LoanBO) accountBO;
        UserContext uc = TestUtils.makeUser();
        loanBO.setUserContext(uc);
        ClientPerformanceHistoryEntity clientPerfHistory = (ClientPerformanceHistoryEntity) ((LoanBO) accountBO)
                .getCustomer().getPerformanceHistory();
        Integer noOfActiveLoans = clientPerfHistory.getNoOfActiveLoans();
        Integer loanCycleNumber = clientPerfHistory.getLoanCycleNumber();
        loanBO.writeOff();
        loanBO.update();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        LoanBO loan = (LoanBO) accountBO;
        clientPerfHistory = (ClientPerformanceHistoryEntity) loan.getCustomer().getPerformanceHistory();
       Assert.assertEquals(loanCycleNumber - 1, clientPerfHistory.getLoanCycleNumber().intValue());
       Assert.assertEquals(noOfActiveLoans - 1, clientPerfHistory.getNoOfActiveLoans().intValue());
    }

    public void testDisbursalLoanForCustomerPerfHistory() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        accountBO = getLoanAccountWithPerformanceHistory(AccountState.LOAN_APPROVED, startDate, 3);

        ClientPerformanceHistoryEntity clientPerfHistory = (ClientPerformanceHistoryEntity) ((LoanBO) accountBO)
                .getCustomer().getPerformanceHistory();
        Integer noOfActiveLoans = clientPerfHistory.getNoOfActiveLoans();
        Integer loanCycleNumber = clientPerfHistory.getLoanCycleNumber();
        ((LoanBO) accountBO).disburseLoan("1234", startDate, Short.valueOf("1"), accountBO.getPersonnel(), startDate,
                Short.valueOf("1"));
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        LoanBO loan = (LoanBO) accountBO;
       Assert.assertEquals(noOfActiveLoans + 1, clientPerfHistory.getNoOfActiveLoans().intValue());
       Assert.assertEquals(loanCycleNumber + 1, clientPerfHistory.getLoanCycleNumber().intValue());
        LoanPerformanceHistoryEntity loanPerfHistory = loan.getPerformanceHistory();
       Assert.assertEquals(getLastInstallmentAccountAction(loan).getActionDate(), loanPerfHistory.getLoanMaturityDate());
    }

    public void testHandleArrearsForCustomerPerfHistory() throws AccountException, SystemException, InvalidDateException {
        accountBO = getLoanAccountWithPerformanceHistory();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        ClientPerformanceHistoryEntity clientPerfHistory = (ClientPerformanceHistoryEntity) ((LoanBO) accountBO)
                .getCustomer().getPerformanceHistory();
        Integer noOfActiveLoans = clientPerfHistory.getNoOfActiveLoans();
        ((LoanBO) accountBO).handleArrears();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        LoanBO loan = (LoanBO) accountBO;
        clientPerfHistory = (ClientPerformanceHistoryEntity) loan.getCustomer().getPerformanceHistory();
       Assert.assertEquals(noOfActiveLoans.intValue(), clientPerfHistory.getNoOfActiveLoans().intValue());
    }

    public void testMakePaymentForCustomerPerfHistory() throws AccountException, SystemException {
        accountBO = getLoanAccountWithPerformanceHistory();
        accountBO.setUserContext(TestObjectFactory.getContext());
        accountBO.changeStatus(AccountState.LOAN_ACTIVE_IN_BAD_STANDING, null, "");

        TestObjectFactory.updateObject(accountBO);
        StaticHibernateUtil.closeSession();
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        ClientPerformanceHistoryEntity clientPerfHistory = (ClientPerformanceHistoryEntity) ((LoanBO) accountBO)
                .getCustomer().getPerformanceHistory();
        Integer noOfActiveLoans = clientPerfHistory.getNoOfActiveLoans();
        LoanPerformanceHistoryEntity loanPerfHistory = ((LoanBO) accountBO).getPerformanceHistory();
        Integer noOfPayments = loanPerfHistory.getNoOfPayments();
        accountBO = applyPaymentandRetrieveAccount();
        LoanBO loan = (LoanBO) accountBO;
        client = TestObjectFactory.getCustomer(client.getCustomerId());
        clientPerfHistory = (ClientPerformanceHistoryEntity) loan.getCustomer().getPerformanceHistory();
       Assert.assertEquals(noOfActiveLoans - 1, clientPerfHistory.getNoOfActiveLoans().intValue());
       Assert.assertEquals(noOfPayments + 1, loan.getPerformanceHistory().getNoOfPayments().intValue());
    }

    public void testDisbursalLoanForGroupPerfHistory() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        accountBO = getLoanAccountWithGroupPerformanceHistory(AccountState.LOAN_APPROVED, startDate, 3);

        GroupPerformanceHistoryEntity groupPerformanceHistoryEntity = (GroupPerformanceHistoryEntity) ((LoanBO) accountBO)
                .getCustomer().getPerformanceHistory();
        ((LoanBO) accountBO).disburseLoan("1234", startDate, Short.valueOf("1"), accountBO.getPersonnel(), startDate,
                Short.valueOf("1"));
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        LoanBO loan = (LoanBO) accountBO;
       Assert.assertEquals(new Money("300.0"), groupPerformanceHistoryEntity.getLastGroupLoanAmount());
        LoanPerformanceHistoryEntity loanPerfHistory = loan.getPerformanceHistory();
       Assert.assertEquals(getLastInstallmentAccountAction(loan).getActionDate(), loanPerfHistory.getLoanMaturityDate());
    }

    /**
     * TODO: Re-enable this test when rounding code correctly handles mid-stream
     * changes to the loan schedule. Note the use of method getLoanAccount(),
     * which radically alters the terms of the loan, unbeknownst to the reader
     * of this code. Here's a trace of this bizarre code:
     * <ul>
     * <li>getLoanAccount() calls
     * <li>TestObjectFactory.createLoanAccount(), which calls
     * <li>LoanBOIntegrationTest.createLoanAccount(). This method initiall
     * creates the loan with terms:
     * <ul>
     * <li>Loan amount 0
     * <li>Interest rate 0.0
     * <li>6 weekly installments
     * <li>no fees
     * </ul>
     * <li>The method then goes on to alter the loan by directly manipulating
     * instance variables:
     * <ul>
     * <li>Add a periodic amount fee of 100 to each installment
     * <li>Change each installment's principal to 100 and interest to 12.0 (but
     * the loan amount is still 300, although the principal due is now 600!)
     * </ul>
     * </ul>
     * This leaves the loan in a badly inconsistent state, causing the (second)
     * call to applyRounding() to break. This test code needs to be refactored!
     * <p>
     * This test now breaks. However, rounding with miscellaneous fees is fully
     * covered by tests in TestLoanRedoDisbursal.java, so it is disabled.
     */
    public void xtestRoundInstallments() throws AccountException, SystemException {
        accountBO = getLoanAccount();
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        ((LoanBO) accountBO).getLoanOffering().setPrinDueLastInst(false);
        List<Short> installmentIdList = new ArrayList<Short>();
        for (AccountActionDateEntity accountAction : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            installmentIdList.add(accountActionDateEntity.getInstallmentId());
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                accountActionDateEntity.setMiscFee(new Money("20.3"));
            }
        }
        ((LoanBO) accountBO).applyRounding();
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();

        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());

        Set<AccountActionDateEntity> actionDateEntities = accountBO.getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkTotalDueWithFees("233.0", paymentsArray[0]);
        checkTotalDueWithFees("212.0", paymentsArray[1]);
        checkTotalDueWithFees("212.0", paymentsArray[2]);
        checkTotalDueWithFees("212.0", paymentsArray[3]);
        checkTotalDueWithFees("212.0", paymentsArray[4]);
        checkTotalDueWithFees("212.0", paymentsArray[5]);
    }

    public void testBuildLoanWithoutLoanOffering() throws Exception {
        createInitialCustomers();
        try {
            loanDao.createLoan(TestUtils.makeUser(), null, group, AccountState.LOAN_APPROVED, new Money("300.0"), Short
                    .valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0, (short) 0, new FundBO(),
                    new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(), DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO,
                    SHORT_ZERO);
            Assert.assertFalse("The Loan object is created for null loan offering", true);
        } catch (AccountException ae) {
           Assert.assertTrue("The Loan object is not created for null loan offering", true);
        }
    }

    public void testBuildForInactiveLoanOffering() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false, false, PrdStatus.LOAN_INACTIVE);
        try {
            LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

            LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED,
                    new Money("300.0"), Short.valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0,
                    (short) 0, new FundBO(), new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(),
                    DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange.getMaxNoOfInstall(),
                    eligibleInstallmentRange.getMinNoOfInstall(), false, null);
            Assert.fail("The Loan object is created for inactive loan offering");
        } catch (AccountException ae) {
           Assert.assertTrue("The Loan object is not created for inactive loan offering", true);
        }
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testBuildLoanWithoutCustomer() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        try {
            LoanBO.createLoan(TestUtils.makeUser(), loanOffering, null, AccountState.LOAN_APPROVED, new Money("300.0"),
                    Short.valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0, (short) 0, new FundBO(),
                    new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(), DEFAULT_LOAN_AMOUNT,
                    DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange
                            .getMinNoOfInstall(), false, null);
            Assert.assertFalse("The Loan object is created for null customer", true);
        } catch (AccountException ae) {
           Assert.assertTrue("The Loan object is not created for null customer", true);
        }
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testBuildForInactiveCustomer() throws Exception {
        createInitialCustomers();
        CustomerBOTestUtils.setCustomerStatus(group, new CustomerStatusEntity(CustomerStatus.GROUP_CLOSED));
        TestObjectFactory.updateObject(group);
        group = TestObjectFactory.getGroup(group.getCustomerId());

        LoanOfferingBO loanOffering = createLoanOffering(false);
        try {
            loanDao.createLoan(TestUtils.makeUser(), loanOffering, null, AccountState.LOAN_APPROVED,
                    new Money("300.0"), Short.valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0,
                    (short) 0, new FundBO(), new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(), DOUBLE_ZERO,
                    DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
            Assert.assertFalse("The Loan object is created for inactive customer", true);
        } catch (AccountException ae) {
           Assert.assertTrue("The Loan object is not created for inactive customer", true);
        }
        TestObjectFactory.removeObject(loanOffering);

    }

    public void testMeetingNotMatchingForCustomerAndLoanOffering() throws Exception {
        createInitialCustomers();
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(MONTHLY,
                EVERY_MONTH, CUSTOMER_MEETING));
        Date startDate = new Date(System.currentTimeMillis());
        LoanOfferingBO loanOffering = createOfferingNoPrincipalInLastInstallment(startDate, meeting);
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        try {
            LoanBO.createLoan(TestUtils.makeUser(), loanOffering, null, AccountState.LOAN_APPROVED, new Money("300.0"),
                    Short.valueOf("6"), startDate, false, 10.0, (short) 0, new FundBO(), new ArrayList<FeeView>(),
                    new ArrayList<CustomFieldView>(), DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT,
                    eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(), false,
                    null);
            Assert.assertFalse("The Loan object is created even if meetings do not match", true);
        } catch (AccountException ae) {
           Assert.assertTrue("The Loan object is not created if meetings do not match", true);
        }
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testMeetingRecurrenceOfLoanOfferingInMultipleOfCustomer() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        createInitialCustomers();
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        LoanOfferingBO loanOffering = createOfferingNoPrincipalInLastInstallment(startDate, meeting);

        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED, new Money("300.0"),
                Short.valueOf("6"), startDate, false, 10.0, (short) 0, new FundBO(), new ArrayList<FeeView>(),
                new ArrayList<CustomFieldView>(), DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange
                        .getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(), false, null);
       Assert.assertTrue("The Loan object is created if meeting recurrence of loan offering is in multiples of customer",
                true);
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testBuildLoanWithoutLoanAmount() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        try {
            LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED, null, Short
                    .valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0, (short) 0, new FundBO(),
                    new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(), DEFAULT_LOAN_AMOUNT,
                    DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange
                            .getMinNoOfInstall(), false, null);
            Assert.assertFalse("The Loan object is created for null customer", true);
        } catch (AccountException ae) {
           Assert.assertTrue("The Loan object is not created for null customer", true);
        }
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testGracePeriodGraterThanMaxNoOfInst() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        try {
            LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED, null, Short
                    .valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0, (short) 5, new FundBO(),
                    new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(), DEFAULT_LOAN_AMOUNT,
                    DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange
                            .getMinNoOfInstall(), false, null);
            Assert.assertFalse("The Loan object is created for grace period greather than max installments", true);
        } catch (AccountException ae) {
           Assert.assertTrue("The Loan object is not created for grace period greather than max installments", true);
        }
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testGracePeriodForInterestNotDedAtDisb() throws Exception {
        createInitialCustomers();
        LoanOfferingBO product = createLoanOffering(false);
        // product.updateLoanOfferingSameForAllLoan(product);
        LoanOfferingInstallmentRange eligibleInstallmentRange = product.getEligibleInstallmentSameForAllLoan();

        LoanBO loan = LoanBO.createLoan(TestUtils.makeUser(), product, group, AccountState.LOAN_APPROVED, new Money(
                "300.0"), Short.valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0, (short) 1,
                new FundBO(), new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(), DEFAULT_LOAN_AMOUNT,
                DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange
                        .getMinNoOfInstall(), false, null);
       Assert.assertEquals(product.getGraceType(), loan.getGraceType());
       Assert.assertEquals(1, loan.getGracePeriodDuration().intValue());
        assertNotSame(new java.sql.Date(DateUtils.getCurrentDateWithoutTimeStamp().getTime()).toString(), loan
                .getAccountActionDate((short) 1).getActionDate().toString());

        TestObjectFactory.removeObject(product);
    }

    /*
     * TODO: turn this back on when interest deducted at disbursement are
     * re-enabled.
     * 
     * 
     * public void testGracePeriodForInterestDedAtDisb() throws
     * NumberFormatException, AccountException, Exception {
     * createInitialCustomers(); LoanOfferingBO loanOffering =
     * createLoanOffering(false, true); LoanOfferingInstallmentRange
     * eligibleInstallmentRange = loanOffering
     * .getEligibleInstallmentSameForAllLoan(); LoanBO loan =
     * LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_APPROVED, new Money("300.0"), Short .valueOf("6"), new
     * Date(System.currentTimeMillis()), true, 10.0, (short) 5, new FundBO(),
     * new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(),
     * DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange
     * .getMaxNoOfInstall(), eligibleInstallmentRange .getMinNoOfInstall(),
     * false, null);Assert.assertEquals(
     * "For interest ded at disb, grace period type should be none",
     * GraceType.NONE, loan.getGraceType());Assert.assertEquals(0,
     * loan.getGracePeriodDuration().intValue());Assert.assertEquals(new
     * java.sql.Date(DateUtils
     * .getCurrentDateWithoutTimeStamp().getTime()).toString(), loan
     * .getAccountActionDate((short) 1).getActionDate().toString());
     * 
     * TestObjectFactory.removeObject(loanOffering); }
     */
    public void testBuildLoanWithValidDisbDate() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        Date disbursementDate = new Date(System.currentTimeMillis());
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();
        try {

            LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED,
                    new Money("300.0"), Short.valueOf("6"), disbursementDate, false, 10.0, (short) 0, new FundBO(),
                    new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(), DEFAULT_LOAN_AMOUNT,
                    DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange
                            .getMinNoOfInstall(), false, null);
           Assert.assertTrue("The Loan object is created for valid disbursement date", true);
        } catch (AccountException ae) {
            Assert.assertFalse("The Loan object is created for valid disbursement date", true);
        }
        TestObjectFactory.removeObject(loanOffering);
    }

    // Ability to enter a loan disbursal date wich doesn't occur on a meeting
    // day
    // TODO : test method to be removed

    /*
     * public void testBuildLoanWithInvalidDisbDate() throws
     * NumberFormatException, AccountException, Exception {
     * createInitialCustomers(); LoanOfferingBO loanOffering =
     * createLoanOffering(false); Date disbursementDate =
     * incrementCurrentDate(3);
     * 
     * try { new LoanBO(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOANACC_APPROVED, new Money("300.0"), Short .valueOf("6"),
     * disbursementDate, false, 10.0, (short) 0, new FundBO(), new
     * ArrayList<FeeView>(), new ArrayList<CustomFieldView>()); Assert.assertFalse(
     * "The Loan object is created for invalid disbursement date", true); }
     * catch (AccountException ae) {Assert.assertTrue(
     * "The Loan object is created for invalid disbursement date", true); }
     * TestObjectFactory.removeObject(loanOffering); }
     */

    public void testBuildLoanWithDisbDateOlderThanCurrentDate() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        Date disbursementDate = offSetCurrentDate(15);
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        try {

            LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED,
                    new Money("300.0"), Short.valueOf("6"), disbursementDate, false, 10.0, (short) 0, new FundBO(),
                    new ArrayList<FeeView>(), new ArrayList<CustomFieldView>(), DEFAULT_LOAN_AMOUNT,
                    DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange
                            .getMinNoOfInstall(), false, null);
            Assert.assertFalse("The Loan object is created for invalid disbursement date", true);
        } catch (AccountException ae) {
           Assert.assertTrue("The Loan object is created for invalid disbursement date", true);
        }
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testBuildLoanWithFee() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        List<FeeView> feeViews = getFeeViews();
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        LoanBO loan = LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED,
                new Money("300.0"), Short.valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0, (short) 0,
                new FundBO(), feeViews, null, DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange
                        .getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(), false, null);

       Assert.assertEquals(2, loan.getAccountFees().size());
        for (AccountFeesEntity accountFees : loan.getAccountFees()) {
            if (accountFees.getFees().getFeeName().equals("One Time Amount Fee")) {
               Assert.assertEquals(new Double("120.0"), accountFees.getFeeAmount());
            } else {
               Assert.assertEquals(new Double("10.0"), accountFees.getFeeAmount());
            }
        }

        Map<String, String> fees1 = new HashMap<String, String>();
        fees1.put("Periodic Fee", "10.0");
        fees1.put("One Time Amount Fee", "120.0");

        Map<String, String> fees2 = new HashMap<String, String>();
        fees2.put("Periodic Fee", "10.0");

        Set<AccountActionDateEntity> actionDateEntities = loan.getAccountActionDates();
       Assert.assertEquals(6, actionDateEntities.size());
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        /*
         * TODO: fix this check checkFees(fees1, "130.0", paymentsArray[0]);
         */
        checkFees(fees2, "10.0", paymentsArray[1]);
        checkFees(fees2, "10.0", paymentsArray[2]);
        checkFees(fees2, "10.0", paymentsArray[3]);
        checkFees(fees2, "10.0", paymentsArray[4]);
        checkFees(fees2, "10.0", paymentsArray[5]);

        deleteFee(feeViews);
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testBuildLoan() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        List<FeeView> feeViews = getFeeViews();
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        LoanBO loan = LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED,
                new Money("300.0"), Short.valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0, (short) 0,
                new FundBO(), feeViews, null, DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT, eligibleInstallmentRange
                        .getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(), false, null);

        Assert.assertNotNull(loan.getLoanSummary());
        Assert.assertNotNull(loan.getPerformanceHistory());
       Assert.assertEquals(new Money("300.0"), loan.getLoanSummary().getOriginalPrincipal());
       Assert.assertEquals(new Money("300.0"), loan.getLoanAmount());
       Assert.assertEquals(new Money("300.0"), loan.getLoanBalance());
       Assert.assertEquals(Short.valueOf("6"), loan.getNoOfInstallments());
       Assert.assertEquals(6, loan.getAccountActionDates().size());
       Assert.assertEquals(loan.getNoOfInstallments().intValue(), loan.getAccountActionDates().size());

        deleteFee(feeViews);
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testCreateLoan() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        List<FeeView> feeViews = getFeeViews();
        boolean isInterestDedAtDisb = false;
        Short noOfinstallments = (short) 6;
        LoanBO loan = createAndRetrieveLoanAccount(loanOffering, isInterestDedAtDisb, feeViews, noOfinstallments);

       Assert.assertEquals(2, loan.getAccountFees().size());
        for (AccountFeesEntity accountFees : loan.getAccountFees()) {
            if (accountFees.getFees().getFeeName().equals("One Time Amount Fee")) {
               Assert.assertEquals(new Double("120.0"), accountFees.getFeeAmount());
            } else {
               Assert.assertEquals(new Double("10.0"), accountFees.getFeeAmount());
            }
        }

        Set<AccountActionDateEntity> actionDateEntities = loan.getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkPrincipalAndInterest("50.4", "0.6", paymentsArray[0]);
        checkPrincipalAndInterest("50.4", "0.6", paymentsArray[1]);
        checkPrincipalAndInterest("50.4", "0.6", paymentsArray[2]);
        checkPrincipalAndInterest("50.4", "0.6", paymentsArray[3]);
        checkPrincipalAndInterest("50.4", "0.6", paymentsArray[4]);
        checkPrincipalAndInterest("48.0", "1.0", paymentsArray[5]);

        Assert.assertNotNull(loan.getLoanSummary());
        Assert.assertNotNull(loan.getPerformanceHistory());
       Assert.assertEquals(new Money("300.0"), loan.getLoanSummary().getOriginalPrincipal());
       Assert.assertEquals(new Money("300.0"), loan.getLoanAmount());
       Assert.assertEquals(new Money("300.0"), loan.getLoanBalance());
       Assert.assertEquals(Short.valueOf("6"), loan.getNoOfInstallments());
       Assert.assertEquals(6, loan.getAccountActionDates().size());
    }

    /*
     * TODO: Turn this test back on when interest deducted at disbursement is
     * re-enabled.
     * 
     * public void testCreateLoanForInterestDedAtDisb() throws
     * NumberFormatException, AccountException, Exception {
     * createInitialCustomers(); LoanOfferingBO loanOffering =
     * createLoanOffering(false, true); List<FeeView> feeViews = getFeeViews();
     * boolean isInterestDedAtDisb = true; Short noOfinstallments = (short) 6;
     * 
     * LoanBO loan = createAndRetrieveLoanAccount(loanOffering,
     * isInterestDedAtDisb, feeViews, noOfinstallments);
     * 
     *Assert.assertEquals(2, loan.getAccountFees().size()); for (AccountFeesEntity
     * accountFees : loan.getAccountFees()) { if
     * (accountFees.getFees().getFeeName() .equals("One Time Amount Fee"))
     *Assert.assertEquals(new Double("120.0"), accountFees.getFeeAmount()); else
     *Assert.assertEquals(new Double("10.0"), accountFees.getFeeAmount()); }
     * 
     * Set<AccountActionDateEntity> actionDateEntities = loan
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkPrincipalAndInterest("0.0", "3.0", paymentsArray[0]);
     * checkPrincipalAndInterest("60.0", "0.0", paymentsArray[1]);
     * checkPrincipalAndInterest("60.0", "0.0", paymentsArray[2]);
     * checkPrincipalAndInterest("60.0", "0.0", paymentsArray[3]);
     * checkPrincipalAndInterest("60.0", "0.0", paymentsArray[4]);
     * checkPrincipalAndInterest("60.0", "0.0", paymentsArray[5]);
     * 
     * Assert.assertNotNull(loan.getLoanSummary());
     * Assert.assertNotNull(loan.getPerformanceHistory());Assert.assertEquals(new
     * Money("300.0"), loan.getLoanSummary() .getOriginalPrincipal());
     *Assert.assertEquals(new Money("300.0"), loan.getLoanAmount());Assert.assertEquals(new
     * Money("300.0"), loan.getLoanBalance());Assert.assertEquals(Short.valueOf("6"),
     * loan.getNoOfInstallments());Assert.assertEquals(6,
     * loan.getAccountActionDates().size()); }
     */

    /*
     * TODO: Add this back in when principal due in last installment is
     * supported again.
     * 
     * public void testCreateLoanForPrincipalAtLastInst() throws
     * NumberFormatException, AccountException, Exception {
     * createInitialCustomers(); LoanOfferingBO loanOffering =
     * createLoanOffering(true); List<FeeView> feeViews = getFeeViews(); boolean
     * isInterestDedAtDisb = false; Short noOfinstallments = (short) 6;
     * 
     * LoanBO loan = createAndRetrieveLoanAccount(loanOffering,
     * isInterestDedAtDisb, feeViews, noOfinstallments);
     * 
     *Assert.assertEquals(2, loan.getAccountFees().size()); for (AccountFeesEntity
     * accountFees : loan.getAccountFees()) { if
     * (accountFees.getFees().getFeeName() .equals("One Time Amount Fee"))
     *Assert.assertEquals(new Double("120.0"), accountFees.getFeeAmount()); else
     *Assert.assertEquals(new Double("10.0"), accountFees.getFeeAmount()); }
     * 
     * Set<AccountActionDateEntity> actionDateEntities = loan
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkPrincipalAndInterest("0.0", "0.5", paymentsArray[0]);
     * checkPrincipalAndInterest("0.0", "0.5", paymentsArray[1]);
     * checkPrincipalAndInterest("0.0", "0.5", paymentsArray[2]);
     * checkPrincipalAndInterest("0.0", "0.5", paymentsArray[3]);
     * checkPrincipalAndInterest("0.0", "0.5", paymentsArray[4]);
     * checkPrincipalAndInterest("300.0", "0.5", paymentsArray[5]);
     * 
     * Assert.assertNotNull(loan.getLoanSummary());
     * Assert.assertNotNull(loan.getPerformanceHistory());Assert.assertEquals(new
     * Money("300.0"), loan.getLoanSummary() .getOriginalPrincipal());
     *Assert.assertEquals(new Money("300.0"), loan.getLoanAmount());Assert.assertEquals(new
     * Money("300.0"), loan.getLoanBalance());Assert.assertEquals(Short.valueOf("6"),
     * loan.getNoOfInstallments());Assert.assertEquals(6,
     * loan.getAccountActionDates().size());
     * 
     * }
     */

    /*
     * TODO: turn this back on when principal due in last installment and
     * interest deducted at disbursement are re-enabled.
     * 
     * public void testCreateLoanForPrincipalAtLastInstAndIntDedAtDisb() throws
     * NumberFormatException, AccountException, Exception {
     * createInitialCustomers(); LoanOfferingBO loanOffering =
     * createLoanOffering(true, true); List<FeeView> feeViews = getFeeViews();
     * boolean isInterestDedAtDisb = true; Short noOfinstallments = (short) 6;
     * 
     * LoanBO loan = createAndRetrieveLoanAccount(loanOffering,
     * isInterestDedAtDisb, feeViews, noOfinstallments);
     * 
     *Assert.assertEquals(2, loan.getAccountFees().size()); for (AccountFeesEntity
     * accountFees : loan.getAccountFees()) { if
     * (accountFees.getFees().getFeeName() .equals("One Time Amount Fee"))
     *Assert.assertEquals(new Double("120.0"), accountFees.getFeeAmount()); else
     *Assert.assertEquals(new Double("10.0"), accountFees.getFeeAmount()); }
     * 
     * Set<AccountActionDateEntity> actionDateEntities = loan
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkPrincipalAndInterest("0.0", "3.0", paymentsArray[0]);
     * checkPrincipalAndInterest("0.0", "0.0", paymentsArray[1]);
     * checkPrincipalAndInterest("0.0", "0.0", paymentsArray[2]);
     * checkPrincipalAndInterest("0.0", "0.0", paymentsArray[3]);
     * checkPrincipalAndInterest("0.0", "0.0", paymentsArray[4]);
     * checkPrincipalAndInterest("300.0", "0.0", paymentsArray[5]);
     * 
     * Assert.assertNotNull(loan.getLoanSummary());
     * Assert.assertNotNull(loan.getPerformanceHistory());Assert.assertEquals(new
     * Money("300.0"), loan.getLoanSummary() .getOriginalPrincipal());
     *Assert.assertEquals(new Money("300.0"), loan.getLoanAmount());Assert.assertEquals(new
     * Money("300.0"), loan.getLoanBalance());Assert.assertEquals(Short.valueOf("6"),
     * loan.getNoOfInstallments());Assert.assertEquals(6,
     * loan.getAccountActionDates().size());
     * 
     * }
     */
    public void testLoanScheduleRoundingWithMiscFee() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        Short noOfinstallments = (short) 2;
        LoanBO loan = createButDontRetrieveLoanAccount(loanOffering, false, null, noOfinstallments, 0.0, new Money(
                "300.0"));
        for (AccountActionDateEntity accountActionDate : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDate;
            if (loanScheduleEntity.getInstallmentId() == 1) {
               Assert.assertEquals(new Money("150.0"), loanScheduleEntity.getTotalDueWithFees());
            } else {
               Assert.assertEquals(new Money("150.0"), loanScheduleEntity.getTotalDueWithFees());
            }
        }
        loan.applyCharge(Short.valueOf(AccountConstants.MISC_FEES), 10.0);
        for (AccountActionDateEntity accountActionDate : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDate;
            if (loanScheduleEntity.getInstallmentId() == 1) {
               Assert.assertEquals(new Money("160.0"), loanScheduleEntity.getTotalDueWithFees());
               Assert.assertEquals(new Money("150.0"), loanScheduleEntity.getPrincipal());
            } else {
               Assert.assertEquals(new Money("150.0"), loanScheduleEntity.getTotalDueWithFees());
            }
        }
    }

    public void testAmountRoundedWhileCreate() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        boolean isInterestDedAtDisb = false;
        Short noOfinstallments = (short) 6;

        LoanBO loan = createAndRetrieveLoanAccount(loanOffering, isInterestDedAtDisb, null, noOfinstallments);

        for (AccountActionDateEntity accountActionDate : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDate;
            if (loanScheduleEntity.getInstallmentId() != 6) {
               Assert.assertEquals(new Money("51.0"), loanScheduleEntity.getTotalDueWithFees());
            } else {
               Assert.assertEquals("The last installment amount is adjusted for rounding", new Money("49.0"),
                        loanScheduleEntity.getTotalDueWithFees());
            }
        }
    }

    public void testAmountNotRoundedWhileCreate() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        boolean isInterestDedAtDisb = false;
        Short noOfinstallments = (short) 6;

        LoanBO loan = createAndRetrieveLoanAccount(loanOffering, isInterestDedAtDisb, null, noOfinstallments, 0.0,
                new Money("300.0"));

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountActionDateEntity accountActionDate : loan.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDate;
            if (loanScheduleEntity.getInstallmentId() != 6) {
               Assert.assertEquals(new Money("50.0"), loanScheduleEntity.getTotalDueWithFees());
            } else {
               Assert.assertEquals("The last installment amount is not adjusted for rounding", new Money("50.0"),
                        loanScheduleEntity.getTotalDueWithFees());
            }
        }
    }

    public void testApplyMiscCharge() throws Exception {
        accountBO = getLoanAccount();
        Money intialTotalFeeAmount = ((LoanBO) accountBO).getLoanSummary().getOriginalFees();
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        UserContext uc = TestUtils.makeUser();
        accountBO.setUserContext(uc);
        accountBO.applyCharge(Short.valueOf(AccountConstants.MISC_FEES), new Double("33"));
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(new Money("33.0"), loanScheduleEntity.getMiscFee());
            }
        }
       Assert.assertEquals(intialTotalFeeAmount.add(new Money("33.0")), ((LoanBO) accountBO).getLoanSummary()
                .getOriginalFees());
        LoanActivityEntity loanActivityEntity = (LoanActivityEntity) ((LoanBO) accountBO).getLoanActivityDetails()
                .toArray()[0];
       Assert.assertEquals(AccountConstants.MISC_FEES_APPLIED, loanActivityEntity.getComments());
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getOriginalFees(), loanActivityEntity.getFeeOutstanding());
    }

    /**
     * TODO: Re-enable this method when rounding code can handle mid-stream
     * changes to the loan that require re-applying applyRounding(). (method
     * LoanBO.applyCharge() does. When this method sets payment status to PAID,
     * applyRounding applies only to unpaid installments, which it cannot
     * currently handle.)
     * <p/>
     * Note: this method should not directly set status to PAID, as it leaves
     * the loan in an inconsistent state. The the status is paid, but the
     * installment's state is inconsistent -- the "paidX" instance variable
     * still have value 0.
     */
    public void testApplyMiscChargeWithFirstInstallmentPaid() throws Exception {
        accountBO = getLoanAccount();
        Money intialTotalFeeAmount = ((LoanBO) accountBO).getLoanSummary().getOriginalFees();
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                loanScheduleEntity.setPaymentStatus(PaymentStatus.PAID);
            }
        }
        UserContext uc = TestUtils.makeUser();
        accountBO.setUserContext(uc);
        accountBO.applyCharge(Short.valueOf("-1"), new Double("33"));

        // Change this to more clearly separate what we are testing for from the
        // machinery needed to get that data?

        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("2"))) {
               Assert.assertEquals(new Money("33.0"), loanScheduleEntity.getMiscFee());
            }
        }
       Assert.assertEquals(intialTotalFeeAmount.add(new Money("33.0")), ((LoanBO) accountBO).getLoanSummary()
                .getOriginalFees());
        LoanActivityEntity loanActivityEntity = (LoanActivityEntity) ((LoanBO) accountBO).getLoanActivityDetails()
                .toArray()[0];
       Assert.assertEquals(AccountConstants.MISC_FEES_APPLIED, loanActivityEntity.getComments());
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getOriginalFees(), loanActivityEntity.getFeeOutstanding());
    }

    public void testApplyUpfrontFee() throws Exception {
        accountBO = getLoanAccount();
        Money intialTotalFeeAmount = ((LoanBO) accountBO).getLoanSummary().getOriginalFees();
        TestObjectFactory.flushandCloseSession();
        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT);
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        UserContext uc = TestUtils.makeUser();
        accountBO.setUserContext(uc);
        accountBO.applyCharge(upfrontFee.getFeeId(), ((RateFeeBO) upfrontFee).getRate());
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
        Date lastAppliedDate = null;
        Money feeAmountApplied = new Money();
        Map<String, String> fees2 = new HashMap<String, String>();
        fees2.put("Upfront Fee", "60.0");
        fees2.put("Mainatnence Fee", "100.0");

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
       Assert.assertEquals(6, actionDateEntities.size());
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkFees(fees2, paymentsArray[0], false);

        // setting of values here
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {

                lastAppliedDate = loanScheduleEntity.getActionDate();
                for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : loanScheduleEntity
                        .getAccountFeesActionDetails()) {
                    if (accountFeesActionDetailEntity.getFee().getFeeName().equalsIgnoreCase("Upfront Fee")) {
                        feeAmountApplied = accountFeesActionDetailEntity.getFeeAmount();
                    }
                }
            }
        }
       Assert.assertEquals(intialTotalFeeAmount.add(feeAmountApplied), ((LoanBO) accountBO).getLoanSummary()
                .getOriginalFees());
        LoanActivityEntity loanActivityEntity = (LoanActivityEntity) ((LoanBO) accountBO).getLoanActivityDetails()
                .toArray()[0];
       Assert.assertEquals(upfrontFee.getFeeName() + " applied", loanActivityEntity.getComments());
       Assert.assertEquals(((LoanBO) accountBO).getLoanSummary().getOriginalFees(), loanActivityEntity.getFeeOutstanding());
        AccountFeesEntity accountFeesEntity = accountBO.getAccountFees(upfrontFee.getFeeId());
       Assert.assertEquals(FeeStatus.ACTIVE, accountFeesEntity.getFeeStatusAsEnum());
       Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(lastAppliedDate.getTime()), DateUtils
                .getDateWithoutTimeStamp(accountFeesEntity.getLastAppliedDate().getTime()));
    }

    public void testUpdateLoanSuccessWithRegeneratingNewRepaymentSchedule() throws Exception {
        // newDate is the new disbursement date
        Date newDate = incrementCurrentDate(14);
        // first installment date is the first installment which is one
        // week after the disbursement date since this is a weekly loan
        Date firstInstallmentDate = incrementCurrentDate(21);
        accountBO = getLoanAccount();
        accountBO.setUserContext(TestObjectFactory.getContext());
        accountBO.changeStatus(AccountState.LOAN_APPROVED, null, "");

        LoanBO loanBO = (LoanBO) accountBO;
        ((LoanBO) accountBO).updateLoan(loanBO.isInterestDeductedAtDisbursement(), loanBO.getLoanAmount(), loanBO
                .getInterestRate(), loanBO.getNoOfInstallments(), newDate, loanBO.getGracePeriodDuration(), loanBO
                .getBusinessActivityId(), loanBO.getCollateralNote(), null, null, false, null);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        Date newActionDate = DateUtils.getDateWithoutTimeStamp(accountBO.getAccountActionDate(Short.valueOf("1"))
                .getActionDate().getTime());
       Assert.assertEquals("New repayment schedule generated, so first installment date should be same as newDate",
                firstInstallmentDate, newActionDate);
    }

    public void testUpdateLoanWithoutRegeneratingNewRepaymentSchedule() throws Exception {
        Date newDate = incrementCurrentDate(14);
        accountBO = getLoanAccount();
        Date oldActionDate = DateUtils.getDateWithoutTimeStamp(accountBO.getAccountActionDate(Short.valueOf("1"))
                .getActionDate().getTime());
        LoanBO loanBO = (LoanBO) accountBO;
        ((LoanBO) accountBO).updateLoan(loanBO.isInterestDeductedAtDisbursement(), loanBO.getLoanAmount(), loanBO
                .getInterestRate(), loanBO.getNoOfInstallments(), newDate, loanBO.getGracePeriodDuration(), loanBO
                .getBusinessActivityId(), loanBO.getCollateralNote(), null, null, false, null);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        group = TestObjectFactory.getCustomer(group.getCustomerId());
        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());
        Date newActionDate = DateUtils.getDateWithoutTimeStamp(accountBO.getAccountActionDate(Short.valueOf("1"))
                .getActionDate().getTime());
       Assert.assertEquals("Didn't generate new repayment schedule, so first installment date should be same as before ",
                oldActionDate, newActionDate);
    }

    /*
     * TODO: turn back on when PrincipalDueInLastPayment is re-enabled
     * 
     * 
     * public void testCreateLoanAccountWithPrincipalDueInLastPayment() throws
     * Exception { Date startDate = new Date(System.currentTimeMillis());
     * MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory
     * .getNewMeetingForToday(WEEKLY, EVERY_SECOND_WEEK, CUSTOMER_MEETING));
     * center = TestObjectFactory.createCenter("Center", meeting); group =
     * TestObjectFactory.createGroupUnderCenter("Group",
     * CustomerStatus.GROUP_ACTIVE, center); LoanOfferingBO loanOffering =
     * TestObjectFactory.createLoanOffering( startDate,
     * center.getCustomerMeeting().getMeeting()); UserContext userContext =
     * TestUtils.makeUser(); userContext.setLocaleId(null); List<FeeView>
     * feeViewList = new ArrayList<FeeView>(); FeeBO periodicFee =
     * TestObjectFactory.createPeriodicAmountFee( "Periodic Fee",
     * FeeCategory.LOAN, "100", RecurrenceType.WEEKLY, Short.valueOf("3"));
     * feeViewList.add(new FeeView(userContext, periodicFee)); FeeBO upfrontFee
     * = TestObjectFactory.createOneTimeRateFee( "Upfront Fee",
     * FeeCategory.LOAN, Double.valueOf("20"), FeeFormula.AMOUNT,
     * FeePayment.UPFRONT); feeViewList.add(new FeeView(userContext,
     * upfrontFee)); FeeBO disbursementFee =
     * TestObjectFactory.createOneTimeAmountFee( "Disbursment Fee",
     * FeeCategory.LOAN, "30", FeePayment.TIME_OF_DISBURSMENT);
     * feeViewList.add(new FeeView(userContext, disbursementFee));
     * 
     * accountBO = LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money( "300.0"),
     * Short.valueOf("6"), startDate, false, 1.2, (short) 0, new FundBO(),
     * feeViewList, null); new TestObjectPersistence().persist(accountBO);
     *Assert.assertEquals(6, accountBO.getAccountActionDates().size());
     * 
     * HashMap fees0 = new HashMap();
     * 
     * HashMap fees1 = new HashMap(); fees1.put("Periodic Fee", "100.0");
     * 
     * HashMap fees2 = new HashMap(); fees2.put("Periodic Fee", "100.0");
     * fees2.put("Upfront Fee", "60.0");
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkLoanScheduleEntity(null, "0.0", "0.1", fees2, paymentsArray[0]);
     * checkLoanScheduleEntity(null, "0.0", "0.1", fees0, paymentsArray[1]);
     * checkLoanScheduleEntity(null, "0.0", "0.1", fees1, paymentsArray[2]);
     * checkLoanScheduleEntity(null, "0.0", "0.1", fees1, paymentsArray[3]);
     * checkLoanScheduleEntity(null, "0.0", "0.1", fees0, paymentsArray[4]);
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "300.0", "0.1",
     * fees1, paymentsArray[5]);
     * 
     *Assert.assertEquals(3, accountBO.getAccountFees().size()); for
     * (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) { if
     * (accountFeesEntity.getFees().getFeeName().equals("Upfront Fee")) {
     *Assert.assertEquals(new Money("60.0"), accountFeesEntity
     * .getAccountFeeAmount());Assert.assertEquals(new Double("20.0"),
     * accountFeesEntity .getFeeAmount()); } else if
     * (accountFeesEntity.getFees().getFeeName().equals( "Disbursment Fee")) {
     *Assert.assertEquals(new Money("30.0"), accountFeesEntity
     * .getAccountFeeAmount());Assert.assertEquals(new Double("30.0"),
     * accountFeesEntity .getFeeAmount()); } else {Assert.assertEquals(new
     * Money("100.0"), accountFeesEntity .getAccountFeeAmount());
     *Assert.assertEquals(new Double("100.0"), accountFeesEntity .getFeeAmount()); } }
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();Assert.assertEquals(new Money("300.0"), loanSummaryEntity
     * .getOriginalPrincipal());Assert.assertEquals(new Money("0.6"),
     * loanSummaryEntity.getOriginalInterest());Assert.assertEquals(new
     * Money("490.0"), loanSummaryEntity.getOriginalFees());Assert.assertEquals(new
     * Money("0.0"), loanSummaryEntity.getOriginalPenalty()); }
     */

    /*
     * TODO: turn back on when InterestDeductedAtDisbursment is re-enabled
     * 
     * public void testCreateLoanAccountWithInterestDeductedAtDisbursment()
     * throws Exception { Date startDate = new Date(System.currentTimeMillis());
     * MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory
     * .getNewMeetingForToday(WEEKLY, EVERY_SECOND_WEEK, CUSTOMER_MEETING));
     * center = TestObjectFactory.createCenter("Center", meeting); group =
     * TestObjectFactory.createGroupUnderCenter("Group",
     * CustomerStatus.GROUP_ACTIVE, center); LoanOfferingBO loanOffering =
     * TestObjectFactory.createLoanOffering( "Loan", ApplicableTo.GROUPS,
     * startDate, PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, true,
     * false, center .getCustomerMeeting().getMeeting()); UserContext
     * userContext = TestUtils.makeUser(); userContext.setLocaleId(null);
     * List<FeeView> feeViewList = new ArrayList<FeeView>(); FeeBO periodicFee =
     * TestObjectFactory.createPeriodicAmountFee( "Periodic Fee",
     * FeeCategory.LOAN, "100", RecurrenceType.WEEKLY, Short.valueOf("3"));
     * feeViewList.add(new FeeView(userContext, periodicFee)); FeeBO upfrontFee
     * = TestObjectFactory.createOneTimeRateFee( "Upfront Fee",
     * FeeCategory.LOAN, Double.valueOf("20"), FeeFormula.AMOUNT,
     * FeePayment.UPFRONT); feeViewList.add(new FeeView(userContext,
     * upfrontFee)); FeeBO disbursementFee =
     * TestObjectFactory.createOneTimeAmountFee( "Disbursment Fee",
     * FeeCategory.LOAN, "30", FeePayment.TIME_OF_DISBURSMENT);
     * feeViewList.add(new FeeView(userContext, disbursementFee));
     * 
     * accountBO = LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money( "300.0"),
     * Short.valueOf("6"), startDate, true, 1.2, (short) 0, new FundBO(),
     * feeViewList, null); new TestObjectPersistence().persist(accountBO);
     * 
     * HashMap fees3 = new HashMap(); fees3.put("Periodic Fee", "100.0");
     * fees3.put("Disbursment Fee", "30.0"); fees3.put("Upfront Fee", "60.0");
     * 
     * HashMap fees0 = new HashMap();
     * 
     * HashMap fees1 = new HashMap(); fees1.put("Periodic Fee", "100.0");
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(0), "0.0", "0.6", fees3,
     * paymentsArray[0]); checkLoanScheduleEntity(null, "60.0", "0.0", fees0,
     * paymentsArray[1]); checkLoanScheduleEntity(null, "60.0", "0.0", fees1,
     * paymentsArray[2]); checkLoanScheduleEntity(null, "60.0", "0.0", fees1,
     * paymentsArray[3]); checkLoanScheduleEntity(null, "60.0", "0.0", fees0,
     * paymentsArray[4]); checkLoanScheduleEntity(incrementCurrentDate(14 * 5),
     * "60.0", "0.0", fees1, paymentsArray[5]);
     * 
     *Assert.assertEquals(3, accountBO.getAccountFees().size()); for
     * (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) { if
     * (accountFeesEntity.getFees().getFeeName().equals("Upfront Fee")) {
     *Assert.assertEquals(new Money("60.0"), accountFeesEntity
     * .getAccountFeeAmount());Assert.assertEquals(new Double("20.0"),
     * accountFeesEntity .getFeeAmount()); } else if
     * (accountFeesEntity.getFees().getFeeName().equals( "Disbursment Fee")) {
     *Assert.assertEquals(new Money("30.0"), accountFeesEntity
     * .getAccountFeeAmount());Assert.assertEquals(new Double("30.0"),
     * accountFeesEntity .getFeeAmount()); } else {Assert.assertEquals(new
     * Money("100.0"), accountFeesEntity .getAccountFeeAmount());
     *Assert.assertEquals(new Double("100.0"), accountFeesEntity .getFeeAmount()); } }
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();Assert.assertEquals(new Money("300.0"), loanSummaryEntity
     * .getOriginalPrincipal());Assert.assertEquals(new Money("0.6"),
     * loanSummaryEntity.getOriginalInterest());Assert.assertEquals(new
     * Money("490.0"), loanSummaryEntity.getOriginalFees());Assert.assertEquals(new
     * Money("0.0"), loanSummaryEntity.getOriginalPenalty()); }
     */

    /*
     * TODO: turn back on when IntersetDeductedAtDisbursement and
     * PrincipalDueInLastPayment are re-enabled
     * 
     * 
     * public void testCreateLoanAccountWithIDADAndPDILI() throws Exception {
     * Date startDate = new Date(System.currentTimeMillis()); MeetingBO meeting
     * = TestObjectFactory.createMeeting(TestObjectFactory
     * .getNewMeetingForToday(WEEKLY, EVERY_SECOND_WEEK, CUSTOMER_MEETING));
     * center = TestObjectFactory.createCenter("Center", meeting); group =
     * TestObjectFactory.createGroupUnderCenter("Group",
     * CustomerStatus.GROUP_ACTIVE, center); LoanOfferingBO loanOffering =
     * TestObjectFactory.createLoanOffering( startDate,
     * center.getCustomerMeeting().getMeeting()); UserContext userContext =
     * TestUtils.makeUser(); userContext.setLocaleId(null); List<FeeView>
     * feeViewList = new ArrayList<FeeView>(); FeeBO periodicFee =
     * TestObjectFactory.createPeriodicAmountFee( "Periodic Fee",
     * FeeCategory.LOAN, "100", RecurrenceType.WEEKLY, Short.valueOf("3"));
     * feeViewList.add(new FeeView(userContext, periodicFee)); FeeBO upfrontFee
     * = TestObjectFactory.createOneTimeRateFee( "Upfront Fee",
     * FeeCategory.LOAN, Double.valueOf("20"), FeeFormula.AMOUNT,
     * FeePayment.UPFRONT); feeViewList.add(new FeeView(userContext,
     * upfrontFee)); FeeBO disbursementFee =
     * TestObjectFactory.createOneTimeAmountFee( "Disbursment Fee",
     * FeeCategory.LOAN, "30", FeePayment.TIME_OF_DISBURSMENT);
     * feeViewList.add(new FeeView(userContext, disbursementFee));
     * 
     * accountBO = LoanBO.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money( "300.0"),
     * Short.valueOf("6"), startDate, false, 1.2, (short) 0, new FundBO(),
     * feeViewList, null); new TestObjectPersistence().persist(accountBO);
     * 
     * HashMap fees0 = new HashMap();
     * 
     * HashMap fees1 = new HashMap(); fees1.put("Periodic Fee", "100.0");
     * 
     * HashMap fees3 = new HashMap(); fees3.put("Periodic Fee", "100.0");
     * fees3.put("Disbursment Fee", "30.0"); fees3.put("Upfront Fee", "60.0");
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(0), "0.0", "0.6", null,
     * null, null, fees3, null, null, null, paymentsArray[0]);
     * checkLoanScheduleEntity(null, "0.0", "0.0", null, null, null, fees0,
     * null, null, null, paymentsArray[1]); checkLoanScheduleEntity(null, "0.0",
     * "0.0", null, null, null, fees1, null, null, null, paymentsArray[2]);
     * checkLoanScheduleEntity(null, "0.0", "0.0", null, null, null, fees1,
     * null, null, null, paymentsArray[3]); checkLoanScheduleEntity(null, "0.0",
     * "0.0", null, null, null, fees0, null, null, null, paymentsArray[4]);
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 5), "300.0", "0.0",
     * null, null, null, fees1, null, null, null, paymentsArray[5]);
     * 
     *Assert.assertEquals(3, accountBO.getAccountFees().size()); for
     * (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) { if
     * (accountFeesEntity.getFees().getFeeName().equals("Upfront Fee")) {
     *Assert.assertEquals(new Money("60.0"), accountFeesEntity
     * .getAccountFeeAmount());Assert.assertEquals(new Double("20.0"),
     * accountFeesEntity .getFeeAmount()); } else if
     * (accountFeesEntity.getFees().getFeeName().equals( "Disbursment Fee")) {
     *Assert.assertEquals(new Money("30.0"), accountFeesEntity
     * .getAccountFeeAmount());Assert.assertEquals(new Double("30.0"),
     * accountFeesEntity .getFeeAmount()); } else {Assert.assertEquals(new
     * Money("100.0"), accountFeesEntity .getAccountFeeAmount());
     *Assert.assertEquals(new Double("100.0"), accountFeesEntity .getFeeAmount()); } }
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();Assert.assertEquals(new Money("300.0"), loanSummaryEntity
     * .getOriginalPrincipal());Assert.assertEquals(new Money("0.6"),
     * loanSummaryEntity.getOriginalInterest());Assert.assertEquals(new
     * Money("490.0"), loanSummaryEntity.getOriginalFees());Assert.assertEquals(new
     * Money("0.0"), loanSummaryEntity.getOriginalPenalty()); }
     */

    public void testCreateNormalLoanAccount() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, center.getCustomerMeeting().getMeeting());
        UserContext userContext = TestUtils.makeUser();
        userContext.setLocaleId(null);
        List<FeeView> feeViewList = new ArrayList<FeeView>();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "100",
                RecurrenceType.WEEKLY, Short.valueOf("3"));
        feeViewList.add(new FeeView(userContext, periodicFee));
        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT);
        feeViewList.add(new FeeView(userContext, upfrontFee));
        FeeBO disbursementFee = TestObjectFactory.createOneTimeAmountFee("Disbursment Fee", FeeCategory.LOAN, "30",
                FeePayment.TIME_OF_DISBURSMENT);
        feeViewList.add(new FeeView(userContext, disbursementFee));

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), startDate, false,
                1.2, (short) 0, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);

        Map<String, String> fees0 = new HashMap<String, String>();

        Map<String, String> fees1 = new HashMap<String, String>();
        fees1.put("Periodic Fee", "100.0");

        Map<String, String> fees2 = new HashMap<String, String>();
        fees2.put("Periodic Fee", "100.0");
        fees2.put("Upfront Fee", "60.0");

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkLoanScheduleEntity(incrementCurrentDate(14), "50.8", "0.2", fees2, paymentsArray[0]);
        checkLoanScheduleEntity(null, "50.8", "0.2", fees0, paymentsArray[1]);
        checkLoanScheduleEntity(null, "50.8", "0.2", fees1, paymentsArray[2]);
        checkLoanScheduleEntity(null, "50.8", "0.2", fees1, paymentsArray[3]);
        checkLoanScheduleEntity(null, "50.8", "0.2", fees0, paymentsArray[4]);
        checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "46.0", "0.0", fees1, paymentsArray[5]);

       Assert.assertEquals(3, accountBO.getAccountFees().size());
        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
            if (accountFeesEntity.getFees().getFeeName().equals("Upfront Fee")) {
               Assert.assertEquals(new Money("60.0"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("20.0"), accountFeesEntity.getFeeAmount());
            } else if (accountFeesEntity.getFees().getFeeName().equals("Disbursment Fee")) {
               Assert.assertEquals(new Money("30.0"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("30.0"), accountFeesEntity.getFeeAmount());
            } else {
               Assert.assertEquals(new Money("100.0"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("100.0"), accountFeesEntity.getFeeAmount());
            }
        }
        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
       Assert.assertEquals(new Money("300.0"), loanSummaryEntity.getOriginalPrincipal());
       Assert.assertEquals(new Money("1.0"), loanSummaryEntity.getOriginalInterest());
       Assert.assertEquals(new Money("490.0"), loanSummaryEntity.getOriginalFees());
       Assert.assertEquals(new Money("0.0"), loanSummaryEntity.getOriginalPenalty());
    }

    public void testCreateNormalLoanAccountWithPricipalOnlyGrace() throws Exception {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", "Loan".substring(0, 1),
                ApplicableTo.GROUPS, new Date(System.currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 1.2,
                (short) 3, InterestType.FLAT, true, false, center.getCustomerMeeting().getMeeting(),
                GraceType.PRINCIPALONLYGRACE, "1", "1");
        UserContext userContext = TestUtils.makeUser();
        userContext.setLocaleId(null);
        List<FeeView> feeViewList = new ArrayList<FeeView>();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "100",
                RecurrenceType.WEEKLY, Short.valueOf("3"));
        feeViewList.add(new FeeView(userContext, periodicFee));
        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT);
        feeViewList.add(new FeeView(userContext, upfrontFee));
        FeeBO disbursementFee = TestObjectFactory.createOneTimeAmountFee("Disbursment Fee", FeeCategory.LOAN, "30",
                FeePayment.TIME_OF_DISBURSMENT);
        feeViewList.add(new FeeView(userContext, disbursementFee));

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), new Date(System
                        .currentTimeMillis()), false, 1.2, (short) 1, new FundBO(), feeViewList, null, DOUBLE_ZERO,
                DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);

        Map<String, String> fees0 = new HashMap<String, String>();

        Map<String, String> fees1 = new HashMap<String, String>();
        fees1.put("Periodic Fee", "100.0");

        Map<String, String> fees2 = new HashMap<String, String>();
        fees2.put("Periodic Fee", "100.0");
        fees2.put("Upfront Fee", "60.0");

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkLoanScheduleEntity(incrementCurrentDate(14), "0.0", "1.0", fees2, paymentsArray[0]);
        checkLoanScheduleEntity(null, "60.8", "0.2", fees0, paymentsArray[1]);
        checkLoanScheduleEntity(null, "60.8", "0.2", fees1, paymentsArray[2]);
        checkLoanScheduleEntity(null, "60.8", "0.2", fees1, paymentsArray[3]);
        checkLoanScheduleEntity(null, "60.8", "0.2", fees0, paymentsArray[4]);
        checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "56.8", "-0.8", fees1, paymentsArray[5]);

       Assert.assertEquals(3, accountBO.getAccountFees().size());
        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
            if (accountFeesEntity.getFees().getFeeName().equals("Upfront Fee")) {
               Assert.assertEquals(new Money("60.0"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("20.0"), accountFeesEntity.getFeeAmount());
            } else if (accountFeesEntity.getFees().getFeeName().equals("Disbursment Fee")) {
               Assert.assertEquals(new Money("30.0"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("30.0"), accountFeesEntity.getFeeAmount());
            } else {
               Assert.assertEquals(new Money("100.0"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("100.0"), accountFeesEntity.getFeeAmount());
            }
        }
        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
       Assert.assertEquals(new Money("300.0"), loanSummaryEntity.getOriginalPrincipal());
       Assert.assertEquals(new Money("1.0"), loanSummaryEntity.getOriginalInterest());
       Assert.assertEquals(new Money("490.0"), loanSummaryEntity.getOriginalFees());
       Assert.assertEquals(new Money("0.0"), loanSummaryEntity.getOriginalPenalty());
    }

    public void testCreateNormalLoanAccountWithMonthlyInstallments() throws Exception {
        Short dayOfMonth = (short) 1;

        /*
         * A date in the past won't work (we don't yet have a way of telling the
         * validation code "pretend it is such-and-such a date"). The
         * errors.startdateexception message says the date must be less than a
         * year in the future (it really means an end date less than the end of
         * next year).
         */
        long sampleTime = new DateMidnight(2009, 11, 25).getMillis();

        MeetingBO meeting = TestObjectFactory.getNewMeeting(MONTHLY, EVERY_SECOND_MONTH, CUSTOMER_MEETING, MONDAY);
        Calendar meetingStart = Calendar.getInstance();
        meetingStart.setTimeInMillis(sampleTime);
        meeting.setMeetingStartDate(meetingStart.getTime());
        meeting.getMeetingDetails().getMeetingRecurrence().setDayNumber(dayOfMonth);
        TestObjectFactory.createMeeting(meeting);
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        Date loanStart = new Date(sampleTime);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, loanStart,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, center.getCustomerMeeting().getMeeting());
        Calendar disbursementDate = new GregorianCalendar();
        disbursementDate.setTimeInMillis(sampleTime);
        int year = disbursementDate.get(Calendar.YEAR);
        int zeroBasedMonth = disbursementDate.get(Calendar.MONTH);
        /*
         * TODO: this "if" is a relic from when sampleTime was based on when the
         * test was run. We should test these two cases somehow.
         */
        if (disbursementDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth.intValue()) {
            disbursementDate = new GregorianCalendar(year, zeroBasedMonth, dayOfMonth);
        } else {
            disbursementDate = new GregorianCalendar(year, zeroBasedMonth + 1, dayOfMonth);
        }
        UserContext userContext = TestUtils.makeUser();
        userContext.setLocaleId(null);
        List<FeeView> feeViewList = new ArrayList<FeeView>();
        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT);
        feeViewList.add(new FeeView(userContext, upfrontFee));
        FeeBO disbursementFee = TestObjectFactory.createOneTimeRateFee("Disbursment Fee", FeeCategory.LOAN, Double
                .valueOf("30"), FeeFormula.AMOUNT_AND_INTEREST, FeePayment.TIME_OF_DISBURSMENT);
        feeViewList.add(new FeeView(userContext, disbursementFee));
        FeeBO firstRepaymentFee = TestObjectFactory.createOneTimeRateFee("First Repayment Fee", FeeCategory.LOAN,
                Double.valueOf("40"), FeeFormula.INTEREST, FeePayment.TIME_OF_FIRSTLOANREPAYMENT);
        feeViewList.add(new FeeView(userContext, firstRepaymentFee));
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "100",
                RecurrenceType.MONTHLY, Short.valueOf("1"));
        feeViewList.add(new FeeView(userContext, periodicFee));
        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), disbursementDate
                        .getTime(), false, 1.2, (short) 0, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO,
                SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);

        Map<String, String> fees1 = new HashMap<String, String>();
        fees1.put("Periodic Fee", "200.0");

        Map<String, String> fees3 = new HashMap<String, String>();
        fees3.put("Periodic Fee", "100.0");
        fees3.put("Upfront Fee", "60.0");
        fees3.put("First Repayment Fee", "1.5");

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkLoanScheduleEntity(null, "50.9", "0.6", fees3, paymentsArray[0]);
        checkLoanScheduleEntity(null, "50.4", "0.6", fees1, paymentsArray[1]);
        checkLoanScheduleEntity(null, "50.4", "0.6", fees1, paymentsArray[2]);
        checkLoanScheduleEntity(null, "50.4", "0.6", fees1, paymentsArray[3]);
        checkLoanScheduleEntity(null, "50.4", "0.6", fees1, paymentsArray[4]);
        checkLoanScheduleEntity(null, "47.5", "1.5", fees1, paymentsArray[5]);

       Assert.assertEquals(4, accountBO.getAccountFees().size());
        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
            if (accountFeesEntity.getFees().getFeeName().equals("Upfront Fee")) {
               Assert.assertEquals(new Money("60.0"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("20.0"), accountFeesEntity.getFeeAmount());
            } else if (accountFeesEntity.getFees().getFeeName().equals("Disbursment Fee")) {
                // TODO: fee_rounding should there be 2 digits to
                // the right of the decimal here?
               Assert.assertEquals(new Money("91.08"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("30.0"), accountFeesEntity.getFeeAmount());
            } else if (accountFeesEntity.getFees().getFeeName().equals("First Repayment Fee")) {
               Assert.assertEquals(new Money("1.44"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("40.0"), accountFeesEntity.getFeeAmount());
            } else {
               Assert.assertEquals(new Money("100.0"), accountFeesEntity.getAccountFeeAmount());
               Assert.assertEquals(new Double("100.0"), accountFeesEntity.getFeeAmount());
            }
        }
        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
       Assert.assertEquals(new Money("300.0"), loanSummaryEntity.getOriginalPrincipal());
        // TODO: fee_rounding is the difference in original interest just
        // a result of compounded rounding adjustments?
        //Assert.assertEquals(new Money("3.6"),
        // loanSummaryEntity.getOriginalInterest());
       Assert.assertEquals(new Money("4.5"), loanSummaryEntity.getOriginalInterest());
        //Assert.assertEquals(new Money("1252.5"),
        // loanSummaryEntity.getOriginalFees());
       Assert.assertEquals(new Money("1252.58"), loanSummaryEntity.getOriginalFees());
       Assert.assertEquals(new Money("0.0"), loanSummaryEntity.getOriginalPenalty());
    }

    public void testDisburseLoanWithAllTypeOfFees() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        Short dayOfMonth = (short) 25;
        MeetingBO meeting = TestObjectFactory.getNewMeeting(MONTHLY, EVERY_MONTH, CUSTOMER_MEETING, MONDAY);

        TestObjectFactory.createMeeting(meeting);
        meeting.setMeetingStartDate(new Date());
        meeting.getMeetingDetails().getMeetingRecurrence().setDayNumber(dayOfMonth);
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, center.getCustomerMeeting().getMeeting());
        Calendar disbursementDate = new GregorianCalendar();
        int year = disbursementDate.get(Calendar.YEAR);
        int month = disbursementDate.get(Calendar.MONTH);
        int day = 25;
        if (disbursementDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth.intValue()) {
            disbursementDate = new GregorianCalendar(year, month, day);
        } else {
            disbursementDate = new GregorianCalendar(year, month + 1, day);
        }
        UserContext userContext = TestUtils.makeUser();
        userContext.setLocaleId(null);
        List<FeeView> feeViewList = new ArrayList<FeeView>();
        FeeBO upfrontFee = TestObjectFactory.createOneTimeRateFee("Upfront Fee", FeeCategory.LOAN,
                Double.valueOf("20"), FeeFormula.AMOUNT, FeePayment.UPFRONT);
        feeViewList.add(new FeeView(userContext, upfrontFee));
        FeeBO disbursementFee = TestObjectFactory.createOneTimeRateFee("Disbursment Fee", FeeCategory.LOAN, Double
                .valueOf("30"), FeeFormula.AMOUNT_AND_INTEREST, FeePayment.TIME_OF_DISBURSMENT);
        feeViewList.add(new FeeView(userContext, disbursementFee));
        FeeBO firstRepaymentFee = TestObjectFactory.createOneTimeRateFee("First Repayment Fee", FeeCategory.LOAN,
                Double.valueOf("40"), FeeFormula.INTEREST, FeePayment.TIME_OF_FIRSTLOANREPAYMENT);
        feeViewList.add(new FeeView(userContext, firstRepaymentFee));
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "100",
                RecurrenceType.MONTHLY, Short.valueOf("1"));
        feeViewList.add(new FeeView(userContext, periodicFee));
        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), disbursementDate
                        .getTime(), false, 1.2, (short) 0, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO,
                SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);

        disbursementDate = new GregorianCalendar();
        year = disbursementDate.get(Calendar.YEAR);
        month = disbursementDate.get(Calendar.MONTH);
        day = 25;
        if (disbursementDate.get(Calendar.DAY_OF_MONTH) == dayOfMonth.intValue()) {
            disbursementDate = new GregorianCalendar(year, month + 1, day);
        } else {
            disbursementDate = new GregorianCalendar(year, month + 2, day);
        }
        ((LoanBO) accountBO).disburseLoan("1234", disbursementDate.getTime(), Short.valueOf("1"), accountBO
                .getPersonnel(), disbursementDate.getTime(), Short.valueOf("1"));
        Session session = StaticHibernateUtil.getSessionTL();
        StaticHibernateUtil.startTransaction();
        session.save(accountBO);
        StaticHibernateUtil.getTransaction().commit();
    }

    public void testUpdateLoanFailure() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        createInitialCustomers();
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, center.getCustomerMeeting()
                .getMeeting());
        accountBO = TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_PENDING_APPROVAL,
                startDate, loanOffering);
        try {
            LoanBO loanBO = (LoanBO) accountBO;
            ((LoanBO) accountBO).updateLoan(loanBO.isInterestDeductedAtDisbursement(), loanBO.getLoanAmount(), loanBO
                    .getInterestRate(), loanBO.getNoOfInstallments(), offSetCurrentDate(15), loanBO
                    .getGracePeriodDuration(), loanBO.getBusinessActivityId(), "Loan account updated", null, null,
                    false, null);
            Assert.fail("The Loan object is created for invalid disbursement date");
        } catch (AccountException expected) {
            if (new ConfigurationPersistence().isRepaymentIndepOfMeetingEnabled()) {
               Assert.assertEquals("exceptions.application.loan.invalidDisbursement", expected.getKey());
            } else {
               Assert.assertEquals("errors.invalidDisbursementDate", expected.getKey());
            }

        }
    }

    public void testUpdateLoanSuccess() {
        accountBO = getLoanAccount();
        try {
            LoanBO loanBO = (LoanBO) accountBO;
            ((LoanBO) accountBO).updateLoan(loanBO.isInterestDeductedAtDisbursement(), loanBO.getLoanAmount(), loanBO
                    .getInterestRate(), loanBO.getNoOfInstallments(), loanBO.getDisbursementDate(), loanBO
                    .getGracePeriodDuration(), loanBO.getBusinessActivityId(), "Loan account updated", null, null,
                    false, null);
           Assert.assertTrue("The Loan object is created for valid disbursement date", true);
        } catch (AccountException ae) {
            Assert.assertFalse("The Loan object is created for valid disbursement date", true);
        }
    }

    public void testUpdateLoanWithInterestDeductedInterestDeductedAtDisbursement() {
        final short NUMBER_OF_INSTALLMENTS = 1;
        accountBO = getLoanAccount();
        try {
            LoanBO loanBO = (LoanBO) accountBO;
            ((LoanBO) accountBO).updateLoan(true, loanBO.getLoanAmount(), loanBO.getInterestRate(),
                    NUMBER_OF_INSTALLMENTS, loanBO.getDisbursementDate(), loanBO.getGracePeriodDuration(), loanBO
                            .getBusinessActivityId(), "Loan account updated", null, null, false, null);
            Assert.fail("Invalid no of installment");
        } catch (AccountException ae) {
           Assert.assertTrue("Invalid no of installment", true);
        }
    }

    public void testUpdateLoanWithInterestDeductedNoInterestDeductedAtDisbursement() {
        final short NUMBER_OF_INSTALLMENTS = 1;
        accountBO = getLoanAccount();
        try {
            LoanBO loanBO = (LoanBO) accountBO;
            ((LoanBO) accountBO).updateLoan(false, loanBO.getLoanAmount(), loanBO.getInterestRate(),
                    NUMBER_OF_INSTALLMENTS, loanBO.getDisbursementDate(), loanBO.getGracePeriodDuration(), loanBO
                            .getBusinessActivityId(), "Loan account updated", null, null, false, null);
        } catch (AccountException ae) {
            Assert.fail("Invalid no of installment");
        }
    }

    /*
     * TODO: fee_rounding 0.3 is expected as the onetime fee, but 0.27 is
     * returned. Should this be rounded or not?
     * 
     * public void testApplyTimeOfFirstRepaymentFee() throws Exception {
     * accountBO = getLoanAccount(); Money intialTotalFeeAmount = ((LoanBO)
     * accountBO).getLoanSummary() .getOriginalFees();
     * TestObjectFactory.flushandCloseSession(); FeeBO oneTimeFee =
     * TestObjectFactory.createOneTimeRateFee( "Onetime Fee", FeeCategory.LOAN,
     * new Double("0.09"), FeeFormula.AMOUNT,
     * FeePayment.TIME_OF_FIRSTLOANREPAYMENT); accountBO =
     * TestObjectFactory.getObject(AccountBO.class, accountBO .getAccountId());
     * UserContext uc = TestUtils.makeUser(); accountBO.setUserContext(uc);
     * accountBO.applyCharge(oneTimeFee.getFeeId(), ((RateFeeBO) oneTimeFee)
     * .getRate()); StaticHibernateUtil.commitTransaction();
     * StaticHibernateUtil.closeSession(); accountBO =
     * TestObjectFactory.getObject(AccountBO.class, accountBO .getAccountId());
     * 
     * HashMap fees2 = new HashMap(); fees2.put("Onetime Fee", "0.3");
     * fees2.put("Mainatnence Fee", "100.0");
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates();Assert.assertEquals(6, actionDateEntities.size());
     * LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkFees(fees2, paymentsArray[0], false);
     * 
     *Assert.assertEquals(intialTotalFeeAmount.add(new Money("0.3")), ((LoanBO)
     * accountBO).getLoanSummary().getOriginalFees()); LoanActivityEntity
     * loanActivityEntity = ((LoanActivityEntity) (((LoanBO) accountBO)
     * .getLoanActivityDetails().toArray())[0]);
     *Assert.assertEquals(oneTimeFee.getFeeName() + " applied", loanActivityEntity
     * .getComments());Assert.assertEquals(((LoanBO)
     * accountBO).getLoanSummary().getOriginalFees(),
     * loanActivityEntity.getFeeOutstanding()); AccountFeesEntity
     * accountFeesEntity = accountBO .getAccountFees(oneTimeFee.getFeeId());
     *Assert.assertEquals(FeeStatus.ACTIVE, accountFeesEntity.getFeeStatusAsEnum()); }
     */
    public void testApplyPaymentForFullPayment() throws Exception {
        accountBO = getLoanAccount();
       Assert.assertEquals(new Money("212.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("212"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money(), ((LoanBO) accountBO).getTotalPaymentDue());
    }

    public void testApplyPaymentForPartialPayment() throws Exception {
        accountBO = getLoanAccount();
       Assert.assertEquals(new Money("212.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("200"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money("12"), ((LoanBO) accountBO).getTotalPaymentDue());
       Assert.assertEquals(Integer.valueOf(0), ((LoanBO) accountBO).getPerformanceHistory().getNoOfPayments());
    }

    public void testApplyPaymentForFuturePayment() throws Exception {
        accountBO = getLoanAccount();

        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money("212.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        LoanScheduleEntity nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO)
                .getApplicableIdsForFutureInstallments().get(0);
       Assert.assertEquals(new Money("212.0"), nextInstallment.getTotalDueWithFees());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("312"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money(), ((LoanBO) accountBO).getTotalPaymentDue());
        nextInstallment = (LoanScheduleEntity) accountBO.getAccountActionDate(nextInstallment.getInstallmentId());
       Assert.assertEquals(new Money("112.0"), nextInstallment.getTotalDueWithFees());
    }

    public void testApplyPaymentForCompletePayment() throws Exception {
        accountBO = getLoanAccount();

        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money("212.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        LoanScheduleEntity nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO)
                .getApplicableIdsForFutureInstallments().get(0);
       Assert.assertEquals(new Money("212.0"), nextInstallment.getTotalDueWithFees());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("1272"),
                accountBO.getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System
                        .currentTimeMillis()), new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money(), ((LoanBO) accountBO).getTotalPaymentDue());
        nextInstallment = (LoanScheduleEntity) accountBO.getAccountActionDate(nextInstallment.getInstallmentId());
       Assert.assertEquals(new Money(), nextInstallment.getTotalDueWithFees());
       Assert.assertEquals(AccountState.LOAN_CLOSED_OBLIGATIONS_MET, accountBO.getState());
    }

    public void testApplyPaymentForPaymentGretaterThanTotalDue() throws Exception {
        accountBO = getLoanAccount();

        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money("212.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        LoanScheduleEntity nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO)
                .getApplicableIdsForFutureInstallments().get(0);
       Assert.assertEquals(new Money("212.0"), nextInstallment.getTotalDueWithFees());
        try {
            accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("1300"),
                    accountBO.getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System
                            .currentTimeMillis()), new Date(System.currentTimeMillis())));
            Assert.assertFalse(true);
        } catch (AccountException e) {
           Assert.assertTrue(true);
        }
    }

    public void testFeeForMultiplePayments() throws Exception {
        accountBO = getLoanAccount();

        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money("212.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        LoanScheduleEntity nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO).getDetailsOfNextInstallment();
       Assert.assertEquals(new Money("100.0"), nextInstallment.getTotalFeeDue());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("10"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
        nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO).getDetailsOfNextInstallment();
       Assert.assertEquals(new Money("90"), nextInstallment.getTotalFeeDue());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("30"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
        nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO).getDetailsOfNextInstallment();
       Assert.assertEquals(new Money("60"), nextInstallment.getTotalFeeDue());
    }

    public void testLoanPerfHistoryForUndisbursedLoans() throws Exception {
        accountBO = getLoanAccount();
        LoanBO loan = (LoanBO) accountBO;
        Date disbursementDate = offSetCurrentDate(28);
        AccountActionDateEntity accountActionDate1 = loan.getAccountActionDate((short) 1);
        AccountActionDateEntity accountActionDate2 = loan.getAccountActionDate((short) 2);
        accountBO.setUserContext(TestObjectFactory.getContext());
        accountBO.changeStatus(AccountState.LOAN_APPROVED, null, "");
        ((LoanScheduleEntity) accountActionDate1).setActionDate(offSetCurrentDate(21));
        ((LoanScheduleEntity) accountActionDate2).setActionDate(offSetCurrentDate(14));
        loan.setDisbursementDate(disbursementDate);
        accountBO = saveAndFetch(loan);
        loan = (LoanBO) accountBO;
       Assert.assertEquals(Integer.valueOf("0"), loan.getPerformanceHistory().getTotalNoOfMissedPayments());
       Assert.assertEquals(Short.valueOf("0"), loan.getPerformanceHistory().getDaysInArrears());
       Assert.assertEquals(Integer.valueOf("0"), loan.getPerformanceHistory().getNoOfPayments());
    }

    public void testFeeForMultiplePaymentsIncludingCompletePayment() throws Exception {
        accountBO = getLoanAccount();

        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money("212.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        LoanScheduleEntity nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO).getDetailsOfNextInstallment();
       Assert.assertEquals(new Money("100.0"), nextInstallment.getTotalFeeDue());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("10"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
        nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO).getDetailsOfNextInstallment();
       Assert.assertEquals(new Money("90"), nextInstallment.getTotalFeeDue());
       Assert.assertEquals(new Money("202.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("202"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
        nextInstallment = (LoanScheduleEntity) ((LoanBO) accountBO).getDetailsOfNextInstallment();
       Assert.assertEquals(new Money(), nextInstallment.getTotalFeeDue());

    }

    /**
     * TODO: Re-enable this test when rounding code correctly handles mid-stream
     * changes to the loan schedule.
     */
    public void testRemoveFeeForPartiallyPaidFeesAccount() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = createOfferingNoPrincipalInLastInstallment(startDate, center.getCustomerMeeting()
                .getMeeting());
        List<FeeView> feeViewList = new ArrayList<FeeView>();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "100",
                RecurrenceType.WEEKLY, Short.valueOf("1"));
        feeViewList.add(new FeeView(TestObjectFactory.getContext(), periodicFee));

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), startDate, false,
                1.2, (short) 0, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());
        StaticHibernateUtil.closeSession();

        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());

        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("60"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, startDate, startDate));
        StaticHibernateUtil.commitTransaction();

        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
            accountBO.removeFees(accountFeesEntity.getFees().getFeeId(), Short.valueOf("1"));
        }
        StaticHibernateUtil.commitTransaction();

        Map<String, String> fees0 = new HashMap<String, String>();

        Map<String, String> fees1 = new HashMap<String, String>();
        fees1.put("Periodic Fee", "60");

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
       Assert.assertEquals(6, actionDateEntities.size());
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkFees(fees1, paymentsArray[0], false);
        checkFees(fees0, paymentsArray[1], false);
        checkFees(fees0, paymentsArray[2], false);
        checkFees(fees0, paymentsArray[3], false);
        checkFees(fees0, paymentsArray[4], false);
        checkFees(fees0, paymentsArray[5], false);

        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
           Assert.assertEquals(FeeStatus.INACTIVE, accountFeesEntity.getFeeStatusAsEnum());
            Assert.assertNull(accountFeesEntity.getLastAppliedDate());
        }
        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
       Assert.assertEquals(new Money("60"), loanSummaryEntity.getFeesPaid());
       Assert.assertEquals(new Money("60"), loanSummaryEntity.getOriginalFees());
       Assert.assertEquals(new Money(), loanSummaryEntity.getFeesDue());
        for (LoanActivityEntity loanActivityEntity : ((LoanBO) accountBO).getLoanActivityDetails()) {
            if (loanActivityEntity.getComments().equalsIgnoreCase("Periodic Fee removed")) {
               Assert.assertEquals(loanSummaryEntity.getFeesDue(), loanActivityEntity.getFeeOutstanding());
               Assert.assertEquals(new Money("1040"), loanActivityEntity.getFee());
                break;
            }
        }
    }

    /**
     * TODO: Re-enable this test when rounding code correctly handles mid-stream
     * changes to the loan schedule.
     */
    public void testApplyChargeForPartiallyPaidFeesAccount() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, center.getCustomerMeeting().getMeeting());
        List<FeeView> feeViewList = new ArrayList<FeeView>();
        FeeBO periodicFee = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "100",
                RecurrenceType.WEEKLY, Short.valueOf("1"));
        feeViewList.add(new FeeView(TestObjectFactory.getContext(), periodicFee));

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), startDate, false,
                1.2, (short) 0, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());
        StaticHibernateUtil.closeSession();

        accountBO = (AccountBO) StaticHibernateUtil.getSessionTL().get(AccountBO.class, accountBO.getAccountId());

        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("60"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, startDate, startDate));
        StaticHibernateUtil.commitTransaction();

        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
            accountBO.removeFees(accountFeesEntity.getFees().getFeeId(), Short.valueOf("1"));
        }
        StaticHibernateUtil.commitTransaction();
        Map<String, String> fees0 = new HashMap<String, String>(0);

        Map<String, String> fees1 = new HashMap<String, String>();
        fees1.put("Periodic Fee", "60");
        Map<String, String> feesPaid1 = new HashMap<String, String>();
        feesPaid1.put("Periodic Fee", "60");

        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(((LoanBO) accountBO)
                .getAccountActionDates());

       Assert.assertEquals(6, paymentsArray.length);
        checkLoanScheduleEntity(null, null, null, null, null, null, fees1, feesPaid1, null, null, paymentsArray[0]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fees0, null, null, null, paymentsArray[1]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fees0, null, null, null, paymentsArray[2]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fees0, null, null, null, paymentsArray[3]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fees0, null, null, null, paymentsArray[4]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fees0, null, null, null, paymentsArray[5]);

        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
           Assert.assertEquals(FeeStatus.INACTIVE, accountFeesEntity.getFeeStatusAsEnum());
            Assert.assertNull(accountFeesEntity.getLastAppliedDate());
        }
        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
       Assert.assertEquals(new Money("60"), loanSummaryEntity.getFeesPaid());
       Assert.assertEquals(new Money("60"), loanSummaryEntity.getOriginalFees());
       Assert.assertEquals(new Money(), loanSummaryEntity.getFeesDue());
        for (LoanActivityEntity loanActivityEntity : ((LoanBO) accountBO).getLoanActivityDetails()) {
            if (loanActivityEntity.getComments().equalsIgnoreCase("Periodic Fee removed")) {
               Assert.assertEquals(loanSummaryEntity.getFeesDue(), loanActivityEntity.getFeeOutstanding());
               Assert.assertEquals(new Money("1040"), loanActivityEntity.getFee());
                break;
            }
        }

        accountBO.setUserContext(TestUtils.makeUser());
        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
            accountBO.applyCharge(accountFeesEntity.getFees().getFeeId(), Double.valueOf("200"));
        }
        StaticHibernateUtil.commitTransaction();

        Map<String, String> fee260 = new HashMap<String, String>();
        fee260.put("Periodic Fee", "260");

        Map<String, String> fee400 = new HashMap<String, String>();
        fee400.put("Periodic Fee", "400");

        Set<AccountActionDateEntity> actionDateEntities1 = ((LoanBO) accountBO).getAccountActionDates();
       Assert.assertEquals(6, actionDateEntities1.size());
        LoanScheduleEntity[] paymentsArray1 = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities1);

        checkLoanScheduleEntity(null, null, null, null, null, null, fee260, null, null, null, paymentsArray1[0]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fee400, null, null, null, paymentsArray1[1]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fee400, null, null, null, paymentsArray1[2]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fee400, null, null, null, paymentsArray1[3]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fee400, null, null, null, paymentsArray1[4]);
        checkLoanScheduleEntity(null, null, null, null, null, null, fee400, null, null, null, paymentsArray1[5]);

        // not sure if the remaining is being handled by validatePayments
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            LoanScheduleEntity loanScheduleEntity = (LoanScheduleEntity) accountActionDateEntity;
            if (!loanScheduleEntity.getInstallmentId().equals(Short.valueOf("1"))) {
               Assert.assertEquals(1, loanScheduleEntity.getAccountFeesActionDetails().size());
                for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : loanScheduleEntity
                        .getAccountFeesActionDetails()) {
                    LoanFeeScheduleEntity loanFeeScheduleEntity = (LoanFeeScheduleEntity) accountFeesActionDetailEntity;
                    Assert.assertNull(loanFeeScheduleEntity.getFeeAmountPaid());
                }
            }
        }

        for (AccountFeesEntity accountFeesEntity : accountBO.getAccountFees()) {
           Assert.assertEquals(FeeStatus.ACTIVE, accountFeesEntity.getFeeStatusAsEnum());
            Assert.assertNotNull(accountFeesEntity.getLastAppliedDate());
        }
        loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
       Assert.assertEquals(new Money("60"), loanSummaryEntity.getFeesPaid());
       Assert.assertEquals(new Money("2260"), loanSummaryEntity.getOriginalFees());
       Assert.assertEquals(new Money("2200"), loanSummaryEntity.getFeesDue());
        for (LoanActivityEntity loanActivityEntity : ((LoanBO) accountBO).getLoanActivityDetails()) {
            if (loanActivityEntity.getComments().equalsIgnoreCase("Periodic Fee applied")) {
               Assert.assertEquals(loanSummaryEntity.getFeesDue(), loanActivityEntity.getFeeOutstanding());
               Assert.assertEquals(new Money("2200"), loanActivityEntity.getFee());
                break;
            }
        }
    }

    public void testPartialPaymentForPrincipalGrace() throws Exception {
        accountBO = getLoanAccount();
        ((LoanScheduleEntity) accountBO.getAccountActionDate((short) 1)).setPrincipal(new Money());
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money("112.0"), ((LoanBO) accountBO).getTotalPaymentDue());
        accountBO.applyPaymentWithPersist(TestObjectFactory.getLoanAccountPaymentData(null, new Money("100"), accountBO
                .getCustomer(), accountBO.getPersonnel(), "432423", (short) 1, new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis())));
        accountBO = saveAndFetch(accountBO);
       Assert.assertEquals(new Money("12"), ((LoanBO) accountBO).getTotalPaymentDue());
       Assert.assertEquals(Integer.valueOf(0), ((LoanBO) accountBO).getPerformanceHistory().getNoOfPayments());
        Assert.assertFalse(((LoanBO) accountBO).getAccountActionDate((short) 1).isPaid());
    }

    public void testGetDaysInArrears() {
        java.sql.Date lastWeekDate = setDate(new GregorianCalendar().DAY_OF_MONTH, -14);

        java.sql.Date twoWeeksBeforeDate = setDate(new GregorianCalendar().DAY_OF_MONTH, -21);
        accountBO = getLoanAccount();
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
            } else if (installment.getInstallmentId().intValue() == 2) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
            }
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(Short.valueOf("21"), ((LoanBO) accountBO).getDaysInArrears());
    }

    public void testGetDaysWithoutPaymentWhendaysLessThanLateness() throws PersistenceException {
        int daysLessThanLateness = -7;
        java.sql.Date lastWeekDate = setDate(new GregorianCalendar().DAY_OF_MONTH, daysLessThanLateness);
        accountBO = getLoanAccount();
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(0, ((LoanBO) accountBO).getDaysWithoutPayment());

    }

    public void testGetDaysWithoutPaymentWhendaysMoreThanLateness() throws PersistenceException {
        int daysMoreThanLateness = -21;
        java.sql.Date twoWeeksBeforeDate = setDate(new GregorianCalendar().DAY_OF_MONTH, daysMoreThanLateness);
        accountBO = getLoanAccount();

        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(21, ((LoanBO) accountBO).getDaysWithoutPayment());

    }

    public void testGetTotalInterestAmountInArrears() {
        java.sql.Date lastWeekDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -1);
        java.sql.Date twoWeeksBeforeDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -2);
        accountBO = getLoanAccount();
        Money interest = new Money();
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
                interest = interest.add(((LoanScheduleEntity) installment).getInterest());
            } else if (installment.getInstallmentId().intValue() == 2) {
                interest = interest.add(((LoanScheduleEntity) installment).getInterest());
                ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
            }
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(interest, ((LoanBO) accountBO).getTotalInterestAmountInArrears());
    }

    public void testGetTotalInterestAmountInArrearsAndOutsideLateness() throws PersistenceException {

        java.sql.Date lastWeekDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -1);
        java.sql.Date twoWeeksBeforeDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -2);
        accountBO = getLoanAccount();
        Money interest = new Money();
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
            } else if (installment.getInstallmentId().intValue() == 2) {
                interest = interest.add(((LoanScheduleEntity) installment).getInterest());
                ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
            }
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(interest, ((LoanBO) accountBO).getTotalInterestAmountInArrearsAndOutsideLateness());
    }

    public void testGetTotalPrincipalAmount() {
        accountBO = getLoanAccount();
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(new Money("600"), ((LoanBO) accountBO).getTotalPrincipalAmount());
    }

    public void testGetTotalPrincipalAmountInArrears() {
        java.sql.Date lastWeekDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -1);
        java.sql.Date twoWeeksBeforeDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -2);
        accountBO = getLoanAccount();

        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
            } else if (installment.getInstallmentId().intValue() == 2) {
                ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
            }
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(new Money("200"), ((LoanBO) accountBO).getTotalPrincipalAmountInArrears());
    }

    public void testGetTotalPrincipalAmountInArrearsAndOutsideLateness() throws PersistenceException {
        java.sql.Date lastWeekDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -1);
        java.sql.Date twoWeeksBeforeDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -2);
        accountBO = getLoanAccount();
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
            }

            else if (installment.getInstallmentId().intValue() == 2) {
                ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
            }
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(new Money("100"), ((LoanBO) accountBO).getTotalPrincipalAmountInArrearsAndOutsideLateness());
    }

    public void testGetDisbursementTerm() throws Exception {
        accountBO = getLoanAccount();
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(0, ((LoanBO) accountBO).getDisbursementTerm());

    }

    public void testGetNetOfSaving() throws Exception {
        accountBO = getLoanAccount();
        LoanBO loanAccount = (LoanBO) accountBO;
       Assert.assertEquals(loanAccount.getRemainingPrincipalAmount().subtract(loanAccount.getCustomer().getSavingsBalance()),
                loanAccount.getNetOfSaving());
    }

    public void testGetPaymentsInArrears() throws Exception {
        java.sql.Date lastWeekDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -1);
        java.sql.Date twoWeeksBeforeDate = setDate(new GregorianCalendar().WEEK_OF_MONTH, -2);

        accountBO = getLoanAccount();
        for (AccountActionDateEntity installment : accountBO.getAccountActionDates()) {
            if (installment.getInstallmentId().intValue() == 1) {
                ((LoanScheduleEntity) installment).setActionDate(lastWeekDate);
            }

            else if (installment.getInstallmentId().intValue() == 2) {
                ((LoanScheduleEntity) installment).setActionDate(twoWeeksBeforeDate);
            }
        }
        TestObjectFactory.updateObject(accountBO);
        TestObjectFactory.flushandCloseSession();
        accountBO = TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
       Assert.assertEquals(1.0, ((LoanBO) accountBO).getPaymentsInArrears(), DELTA);

    }

    public void testSaveLoanForInvalidConnection() throws Exception {
        createInitialCustomers();
        LoanOfferingBO loanOffering = createLoanOffering(false);
        List<FeeView> feeViews = getFeeViews();

        LoanBO loan = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group, AccountState.LOAN_APPROVED,
                new Money("300.0"), Short.valueOf("6"), new Date(System.currentTimeMillis()), false, 10.0, (short) 0,
                new FundBO(), feeViews, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        TestObjectFactory.simulateInvalidConnection();
        try {
            loan.save();
            Assert.fail();
        } catch (AccountException e) {
           Assert.assertTrue(true);
        } finally {
            StaticHibernateUtil.closeSession();
        }
        deleteFee(feeViews);
        TestObjectFactory.removeObject(loanOffering);
    }

    public void testUpdateLoanFOrInvalidConnection() {
        accountBO = getLoanAccount();
        TestObjectFactory.simulateInvalidConnection();
        try {
            accountBO.update();
            Assert.fail();
        } catch (AccountException e) {
           Assert.assertTrue(true);
        } finally {
            StaticHibernateUtil.closeSession();
        }

    }

    public void testSuccessUpdateTotalFeeAmount() {
        accountBO = getLoanAccount();
        LoanBO loanBO = (LoanBO) accountBO;
        LoanSummaryEntity loanSummaryEntity = loanBO.getLoanSummary();
        Money orignalFeesAmount = loanSummaryEntity.getOriginalFees();
        loanBO.updateTotalFeeAmount(new Money(TestObjectFactory.getMFICurrency(), "20"));
       Assert.assertEquals(loanSummaryEntity.getOriginalFees(), orignalFeesAmount.subtract(new Money(TestObjectFactory
                .getMFICurrency(), "20")));
    }

    public void testCreateLoanAccountWithDecliningInterestNoGracePeriod() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", "L", ApplicableTo.GROUPS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 12.0, (short) 3, InterestType.DECLINING, false, false, center
                        .getCustomerMeeting().getMeeting(), GraceType.NONE, "1", "1");
        List<FeeView> feeViewList = new ArrayList<FeeView>();

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), startDate, false, // 6
                // installments
                12.0, (short) 0, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());

        Map<String, String> fees0 = new HashMap<String, String>();

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkLoanScheduleEntity(incrementCurrentDate(7 * 1), "50.3", "0.7", fees0, paymentsArray[0]);

        checkLoanScheduleEntity(incrementCurrentDate(7 * 2), "50.4", "0.6", fees0, paymentsArray[1]);

        checkLoanScheduleEntity(incrementCurrentDate(7 * 3), "50.5", "0.5", fees0, paymentsArray[2]);

        checkLoanScheduleEntity(incrementCurrentDate(7 * 4), "50.6", "0.4", fees0, paymentsArray[3]);

        checkLoanScheduleEntity(incrementCurrentDate(7 * 5), "50.7", "0.3", fees0, paymentsArray[4]);

        checkLoanScheduleEntity(incrementCurrentDate(7 * 6), "47.5", "0.5", fees0, paymentsArray[5]);

        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();

       Assert.assertEquals(new Money("300.0"), loanSummaryEntity.getOriginalPrincipal());

    }

    /**
     * Check amount of the account fee in the installment. Note that this only
     * works correctly if the installment has just one account fee.
     */
    /*
     * private void assertOneInstallmentFee (Money expected, LoanScheduleEntity
     * installment) { Set<AccountFeesActionDetailEntity> actionDetails =
     * installment.getAccountFeesActionDetails(); for
     * (AccountFeesActionDetailEntity detail : actionDetails) {Assert.assertEquals
     * (expected, detail.getFeeAmount()); } }
     * 
     * private void assertInstallmentDetails (LoanScheduleEntity installment,
     * Double principal, Double interest, Double accountFee, Double miscFee,
     * Double miscPenalty) {Assert.assertEquals(new Money(principal.toString()),
     * installment.getPrincipalDue());Assert.assertEquals(new
     * Money(interest.toString()), installment.getInterestDue());
     *Assert.assertEquals(new Money(miscFee.toString()), installment.getMiscFeeDue());
     *Assert.assertEquals(new Money(miscPenalty.toString()),
     * installment.getMiscPenaltyDue()); assertOneInstallmentFee(new
     * Money(accountFee.toString()), installment); }
     */
    public void testCreateLoanAccountWithDecliningInterestWithFeesNoGracePeriod() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", "L", ApplicableTo.GROUPS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 12.0, (short) 3, InterestType.DECLINING, false, false, center
                        .getCustomerMeeting().getMeeting(), GraceType.NONE, "1", "1");

        FeeBO fee = TestObjectFactory.createPeriodicRateFee("Periodic Rate Fee", FeeCategory.LOAN, new Double("9.0"),
                FeeFormula.INTEREST, RecurrenceType.WEEKLY, new Short("1"), userContext, meeting);
        feeViews.add(new FeeView(userContext, fee));
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), startDate, false, // 6
                12.0, (short) 0, new FundBO(), feeViews, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());

        LoanBO loan = (LoanBO) accountBO;
        loan.applyCharge(Short.valueOf(AccountConstants.MISC_FEES), 2.0);
        loan.applyCharge(Short.valueOf(AccountConstants.MISC_PENALTY), 10.0);

        Set<AccountActionDateEntity> actionDateEntities = loan.getAccountActionDates();
        LoanScheduleEntity[] sortedInstallments = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
        LoanTestUtils.assertInstallmentDetails(sortedInstallments[0], 50.0, 0.7, 0.3, 2.0, 10.0);
        LoanTestUtils.assertInstallmentDetails(sortedInstallments[1], 50.1, 0.6, 0.3, 0.0, 0.0);
        LoanTestUtils.assertInstallmentDetails(sortedInstallments[2], 50.2, 0.5, 0.3, 0.0, 0.0);
        LoanTestUtils.assertInstallmentDetails(sortedInstallments[3], 50.3, 0.4, 0.3, 0.0, 0.0);
        LoanTestUtils.assertInstallmentDetails(sortedInstallments[4], 50.4, 0.3, 0.3, 0.0, 0.0);
        LoanTestUtils.assertInstallmentDetails(sortedInstallments[5], 49.0, 0.1, -0.1, 0.0, 0.0);

        /*
         * HashMap fees0 = new HashMap();
         * 
         * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO)
         * accountBO) .getAccountActionDates(); LoanScheduleEntity[]
         * paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
         * 
         * checkLoanScheduleEntity(incrementCurrentDate(7 * 1), "50.0", "0.7",
         * fees0, paymentsArray[0]); assertOneInstallmentFee(new Money("0.3"),
         * paymentsArray[0]);
         * 
         * checkLoanScheduleEntity(incrementCurrentDate(7 * 2), "50.1", "0.6",
         * fees0, paymentsArray[1]); assertOneInstallmentFee(new Money("0.3"),
         * paymentsArray[1]);
         * 
         * checkLoanScheduleEntity(incrementCurrentDate(7 * 3), "50.2", "0.5",
         * fees0, paymentsArray[2]); assertOneInstallmentFee(new Money("0.3"),
         * paymentsArray[2]);
         * 
         * checkLoanScheduleEntity(incrementCurrentDate(7 * 4), "50.3", "0.4",
         * fees0, paymentsArray[3]); assertOneInstallmentFee(new Money("0.3"),
         * paymentsArray[3]);
         * 
         * checkLoanScheduleEntity(incrementCurrentDate(7 * 5), "50.4", "0.3",
         * fees0, paymentsArray[4]); assertOneInstallmentFee(new Money("0.3"),
         * paymentsArray[4]);
         * 
         * checkLoanScheduleEntity(incrementCurrentDate(7 * 6), "49.0", "0.1",
         * fees0, paymentsArray[5]); assertOneInstallmentFee(new Money("-0.1"),
         * paymentsArray[5]);
         * 
         * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
         * .getLoanSummary();
         * 
         *Assert.assertEquals(new Money("300.0"), loanSummaryEntity
         * .getOriginalPrincipal());
         */
    }

    public void testCreateLoanAccountWithZeroFlatInterest() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", "L", ApplicableTo.GROUPS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 0.0, (short) 3, InterestType.FLAT, false, false, center
                        .getCustomerMeeting().getMeeting(), GraceType.NONE, "1", "1");
        List<FeeView> feeViewList = new ArrayList<FeeView>();

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), startDate, false,
                0.0, (short) 0, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());

        Map<String, String> fees0 = new HashMap<String, String>();

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 1), "50.0", "0.0", fees0, paymentsArray[0]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 2), "50.0", "0.0", fees0, paymentsArray[1]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 3), "50.0", "0.0", fees0, paymentsArray[2]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 4), "50.0", "0.0", fees0, paymentsArray[3]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 5), "50.0", "0.0", fees0, paymentsArray[4]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "50.0", "0.0", fees0, paymentsArray[5]);

        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();

       Assert.assertEquals(new Money("300.0"), loanSummaryEntity.getOriginalPrincipal());

    }

    public void testCreateLoanAccountWithZeroDecliningInterest() throws Exception {
        Date startDate = new Date(System.currentTimeMillis());

        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", "L", ApplicableTo.GROUPS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 0.0, (short) 3, InterestType.DECLINING, false, false, center
                        .getCustomerMeeting().getMeeting(), GraceType.NONE, "1", "1");
        List<FeeView> feeViewList = new ArrayList<FeeView>();

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), startDate, false,
                0.0, (short) 0, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());

        Map<String, String> fees0 = new HashMap<String, String>();

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 1), "50.0", "0.0", fees0, paymentsArray[0]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 2), "50.0", "0.0", fees0, paymentsArray[1]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 3), "50.0", "0.0", fees0, paymentsArray[2]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 4), "50.0", "0.0", fees0, paymentsArray[3]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 5), "50.0", "0.0", fees0, paymentsArray[4]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "50.0", "0.0", fees0, paymentsArray[5]);

        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();

       Assert.assertEquals(new Money("300.0"), loanSummaryEntity.getOriginalPrincipal());

    }

    public void testCreateLoanAccountWithDecliningInterestGraceAllRepayments() throws Exception {

        short graceDuration = (short) 2;
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                EVERY_SECOND_WEEK, CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, new Date(System
                .currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 12.0, (short) 3, InterestType.DECLINING, center
                .getCustomerMeeting().getMeeting());
        List<FeeView> feeViewList = new ArrayList<FeeView>();

        accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), new Date(System
                        .currentTimeMillis()), false, // 6 installments
                12.0, graceDuration, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO, SHORT_ZERO);
        new TestObjectPersistence().persist(accountBO);
       Assert.assertEquals(6, accountBO.getAccountActionDates().size());
        Map<String, String> fees0 = new HashMap<String, String>();

        Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
        LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

        checkLoanScheduleEntity(incrementCurrentDate(14 * (1 + graceDuration)), "49.6", "1.4", fees0, paymentsArray[0]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * (2 + graceDuration)), "49.8", "1.2", fees0, paymentsArray[1]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * (3 + graceDuration)), "50.0", "1.0", fees0, paymentsArray[2]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * (4 + graceDuration)), "50.3", "0.7", fees0, paymentsArray[3]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * (5 + graceDuration)), "50.5", "0.5", fees0, paymentsArray[4]);

        checkLoanScheduleEntity(incrementCurrentDate(14 * (6 + graceDuration)), "49.8", "0.2", fees0, paymentsArray[5]);

        LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
       Assert.assertEquals(new Money("300.0"), loanSummaryEntity.getOriginalPrincipal());

    }

    public void testCreateLoanAccountWithDecliningInterestGracePrincipalOnly() throws Exception {

        BigDecimal savedInitialRoundingMode = AccountingRules.getInitialRoundOffMultiple();
        BigDecimal savedFinalRoundingMode = AccountingRules.getFinalRoundOffMultiple();

        AccountingRules.setInitialRoundOffMultiple(new BigDecimal("0.1"));
        AccountingRules.setFinalRoundOffMultiple(new BigDecimal("0.1"));

        try {
            short graceDuration = (short) 2;
            MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY,
                    EVERY_SECOND_WEEK, CUSTOMER_MEETING));
            center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
            group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
            LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan", "L", ApplicableTo.GROUPS,
                    new Date(System.currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 12.0, (short) 3,
                    InterestType.DECLINING, false, false, center.getCustomerMeeting().getMeeting(),
                    GraceType.PRINCIPALONLYGRACE, "1", "1");
            List<FeeView> feeViewList = new ArrayList<FeeView>();

            accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
                    AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money("300.0"), Short.valueOf("6"), new Date(System
                            .currentTimeMillis()),
                    false, // 6 installments
                    12.0, graceDuration, new FundBO(), feeViewList, null, DOUBLE_ZERO, DOUBLE_ZERO, SHORT_ZERO,
                    SHORT_ZERO);
            new TestObjectPersistence().persist(accountBO);
           Assert.assertEquals(6, accountBO.getAccountActionDates().size());

            Map<String, String> fees0 = new HashMap<String, String>();

            Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO).getAccountActionDates();
            LoanScheduleEntity[] paymentsArray = LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);

            checkLoanScheduleEntity(incrementCurrentDate(14 * 1), "0.0", "1.4", fees0, paymentsArray[0]);

            checkLoanScheduleEntity(incrementCurrentDate(14 * 2), "0.0", "1.4", fees0, paymentsArray[1]);

            checkLoanScheduleEntity(incrementCurrentDate(14 * 3), "74.5", "1.4", fees0, paymentsArray[2]);

            checkLoanScheduleEntity(incrementCurrentDate(14 * 4), "74.8", "1.1", fees0, paymentsArray[3]);

            checkLoanScheduleEntity(incrementCurrentDate(14 * 5), "75.2", "0.7", fees0, paymentsArray[4]);

            checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "75.5", "0.3", fees0, paymentsArray[5]);

            LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO).getLoanSummary();
           Assert.assertEquals(new Money("300.0"), loanSummaryEntity.getOriginalPrincipal());
        } finally {
            AccountingRules.setInitialRoundOffMultiple(savedInitialRoundingMode);
            AccountingRules.setFinalRoundOffMultiple(savedFinalRoundingMode);
        }
    }

    /*
     * TODO: turn back on when PrincipalDueInLastPayment is re-enabled
     * 
     * 
     * public void
     * testCreateLoanAccountWithDecliningInterestPrincipalDueOnLastInstallment()
     * throws NumberFormatException, PropertyNotFoundException, SystemException,
     * ApplicationException { Date startDate = new
     * Date(System.currentTimeMillis());
     * 
     * short graceDuration = (short) 2; MeetingBO meeting =
     * TestObjectFactory.createMeeting(TestObjectFactory
     * .getNewMeetingForToday(WEEKLY, EVERY_SECOND_WEEK, CUSTOMER_MEETING));
     * center = TestObjectFactory.createCenter("Center", meeting); group =
     * TestObjectFactory.createGroupUnderCenter("Group",
     * CustomerStatus.GROUP_ACTIVE, center); LoanOfferingBO loanOffering =
     * TestObjectFactory.createLoanOffering( "Loan", "Loan".substring(0, 1),
     * ApplicableTo.GROUPS, startDate, PrdStatus.LOAN_ACTIVE, 300.0, 1.2,
     * (short) 3, InterestType.DECLINING, false, true, center
     * .getCustomerMeeting().getMeeting(), GraceType.NONE, "1", "1");
     * List<FeeView> feeViewList = new ArrayList<FeeView>();
     * 
     * accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money( "300.0"),
     * Short.valueOf("6"), startDate, false, // 6 // installments 1.2,
     * graceDuration, new FundBO(), feeViewList, null); new
     * TestObjectPersistence().persist(accountBO);Assert.assertEquals(6,
     * accountBO.getAccountActionDates().size());
     * 
     * HashMap fees0 = new HashMap();
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 1), "0.0", "0.1",
     * fees0, paymentsArray[0]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 2), "0.0", "0.1",
     * fees0, paymentsArray[1]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 3), "0.0", "0.1",
     * fees0, paymentsArray[2]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 4), "0.0", "0.1",
     * fees0, paymentsArray[3]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 5), "0.0", "0.1",
     * fees0, paymentsArray[4]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "300.0", "0.1",
     * fees0, paymentsArray[5]);
     * 
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();Assert.assertEquals(new Money("300.0"), loanSummaryEntity
     * .getOriginalPrincipal());
     * 
     * }
     */
    /*
     * TODO: turn back on when EPI (equal principal loans) is re-enabled
     * 
     * 
     * 
     * public void
     * testCreateLoanAccountWithEqualPrincipalDecliningInterestNoGracePeriod()
     * throws NumberFormatException, PropertyNotFoundException, SystemException,
     * ApplicationException { Date startDate = new
     * Date(System.currentTimeMillis());
     * 
     * MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory
     * .getNewMeetingForToday(WEEKLY, EVERY_SECOND_WEEK, CUSTOMER_MEETING));
     * center = TestObjectFactory.createCenter("Center", meeting); group =
     * TestObjectFactory.createGroupUnderCenter("Group",
     * CustomerStatus.GROUP_ACTIVE, center); LoanOfferingBO loanOffering =
     * TestObjectFactory.createLoanOffering( "Loan", "L", ApplicableTo.GROUPS,
     * startDate, PrdStatus.LOAN_ACTIVE, 300.0, 1.2, (short) 3,
     * InterestType.DECLINING_EPI, false, false, center
     * .getCustomerMeeting().getMeeting(), GraceType.NONE, "1", "1");
     * List<FeeView> feeViewList = new ArrayList<FeeView>();
     * 
     * accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money( "300.0"),
     * Short.valueOf("6"), startDate, false, // 6 // installments 1.2, (short)
     * 0, new FundBO(), feeViewList, null); new
     * TestObjectPersistence().persist(accountBO);Assert.assertEquals(6,
     * accountBO.getAccountActionDates().size());
     * 
     * HashMap fees0 = new HashMap();
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 1), "50.9", "0.1",
     * fees0, paymentsArray[0]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 2), "50.9", "0.1",
     * fees0, paymentsArray[1]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 3), "50.9", "0.1",
     * fees0, paymentsArray[2]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 4), "50.9", "0.1",
     * fees0, paymentsArray[3]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 5), "50.0", "0.0",
     * fees0, paymentsArray[4]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "46.4", "0.0",
     * fees0, paymentsArray[5]);
     * 
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();
     * 
     *Assert.assertEquals(new Money("300.0"), loanSummaryEntity
     * .getOriginalPrincipal());
     * 
     * }
     * 
     * 
     * public void
     * testCreateLoanAccountWithEqualPrincipalDecliningInterestGraceAllRepayments
     * () throws NumberFormatException, PropertyNotFoundException,
     * SystemException, ApplicationException {
     * 
     * short graceDuration = (short) 2; MeetingBO meeting =
     * TestObjectFactory.createMeeting(TestObjectFactory
     * .getNewMeetingForToday(WEEKLY, EVERY_SECOND_WEEK, CUSTOMER_MEETING));
     * center = TestObjectFactory.createCenter("Center", meeting); group =
     * TestObjectFactory.createGroupUnderCenter("Group",
     * CustomerStatus.GROUP_ACTIVE, center); LoanOfferingBO loanOffering =
     * TestObjectFactory.createLoanOffering( "Loan", ApplicableTo.GROUPS, new
     * Date(System .currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 1.2,
     * (short) 3, InterestType.DECLINING_EPI,
     * center.getCustomerMeeting().getMeeting()); List<FeeView> feeViewList =
     * new ArrayList<FeeView>();
     * 
     * accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money( "300.0"),
     * Short.valueOf("6"), new Date(System .currentTimeMillis()), false, // 6
     * installments 1.2, graceDuration, new FundBO(), feeViewList, null); new
     * TestObjectPersistence().persist(accountBO);Assert.assertEquals(6,
     * accountBO.getAccountActionDates().size()); HashMap fees0 = new HashMap();
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * (1 + graceDuration)),
     * "50.9", "0.1", fees0, paymentsArray[0]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * (2 + graceDuration)),
     * "50.9", "0.1", fees0, paymentsArray[1]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * (3 + graceDuration)),
     * "50.9", "0.1", fees0, paymentsArray[2]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * (4 + graceDuration)),
     * "50.9", "0.1", fees0, paymentsArray[3]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * (5 + graceDuration)),
     * "50.0", "0.0", fees0, paymentsArray[4]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * (6 + graceDuration)),
     * "46.4", "0.0", fees0, paymentsArray[5]);
     * 
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();Assert.assertEquals(new Money("300.0"), loanSummaryEntity
     * .getOriginalPrincipal());
     * 
     * }
     * 
     * public void
     * testCreateLoanAccountWithEqualPrincipalDecliningInterestGracePrincipalOnly
     * () throws NumberFormatException, PropertyNotFoundException,
     * SystemException, ApplicationException {
     * 
     * short graceDuration = (short) 2; MeetingBO meeting =
     * TestObjectFactory.createMeeting(TestObjectFactory
     * .getNewMeetingForToday(WEEKLY, EVERY_SECOND_WEEK, CUSTOMER_MEETING));
     * center = TestObjectFactory.createCenter("Center", meeting); group =
     * TestObjectFactory.createGroupUnderCenter("Group",
     * CustomerStatus.GROUP_ACTIVE, center); LoanOfferingBO loanOffering =
     * TestObjectFactory.createLoanOffering( "Loan", "L", ApplicableTo.GROUPS,
     * new Date(System .currentTimeMillis()), PrdStatus.LOAN_ACTIVE, 300.0, 1.2,
     * (short) 3, InterestType.DECLINING_EPI, false, false,
     * center.getCustomerMeeting().getMeeting(), GraceType.PRINCIPALONLYGRACE,
     * "1", "1"); List<FeeView> feeViewList = new ArrayList<FeeView>();
     * 
     * accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money( "300.0"),
     * Short.valueOf("6"), new Date(System .currentTimeMillis()), false, // 6
     * installments 1.2, graceDuration, new FundBO(), feeViewList, null); new
     * TestObjectPersistence().persist(accountBO);Assert.assertEquals(6,
     * accountBO.getAccountActionDates().size());
     * 
     * HashMap fees0 = new HashMap();
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 1), "0.0", "0.1",
     * fees0, paymentsArray[0]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 2), "0.0", "0.1",
     * fees0, paymentsArray[1]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 3), "75.0", "0.1",
     * fees0, paymentsArray[2]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 4), "75.0", "0.1",
     * fees0, paymentsArray[3]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 5), "75.0", "0.1",
     * fees0, paymentsArray[4]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "75.0", "0.0",
     * fees0, paymentsArray[5]);
     * 
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();Assert.assertEquals(new Money("300.0"), loanSummaryEntity
     * .getOriginalPrincipal());
     * 
     * }
     * 
     * 
     * public void
     * testCreateLoanAccountWithEqualPrincipalDecliningInterestPrincipalDueOnLastInstallment
     * () throws NumberFormatException, PropertyNotFoundException,
     * SystemException, ApplicationException { Date startDate = new
     * Date(System.currentTimeMillis());
     * 
     * short graceDuration = (short) 2; MeetingBO meeting =
     * TestObjectFactory.createMeeting(TestObjectFactory
     * .getNewMeetingForToday(WEEKLY, EVERY_SECOND_WEEK, CUSTOMER_MEETING));
     * center = TestObjectFactory.createCenter("Center", meeting); group =
     * TestObjectFactory.createGroupUnderCenter("Group",
     * CustomerStatus.GROUP_ACTIVE, center); LoanOfferingBO loanOffering =
     * TestObjectFactory.createLoanOffering( "Loan", "Loan".substring(0, 1),
     * ApplicableTo.GROUPS, startDate, PrdStatus.LOAN_ACTIVE, 300.0, 1.2,
     * (short) 3, InterestType.DECLINING_EPI, false, true, center
     * .getCustomerMeeting().getMeeting(), GraceType.NONE, "1", "1");
     * List<FeeView> feeViewList = new ArrayList<FeeView>();
     * 
     * accountBO = loanDao.createLoan(TestUtils.makeUser(), loanOffering, group,
     * AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, new Money( "300.0"),
     * Short.valueOf("6"), startDate, false, // 6 // installments 1.2,
     * graceDuration, new FundBO(), feeViewList, null); new
     * TestObjectPersistence().persist(accountBO);Assert.assertEquals(6,
     * accountBO.getAccountActionDates().size());
     * 
     * HashMap fees0 = new HashMap();
     * 
     * Set<AccountActionDateEntity> actionDateEntities = ((LoanBO) accountBO)
     * .getAccountActionDates(); LoanScheduleEntity[] paymentsArray =
     * LoanBOTestUtils.getSortedAccountActionDateEntity(actionDateEntities);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 1), "0.0", "0.1",
     * fees0, paymentsArray[0]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 2), "0.0", "0.1",
     * fees0, paymentsArray[1]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 3), "0.0", "0.1",
     * fees0, paymentsArray[2]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 4), "0.0", "0.1",
     * fees0, paymentsArray[3]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 5), "0.0", "0.1",
     * fees0, paymentsArray[4]);
     * 
     * checkLoanScheduleEntity(incrementCurrentDate(14 * 6), "300.0", "0.1",
     * fees0, paymentsArray[5]);
     * 
     * LoanSummaryEntity loanSummaryEntity = ((LoanBO) accountBO)
     * .getLoanSummary();Assert.assertEquals(new Money("300.0"), loanSummaryEntity
     * .getOriginalPrincipal());
     * 
     * }
     */

    private java.sql.Date setDate(final int dayUnit, final int interval) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(DateUtils.getCurrentDateWithoutTimeStamp());
        calendar.add(dayUnit, interval);
        return new java.sql.Date(calendar.getTimeInMillis());
    }

    private LoanBO createAndRetrieveLoanAccount(final LoanOfferingBO loanOffering, final boolean isInterestDedAtDisb,
            final List<FeeView> feeViews, final Short noOfinstallments, final Double interestRate, final Money loanAmount)
            throws NumberFormatException, AccountException, SystemException, ApplicationException {
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        // LoanBO loan = LoanBO.createLoan(TestUtils.makeUser(), loanOffering,
        LoanBO loan = LoanBO
                .createLoan(userContext, loanOffering, group, AccountState.LOAN_APPROVED, new Money("300.0"),
                        noOfinstallments, new Date(System.currentTimeMillis()), isInterestDedAtDisb, interestRate,
                        (short) 0, new FundBO(), feeViews, null, DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT,
                        eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(),
                        false, null);
        loan.save();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        accountBO = TestObjectFactory.getObject(AccountBO.class, loan.getAccountId());
        LoanBO loanBO = (LoanBO) accountBO;
        return loanBO;
    }

    /**
     * Added by Keith -- the original, createAndRetrieveLoanAccount(), when it
     * retrieves the loan from the database, would have its userContext set to
     * null -- this would happen in the test method
     * testLoanScheduleRoundingWithMiscFee(). This modified method saves the
     * loan but does not replace it with the retrieved one before returning it.
     */
    private LoanBO createButDontRetrieveLoanAccount(final LoanOfferingBO loanOffering, final boolean isInterestDedAtDisb,
            final List<FeeView> feeViews, final Short noOfinstallments, final Double interestRate, final Money loanAmount)
            throws Exception {
        LoanOfferingInstallmentRange eligibleInstallmentRange = loanOffering.getEligibleInstallmentSameForAllLoan();

        // LoanBO loan = LoanBO.createLoan(TestUtils.makeUser(), loanOffering,
        LoanBO loan = LoanBO
                .createLoan(userContext, loanOffering, group, AccountState.LOAN_APPROVED, new Money("300.0"),
                        noOfinstallments, new Date(System.currentTimeMillis()), isInterestDedAtDisb, interestRate,
                        (short) 0, new FundBO(), feeViews, null, DEFAULT_LOAN_AMOUNT, DEFAULT_LOAN_AMOUNT,
                        eligibleInstallmentRange.getMaxNoOfInstall(), eligibleInstallmentRange.getMinNoOfInstall(),
                        false, null);
        loan.save();
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();

        return loan;
    }

    private LoanBO createAndRetrieveLoanAccount(final LoanOfferingBO loanOffering, final boolean isInterestDedAtDisb,
            final List<FeeView> feeViews, final Short noOfinstallments) throws NumberFormatException, AccountException,
            SystemException, ApplicationException {
        return createAndRetrieveLoanAccount(loanOffering, isInterestDedAtDisb, feeViews, noOfinstallments, 10.0,
                new Money("300.0"));
    }

    private LoanOfferingBO createLoanOffering(final boolean isPrincipalAtLastInst) {
        return createLoanOffering(isPrincipalAtLastInst, false, PrdStatus.LOAN_ACTIVE);
    }

    // TODO: check where this is used to see if the test cases that use it
    // need to be commented out
    private LoanOfferingBO createLoanOffering(final boolean principalAtLastInst, final boolean interestDeductedAtDisbursement,
            final PrdStatus status) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        return TestObjectFactory.createLoanOffering("Loan", ApplicableTo.GROUPS, new Date(System.currentTimeMillis()),
                status, 300.0, 1.2, 3, InterestType.FLAT, interestDeductedAtDisbursement, principalAtLastInst, meeting);
    }

    private List<FeeView> getFeeViews() {
        FeeBO fee1 = TestObjectFactory.createOneTimeAmountFee("One Time Amount Fee", FeeCategory.LOAN, "120.0",
                FeePayment.TIME_OF_DISBURSMENT);
        FeeBO fee3 = TestObjectFactory.createPeriodicAmountFee("Periodic Fee", FeeCategory.LOAN, "10.0",
                RecurrenceType.WEEKLY, (short) 1);
        List<FeeView> feeViews = new ArrayList<FeeView>();
        FeeView feeView1 = new FeeView(userContext, fee1);
        FeeView feeView2 = new FeeView(userContext, fee3);
        feeViews.add(feeView1);
        feeViews.add(feeView2);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        return feeViews;
    }

    private void deleteFee(final List<FeeView> feeViews) {
        for (FeeView feeView : feeViews) {
            TestObjectFactory.cleanUp((FeeBO) TestObjectFactory.getObject(FeeBO.class, feeView.getFeeIdValue()));
        }

    }

    private AccountBO getLoanAccount() {
        Date startDate = new Date(System.currentTimeMillis());
        createInitialCustomers();
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, center.getCustomerMeeting()
                .getMeeting());
        return TestObjectFactory.createLoanAccount("42423142341", group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);
    }

    private AccountBO getBasicLoanAccount() {
        Date startDate = new Date(System.currentTimeMillis());
        createInitialCustomers();
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, center.getCustomerMeeting()
                .getMeeting());
        return TestObjectFactory.createBasicLoanAccount(group, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, startDate,
                loanOffering);
    }

    private void createInitialCustomers() {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewWeeklyMeeting(EVERY_WEEK));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
    }

    private void changeFirstInstallmentDateToNextDate(final AccountBO accountBO) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day + 1);
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            ((LoanScheduleEntity) accountActionDateEntity).setActionDate(new java.sql.Date(currentDateCalendar
                    .getTimeInMillis()));
            break;
        }
    }

    private AccountBO applyPaymentandRetrieveAccount() throws AccountException, SystemException {
        Date startDate = new Date(System.currentTimeMillis());
        PaymentData paymentData = PaymentData.createPaymentData(new Money(Configuration.getInstance().getSystemConfig()
                .getCurrency(), "212.0"), accountBO.getPersonnel(), Short.valueOf("1"), startDate);
        paymentData.setReceiptDate(startDate);
        paymentData.setReceiptNum("5435345");

        accountBO.applyPaymentWithPersist(paymentData);
        StaticHibernateUtil.commitTransaction();
        StaticHibernateUtil.closeSession();
        return TestObjectFactory.getObject(AccountBO.class, accountBO.getAccountId());
    }

    private AccountBO getLoanAccountWithMiscFeeAndPenalty(final AccountState state, final Date startDate, final int disbursalType,
            final Money miscFee, final Money miscPenalty) {
        LoanBO accountBO = getLoanAccount(state, startDate, disbursalType);
        for (AccountActionDateEntity accountAction : accountBO.getAccountActionDates()) {
            LoanScheduleEntity accountActionDateEntity = (LoanScheduleEntity) accountAction;
            if (accountActionDateEntity.getInstallmentId().equals(Short.valueOf("1"))) {
                accountActionDateEntity.setMiscFee(miscFee);
                accountActionDateEntity.setMiscPenalty(miscPenalty);
                break;
            }
        }
        LoanSummaryEntity loanSummaryEntity = accountBO.getLoanSummary();
        loanSummaryEntity.setOriginalFees(loanSummaryEntity.getOriginalFees().add(miscFee));
        loanSummaryEntity.setOriginalPenalty(loanSummaryEntity.getOriginalPenalty().add(miscPenalty));
        TestObjectPersistence testObjectPersistence = new TestObjectPersistence();
        testObjectPersistence.update(accountBO);
        return testObjectPersistence.getObject(LoanBO.class, accountBO.getAccountId());
    }

    private void changeInstallmentDate(final AccountBO accountBO, final int numberOfDays, final Short installmentId) {
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            if (accountActionDateEntity.getInstallmentId().equals(installmentId)) {
                Calendar dateCalendar = new GregorianCalendar();
                dateCalendar.setTimeInMillis(accountActionDateEntity.getActionDate().getTime());
                int year = dateCalendar.get(Calendar.YEAR);
                int month = dateCalendar.get(Calendar.MONTH);
                int day = dateCalendar.get(Calendar.DAY_OF_MONTH);
                dateCalendar = new GregorianCalendar(year, month, day - numberOfDays);
                ((LoanScheduleEntity) accountActionDateEntity).setActionDate(new java.sql.Date(dateCalendar
                        .getTimeInMillis()));
                break;
            }
        }
    }

    private AccountBO getLoanAccountWithPerformanceHistory() {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient(this.getClass().getSimpleName() + " Client", CustomerStatus.CLIENT_ACTIVE, group);
        /*
         * ((ClientBO) client).getPerformanceHistory().setLoanCycleNumber(1);
         * TestObjectFactory.updateObject(client);
         */
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        accountBO = TestObjectFactory.createLoanAccount("42423142341", client, AccountState.LOAN_APPROVED, startDate,
                loanOffering);
        ((ClientBO) client).getClientPerformanceHistory().updateLoanCounter(loanOffering, YesNoFlag.YES);
        TestObjectFactory.updateObject(client);
        TestObjectFactory.updateObject(accountBO);
        return accountBO;
    }

    private AccountBO getLoanAccountWithPerformanceHistory(final AccountState state, final Date startDate, final int disbursalType) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient(this.getClass().getSimpleName() + " Client", CustomerStatus.CLIENT_ACTIVE, group);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        // ((ClientBO) client).getPerformanceHistory().setLoanCycleNumber(1);
        accountBO = TestObjectFactory.createLoanAccountWithDisbursement("99999999999", client, state, startDate,
                loanOffering, disbursalType);
        ((ClientBO) client).getClientPerformanceHistory().updateLoanCounter(loanOffering, YesNoFlag.YES);
        return accountBO;

    }

    private AccountBO getLoanAccountWithGroupPerformanceHistory(final AccountState state, final Date startDate, final int disbursalType) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(this.getClass().getSimpleName() + " Loan", ApplicableTo.CLIENTS, startDate,
                PrdStatus.LOAN_ACTIVE, 300.0, 1.2, 3, InterestType.FLAT, meeting);
        accountBO = TestObjectFactory.createLoanAccountWithDisbursement("99999999999", group, state, startDate,
                loanOffering, disbursalType);
        return accountBO;

    }

    private AccountActionDateEntity getLastInstallmentAccountAction(final LoanBO loanBO) {
        AccountActionDateEntity nextAccountAction = null;
        if (loanBO.getAccountActionDates() != null && loanBO.getAccountActionDates().size() > 0) {
            for (AccountActionDateEntity accountAction : loanBO.getAccountActionDates()) {
                if (null == nextAccountAction) {
                    nextAccountAction = accountAction;
                } else if (nextAccountAction.getInstallmentId() < accountAction.getInstallmentId()) {
                    nextAccountAction = accountAction;
                }
            }
        }
        return nextAccountAction;
    }

    private void changeFirstInstallmentDate(final AccountBO accountBO, final int numberOfDays) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - numberOfDays);
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            ((LoanScheduleEntity) accountActionDateEntity).setActionDate(new java.sql.Date(currentDateCalendar
                    .getTimeInMillis()));
            break;
        }
    }

    private void changeFirstInstallmentDate(final AccountBO accountBO) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - 1);
        for (AccountActionDateEntity accountActionDateEntity : accountBO.getAccountActionDates()) {
            ((LoanScheduleEntity) accountActionDateEntity).setActionDate(new java.sql.Date(currentDateCalendar
                    .getTimeInMillis()));
            break;
        }
    }

    private AccountBO saveAndFetch(final AccountBO account) throws Exception {
        TestObjectFactory.updateObject(account);
        StaticHibernateUtil.closeSession();
        return accountPersistence.getAccount(account.getAccountId());
    }

    private java.sql.Date offSetCurrentDate(final int noOfDays) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day - noOfDays);
        return new java.sql.Date(currentDateCalendar.getTimeInMillis());
    }

    private LoanBO getLoanAccount(final AccountState state, final Date startDate, final int disbursalType) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        return TestObjectFactory.createLoanAccountWithDisbursement("99999999999", group, state, startDate,
                loanOffering, disbursalType);

    }

    private Date incrementCurrentDate(final int noOfDays) {
        Calendar currentDateCalendar = new GregorianCalendar();
        int year = currentDateCalendar.get(Calendar.YEAR);
        int month = currentDateCalendar.get(Calendar.MONTH);
        int day = currentDateCalendar.get(Calendar.DAY_OF_MONTH);
        currentDateCalendar = new GregorianCalendar(year, month, day + noOfDays);
        return DateUtils.getDateWithoutTimeStamp(currentDateCalendar.getTimeInMillis());
    }

    private void checkLoanScheduleEntity(final Date date, final String principal, final String interest, final String miscFee,
            final String miscPenalty, final String penalty, final Map<String, String> fees,
            final Map<String, String> feesPaid, final String totalFeeDue, final String totalDueWithFees,
            final LoanScheduleEntity entity) {
        if (date != null) {
            assertDate(date, entity);
        }

        if (principal != null) {
           Assert.assertEquals(new Money(principal), entity.getPrincipal());
        }
        if (interest != null) {
           Assert.assertEquals(new Money(interest), entity.getInterest());
        }

        if (miscFee != null) {
           Assert.assertEquals(new Money(miscFee), entity.getMiscFee());
        }

        if (miscPenalty != null) {
           Assert.assertEquals(new Money(miscPenalty), entity.getMiscPenalty());
        }

        if (penalty != null) {
           Assert.assertEquals(new Money(penalty), entity.getPenalty());
        }

        if (fees != null) {
            checkFees(fees, entity, false);
        }

        if (feesPaid != null) {
            checkFees(feesPaid, entity, true);
        }

        if (totalFeeDue != null) {
            checkTotalDue(totalFeeDue, entity);
        }

        if (totalDueWithFees != null) {
            checkTotalDueWithFees(totalDueWithFees, entity);
        }
    }

    private void checkTotalDueWithFees(final String totalDueWithFees, final LoanScheduleEntity entity) {
       Assert.assertEquals(new Money(totalDueWithFees), entity.getTotalDueWithFees());
    }

    private void checkTotalDue(final String totalFeeDue, final LoanScheduleEntity entity) {
       Assert.assertEquals(new Money(totalFeeDue), entity.getTotalFeeDue());
    }

    private void checkLoanScheduleEntity(final Date date, final String principal, final String interest,
            final Map<String, String> fees,
            final LoanScheduleEntity entity) {
        if (date != null) {
            assertDate(date, entity);
        }

       Assert.assertEquals(new Money(principal), entity.getPrincipal());
       Assert.assertEquals(new Money(interest), entity.getInterest());

        checkFees(fees, entity, false);
    }

    private void assertDate(final Date date, final LoanScheduleEntity entity) {
       Assert.assertEquals(DateUtils.getDateWithoutTimeStamp(date.getTime()), DateUtils.getDateWithoutTimeStamp(entity
                .getActionDate().getTime()));
    }

    private void checkFees(final Map<String, String> expectedFess, final String totalFeeDue,
            final LoanScheduleEntity entity) {
        checkFees(expectedFess, entity, false);
       Assert.assertEquals(new Money(totalFeeDue), entity.getTotalFeeDue());
    }

    private void checkPrincipalAndInterest(final String principal, final String interest, final LoanScheduleEntity entity) {
       Assert.assertEquals(new Money(principal), entity.getPrincipal());
       Assert.assertEquals(new Money(interest), entity.getInterest());
    }

    private void checkFees(final Map<String, String> expected, final LoanScheduleEntity loanScheduleEntity,
            final boolean checkPaid) {
        Set<AccountFeesActionDetailEntity> accountFeesActionDetails = loanScheduleEntity.getAccountFeesActionDetails();
       Assert.assertEquals("fees were " + feeNames(accountFeesActionDetails), expected.size(), accountFeesActionDetails
                .size());

        for (AccountFeesActionDetailEntity accountFeesActionDetailEntity : accountFeesActionDetails) {

            if (expected.get(accountFeesActionDetailEntity.getFee().getFeeName()) != null) {
               Assert.assertEquals(new Money(expected.get(accountFeesActionDetailEntity.getFee().getFeeName())),
                        checkPaid ? accountFeesActionDetailEntity.getFeeAmountPaid() : accountFeesActionDetailEntity
                                .getFeeAmount());
            } else {

                Assert.assertFalse("Fee amount not found for " + accountFeesActionDetailEntity.getFee().getFeeName(), true);
            }
        }
    }

    private String feeNames(final Collection<AccountFeesActionDetailEntity> details) {
        StringBuilder debugString = new StringBuilder();
        for (Iterator<AccountFeesActionDetailEntity> iter = details.iterator(); iter.hasNext();) {
            AccountFeesActionDetailEntity detail = iter.next();
            debugString.append(detail.getFee().getFeeName());
            if (iter.hasNext()) {
                debugString.append(", ");
            }
        }
        return debugString.toString();
    }

    private void createInitialObjects() {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient(this.getClass().getSimpleName() + " Client", CustomerStatus.CLIENT_ACTIVE, group);
    }

    private AccountBO getLoanAccount(final CustomerBO customer, final MeetingBO meeting) {
        Date startDate = new Date(System.currentTimeMillis());
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", customer, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);

    }

    private AccountBO createLoanAccount() {
        Date startDate = new Date(System.currentTimeMillis());
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getNewMeetingForToday(WEEKLY, EVERY_WEEK,
                CUSTOMER_MEETING));
        center = TestObjectFactory.createCenter(this.getClass().getSimpleName() + " Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter(this.getClass().getSimpleName() + " Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        accountBO = TestObjectFactory.createLoanAccount("42423142341", group,
                AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, startDate, loanOffering);
        return accountBO;
    }

    private List<CustomFieldView> getCustomFields() {
        List<CustomFieldView> fields = new ArrayList<CustomFieldView>();
        fields.add(new CustomFieldView(Short.valueOf("5"), "value1", CustomFieldType.ALPHA_NUMERIC));
        return fields;
    }

    public void testIsDisbursementDateLessThanCurrentDateShouldReturnTrueIfDateInPast() throws Exception {
       Assert.assertTrue(LoanBO.isDisbursementDateLessThanCurrentDate(getDateFromToday(-1)));
    }

    public void testIsDisbursementDateLessThanCurrentDateShouldReturnFalseIfDateInFuture() throws Exception {
        Assert.assertFalse(LoanBO.isDisbursementDateLessThanCurrentDate(getDateFromToday(1)));
    }

    public void testIsDisbursementDateLessThanCurrentDateShouldReturnFalseIfDateIsToday() throws Exception {
        Assert.assertFalse(LoanBO.isDisbursementDateLessThanCurrentDate(getCurrentDateWithoutTimeStamp()));
    }

    public void testIsDisbursementDateAfterProductStartDateShouldReturnFalseIfProductStartDateIsLaterThanDisbursement()
            throws Exception {
        Assert.assertFalse(LoanBO.isDisbursementDateAfterProductStartDate(currentDate(), new LoanOfferingBO() {
            @Override
            public Date getStartDate() {
                return getDateFromToday(1);
            }
        }));
    }

    public void testIsDisbursementDateAfterProductStartDateShouldReturnTrueIfProductStartDateIsEarlierThanDisbursement()
            throws Exception {
       Assert.assertTrue(LoanBO.isDisbursementDateAfterProductStartDate(currentDate(), new LoanOfferingBO() {
            @Override
            public Date getStartDate() {
                return getDateFromToday(-1);
            }
        }));
    }

    public void testIsDisbursementDateAfterProductStartDateShouldReturnTrueIfProductStartDateSameAsDisbursement()
            throws Exception {
       Assert.assertTrue(LoanBO.isDisbursementDateAfterProductStartDate(currentDate(), new LoanOfferingBO() {
            @Override
            public Date getStartDate() {
                return currentDate();
            }
        }));
    }

    public void testDisbursementDateInvalidForRedoIfDisbursementBeforeCustomerActivationDate() throws Exception {
        Assert.assertFalse(LoanBO.isDibursementDateValidForRedoLoan(new LoanOfferingBO() {
            @Override
            public Date getStartDate() {
                return DateUtils.getDate(2008, Calendar.JANUARY, 1);
            }
        }, new ClientBO() {
            @Override
            public Date getCustomerActivationDate() {
                return DateUtils.getDate(2008, Calendar.JANUARY, 2);
            }
        }, DateUtils.getDate(2008, Calendar.JANUARY, 1)));
    }

    public void testDisbursementDateInvalidForRedoIfDisbursementBeforeProductStartDate() throws Exception {
        Assert.assertFalse(LoanBO.isDibursementDateValidForRedoLoan(new LoanOfferingBO() {
            @Override
            public Date getStartDate() {
                return DateUtils.getDate(2008, Calendar.JANUARY, 2);
            }
        }, new ClientBO() {
            @Override
            public Date getCustomerActivationDate() {
                return DateUtils.getDate(2008, Calendar.JANUARY, 1);
            }
        }, DateUtils.getDate(2008, Calendar.JANUARY, 1)));
    }

    public void testDisbursementDateValidForRedoIfDisbursementAfterActivationAndProductStartDate() throws Exception {
       Assert.assertTrue(LoanBO.isDibursementDateValidForRedoLoan(new LoanOfferingBO() {
            @Override
            public Date getStartDate() {
                return DateUtils.getDate(2008, Calendar.JANUARY, 1);
            }
        }, new ClientBO() {
            @Override
            public Date getCustomerActivationDate() {
                return DateUtils.getDate(2008, Calendar.JANUARY, 1);
            }
        }, DateUtils.getDate(2008, Calendar.JANUARY, 1)));
    }
}
