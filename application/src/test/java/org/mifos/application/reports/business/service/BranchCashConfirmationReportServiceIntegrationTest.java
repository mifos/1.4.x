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

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.mifos.framework.util.helpers.MoneyFactory.ZERO;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mifos.application.cashconfirmationreport.BranchCashConfirmationDisbursementBO;
import org.mifos.application.cashconfirmationreport.BranchCashConfirmationInfoBO;
import org.mifos.application.cashconfirmationreport.BranchCashConfirmationIssueBO;
import org.mifos.application.cashconfirmationreport.BranchCashConfirmationReportBO;
import org.mifos.application.cashconfirmationreport.persistence.BranchCashConfirmationReportPersistence;
import org.mifos.application.office.business.service.OfficeBusinessService;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;

public class BranchCashConfirmationReportServiceIntegrationTest extends BranchReportIntegrationTestCase {

    public BranchCashConfirmationReportServiceIntegrationTest() throws Exception {
        super();
    }

    private BranchCashConfirmationReportPersistence persistenceMock;
    private BranchCashConfirmationReportService service;

    public void testGetCenterIssues() throws ServiceException {
        BranchCashConfirmationReportBO reportBO = new BranchCashConfirmationReportBO(BRANCH_ID_SHORT, RUN_DATE);
        BranchCashConfirmationInfoBO issueBO = new BranchCashConfirmationIssueBO("SOME PRODUCT", ZERO);
        reportBO.addCenterIssue(issueBO);
        BranchCashConfirmationInfoBO anotherIssue = new BranchCashConfirmationIssueBO("SOMEMORE", ZERO);
        reportBO.addCenterIssue(anotherIssue);
        Session session = StaticHibernateUtil.getSessionTL();
        Transaction transaction = session.beginTransaction();
        session.save(reportBO);
        List<BranchCashConfirmationInfoBO> centerIssues = ReportServiceFactory.getBranchCashConfirmationReportService(
                null).getCenterIssues(BRANCH_ID, RUN_DATE_STR);
        Assert.assertNotNull(centerIssues);
       Assert.assertEquals(2, centerIssues.size());
       Assert.assertTrue(centerIssues.contains(issueBO));
       Assert.assertTrue(centerIssues.contains(anotherIssue));
        transaction.rollback();
    }

    public void testGetDisbursements() throws Exception {
        expect(persistenceMock.getDisbursements(BRANCH_ID_SHORT, RUN_DATE)).andReturn(
                new ArrayList<BranchCashConfirmationDisbursementBO>());
        replay(persistenceMock);
        service.getDisbursements(BRANCH_ID, RUN_DATE_STR);
        verify(persistenceMock);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        persistenceMock = createMock(BranchCashConfirmationReportPersistence.class);
        service = new BranchCashConfirmationReportService(persistenceMock, new OfficeBusinessService());
    }
}
