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

package org.mifos.application.accounts.business;

import static org.mifos.application.accounts.util.helpers.AccountTypes.LOAN_ACCOUNT;
import static org.mifos.application.accounts.util.helpers.AccountTypes.SAVINGS_ACCOUNT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.mifos.application.accounts.exceptions.AccountException;
import org.mifos.application.accounts.financial.business.FinancialTransactionBO;
import org.mifos.application.accounts.financial.business.service.FinancialBusinessService;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.persistence.AccountPersistence;
import org.mifos.application.accounts.util.helpers.AccountActionTypes;
import org.mifos.application.accounts.util.helpers.AccountConstants;
import org.mifos.application.accounts.util.helpers.AccountExceptionConstants;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.accounts.util.helpers.AccountTypes;
import org.mifos.application.accounts.util.helpers.CustomerAccountPaymentData;
import org.mifos.application.accounts.util.helpers.FeeInstallment;
import org.mifos.application.accounts.util.helpers.InstallmentDate;
import org.mifos.application.accounts.util.helpers.PaymentData;
import org.mifos.application.accounts.util.helpers.WaiveEnum;
import org.mifos.application.customer.business.CustomerAccountBO;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.CustomerMeetingEntity;
import org.mifos.application.customer.persistence.CustomerPersistence;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.fees.business.FeeBO;
import org.mifos.application.fees.persistence.FeePersistence;
import org.mifos.application.fees.util.helpers.FeeFrequencyType;
import org.mifos.application.fees.util.helpers.FeeStatus;
import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.master.business.CustomFieldView;
import org.mifos.application.master.persistence.MasterPersistence;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.meeting.exceptions.MeetingException;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.application.personnel.persistence.PersonnelPersistence;
import org.mifos.config.AccountingRules;
import org.mifos.framework.business.BusinessObject;
import org.mifos.framework.components.configuration.persistence.ConfigurationPersistence;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.exceptions.InvalidDateException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.security.util.ActivityMapper;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.DateTimeService;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.StringUtils;

public class AccountBO extends BusinessObject {

    private final Integer accountId;
    protected String globalAccountNum;
    private String externalId;
    protected final AccountTypeEntity accountType;
    protected final CustomerBO customer;
    protected final OfficeBO office;
    protected PersonnelBO personnel;
    private AccountStateEntity accountState;
    private Date closedDate;
    private Integer offsettingAllowable;

    // associations
    protected Set<AccountNotesEntity> accountNotes;
    protected Set<AccountStatusChangeHistoryEntity> accountStatusChangeHistory;
    private final Set<AccountFlagMapping> accountFlags;
    private final Set<AccountFeesEntity> accountFees;
    private final Set<AccountActionDateEntity> accountActionDates;
    private Set<AccountPaymentEntity> accountPayments;
    private final Set<AccountCustomFieldEntity> accountCustomFields;

    /*
     * Injected Persistence classes
     *
     * DO NOT ACCESS THESE MEMBERS DIRECTLY! ALWAYS USE THE GETTER!
     *
     * The Persistence classes below are used by this class and can be injected
     * via a setter for testing purposes. In order for this mechanism to work
     * correctly, the getter must be used to access them because the getter will
     * initialize the Persistence class if it has not been injected.
     *
     * Long term these references to Persistence classes should probably be
     * eliminated.
     */
    private AccountPersistence accountPersistence = null;
    private ConfigurationPersistence configurationPersistence = null;
    private CustomerPersistence customerPersistence = null;
    private FeePersistence feePersistence = null;
    private MasterPersistence masterPersistence = null;
    private PersonnelPersistence personnelPersistence = null;
    private DateTimeService dateTimeService = null;
    private FinancialBusinessService financialBusinessService = null;

    public FinancialBusinessService getFinancialBusinessService() {
        if (null == financialBusinessService) {
            financialBusinessService = new FinancialBusinessService();
        }
        return financialBusinessService;
    }

    public void setFinancialBusinessService(final FinancialBusinessService financialBusinessService) {
        this.financialBusinessService = financialBusinessService;
    }

    public DateTimeService getDateTimeService() {
        if (null == dateTimeService) {
            dateTimeService = new DateTimeService();
        }
        return dateTimeService;
    }

    public void setDateTimeService(final DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public AccountPersistence getAccountPersistence() {
        if (null == accountPersistence) {
            accountPersistence = new AccountPersistence();
        }
        return accountPersistence;
    }

    public void setAccountPersistence(final AccountPersistence accountPersistence) {
        this.accountPersistence = accountPersistence;
    }

    public ConfigurationPersistence getConfigurationPersistence() {
        if (null == configurationPersistence) {
            configurationPersistence = new ConfigurationPersistence();
        }
        return configurationPersistence;
    }

    public void setConfigurationPersistence(final ConfigurationPersistence configurationPersistence) {
        this.configurationPersistence = configurationPersistence;
    }

    public CustomerPersistence getCustomerPersistence() {
        if (null == customerPersistence) {
            customerPersistence = new CustomerPersistence();
        }
        return customerPersistence;
    }

    public void setCustomerPersistence(final CustomerPersistence customerPersistence) {
        this.customerPersistence = customerPersistence;
    }

    public FeePersistence getFeePersistence() {
        if (null == feePersistence) {
            feePersistence = new FeePersistence();
        }
        return feePersistence;
    }

    public void setFeePersistence(final FeePersistence feePersistence) {
        this.feePersistence = feePersistence;
    }

    public MasterPersistence getMasterPersistence() {
        if (null == masterPersistence) {
            masterPersistence = new MasterPersistence();
        }
        return masterPersistence;
    }

    public void setMasterPersistence(final MasterPersistence masterPersistence) {
        this.masterPersistence = masterPersistence;
    }

    public PersonnelPersistence getPersonnelPersistence() {
        if (null == personnelPersistence) {
            personnelPersistence = new PersonnelPersistence();
        }
        return personnelPersistence;
    }

    public void setPersonnelPersistence(final PersonnelPersistence personnelPersistence) {
        this.personnelPersistence = personnelPersistence;
    }

    /**
     * default constructor for hibernate usage
     */
    protected AccountBO() {
        this(null);
    }

    /**
     * TODO - keithw - work in progress
     * 
     * minimal legal constructor
     */
    public AccountBO(final AccountTypes accountType, final AccountState accountState, final CustomerBO customer,
            final Integer offsettingAllowable, final Set<AccountActionDateEntity> scheduledPayments,
            final Set<AccountFeesEntity> accountFees, final OfficeBO office,
            final PersonnelBO accountOfficer, final Date createdDate,
            final Short createdByUserId) {
        this.accountType = new AccountTypeEntity(accountType.getValue());
        this.accountState = new AccountStateEntity(accountState);
        this.customer = customer;
        this.offsettingAllowable = offsettingAllowable;
        this.createdDate = createdDate;
        this.createdBy = createdByUserId;
        this.accountActionDates = scheduledPayments;
        this.accountFees = accountFees;
        this.office = office;
        this.personnel = accountOfficer;
        
        this.accountId = null;
        this.accountFlags = new HashSet<AccountFlagMapping>();
        this.accountCustomFields = new HashSet<AccountCustomFieldEntity>();
        this.accountPayments = new LinkedHashSet<AccountPaymentEntity>();
    }

    AccountBO(final Integer accountId) {
        this.accountId = accountId;
        globalAccountNum = null;
        customer = null;
        office = null;
        personnel = null;
        accountType = null;
        accountFees = new HashSet<AccountFeesEntity>();
        // TODO: Need an ordered collection here - this generalized Set
        // attribute should ultimately be a List
        accountPayments = new LinkedHashSet<AccountPaymentEntity>();
        // TODO: Need an ordered collection here - this generalized Set
        // attribute should ultimately be a List
        accountActionDates = new LinkedHashSet<AccountActionDateEntity>();
        accountCustomFields = new HashSet<AccountCustomFieldEntity>();
        accountNotes = new HashSet<AccountNotesEntity>();
        accountStatusChangeHistory = new HashSet<AccountStatusChangeHistoryEntity>();
        accountFlags = new HashSet<AccountFlagMapping>();
        offsettingAllowable = new Integer(1);
    }

    protected AccountBO(final UserContext userContext, final CustomerBO customer, final AccountTypes accountType,
            final AccountState accountState) throws AccountException {
        super(userContext);
        validate(userContext, customer, accountType, accountState);
        accountFees = new HashSet<AccountFeesEntity>();
        // TODO: Need an ordered collection here - this generalized Set
        // attribute should ultimately be a List
        accountPayments = new LinkedHashSet<AccountPaymentEntity>();
        // TODO: Need an ordered collection here - this generalized Set
        // attribute should ultimately be a List
        accountActionDates = new LinkedHashSet<AccountActionDateEntity>();
        accountCustomFields = new HashSet<AccountCustomFieldEntity>();
        accountNotes = new HashSet<AccountNotesEntity>();
        accountStatusChangeHistory = new HashSet<AccountStatusChangeHistoryEntity>();
        accountFlags = new HashSet<AccountFlagMapping>();
        this.accountId = null;
        this.customer = customer;
        this.accountType = new AccountTypeEntity(accountType.getValue());
        this.office = customer.getOffice();
        this.personnel = customer.getPersonnel();
        this.setAccountState(new AccountStateEntity(accountState));
        offsettingAllowable = new Integer(1);
        setCreateDetails();
    }

    public Integer getAccountId() {
        return accountId;
    }

    public String getGlobalAccountNum() {
        return globalAccountNum;
    }

    /**
     * Obsolete; most/all callers should call {@link #getType()} instead.
     */
    public AccountTypeEntity getAccountType() {
        return accountType;
    }

    public CustomerBO getCustomer() {
        return customer;
    }

    public OfficeBO getOffice() {
        return office;
    }

    public PersonnelBO getPersonnel() {
        return personnel;
    }

    public Set<AccountNotesEntity> getAccountNotes() {
        return accountNotes;
    }

    public Set<AccountStatusChangeHistoryEntity> getAccountStatusChangeHistory() {
        return accountStatusChangeHistory;
    }

    /**
     * For most purposes this is deprecated and one should call
     * {@link #getState()} instead.
     */
    public AccountStateEntity getAccountState() {
        return accountState;
    }

    public Set<AccountFlagMapping> getAccountFlags() {
        return accountFlags;
    }

    public Set<AccountFeesEntity> getAccountFees() {
        return accountFees;
    }

    public Set<AccountActionDateEntity> getAccountActionDates() {
        return accountActionDates;
    }

    public Set<AccountPaymentEntity> getAccountPayments() {
        return accountPayments;
    }

    public Set<AccountCustomFieldEntity> getAccountCustomFields() {
        return accountCustomFields;
    }

    public Date getClosedDate() {
        return (Date) (closedDate == null ? null : closedDate.clone());
    }

    protected void setGlobalAccountNum(final String globalAccountNum) {
        this.globalAccountNum = globalAccountNum;
    }

    public void setAccountPayments(final Set<AccountPaymentEntity> accountPayments) {
        this.accountPayments = accountPayments;
    }

    protected void setAccountState(final AccountStateEntity accountState) {
        this.accountState = accountState;
    }

    public void setPersonnel(final PersonnelBO personnel) {
        this.personnel = personnel;
    }

    protected void setClosedDate(final Date closedDate) {
        this.closedDate = (Date) (closedDate == null ? null : closedDate.clone());
    }

    protected void addAccountStatusChangeHistory(final AccountStatusChangeHistoryEntity accountStatusChangeHistoryEntity) {
        this.accountStatusChangeHistory.add(accountStatusChangeHistoryEntity);
    }

    protected void addAccountFees(final AccountFeesEntity fees) {
        accountFees.add(fees);
    }

    protected void addAccountActionDate(final AccountActionDateEntity accountAction) {
        if (accountAction == null) {
            throw new NullPointerException();
        }
        accountActionDates.add(accountAction);
    }

    protected void addAccountPayment(final AccountPaymentEntity payment) {
        if (accountPayments == null) {
            accountPayments = new LinkedHashSet<AccountPaymentEntity>();
        }
        accountPayments.add(payment);
    }

    public void addAccountNotes(final AccountNotesEntity notes) {
        accountNotes.add(notes);
    }

    protected void addAccountFlag(final AccountStateFlagEntity flagDetail) {
        AccountFlagMapping flagMap = new AccountFlagMapping();
        flagMap.setCreatedBy(this.getUserContext().getId());
        flagMap.setCreatedDate(getDateTimeService().getCurrentJavaDateTime());
        flagMap.setFlag(flagDetail);
        this.accountFlags.add(flagMap);
    }

    public void addAccountCustomField(final AccountCustomFieldEntity customField) {
        if (customField.getFieldId() != null) {
            AccountCustomFieldEntity accountCustomField = getAccountCustomField(customField.getFieldId());
            if (accountCustomField == null) {
                customField.setAccount(this);
                this.accountCustomFields.add(customField);
            } else {
                accountCustomField.setFieldValue(customField.getFieldValue());
            }
        }
    }

    protected void addcustomFields(final List<CustomFieldView> customFields) {
        if (customFields != null) {
            for (CustomFieldView view : customFields) {
                this.getAccountCustomFields().add(
                        new AccountCustomFieldEntity(this, view.getFieldId(), view.getFieldValue()));
            }
        }
    }

    public final void applyPayment(final PaymentData paymentData) throws AccountException {
        AccountPaymentEntity accountPayment = makePayment(paymentData);
        addAccountPayment(accountPayment);
        buildFinancialEntries(accountPayment.getAccountTrxns());
    }    
    /*
     * Take raw PaymentData (usually from a web page) and enter it into Mifos.
     */
    /**
     * FIXME: - keithw - this should use model concept
     * {@link AccountPaymentEntity} and not {@link PaymentData} dto
     */
    public final void applyPayment(final PaymentData paymentData, final boolean persistChanges) throws AccountException {
        applyPayment(paymentData);
        if (persistChanges) {
            try {
                getAccountPersistence().createOrUpdate(this);
            } catch (PersistenceException e) {
                throw new AccountException(e);
            }
        }
    }

    /**
     * FIXME: - keithw - this should use model concept
     * {@link AccountPaymentEntity} and not {@link PaymentData} dto
     */
    public final void applyPaymentWithPersist(final PaymentData paymentData) throws AccountException {
        applyPayment(paymentData, true);
    }

    public PaymentData createPaymentData(final UserContext userContext, final Money amount, final Date trxnDate, final String receiptId,
            final Date receiptDate, final Short paymentTypeId) {
        return createPaymentData(userContext.getId(), amount, trxnDate, receiptId, receiptDate, paymentTypeId);
    }
    
    public PaymentData createPaymentData(final Short personnelId, final Money amount, final Date trxnDate, final String receiptId,
            final Date receiptDate, final Short paymentTypeId) {
        PersonnelBO personnel;
        try {
            personnel = getPersonnelPersistence().getPersonnel(personnelId);
        } catch (PersistenceException e) {
            // Generally this is the UserContext id, which shouldn't ever
            // be invalid
            throw new IllegalStateException(AccountConstants.ERROR_INVALID_PERSONNEL);
        }
        if (personnel == null) {
            // see above catch clause
            throw new IllegalStateException(AccountConstants.ERROR_INVALID_PERSONNEL);
        }

        PaymentData paymentData = PaymentData.createPaymentData(amount, personnel, paymentTypeId, trxnDate);
        if (receiptDate != null) {
            paymentData.setReceiptDate(receiptDate);
        }
        if (receiptId != null) {
            paymentData.setReceiptNum(receiptId);
        }

        for (AccountActionDateEntity installment : getTotalInstallmentsDue()) {
            if (this instanceof CustomerAccountBO) {
                paymentData.addAccountPaymentData(new CustomerAccountPaymentData(installment));
            }
        }
        return paymentData;
    }

    public final void adjustPmnt(final String adjustmentComment) throws AccountException {
        if (isAdjustPossibleOnLastTrxn()) {
            MifosLogManager.getLogger(LoggerConstants.ACCOUNTSLOGGER).debug(
                    "Adjustment is possible hence attempting to adjust.");
            adjustPayment(getLastPmnt(), getLoggedInUser(), adjustmentComment);
            try {
                getAccountPersistence().createOrUpdate(this);
            } catch (PersistenceException e) {
                throw new AccountException(AccountExceptionConstants.CANNOTADJUST, e);
            }
        } else {
            throw new AccountException(AccountExceptionConstants.CANNOTADJUST);
        }
    }

    public final void adjustLastPayment(final String adjustmentComment) throws AccountException {
        if (isAdjustPossibleOnLastTrxn()) {
            MifosLogManager.getLogger(LoggerConstants.ACCOUNTSLOGGER).debug(
                    "Adjustment is possible hence attempting to adjust.");

            adjustPayment(getLastPmntToBeAdjusted(), getLoggedInUser(), adjustmentComment);
            try {
                getAccountPersistence().createOrUpdate(this);
            } catch (PersistenceException e) {
                throw new AccountException(AccountExceptionConstants.CANNOTADJUST, e);
            }
        } else {
            throw new AccountException(AccountExceptionConstants.CANNOTADJUST);
        }

    }

    protected final void adjustPayment(final AccountPaymentEntity accountPayment, final PersonnelBO personnel,
            final String adjustmentComment) throws AccountException {
        List<AccountTrxnEntity> reversedTrxns = accountPayment.reversalAdjustment(personnel, adjustmentComment);
        updateInstallmentAfterAdjustment(reversedTrxns);
        buildFinancialEntries(new HashSet(reversedTrxns));
    }

    public final void handleChangeInMeetingSchedule() throws AccountException {
        AccountActionDateEntity accountActionDateEntity = getDetailsOfNextInstallment();
        Date currentDate = DateUtils.getCurrentDateWithoutTimeStamp();
        if (accountActionDateEntity != null) {
            short installmentId = accountActionDateEntity.getInstallmentId();
            if (accountActionDateEntity.getActionDate().compareTo(currentDate) == 0) {
                installmentId += 1;
            }
            regenerateFutureInstallments(installmentId);
            try {
                getAccountPersistence().createOrUpdate(this);
            } catch (PersistenceException e) {
                throw new AccountException(e);
            }
        } else {
            resetUpdatedFlag();
        }
    }

    protected void resetUpdatedFlag() throws AccountException {
    }

    public void changeStatus(final AccountState newStatus, final Short flagId, final String comment) throws AccountException {
        changeStatus(newStatus.getValue(), flagId, comment);
    }

    public final void changeStatus(final Short newStatusId, final Short flagId, final String comment) throws AccountException {
        try {
            MifosLogManager.getLogger(LoggerConstants.ACCOUNTSLOGGER).debug(
                    "In the change status method of AccountBO:: new StatusId= " + newStatusId);
            activationDateHelper(newStatusId);
            MasterPersistence masterPersistence = getMasterPersistence();
            AccountStateEntity accountStateEntity = (AccountStateEntity) masterPersistence.getPersistentObject(
                    AccountStateEntity.class, newStatusId);
            accountStateEntity.setLocaleId(this.getUserContext().getLocaleId());
            AccountStateFlagEntity accountStateFlagEntity = null;
            if (flagId != null) {
                accountStateFlagEntity = (AccountStateFlagEntity) masterPersistence.getPersistentObject(
                        AccountStateFlagEntity.class, flagId);
            }
            PersonnelBO personnel = getPersonnelPersistence().getPersonnel(getUserContext().getId());
            AccountStatusChangeHistoryEntity historyEntity = new AccountStatusChangeHistoryEntity(this
                    .getAccountState(), accountStateEntity, personnel, this);
            AccountNotesEntity accountNotesEntity = new AccountNotesEntity(new DateTimeService()
                    .getCurrentJavaSqlDate(), comment, personnel, this);
            this.addAccountStatusChangeHistory(historyEntity);
            this.setAccountState(accountStateEntity);
            this.addAccountNotes(accountNotesEntity);
            if (accountStateFlagEntity != null) {
                setFlag(accountStateFlagEntity);
            }
            if (newStatusId.equals(AccountState.LOAN_CANCELLED.getValue())
                    || newStatusId.equals(AccountState.LOAN_CLOSED_OBLIGATIONS_MET.getValue())
                    || newStatusId.equals(AccountState.LOAN_CLOSED_WRITTEN_OFF.getValue())
                    || newStatusId.equals(AccountState.SAVINGS_CANCELLED.getValue())) {
                this.setClosedDate(getDateTimeService().getCurrentJavaDateTime());
            }
            if (newStatusId.equals(AccountState.LOAN_CLOSED_WRITTEN_OFF.getValue())) {
                writeOff();
            }

            if (newStatusId.equals(AccountState.LOAN_CLOSED_RESCHEDULED.getValue())) {
                reschedule();
            }

            MifosLogManager.getLogger(LoggerConstants.ACCOUNTSLOGGER).debug(
                    "Coming out successfully from the change status method of AccountBO");
        } catch (PersistenceException e) {
            throw new AccountException(e);
        }
    }

    protected void writeOff() throws AccountException {
    }

    protected void reschedule() throws AccountException {
    }

    protected void updateClientPerformanceOnRescheduleLoan() {
    }

    protected void updateAccountFeesEntity(final Short feeId) {
        AccountFeesEntity accountFees = getAccountFees(feeId);
        if (accountFees != null) {
            accountFees.changeFeesStatus(FeeStatus.INACTIVE, getDateTimeService().getCurrentJavaDateTime());
            accountFees.setLastAppliedDate(null);
        }
    }

    public AccountFeesEntity getAccountFees(final Short feeId) {
        for (AccountFeesEntity accountFeesEntity : this.getAccountFees()) {
            if (accountFeesEntity.getFees().getFeeId().equals(feeId)) {
                return accountFeesEntity;
            }
        }
        return null;
    }

    public FeeBO getAccountFeesObject(final Short feeId) {
        AccountFeesEntity accountFees = getAccountFees(feeId);
        if (accountFees != null) {
            return accountFees.getFees();
        }
        return null;
    }

    public Boolean isFeeActive(final Short feeId) {
        AccountFeesEntity accountFees = getAccountFees(feeId);
        return accountFees.getFeeStatus() == null || accountFees.getFeeStatus().equals(FeeStatus.ACTIVE.getValue());
    }

    protected Money removeSign(final Money amount) {
        if (amount != null && amount.getAmountDoubleValue() < 0) {
            return amount.negate();
        } else {
            return amount;
        }
    }

    public double getLastPmntAmnt() {
        if (null != accountPayments && accountPayments.size() > 0) {
            return getLastPmnt().getAmount().getAmountDoubleValue();
        }
        return 0;
    }

    public double getLastPmntAmntToBeAdjusted() {
        if (null != getLastPmntToBeAdjusted() && null != accountPayments && accountPayments.size() > 0) {
            return getLastPmntToBeAdjusted().getAmount().getAmountDoubleValue();
        }
        return 0;
    }

    public AccountPaymentEntity getLastPmnt() {
        AccountPaymentEntity lastPmnt = null;
        for (AccountPaymentEntity accntPayment : accountPayments) {
            lastPmnt = accntPayment;
            break;
        }
        return lastPmnt;
    }

    public AccountPaymentEntity getLastPmntToBeAdjusted() {
        AccountPaymentEntity accntPmnt = null;
        int i = 0;
        for (AccountPaymentEntity accntPayment : accountPayments) {
            i = i + 1;
            if (i == accountPayments.size()) {
                break;
            }
            if (accntPayment.getAmount().getAmountDoubleValue() != 0) {
                accntPmnt = accntPayment;
                break;
            }

        }
        return accntPmnt;
    }

    public AccountActionDateEntity getAccountActionDate(final Short installmentId) {
        if (null != accountActionDates && accountActionDates.size() > 0) {
            for (AccountActionDateEntity accntActionDate : accountActionDates) {
                if (accntActionDate.getInstallmentId().equals(installmentId)) {
                    return accntActionDate;
                }
            }
        }
        return null;
    }

    public AccountActionDateEntity getAccountActionDate(final Short installmentId, final Integer customerId) {
        if (null != accountActionDates && accountActionDates.size() > 0) {
            for (AccountActionDateEntity accntActionDate : accountActionDates) {
                if (accntActionDate.getInstallmentId().equals(installmentId)
                        && accntActionDate.getCustomer().getCustomerId().equals(customerId)) {
                    return accntActionDate;
                }
            }
        }
        return null;
    }

    /*
     * Return those unpaid AccountActionDateEntities after the next installment.
     */
    public List<AccountActionDateEntity> getApplicableIdsForFutureInstallments() {
        List<AccountActionDateEntity> futureActionDateList = new ArrayList<AccountActionDateEntity>();
        AccountActionDateEntity nextInstallment = getDetailsOfNextInstallment();
        if (nextInstallment != null) {
            for (AccountActionDateEntity accountActionDate : getAccountActionDates()) {
                if (!accountActionDate.isPaid()) {
                    if (accountActionDate.getInstallmentId() > nextInstallment.getInstallmentId()) {
                        futureActionDateList.add(accountActionDate);
                    }
                }
            }
        }
        return futureActionDateList;
    }

    protected List<AccountActionDateEntity> getApplicableIdsForDueInstallments() {
        List<AccountActionDateEntity> dueActionDateList = new ArrayList<AccountActionDateEntity>();
        AccountActionDateEntity nextInstallment = getDetailsOfNextInstallment();
        if (nextInstallment == null || !nextInstallment.isPaid()) {
            dueActionDateList.addAll(getDetailsOfInstallmentsInArrears());
            if (nextInstallment != null) {
                dueActionDateList.add(nextInstallment);
            }
        }
        return dueActionDateList;
    }

    public List<AccountActionDateEntity> getPastInstallments() {
        List<AccountActionDateEntity> pastActionDateList = new ArrayList<AccountActionDateEntity>();
        for (AccountActionDateEntity accountActionDateEntity : getAccountActionDates()) {
            if (accountActionDateEntity.compareDate(DateUtils.getCurrentDateWithoutTimeStamp()) < 0) {
                pastActionDateList.add(accountActionDateEntity);
            }
        }
        return pastActionDateList;
    }

    public List<AccountActionDateEntity> getAllInstallments() {
        List<AccountActionDateEntity> actionDateList = new ArrayList<AccountActionDateEntity>();
        for (AccountActionDateEntity accountActionDateEntity : getAccountActionDates()) {
            actionDateList.add(accountActionDateEntity);
        }
        return actionDateList;
    }

    public List<TransactionHistoryView> getTransactionHistoryView() {
        List<TransactionHistoryView> trxnHistory = new ArrayList<TransactionHistoryView>();
        for (AccountPaymentEntity accountPayment : getAccountPayments()) {
            for (AccountTrxnEntity accountTrxn : accountPayment.getAccountTrxns()) {
                for (FinancialTransactionBO financialTrxn : accountTrxn.getFinancialTransactions()) {
                    TransactionHistoryView transactionHistory = new TransactionHistoryView();
                    setFinancialEntries(financialTrxn, transactionHistory);
                    setAccountingEntries(accountTrxn, transactionHistory);
                    trxnHistory.add(transactionHistory);
                }
            }
        }

        return trxnHistory;
    }

    public Date getNextMeetingDate() {
        AccountActionDateEntity nextAccountAction = getDetailsOfNextInstallment();
        if (nextAccountAction != null) {
            return nextAccountAction.getActionDate();
        }
        // calculate the next date based on the customer's meeting object
        CustomerMeetingEntity customerMeeting = customer.getCustomerMeeting();
        if (customerMeeting != null) {
            Date nextMeetingDate = customerMeeting.getMeeting().getFirstDate(
                    getDateTimeService().getCurrentJavaDateTime());
            return new java.sql.Date(nextMeetingDate.getTime());
        }
        return new java.sql.Date(DateUtils.getCurrentDateWithoutTimeStamp().getTime());
    }

    public List<AccountActionDateEntity> getDetailsOfInstallmentsInArrears() {
        List<AccountActionDateEntity> installmentsInArrears = new ArrayList<AccountActionDateEntity>();
        Date currentDate = DateUtils.getCurrentDateWithoutTimeStamp();
        if (getAccountActionDates() != null && getAccountActionDates().size() > 0) {
            for (AccountActionDateEntity accountAction : getAccountActionDates()) {
                if (accountAction.getActionDate().compareTo(currentDate) < 0 && !accountAction.isPaid()) {
                    installmentsInArrears.add(accountAction);
                }
            }
        }
        return installmentsInArrears;
    }

    /**
     * Return the earliest-dated AccountActionDateEntity on or after today.
     */
    public AccountActionDateEntity getDetailsOfNextInstallment() {
        AccountActionDateEntity nextAccountAction = null;
        Date currentDate = DateUtils.getCurrentDateWithoutTimeStamp();
        if (getAccountActionDates() != null && getAccountActionDates().size() > 0) {
            for (AccountActionDateEntity accountAction : getAccountActionDates()) {
                if (accountAction.getActionDate().compareTo(currentDate) >= 0) {
                    if (null == nextAccountAction
                            || nextAccountAction.getInstallmentId() > accountAction.getInstallmentId()) {
                        nextAccountAction = accountAction;
                    }
                }
            }
        }
        return nextAccountAction;
    }

    public Money getTotalAmountDue() {
        Money totalAmt = getTotalAmountInArrears();
        AccountActionDateEntity nextInstallment = getDetailsOfNextInstallment();
        if (nextInstallment != null && !nextInstallment.isPaid()) {
            totalAmt = totalAmt.add(getDueAmount(nextInstallment));
        }
        return totalAmt;
    }

    public Money getTotalPaymentDue() {
        Money totalAmt = getTotalAmountInArrears();
        AccountActionDateEntity nextInstallment = getDetailsOfNextInstallment();
        if (nextInstallment != null
                && !nextInstallment.isPaid()
                && DateUtils.getDateWithoutTimeStamp(nextInstallment.getActionDate().getTime()).equals(
                        DateUtils.getCurrentDateWithoutTimeStamp())) {
            totalAmt = totalAmt.add(getDueAmount(nextInstallment));
        }
        return totalAmt;
    }

    public Money getTotalAmountInArrears() {
        List<AccountActionDateEntity> installmentsInArrears = getDetailsOfInstallmentsInArrears();
        Money totalAmount = new Money();
        if (installmentsInArrears != null && installmentsInArrears.size() > 0) {
            for (AccountActionDateEntity accountAction : installmentsInArrears) {
                totalAmount = totalAmount.add(getDueAmount(accountAction));
            }
        }
        return totalAmount;
    }

    public List<AccountActionDateEntity> getTotalInstallmentsDue() {
        List<AccountActionDateEntity> dueInstallments = getDetailsOfInstallmentsInArrears();
        AccountActionDateEntity nextInstallment = getDetailsOfNextInstallment();
        if (nextInstallment != null
                && !nextInstallment.isPaid()
                && DateUtils.getDateWithoutTimeStamp(nextInstallment.getActionDate().getTime()).equals(
                        DateUtils.getCurrentDateWithoutTimeStamp())) {
            dueInstallments.add(nextInstallment);
        }
        return dueInstallments;
    }

    public boolean isTrxnDateBeforePreviousMeetingDateAllowed(final Date trxnDate) {
        try {
            Date meetingDate = getCustomerPersistence().getLastMeetingDateForCustomer(getCustomer().getCustomerId());

            if (getConfigurationPersistence().isRepaymentIndepOfMeetingEnabled()) {
                // payment date for loans must be >= disbursement date
                if (this instanceof LoanBO) {
                    Date apporvalDate = this.getAccountApprovalDate();
                    // This is call for disbursment.
                    if (this.getState().equals(AccountState.LOAN_APPROVED)) {
                        return trxnDate.compareTo(DateUtils.getDateWithoutTimeStamp(apporvalDate)) >= 0;
                    } else {
                        // This is payment action.
                        if (meetingDate == null) {
                            // if meetings are not present..
                            return trxnDate.compareTo(DateUtils.getDateWithoutTimeStamp(apporvalDate)) >= 0;
                        } else {
                            final Date meetingDateWithoutTimeStamp = DateUtils.getDateWithoutTimeStamp(meetingDate);
                            // if the last meeting date was prior to approval
                            // date, then the transactions should be after the
                            // approval date.
                            if (apporvalDate.compareTo(meetingDateWithoutTimeStamp) > 0) {
                                return trxnDate.compareTo(DateUtils.getDateWithoutTimeStamp(apporvalDate)) >= 0;
                            } else {
                                // if the last meeting is after the appoval date
                                // then the transaction should be after the
                                // meeting.
                                return trxnDate.compareTo(DateUtils.getDateWithoutTimeStamp(meetingDate)) >= 0;
                            }
                        }
                    }

                }
                // must be >= creation date for other accounts
                return trxnDate.compareTo(DateUtils.getDateWithoutTimeStamp(this.getCreatedDate())) >= 0;
            } else {
                if (meetingDate != null) {
                    return trxnDate.compareTo(DateUtils.getDateWithoutTimeStamp(meetingDate)) >= 0;
                }
                return false;
            }
        } catch (PersistenceException e) {
            // This should only occur if Customer is null which shouldn't
            // happen.
            // Or we had some configuration/binding error, so we'll throw a
            // runtime exception here.
            throw new IllegalStateException(e);
        }
    }

    public boolean isTrxnDateValid(final Date trxnDate) throws AccountException {
        if (AccountingRules.isBackDatedTxnAllowed()) {
            return isTrxnDateBeforePreviousMeetingDateAllowed(trxnDate);
        }
        return trxnDate.equals(DateUtils.getCurrentDateWithoutTimeStamp());
    }

    public List<AccountNotesEntity> getRecentAccountNotes() {
        List<AccountNotesEntity> notes = new ArrayList<AccountNotesEntity>();
        int count = 0;
        for (AccountNotesEntity accountNotesEntity : getAccountNotes()) {
            if (count > 2) {
                break;
            }
            notes.add(accountNotesEntity);
            count++;
        }
        return notes;
    }

    public void update() throws AccountException {
        setUpdateDetails();
        try {
            getAccountPersistence().createOrUpdate(this);
        } catch (PersistenceException e) {
            throw new AccountException(e);
        }
    }

    public AccountState getState() {
        return AccountState.fromShort(getAccountState().getId());
    }

    protected void updateAccountActivity(final Money principal, final Money interest, final Money fee, final Money penalty, final Short personnelId,
            final String description) throws AccountException {
    }

    public void waiveAmountDue(final WaiveEnum waiveType) throws AccountException {
    }

    public void waiveAmountOverDue(final WaiveEnum waiveType) throws AccountException {
    }

    public Boolean isCurrentDateGreaterThanFirstInstallment() {
        for (AccountActionDateEntity accountActionDateEntity : getAccountActionDates()) {
            if (DateUtils.getCurrentDateWithoutTimeStamp().compareTo(
                    DateUtils.getDateWithoutTimeStamp(accountActionDateEntity.getActionDate().getTime())) >= 0) {
                return true;
            }
        }
        return false;
    }

    public void applyCharge(final Short feeId, final Double charge) throws AccountException, PersistenceException {
    }

    public AccountTypes getType() {
        throw new RuntimeException("should be implemented in subclass");
    }

    public boolean isOpen() {
        return true;
    }

    public boolean isUpcomingInstallmentUnpaid() {
        AccountActionDateEntity accountActionDateEntity = getDetailsOfUpcomigInstallment();
        if (accountActionDateEntity != null) {
            if (!accountActionDateEntity.isPaid()) {
                return true;
            }
        }
        return false;
    }

    public AccountActionDateEntity getDetailsOfUpcomigInstallment() {
        AccountActionDateEntity nextAccountAction = null;
        Date currentDate = DateUtils.getCurrentDateWithoutTimeStamp();
        if (getAccountActionDates() != null && getAccountActionDates().size() > 0) {
            for (AccountActionDateEntity accountAction : getAccountActionDates()) {
                if (accountAction.getActionDate().compareTo(currentDate) > 0) {
                    if (null == nextAccountAction) {
                        nextAccountAction = accountAction;
                    } else if (nextAccountAction.getInstallmentId() > accountAction.getInstallmentId()) {
                        nextAccountAction = accountAction;
                    }
                }
            }
        }
        return nextAccountAction;
    }

    /*
     * Delegate to an injected service.
     */
    protected final void buildFinancialEntries(final Set<AccountTrxnEntity> accountTrxns) throws AccountException {
        getFinancialBusinessService().buildFinancialEntries(accountTrxns);
    }

    protected final String generateId(final String officeGlobalNum) throws AccountException {
        StringBuilder systemId = new StringBuilder();
        systemId.append(officeGlobalNum);
        try {
            systemId.append(StringUtils.lpad(getAccountId().toString(), '0', 11));
        } catch (Exception se) {
            throw new AccountException(AccountExceptionConstants.IDGenerationException, se);
        }
        return systemId.toString();
    }

    protected final List<AccountActionDateEntity> getDueInstallments() {
        List<AccountActionDateEntity> dueInstallmentList = new ArrayList<AccountActionDateEntity>();
        for (AccountActionDateEntity accountActionDateEntity : getAccountActionDates()) {
            if (!accountActionDateEntity.isPaid()) {
                if (accountActionDateEntity.compareDate(DateUtils.getCurrentDateWithoutTimeStamp()) > 0) {
                    dueInstallmentList.add(accountActionDateEntity);
                }
            }
        }
        return dueInstallmentList;
    }

    protected final List<AccountActionDateEntity> getTotalDueInstallments() {
        List<AccountActionDateEntity> dueInstallmentList = new ArrayList<AccountActionDateEntity>();
        for (AccountActionDateEntity accountActionDateEntity : getAccountActionDates()) {
            if (!accountActionDateEntity.isPaid()) {
                if (accountActionDateEntity.compareDate(DateUtils.getCurrentDateWithoutTimeStamp()) >= 0) {
                    dueInstallmentList.add(accountActionDateEntity);
                }
            }
        }
        return dueInstallmentList;
    }

    protected final Boolean isFeeAlreadyApplied(final FeeBO fee) {
        return getAccountFees(fee.getFeeId()) != null;
    }

    protected final AccountFeesEntity getAccountFee(final FeeBO fee, final Double charge) {
        AccountFeesEntity accountFee = null;
        if (fee.isPeriodic() && isFeeAlreadyApplied(fee)) {
            accountFee = getAccountFees(fee.getFeeId());
            accountFee.setFeeAmount(charge);
            accountFee.setFeeStatus(FeeStatus.ACTIVE);
            accountFee.setStatusChangeDate(getDateTimeService().getCurrentJavaDateTime());
        } else {
            accountFee = new AccountFeesEntity(this, fee, charge, FeeStatus.ACTIVE.getValue(), null, null);
        }
        return accountFee;
    }

    protected final List<InstallmentDate> getInstallmentDates(final MeetingBO meeting, final Short noOfInstallments,
            final Short installmentToSkip) throws AccountException {
        return getInstallmentDates(meeting, noOfInstallments, installmentToSkip, false);
    }

    protected final List<InstallmentDate> getInstallmentDates(final MeetingBO meeting, final Short noOfInstallments,
            final Short installmentToSkip, final boolean isRepaymentIndepOfMeetingEnabled) throws AccountException {

        return getInstallmentDates(meeting, noOfInstallments, installmentToSkip, isRepaymentIndepOfMeetingEnabled, true);
    }

    protected final List<InstallmentDate> getInstallmentDates(final MeetingBO meeting, final Short noOfInstallments,
            final Short installmentToSkip, final boolean isRepaymentIndepOfMeetingEnabled, final boolean adjustForHolidays)
            throws AccountException {
        MifosLogManager.getLogger(LoggerConstants.ACCOUNTSLOGGER).debug("Generating intallment dates");
        if (noOfInstallments > 0) {
            try {
                List<Date> dueDates;
                if (isRepaymentIndepOfMeetingEnabled) {
                    dueDates = meeting.getAllDatesWithRepaymentIndepOfMeetingEnabled(noOfInstallments
                            + installmentToSkip, adjustForHolidays);
                } else {
                    dueDates = meeting.getAllDates(noOfInstallments + installmentToSkip, adjustForHolidays);
                }

                return createInstallmentDates(installmentToSkip, dueDates);
            } catch (MeetingException e) {
                throw new AccountException(e);
            }
        }
        return new ArrayList<InstallmentDate>();
    }

    private List<InstallmentDate> createInstallmentDates(final Short installmentToSkip, final List<Date> dueDates) {
        List<InstallmentDate> installmentDates = new ArrayList<InstallmentDate>();
        int installmentId = 1;
        for (Date date : dueDates) {
            installmentDates.add(new InstallmentDate((short) installmentId++, date));
        }
        removeInstallmentsNeedNotPay(installmentToSkip, installmentDates);
        return installmentDates;
    }

    @Deprecated
    protected final List<FeeInstallment> getFeeInstallments(final List<InstallmentDate> installmentDates)
            throws AccountException {
        List<FeeInstallment> feeInstallmentList = new ArrayList<FeeInstallment>();
        for (AccountFeesEntity accountFeesEntity : getAccountFees()) {
            if (accountFeesEntity.isActive()) {
                Short accountFeeType = accountFeesEntity.getFees().getFeeFrequency().getFeeFrequencyType().getId();
                if (accountFeeType.equals(FeeFrequencyType.ONETIME.getValue())) {
                    feeInstallmentList.add(handleOneTime(accountFeesEntity, installmentDates));
                } else if (accountFeeType.equals(FeeFrequencyType.PERIODIC.getValue())) {
                    feeInstallmentList.addAll(handlePeriodic(accountFeesEntity, installmentDates));
                }
            }
        }
        return feeInstallmentList;
    }

    /**
     *
     * @param installmentDates
     *            dates adjusted for holidays
     * @param nonAdjustedInstallmentDates
     *            dates not adjusted for holidays
     */
    protected final List<FeeInstallment> getFeeInstallments(final List<InstallmentDate> installmentDates,
            final List<InstallmentDate> nonAdjustedInstallmentDates) throws AccountException {
        List<FeeInstallment> feeInstallmentList = new ArrayList<FeeInstallment>();
        for (AccountFeesEntity accountFeesEntity : getAccountFees()) {
            if (accountFeesEntity.isActive()) {
                Short accountFeeType = accountFeesEntity.getFees().getFeeFrequency().getFeeFrequencyType().getId();
                if (accountFeeType.equals(FeeFrequencyType.ONETIME.getValue())) {
                    feeInstallmentList.add(handleOneTime(accountFeesEntity, installmentDates));
                } else if (accountFeeType.equals(FeeFrequencyType.PERIODIC.getValue())) {
                    feeInstallmentList.addAll(handlePeriodic(accountFeesEntity, installmentDates,
                            nonAdjustedInstallmentDates));
                }
            }
        }
        return feeInstallmentList;
    }

    protected final List<Date> getFeeDates(final MeetingBO feeMeetingFrequency, final List<InstallmentDate> installmentDates)
            throws AccountException {
        MeetingBO customerMeeting = getCustomer().getCustomerMeeting().getMeeting();
        Short recurAfter = customerMeeting.getMeetingDetails().getRecurAfter();
        Date meetingStartDate = customerMeeting.getMeetingStartDate();
        customerMeeting.getMeetingDetails().setRecurAfter(feeMeetingFrequency.getMeetingDetails().getRecurAfter());
        customerMeeting.setMeetingStartDate(installmentDates.get(0).getInstallmentDueDate());
        Date repaymentEndDate = installmentDates.get(installmentDates.size() - 1).getInstallmentDueDate();

        try {
            return customerMeeting.getAllDates(repaymentEndDate);
        } catch (MeetingException e) {
            throw new AccountException(e);
        } finally {
            customerMeeting.setMeetingStartDate(meetingStartDate);
            customerMeeting.getMeetingDetails().setRecurAfter(recurAfter);
        }
    }

    protected final FeeInstallment buildFeeInstallment(final Short installmentId, final Money accountFeeAmount,
            final AccountFeesEntity accountFee) {
        FeeInstallment feeInstallment = new FeeInstallment();
        feeInstallment.setInstallmentId(installmentId);
        feeInstallment.setAccountFee(accountFeeAmount);
        feeInstallment.setAccountFeesEntity(accountFee);
        accountFee.setAccountFeeAmount(accountFeeAmount);
        return feeInstallment;
    }

    protected final Short getMatchingInstallmentId(final List<InstallmentDate> installmentDates, final Date feeDate) {
        for (InstallmentDate installmentDate : installmentDates) {
            if (DateUtils.getDateWithoutTimeStamp(installmentDate.getInstallmentDueDate().getTime()).compareTo(
                    DateUtils.getDateWithoutTimeStamp(feeDate.getTime())) >= 0) {
                return installmentDate.getInstallmentId();
            }
        }
        return null;
    }

    protected final List<FeeInstallment> mergeFeeInstallments(final List<FeeInstallment> feeInstallmentList) {
        List<FeeInstallment> newFeeInstallmentList = new ArrayList<FeeInstallment>();
        for (Iterator<FeeInstallment> iterator = feeInstallmentList.iterator(); iterator.hasNext();) {
            FeeInstallment feeInstallment = iterator.next();
            iterator.remove();
            FeeInstallment feeInstTemp = null;
            for (FeeInstallment feeInst : newFeeInstallmentList) {
                if (feeInst.getInstallmentId().equals(feeInstallment.getInstallmentId())
                        && feeInst.getAccountFeesEntity().equals(feeInstallment.getAccountFeesEntity())) {
                    feeInstTemp = feeInst;
                    break;
                }
            }
            if (feeInstTemp != null) {
                newFeeInstallmentList.remove(feeInstTemp);
                feeInstTemp.setAccountFee(feeInstTemp.getAccountFee().add(feeInstallment.getAccountFee()));
                newFeeInstallmentList.add(feeInstTemp);
            } else {
                newFeeInstallmentList.add(feeInstallment);
            }
        }
        return newFeeInstallmentList;
    }

    protected boolean isCurrentDateEquallToInstallmentDate() {
        for (AccountActionDateEntity accountActionDateEntity : getAccountActionDates()) {
            if (!accountActionDateEntity.isPaid()) {
                if (accountActionDateEntity.compareDate(DateUtils.getCurrentDateWithoutTimeStamp()) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    protected List<AccountFeesEntity> getPeriodicFeeList() {
        List<AccountFeesEntity> periodicFeeList = new ArrayList<AccountFeesEntity>();
        for (AccountFeesEntity accountFee : getAccountFees()) {
            if (accountFee.getFees().isPeriodic()) {
                getFeePersistence().getFee(accountFee.getFees().getFeeId());
                periodicFeeList.add(accountFee);
            }
        }
        return periodicFeeList;
    }

    protected void deleteFutureInstallments() throws AccountException {
        List<AccountActionDateEntity> futureInstllments = getApplicableIdsForFutureInstallments();
        for (AccountActionDateEntity accountActionDateEntity : futureInstllments) {
            accountActionDates.remove(accountActionDateEntity);
            try {
                getAccountPersistence().delete(accountActionDateEntity);
            } catch (PersistenceException e) {
                throw new AccountException(e);
            }
        }
    }

    /**
     * This can be very inefficient if an account has many action dates. Note
     * that an account having too many action dates may also be a sign of bad
     * data--at least, this was the case with some GK data.
     */
    public Short getLastInstallmentId() {
        Short lastInstallmentId = null;
        for (AccountActionDateEntity date : this.getAccountActionDates()) {

            if (lastInstallmentId == null) {
                lastInstallmentId = date.getInstallmentId();
            } else {
                if (lastInstallmentId < date.getInstallmentId()) {
                    lastInstallmentId = date.getInstallmentId();
                }
            }
        }
        return lastInstallmentId;

    }

    protected List<AccountTrxnEntity> getAccountTrxnsOrderByTrxnCreationDate() {
        List<AccountTrxnEntity> accountTrxnList = new ArrayList<AccountTrxnEntity>();
        for (AccountPaymentEntity payment : getAccountPayments()) {
            accountTrxnList.addAll(payment.getAccountTrxns());
        }

        Collections.sort(accountTrxnList, new Comparator<AccountTrxnEntity>() {
            public int compare(final AccountTrxnEntity trx1, final AccountTrxnEntity trx2) {
                if (trx1.getCreatedDate().equals(trx2.getCreatedDate())) {
                    return trx1.getAccountTrxnId().compareTo(trx2.getAccountTrxnId());
                } else {
                    return trx1.getCreatedDate().compareTo(trx2.getCreatedDate());
                }
            }
        });
        return accountTrxnList;
    }

    protected List<AccountTrxnEntity> getAccountTrxnsOrderByTrxnDate() {
        List<AccountTrxnEntity> accountTrxnList = new ArrayList<AccountTrxnEntity>();
        for (AccountPaymentEntity payment : getAccountPayments()) {
            accountTrxnList.addAll(payment.getAccountTrxns());
        }

        Collections.sort(accountTrxnList, new Comparator<AccountTrxnEntity>() {
            public int compare(final AccountTrxnEntity trx1, final AccountTrxnEntity trx2) {
                if (trx1.getActionDate().equals(trx2.getActionDate())) {
                    return trx1.getAccountTrxnId().compareTo(trx2.getAccountTrxnId());
                } else {
                    return trx1.getActionDate().compareTo(trx2.getActionDate());
                }
            }
        });
        return accountTrxnList;
    }

    protected void resetAccountActionDates() {
        this.accountActionDates.clear();
    }

    protected void updateInstallmentAfterAdjustment(final List<AccountTrxnEntity> reversedTrxns) throws AccountException {
    }

    protected Money getDueAmount(final AccountActionDateEntity installment) {
        return null;
    }

    protected void regenerateFutureInstallments(final Short nextIntallmentId) throws AccountException {
    }

    protected List<InstallmentDate> getInstallmentDates(final MeetingBO Meeting, final Integer installmentSkipToStartRepayment)
            throws AccountException {
        return null;
    }

    @Deprecated
    protected List<FeeInstallment> handlePeriodic(final AccountFeesEntity accountFees, final List<InstallmentDate> installmentDates)
            throws AccountException {
        return null;
    }

    protected List<FeeInstallment> handlePeriodic(final AccountFeesEntity accountFees,
            final List<InstallmentDate> installmentDates, final List<InstallmentDate> nonAdjustedInstallmentDates)
            throws AccountException {
        return null;
    }

    /**
     * FIXME: - keithw - this should use model concept
     * {@link AccountPaymentEntity} and not {@link PaymentData} dto
     */
    protected AccountPaymentEntity makePayment(final PaymentData accountPaymentData) throws AccountException {
        return null;
    }

    // FIXME - keithw - empty implementation so should be abstract (account is an abstract concept?)
    protected void updateTotalFeeAmount(final Money totalFeeAmount) {
    }

    // FIXME - keithw - empty implementation so should be abstract (account is an abstract concept?)
    protected void updateTotalPenaltyAmount(final Money totalPenaltyAmount) {
    }

    protected Money updateAccountActionDateEntity(final List<Short> intallmentIdList, final Short feeId) {
        return new Money();
    }

    protected boolean isAdjustPossibleOnLastTrxn() {
        return false;
    }

    public void removeFees(final Short feeId, final Short personnelId) throws AccountException {
    }

    protected void activationDateHelper(final Short newStatusId) throws AccountException {
    }

    /**
     * Return list of unpaid AccountActionDateEntities occurring on or after
     * today
     */
    protected List<Short> getApplicableInstallmentIdsForRemoveFees() {
        List<Short> installmentIdList = new ArrayList<Short>();
        for (AccountActionDateEntity accountActionDateEntity : getApplicableIdsForFutureInstallments()) {
            installmentIdList.add(accountActionDateEntity.getInstallmentId());
        }
        AccountActionDateEntity accountActionDateEntity = getDetailsOfNextInstallment();
        if (accountActionDateEntity != null
                && !DateUtils.getDateWithoutTimeStamp(accountActionDateEntity.getActionDate().getTime()).equals(
                        DateUtils.getCurrentDateWithoutTimeStamp())) {
            installmentIdList.add(accountActionDateEntity.getInstallmentId());
        }
        return installmentIdList;
    }

    protected List<AccountTrxnEntity> reversalAdjustment(final String adjustmentComment, final AccountPaymentEntity lastPayment)
            throws AccountException {
        return lastPayment.reversalAdjustment(getLoggedInUser(), adjustmentComment);

    }

    private void setFinancialEntries(final FinancialTransactionBO financialTrxn, final TransactionHistoryView transactionHistory) {
        String debit = "-";
        String credit = "-";
        String notes = "-";
        if (financialTrxn.isDebitEntry()) {
            debit = String.valueOf(removeSign(financialTrxn.getPostedAmount()));
        } else if (financialTrxn.isCreditEntry()) {
            credit = String.valueOf(removeSign(financialTrxn.getPostedAmount()));
        }
        Short entityId = financialTrxn.getAccountTrxn().getAccountActionEntity().getId();
        if (financialTrxn.getNotes() != null && !financialTrxn.getNotes().equals("")
                && !entityId.equals(AccountActionTypes.CUSTOMER_ACCOUNT_REPAYMENT.getValue())
                && !entityId.equals(AccountActionTypes.LOAN_REPAYMENT.getValue())) {
            notes = financialTrxn.getNotes();
        }

        transactionHistory.setFinancialEnteries(financialTrxn.getTrxnId(), financialTrxn.getActionDate(), financialTrxn
                .getFinancialAction().getName(), financialTrxn.getGlcode().getGlcode(), debit, credit, financialTrxn
                .getPostedDate(), notes);

    }

    private void setAccountingEntries(final AccountTrxnEntity accountTrxn, final TransactionHistoryView transactionHistory) {

        transactionHistory.setAccountingEnteries(accountTrxn.getAccountPayment().getPaymentId(), String
                .valueOf(removeSign(accountTrxn.getAmount())), accountTrxn.getCustomer().getDisplayName(), accountTrxn
                .getPersonnel().getDisplayName());
    }

    private AccountCustomFieldEntity getAccountCustomField(final Short fieldId) {
        if (null != this.accountCustomFields && this.accountCustomFields.size() > 0) {
            for (AccountCustomFieldEntity obj : this.accountCustomFields) {
                if (obj.getFieldId().equals(fieldId)) {
                    return obj;
                }
            }
        }
        return null;
    }

    protected final FeeInstallment handleOneTime(final AccountFeesEntity accountFee, final List<InstallmentDate> installmentDates) {
        Money accountFeeAmount = accountFee.getAccountFeeAmount();
        Date feeDate = installmentDates.get(0).getInstallmentDueDate();
        MifosLogManager.getLogger(LoggerConstants.ACCOUNTSLOGGER).debug("Handling OneTime fee" + feeDate);
        Short installmentId = getMatchingInstallmentId(installmentDates, feeDate);
        MifosLogManager.getLogger(LoggerConstants.ACCOUNTSLOGGER).debug(
                "OneTime fee applicable installment id " + installmentId);
        return buildFeeInstallment(installmentId, accountFeeAmount, accountFee);
    }

    private void removeInstallmentsNeedNotPay(final Short installmentSkipToStartRepayment,
            final List<InstallmentDate> installmentDates) {
        int removeCounter = 0;
        for (int i = 0; i < installmentSkipToStartRepayment; i++) {
            installmentDates.remove(removeCounter);
        }
        // re-adjust the installment ids
        if (installmentSkipToStartRepayment > 0) {
            int count = installmentDates.size();
            for (int i = 0; i < count; i++) {
                InstallmentDate instDate = installmentDates.get(i);
                instDate.setInstallmentId(new Short(Integer.toString(i + 1)));
            }
        }
    }

    private void validate(final UserContext userContext, final CustomerBO customer, final AccountTypes accountType,
            final AccountState accountState) throws AccountException {
        if (userContext == null || customer == null || accountType == null || accountState == null) {
            throw new AccountException(AccountExceptionConstants.CREATEEXCEPTION);
        }
    }

    private void setFlag(final AccountStateFlagEntity accountStateFlagEntity) {
        accountStateFlagEntity.setLocaleId(this.getUserContext().getLocaleId());
        Iterator iter = this.getAccountFlags().iterator();
        while (iter.hasNext()) {
            AccountFlagMapping currentFlag = (AccountFlagMapping) iter.next();
            if (!currentFlag.getFlag().isFlagRetained()) {
                iter.remove();
            }
        }
        this.addAccountFlag(accountStateFlagEntity);
    }

    private PersonnelBO getLoggedInUser() throws AccountException {
        try {
            return getPersonnelPersistence().getPersonnel(getUserContext().getId());
        } catch (PersistenceException pe) {
            throw new AccountException(pe);
        }
    }

    protected void updateCustomFields(final List<CustomFieldView> customFields) throws InvalidDateException {
        if (customFields == null) {
            return;
        }
        for (CustomFieldView fieldView : customFields) {
            if (fieldView.getFieldType().equals(CustomFieldType.DATE.getValue())
                    && org.apache.commons.lang.StringUtils.isNotBlank(fieldView.getFieldValue())) {
                fieldView.convertDateToUniformPattern(getUserContext().getPreferredLocale());
            }
            if (getAccountCustomFields().size() > 0) {
                for (AccountCustomFieldEntity fieldEntity : getAccountCustomFields()) {
                    if (fieldView.getFieldId().equals(fieldEntity.getFieldId())) {
                        fieldEntity.setFieldValue(fieldView.getFieldValue());
                    }
                }
            } else {
                for (CustomFieldView view : customFields) {
                    this.getAccountCustomFields().add(
                            new AccountCustomFieldEntity(this, view.getFieldId(), view.getFieldValue()));
                }
            }
        }
    }

    public Date getAccountApprovalDate() {
        Date approvalDate = null;
        Set<AccountStatusChangeHistoryEntity> statusChangeHistory = this.getAccountStatusChangeHistory();

        for (AccountStatusChangeHistoryEntity status : statusChangeHistory) {

            if (status.getNewStatus().isInState(AccountState.LOAN_APPROVED)) {
                approvalDate = status.getCreatedDate();
                break;
            }
        }
        return approvalDate;
    }

    public Integer getOffsettingAllowable() {
        return offsettingAllowable;
    }

    public void setOffsettingAllowable(final Integer offsettingAllowable) {
        this.offsettingAllowable = offsettingAllowable;
    }

    public boolean isInState(final AccountState state) {
        return accountState.isInState(state);
    }

    public boolean isLoanAccount() {
        return isOfType(LOAN_ACCOUNT);
    }

    public boolean isSavingsAccount() {
        return isOfType(SAVINGS_ACCOUNT);
    }

    public boolean isOfType(final AccountTypes accountType) {
        return accountType.equals(getType());
    }

    @Override
    public String toString() {
        return "{" + globalAccountNum + "}";
    }

    public boolean isActiveLoanAccount() {
        return AccountState.fromShort(accountState.getId()).isActiveLoanAccountState();
    }

    public void setExternalId(String externalId) {
        this.externalId= externalId;        
    }
    public String getExternalId() {
        return externalId;        
    }
    
    /**
     * Return true if a given payment amount valid for this account.
     * @param amount the payment amount to validate.
     * 
     */
    public boolean paymentAmountIsValid(final Money amount) {
        return true;
    }    
}
