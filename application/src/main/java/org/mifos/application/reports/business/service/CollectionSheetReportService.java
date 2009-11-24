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

package org.mifos.application.reports.business.service;

import static org.mifos.framework.util.helpers.FilePaths.REPORT_PRODUCT_OFFERING_CONFIG;
import static org.mifos.framework.util.helpers.NumberUtils.convertIntegerToShort;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mifos.application.accounts.business.service.AccountBusinessService;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.collectionsheet.business.CollSheetCustBO;
import org.mifos.application.collectionsheet.business.CollSheetLnDetailsEntity;
import org.mifos.application.collectionsheet.business.CollSheetSavingsDetailsEntity;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.service.CustomerBusinessService;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.office.business.service.OfficeBusinessService;
import org.mifos.application.personnel.business.service.PersonnelBusinessService;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.application.productdefinition.business.SavingsOfferingBO;
import org.mifos.application.productdefinition.business.service.LoanPrdBusinessService;
import org.mifos.application.productdefinition.business.service.SavingsPrdBusinessService;
import org.mifos.application.reports.business.dto.CollectionSheetReportDTO;
import org.mifos.application.reports.business.dto.CollectionSheetReportData;
import org.mifos.application.reports.util.helpers.ReportUtils;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.util.CollectionUtils;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.NumberUtils;
import org.mifos.framework.util.helpers.Predicate;
import org.springframework.core.io.ClassPathResource;

// public interface for services used by collection sheet report
/**
 * 
 * @deprecated keithw - don't think that this is useful anymore???
 */
@Deprecated
public class CollectionSheetReportService implements ICollectionSheetReportService {
    private final OfficeBusinessService officeBusinessService;
    private final CollectionSheetService collectionSheetService;
    private final AccountBusinessService accountBusinessService;
    private final ReportProductOfferingService reportProductOfferingService;
    private final CascadingReportParameterService cascadingReportParameterService;

    CollectionSheetReportService(final CollectionSheetService collectionSheetService,
            final OfficeBusinessService officeBusinessService, final AccountBusinessService accountBusinessService,
            final ReportProductOfferingService reportProductOfferingService,
            final CascadingReportParameterService cascadingReportParameterService) {
        super();
        this.collectionSheetService = collectionSheetService;
        this.officeBusinessService = officeBusinessService;
        this.accountBusinessService = accountBusinessService;
        this.reportProductOfferingService = reportProductOfferingService;
        this.cascadingReportParameterService = cascadingReportParameterService;
    }

    CollectionSheetReportService() {
        this(new CollectionSheetService(), new OfficeBusinessService(), new AccountBusinessService(),
                new ReportProductOfferingService(new LoanPrdBusinessService(), new SavingsPrdBusinessService(),
                        new ClassPathResource(REPORT_PRODUCT_OFFERING_CONFIG)), new CascadingReportParameterService(
                        new ReportsParameterService(), new PersonnelBusinessService(), new CustomerBusinessService()));
    }

    public List<CollectionSheetReportDTO> getCollectionSheets(final Integer branchId, final Integer officerId, final Integer customerId,
            final Date meetingDate) throws Exception {
        OfficeBO office = officeBusinessService.getOffice(NumberUtils.convertIntegerToShort(branchId));
        List<CustomerBO> applicableCustomers = cascadingReportParameterService.getApplicableCustomers(
                convertIntegerToShort(branchId), convertIntegerToShort(officerId), customerId);
        java.sql.Date sqlMeetingDate = DateUtils.convertToSqlDate(meetingDate);
        List<CollectionSheetReportDTO> collectionSheetReport = new ArrayList<CollectionSheetReportDTO>();
        for (CustomerBO customer : applicableCustomers) {
            collectionSheetReport.addAll(collectCenterLevelData(office, customer, sqlMeetingDate));
        }
        return collectionSheetReport;
    }

    private List<CollectionSheetReportDTO> collectCenterLevelData(final OfficeBO office, final CustomerBO center,
            final java.sql.Date meetingDate) throws Exception {
        List<CollectionSheetReportDTO> collectionSheets = new ArrayList<CollectionSheetReportDTO>();
        List<CollSheetCustBO> centerCollectionSheets = collectionSheetService
                .getCollectionSheetForCustomerOnMeetingDate(meetingDate, center.getCustomerId(), center.getPersonnel()
                        .getPersonnelId(), CustomerLevel.CENTER);
        for (CollSheetCustBO centerCollectionSheet : centerCollectionSheets) {
            List<CollSheetCustBO> collectionSheetForGroups = collectionSheetService.getCollectionSheetForGroups(
                    meetingDate, centerCollectionSheet, center.getPersonnel().getPersonnelId());
            collectionSheets.addAll(collectGroupLevelData(office, center, meetingDate, collectionSheetForGroups));
        }
        return collectionSheets;
    }

    private List<CollectionSheetReportDTO> collectGroupLevelData(final OfficeBO office, final CustomerBO customer,
            final java.sql.Date meetingDate, final List<CollSheetCustBO> groupCollectionSheets) throws Exception {
        LoanOfferingBO loanOffering1 = reportProductOfferingService.getLoanOffering1();
        LoanOfferingBO loanOffering2 = reportProductOfferingService.getLoanOffering2();
        SavingsOfferingBO savingsOffering1 = reportProductOfferingService.getSavingsOffering1();
        SavingsOfferingBO savingsOffering2 = reportProductOfferingService.getSavingsOffering2();
        List<CollectionSheetReportDTO> collectionSheetReport = new ArrayList<CollectionSheetReportDTO>();
        try {
            for (CollSheetCustBO groupCollectionSheet : groupCollectionSheets) {
                List<CollSheetCustBO> clientCollectionSheets = collectionSheetService.getCollectionSheetForCustomers(
                        meetingDate, groupCollectionSheet, customer.getPersonnel().getPersonnelId());
                for (CollSheetCustBO clientCollectionSheet : clientCollectionSheets) {
                    collectionSheetReport.add(new CollectionSheetReportDTO(getLoanProduct(clientCollectionSheet,
                            loanOffering1), getLoanProduct(clientCollectionSheet, loanOffering2), getSavingProduct(
                            clientCollectionSheet, savingsOffering1), getSavingProduct(clientCollectionSheet,
                            savingsOffering2), clientCollectionSheet.getCustId(), clientCollectionSheet
                            .getParentCustomerId(), clientCollectionSheet.getCustDisplayName(), office.getOfficeName(),
                            groupCollectionSheet.getCustDisplayName(), customer, loanOffering1
                                    .getPrdOfferingShortName(), loanOffering2.getPrdOfferingShortName(),
                            savingsOffering1.getPrdOfferingShortName(), savingsOffering2.getPrdOfferingShortName(),
                            DateUtils.convertSqlToDate(meetingDate)));
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }
        return collectionSheetReport;
    }

    CollSheetLnDetailsEntity getLoanProduct(final CollSheetCustBO collectionSheet, final LoanOfferingBO productOffering)
            throws Exception {
        return CollectionUtils.find(collectionSheet.getCollectionSheetLoanDetails(),
                new Predicate<CollSheetLnDetailsEntity>() {
                    public boolean evaluate(final CollSheetLnDetailsEntity loanDetail) throws Exception {
                        return ((LoanBO) accountBusinessService.getAccount(loanDetail.getAccountId()))
                                .isOfProductOffering(productOffering);
                    }
                });
    }

    CollSheetSavingsDetailsEntity getSavingProduct(final CollSheetCustBO collectionSheet,
            final SavingsOfferingBO productOffering) throws Exception {
        return CollectionUtils.find(collectionSheet.getCollSheetSavingsDetails(),
                new Predicate<CollSheetSavingsDetailsEntity>() {
                    public boolean evaluate(final CollSheetSavingsDetailsEntity savingDetail) throws Exception {
                        return ((SavingsBO) accountBusinessService.getAccount(savingDetail.getAccountId()))
                                .isOfProductOffering(productOffering);
                    }
                });
    }

    public List<CollectionSheetReportData> getReportData(final Integer branchId, final String meetingDate, final Integer personnelId,
            final Integer centerId) throws ServiceException {
        try {
            Date meetingDateAsDate = ReportUtils.parseReportDate(meetingDate);
            return collectionSheetService.extractReportData(branchId, meetingDateAsDate, personnelId, centerId);
        } catch (ParseException e) {
            throw new ServiceException(e);
        }
    }

    public boolean displaySignatureColumn(final Integer columnNumber) throws ServiceException {
        return reportProductOfferingService.displaySignatureColumn(columnNumber);
    }
}
