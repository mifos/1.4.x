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

package org.mifos.application.accounts.loan.struts.action;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.Ignore;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.loan.struts.actionforms.LoanDisbursmentActionForm;
import org.mifos.application.accounts.util.helpers.AccountConstants;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.master.util.helpers.MasterConstants;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;

@Ignore
public class LoanDisbursmentActionStrutsTest extends MifosMockStrutsTestCase {

    public LoanDisbursmentActionStrutsTest() throws SystemException, ApplicationException {
        super();
    }

    protected UserContext userContext = null;

    protected LoanBO loanBO = null;

    protected LoanBO secondLoanBO = null;

    protected CustomerBO center = null;

    protected CustomerBO group = null;

    protected CustomerBO center2 = null;

    protected CustomerBO group2 = null;
    private Date currentDate = null;

    private String flowKey;

    @Override
    protected void setUp() throws Exception {

        super.setUp();

        userContext = TestObjectFactory.getContext();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");
        request.getSession(false).setAttribute("ActivityContext", TestObjectFactory.getActivityContext());
        flowKey = createFlow(request, LoanDisbursmentAction.class);
        setRequestPathInfo("/loanDisbursmentAction");
        currentDate = new Date(System.currentTimeMillis());
    }

    @Override
    protected void tearDown() throws Exception {
        TestObjectFactory.cleanUp(loanBO);
        TestObjectFactory.cleanUp(group);
        TestObjectFactory.cleanUp(center);
        TestObjectFactory.cleanUp(secondLoanBO);
        TestObjectFactory.cleanUp(group2);
        TestObjectFactory.cleanUp(center2);

        StaticHibernateUtil.closeSession();
        super.tearDown();
    }

    private LoanBO getLoanAccount(AccountState state, Date startDate, int disbursalType) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createCenter("Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        return TestObjectFactory.createLoanAccountWithDisbursement("99999999999", group, state, startDate,
                loanOffering, disbursalType);

    }

    private LoanBO getLoanAccountInGoodStanding(AccountState state, Date startDate, int disbursalType) {
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center2 = TestObjectFactory.createCenter("Center2", meeting);
        group2 = TestObjectFactory.createGroupUnderCenter("Group2", CustomerStatus.GROUP_ACTIVE, center2);

        return TestObjectFactory.createLoanAccountWithDisbursement("888888", group2, state, startDate, loanBO
                .getLoanOffering(), disbursalType);

    }

    public void testLoad() throws Exception {
        createInitialObjects(2);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("method", "load");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyForward(Constants.LOAD_SUCCESS);
        Assert.assertNotNull(SessionUtils.getAttribute(MasterConstants.PAYMENT_TYPE, request));
        LoanDisbursmentActionForm actionForm = (LoanDisbursmentActionForm) request.getSession().getAttribute(
                "loanDisbursmentActionForm");
       Assert.assertEquals(actionForm.getAmount(), loanBO.getAmountTobePaidAtdisburtail());
       Assert.assertEquals(actionForm.getLoanAmount(), loanBO.getLoanAmount());
    }

    public void testPreviewFailure_NomaadatoryFieds() {
        createInitialObjects(2);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        String[] errors = { AccountConstants.ERROR_MANDATORY, AccountConstants.ERROR_MANDATORY };
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("method", "preview");
        actionPerform();

        verifyInputForward();
        verifyActionErrors(errors);

    }

    public void testPreviewFailure_futureTxnDate() {
        createInitialObjects(2);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");

        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.YEAR, 1);
        addRequestParameter("receiptDate", sdf.format(new Date(calendar.getTimeInMillis())));
        addRequestParameter("transactionDate", sdf.format(new Date(calendar.getTimeInMillis())));
        addRequestParameter("paymentTypeId", "1");
        String[] errors = { AccountConstants.ERROR_FUTUREDATE, AccountConstants.ERROR_FUTUREDATE };
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("method", "preview");
        actionPerform();

        verifyInputForward();
        verifyActionErrors(errors);

    }

    public void testPreviewSucess() {
        createInitialObjects(2);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        addRequestParameter("receiptDate", sdf.format(currentDate));
        addRequestParameter("transactionDate", sdf.format(currentDate));
        addRequestParameter("paymentTypeId", "1");
        addRequestParameter("paymentModeOfPayment", "1");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        addRequestParameter("method", "preview");
        actionPerform();
        verifyNoActionErrors();
        verifyForward(Constants.PREVIEW_SUCCESS);

    }

    public void testPreviuos() {
        createInitialObjects(2);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        addRequestParameter("method", "previous");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward(Constants.PREVIOUS_SUCCESS);

    }

    public void testUpdate() throws Exception {
        createInitialObjects(2);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        addRequestParameter("receiptDate", sdf.format(currentDate));
        addRequestParameter("transactionDate", sdf.format(currentDate));
        SessionUtils.setAttribute(Constants.BUSINESS_KEY, loanBO, request);
        request.getSession().setAttribute(Constants.USER_CONTEXT_KEY, userContext);
        addRequestParameter("paymentTypeId", "1");
        addRequestParameter("paymentModeOfPayment", "1");
        addRequestParameter("method", "update");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionErrors();
        verifyForward(Constants.UPDATE_SUCCESS);

    }

    public void testUpdateNopaymentAtDisbursal() throws Exception {
        createInitialObjects(3);
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        addRequestParameter("receiptDate", sdf.format(currentDate));
        addRequestParameter("transactionDate", sdf.format(currentDate));
        SessionUtils.setAttribute(Constants.BUSINESS_KEY, loanBO, request);
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        request.getSession().setAttribute(Constants.USER_CONTEXT_KEY, userContext);
        addRequestParameter("paymentTypeId", "1");
        addRequestParameter("method", "update");
        actionPerform();
        verifyNoActionErrors();
        verifyForward(Constants.UPDATE_SUCCESS);

    }

    private void createInitialObjects(int disbursalType) {
        // SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        // String date = sdf.format(currentDate);
        loanBO = getLoanAccount(AccountState.LOAN_APPROVED, currentDate, disbursalType);
        secondLoanBO = getLoanAccountInGoodStanding(AccountState.LOAN_ACTIVE_IN_GOOD_STANDING, currentDate,
                disbursalType);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("accountId", loanBO.getAccountId().toString());

    }
}
