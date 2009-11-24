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
package org.mifos.application.servicefacade;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.business.AccountPaymentEntity;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.loan.util.helpers.LoanAccountsProductView;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.collectionsheet.business.CollectionSheetEntryGridDto;
import org.mifos.application.collectionsheet.business.CollectionSheetEntryView;
import org.mifos.application.collectionsheet.util.helpers.CollectionSheetDataView;
import org.mifos.application.customer.business.CustomerView;
import org.mifos.application.customer.client.business.ClientAttendanceBO;
import org.mifos.application.customer.persistence.CustomerPersistence;
import org.mifos.application.customer.util.helpers.CustomerAccountView;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.master.business.CustomValueListElement;
import org.mifos.application.master.business.MasterDataEntity;
import org.mifos.application.master.business.MifosCurrency;
import org.mifos.application.master.business.PaymentTypeEntity;
import org.mifos.application.master.persistence.MasterPersistence;
import org.mifos.application.master.util.helpers.MasterConstants;
import org.mifos.application.office.business.OfficeView;
import org.mifos.application.office.persistence.OfficePersistence;
import org.mifos.application.personnel.business.PersonnelView;
import org.mifos.application.personnel.persistence.PersonnelPersistence;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.config.AccountingRules;
import org.mifos.config.ClientRules;
import org.mifos.core.MifosRuntimeException;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.components.logger.MifosLogger;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.DateUtils;

/**
 * Default implementation of {@link CollectionSheetServiceFacade}.
 */
public class CollectionSheetServiceFacadeWebTier implements CollectionSheetServiceFacade {
    private static final MifosLogger logger = MifosLogManager.getLogger(LoggerConstants.COLLECTIONSHEETLOGGER);

    private final OfficePersistence officePersistence;
    private final MasterPersistence masterPersistence;
    private final PersonnelPersistence personnelPersistence;
    private final CustomerPersistence customerPersistence;
    private final CollectionSheetService collectionSheetService;
    private final ClientAttendanceAssembler clientAttendanceAssembler;
    private final LoanAccountAssembler loanAccountAssembler;
    private final CustomerAccountAssembler customerAccountAssembler;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final AccountPaymentAssembler accountPaymentAssembler;
    private final CollectionSheetDtoTranslator collectionSheetTranslator;

    public CollectionSheetServiceFacadeWebTier(final OfficePersistence officePersistence,
            final MasterPersistence masterPersistence, final PersonnelPersistence personnelPersistence,
            final CustomerPersistence customerPersistence, final CollectionSheetService collectionSheetService,
            final ClientAttendanceAssembler clientAttendanceAssembler, final LoanAccountAssembler loanAccountAssembler,
            final CustomerAccountAssembler customerAccountAssember,
            final SavingsAccountAssembler savingsAccountAssembler,
            final AccountPaymentAssembler accountPaymentAssembler,
            final CollectionSheetDtoTranslator collectionSheetTranslator) {
        this.officePersistence = officePersistence;
        this.masterPersistence = masterPersistence;
        this.personnelPersistence = personnelPersistence;
        this.customerPersistence = customerPersistence;
        this.collectionSheetService = collectionSheetService;
        this.clientAttendanceAssembler = clientAttendanceAssembler;
        this.loanAccountAssembler = loanAccountAssembler;
        this.customerAccountAssembler = customerAccountAssember;
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.accountPaymentAssembler = accountPaymentAssembler;
        this.collectionSheetTranslator = collectionSheetTranslator;
    }

    public CollectionSheetEntryFormDto loadAllActiveBranchesAndSubsequentDataIfApplicable(final UserContext userContext) {

        final Short branchId = userContext.getBranchId();
        final Short centerHierarchyExists = ClientRules.getCenterHierarchyExists() ? Constants.YES : Constants.NO;

        List<OfficeView> activeBranches = new ArrayList<OfficeView>();
        List<ListItem<Short>> paymentTypesDtoList = new ArrayList<ListItem<Short>>();
        List<CustomerView> customerList = new ArrayList<CustomerView>();
        List<PersonnelView> loanOfficerList = new ArrayList<PersonnelView>();
        Short reloadFormAutomatically = Constants.YES;
        final Short backDatedTransactionAllowed = Constants.NO;

        try {
            final List<MasterDataEntity> paymentTypesList = masterPersistence.retrieveMasterEntities(
                    PaymentTypeEntity.class, Short.valueOf("1"));
            paymentTypesDtoList = convertToPaymentTypesListItemDto(paymentTypesList);

            activeBranches = officePersistence.getActiveOffices(branchId);
            if (activeBranches.size() == 1) {
                loanOfficerList = personnelPersistence.getActiveLoanOfficersInBranch(PersonnelConstants.LOAN_OFFICER,
                        branchId, userContext.getId(), userContext.getLevelId());

                if (loanOfficerList.size() == 1) {

                    Short customerLevel;
                    if (centerHierarchyExists.equals(Constants.YES)) {
                        customerLevel = Short.valueOf(CustomerLevel.CENTER.getValue());
                    } else {
                        customerLevel = Short.valueOf(CustomerLevel.GROUP.getValue());
                    }

                    customerList = customerPersistence.getActiveParentList(loanOfficerList.get(0).getPersonnelId(),
                            customerLevel, branchId);

                    if (customerList.size() == 1) {
                        reloadFormAutomatically = Constants.YES;
                    }

                    reloadFormAutomatically = Constants.NO;
                }
            }
        } catch (PersistenceException e) {
            throw new MifosRuntimeException(e);
        }

        return new CollectionSheetEntryFormDto(activeBranches, paymentTypesDtoList, loanOfficerList, customerList,
                reloadFormAutomatically, centerHierarchyExists, backDatedTransactionAllowed);
    }

    public CollectionSheetEntryFormDto loadLoanOfficersForBranch(final Short branchId, final UserContext userContext,
            final CollectionSheetEntryFormDto formDto) {

        List<PersonnelView> loanOfficerList = new ArrayList<PersonnelView>();
        try {
            loanOfficerList = personnelPersistence.getActiveLoanOfficersInBranch(PersonnelConstants.LOAN_OFFICER,
                    branchId, userContext.getId(), userContext.getLevelId());
        } catch (PersistenceException e) {
            throw new MifosRuntimeException(e);
        }

        return new CollectionSheetEntryFormDto(formDto.getActiveBranchesList(), formDto.getPaymentTypesList(),
                loanOfficerList, formDto.getCustomerList(), formDto.getReloadFormAutomatically(), formDto
                        .getCenterHierarchyExists(), formDto.getBackDatedTransactionAllowed());
    }

    public CollectionSheetEntryFormDto loadCustomersForBranchAndLoanOfficer(final Short personnelId,
            final Short officeId, final CollectionSheetEntryFormDto formDto) {

        Short customerLevel;
        if (formDto.getCenterHierarchyExists().equals(Constants.YES)) {
            customerLevel = Short.valueOf(CustomerLevel.CENTER.getValue());
        } else {
            customerLevel = Short.valueOf(CustomerLevel.GROUP.getValue());
        }

        List<CustomerView> customerList = new ArrayList<CustomerView>();
        try {
            customerList = customerPersistence.getActiveParentList(personnelId, customerLevel, officeId);
        } catch (PersistenceException e) {
            throw new MifosRuntimeException(e);
        }
        return new CollectionSheetEntryFormDto(formDto.getActiveBranchesList(), formDto.getPaymentTypesList(), formDto
                .getLoanOfficerList(), customerList, formDto.getReloadFormAutomatically(), formDto
                .getCenterHierarchyExists(), formDto.getBackDatedTransactionAllowed());
    }

    public CollectionSheetEntryFormDto loadMeetingDateForCustomer(final Integer customerId,
            final CollectionSheetEntryFormDto formDto) {

        Short backDatedTransactionAllowed = Constants.NO;
        java.util.Date meetingDate = null;
        try {
            meetingDate = customerPersistence.getLastMeetingDateForCustomer(customerId);

            final boolean isBackDatedTrxnAllowed = AccountingRules.isBackDatedTxnAllowed();
            if (meetingDate == null) {
                meetingDate = DateUtils.getCurrentDateWithoutTimeStamp();
            }
            backDatedTransactionAllowed = isBackDatedTrxnAllowed ? Constants.YES : Constants.NO;
        } catch (PersistenceException e) {
            throw new MifosRuntimeException(e);
        }

        return new CollectionSheetEntryFormDto(formDto.getActiveBranchesList(), formDto.getPaymentTypesList(), formDto
                .getLoanOfficerList(), formDto.getCustomerList(), formDto.getReloadFormAutomatically(), formDto
                .getCenterHierarchyExists(), backDatedTransactionAllowed, meetingDate);
    }

    public CollectionSheetEntryGridDto generateCollectionSheetEntryGridView(
            final CollectionSheetFormEnteredDataDto formEnteredDataDto, final MifosCurrency currency) {

        final CollectionSheetDto collectionSheet = collectionSheetService.retrieveCollectionSheet(formEnteredDataDto
                .getCustomer().getCustomerId(), formEnteredDataDto.getMeetingDate());

        try {
            final List<CustomValueListElement> attendanceTypesList = masterPersistence.getCustomValueList(
                    MasterConstants.ATTENDENCETYPES, "org.mifos.application.master.business.CustomerAttendanceType",
                    "attendanceId").getCustomValueListElements();

            final CollectionSheetEntryGridDto translatedGridView = collectionSheetTranslator.toLegacyDto(
                    collectionSheet, formEnteredDataDto, attendanceTypesList, currency);

            return translatedGridView;
        } catch (SystemException e) {
            throw new MifosRuntimeException(e);
        } catch (ApplicationException e) {
            throw new MifosRuntimeException(e);
        }
    }

    public CollectionSheetEntryGridDto previewCollectionSheetEntry(
            final CollectionSheetEntryGridDto previousCollectionSheetEntryDto, final CollectionSheetDataView dataView) {

        CollectionSheetEntryGridDto newCollectionSheetEntryGridDto = null;

        final CollectionSheetEntryView bulkEntryParent = previousCollectionSheetEntryDto.getBulkEntryParent();
        switch (CustomerLevel.getLevel(bulkEntryParent.getCustomerDetail().getCustomerLevelId())) {
        case CENTER:
            newCollectionSheetEntryGridDto = new CollectionSheetEntryGridDtoTranslator().translateAsCenter(
                    previousCollectionSheetEntryDto, dataView);
            break;
        case GROUP:
        case CLIENT:
            newCollectionSheetEntryGridDto = new CollectionSheetEntryGridDtoTranslator().translateAsGroup(
                    previousCollectionSheetEntryDto, dataView);
            break;
        }

        return newCollectionSheetEntryGridDto;
    }

    public CollectionSheetErrorsView saveCollectionSheet(
            final CollectionSheetEntryGridDto previousCollectionSheetEntryDto,
            final CollectionSheetEntryDecomposedView decomposedViews, final Short userId) {

        final AccountPaymentEntity payment = accountPaymentAssembler.fromDto(userId, previousCollectionSheetEntryDto);

        final List<String> failedSavingsDepositAccountNums = new ArrayList<String>();
        final List<String> failedSavingsWithdrawalNums = new ArrayList<String>();
        final List<String> failedCustomerAccountPaymentNums = new ArrayList<String>();

        final List<CollectionSheetEntryView> collectionSheeetEntryViews = decomposedViews
                .getParentCollectionSheetEntryViews();
        final List<SavingsBO> savingsAccounts = savingsAccountAssembler.fromDto(collectionSheeetEntryViews, payment,
                failedSavingsDepositAccountNums, failedSavingsWithdrawalNums);

        final List<CollectionSheetEntryView> collectionSheetEntryViews = decomposedViews
                .getParentCollectionSheetEntryViews();
        final List<ClientAttendanceBO> clientAttendances = clientAttendanceAssembler.fromDto(collectionSheetEntryViews,
                payment.getPaymentDate());

        final List<LoanAccountsProductView> loanAccountProductViews = decomposedViews.getLoanAccountViews();
        final List<LoanBO> loanAccounts = loanAccountAssembler.fromDto(loanAccountProductViews, payment);

        final List<CustomerAccountView> customerAccountViews = decomposedViews.getCustomerAccountViews();
        final List<AccountBO> customerAccounts = customerAccountAssembler.fromDto(customerAccountViews, payment,
                failedCustomerAccountPaymentNums);

        boolean databaseErrorOccurred = false;
        Throwable databaseError = null;

        try {
            collectionSheetService.saveCollectionSheet(clientAttendances, loanAccounts, customerAccounts,
                    savingsAccounts);
        } catch (HibernateException e) {
            logger.error("database error saving collection sheet", e);
            databaseErrorOccurred = true;
            databaseError = e;
        }

        return new CollectionSheetErrorsView(failedSavingsDepositAccountNums, failedSavingsWithdrawalNums,
                failedCustomerAccountPaymentNums, databaseErrorOccurred, databaseError);
    }

    private List<ListItem<Short>> convertToPaymentTypesListItemDto(final List<MasterDataEntity> paymentTypesList) {
        List<ListItem<Short>> paymentTypesDtoList = new ArrayList<ListItem<Short>>();
        for (MasterDataEntity paymentType : paymentTypesList) {
            paymentTypesDtoList.add(new ListItem<Short>(paymentType.getId(), paymentType.getName()));
        }
        return paymentTypesDtoList;
    }
}