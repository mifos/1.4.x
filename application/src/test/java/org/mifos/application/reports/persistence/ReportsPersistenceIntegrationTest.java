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

package org.mifos.application.reports.persistence;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.hibernate.Query;
import org.hibernate.Session;
import org.mifos.application.reports.business.ReportsBO;
import org.mifos.application.reports.business.ReportsCategoryBO;
import org.mifos.application.reports.business.ReportsDataSource;
import org.mifos.application.reports.business.ReportsJasperMap;
import org.mifos.application.reports.business.ReportsParams;
import org.mifos.application.reports.util.helpers.ReportsConstants;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.TestDatabase;

public class ReportsPersistenceIntegrationTest extends MifosIntegrationTestCase {

    public ReportsPersistenceIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    private ReportsPersistence reportsPersistence;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        reportsPersistence = new ReportsPersistence();
    }

    public void testGetAllReportCategories() {
        List<ReportsCategoryBO> listOfReportCategories = reportsPersistence.getAllReportCategories();
        Session session = StaticHibernateUtil.getSessionTL();
        Query query = session.createQuery("select count(*) from ReportsCategoryBO");
        List list = query.list();

       Assert.assertEquals(((Long) list.get(0)).intValue(), listOfReportCategories.size());
       Assert.assertEquals("1", listOfReportCategories.get(0).getReportCategoryId().toString());
       Assert.assertEquals("Client Detail", listOfReportCategories.get(0).getReportCategoryName());
       Assert.assertEquals("2", listOfReportCategories.get(1).getReportCategoryId().toString());
       Assert.assertEquals("Performance", listOfReportCategories.get(1).getReportCategoryName());
       Assert.assertEquals("3", listOfReportCategories.get(2).getReportCategoryId().toString());
       Assert.assertEquals("Center", listOfReportCategories.get(2).getReportCategoryName());
       Assert.assertEquals("4", listOfReportCategories.get(3).getReportCategoryId().toString());
       Assert.assertEquals("Loan Product Detail", listOfReportCategories.get(3).getReportCategoryName());
       Assert.assertEquals("5", listOfReportCategories.get(4).getReportCategoryId().toString());
       Assert.assertEquals("Status", listOfReportCategories.get(4).getReportCategoryName());
       Assert.assertEquals("6", listOfReportCategories.get(5).getReportCategoryId().toString());
       Assert.assertEquals("Analysis", listOfReportCategories.get(5).getReportCategoryName());
       Assert.assertEquals("7", listOfReportCategories.get(6).getReportCategoryId().toString());
       Assert.assertEquals("Miscellaneous", listOfReportCategories.get(6).getReportCategoryName());

    }

    public void testGetAllReportsForACategory() {
        List<ReportsCategoryBO> listOfReportCategories = reportsPersistence.getAllReportCategories();
        Set<ReportsBO> reportsSet = listOfReportCategories.get(0).getReportsSet();

        for (Iterator iter = reportsSet.iterator(); iter.hasNext();) {
            ReportsBO reports = (ReportsBO) iter.next();
            if (reports.getReportId().equals("1")) {
               Assert.assertEquals("Client Detail", reports.getReportName());
            } else if (reports.getReportId().equals("2")) {
               Assert.assertEquals("Performance", reports.getReportName());
            } else if (reports.getReportId().equals("3")) {
               Assert.assertEquals("Kendra", reports.getReportName());
            } else if (reports.getReportId().equals("4")) {
               Assert.assertEquals("Loan Product Detail", reports.getReportName());
            } else if (reports.getReportId().equals("5")) {
               Assert.assertEquals("Status", reports.getReportName());
            } else if (reports.getReportId().equals("6")) {
               Assert.assertEquals("Analysis", reports.getReportName());
            } else if (reports.getReportId().equals("7")) {
               Assert.assertEquals("Miscellaneous", reports.getReportName());
            } else {
                /*
                 * We always get here, because the above code is comparing a
                 * Short to a String. TODO: how do we really want to test this,
                 * anyway?
                 */
                // Assert.fail("unexpected report " + reports.getReportId());
            }
        }
    }

    public void testGetReportPath() {
        List<ReportsCategoryBO> listOfReportCategories = reportsPersistence.getAllReportCategories();
        Set<ReportsBO> reportsSet = listOfReportCategories.get(0).getReportsSet();

        for (Iterator iter = reportsSet.iterator(); iter.hasNext();) {
            ReportsBO reports = (ReportsBO) iter.next();
            if (reports.getReportId().equals("1"))
               Assert.assertEquals("report_designer", reports.getReportIdentifier());
        }
    }

    public void testGetAllParameters() throws Exception {
        {
            List<ReportsParams> parameters = reportsPersistence.getAllReportParams();
           Assert.assertEquals(0, parameters.size());
        }

        TestDatabase database = TestDatabase.makeStandard();
        database.execute("insert into report_parameter(name, type, classname)"
                + "values('my_report', 'my_type', 'my_class')");
        Session session = database.openSession();
        List<ReportsParams> moreParameters = reportsPersistence.getAllReportParams(session);
       Assert.assertEquals(1, moreParameters.size());
        ReportsParams parameter = moreParameters.get(0);
       Assert.assertEquals("my_report", parameter.getName());
       Assert.assertEquals("my_type", parameter.getType());
       Assert.assertEquals("my_class", parameter.getClassname());

        /*
         * Just a sanity check that the above is mucking with the local database
         * not the global test database.
         */
        {
            List<ReportsParams> parameters = reportsPersistence.getAllReportParams();
           Assert.assertEquals(0, parameters.size());
        }
    }

    public void testCreateJasper() throws Exception {
        TestDatabase database = TestDatabase.makeStandard();
        Session session = database.openSession();
        ReportsJasperMap jasperMap = new ReportsJasperMap(null, "report.jrxml");
        new ReportsPersistence().createJasperMap(session, jasperMap);
        short reportId = jasperMap.getReportId();

        Session session2 = database.openSession();
        ReportsJasperMap reRead = new ReportsPersistence().oneJasperOfReportId(session2, reportId);
       Assert.assertEquals("report.jrxml", reRead.getReportJasper());
    }

    public void testGetReport() {
        Short reportId = 28;
        ReportsBO report = reportsPersistence.getReport(reportId);
       Assert.assertEquals(reportId, report.getReportId());
    }

    public void testGetReportCategoryByCategoryId() {
        Short reportCategoryId = 1;
        ReportsCategoryBO reportCategory = reportsPersistence.getReportCategoryByCategoryId(reportCategoryId);
       Assert.assertEquals(reportCategoryId, reportCategory.getReportCategoryId());
    }

    public void testGetAllReports() {
        org.hibernate.Session session = StaticHibernateUtil.getSessionTL();
        Query query = session.createQuery("select count(*) from ReportsBO");
        List list = query.list();
        int reportsNum = ((Long) list.get(0)).intValue();
       Assert.assertEquals(reportsNum, reportsPersistence.getAllReports().size());
    }

    public void testViewDataSource() throws Exception {
        List<ReportsDataSource> queryResult = reportsPersistence.viewDataSource(1);
        Iterator itrQueryResult = queryResult.iterator();
        while (itrQueryResult.hasNext()) {
            ReportsDataSource objReportsDataSource = (ReportsDataSource) itrQueryResult.next();
           Assert.assertEquals(ReportsConstants.HIDDEN_PASSWORD, objReportsDataSource.getPassword());
        }
    }

}
