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

package org.mifos.application.accounts.struts.action;

import java.sql.Date;
import java.util.List;

import junit.framework.Assert;

import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.business.service.AccountBusinessService;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.util.helpers.AccountConstants;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.accounts.util.helpers.ApplicableCharge;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.framework.MifosMockStrutsTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class ApplyChargeActionStrutsTest extends MifosMockStrutsTestCase {

    public ApplyChargeActionStrutsTest() throws SystemException, ApplicationException {
        super();
    }

    private AccountBO accountBO;

    private UserContext userContext;

    private CustomerBO client;

    private CustomerBO group;

    private CustomerBO center;

    private MeetingBO meeting;

    private String flowKey;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        userContext = TestObjectFactory.getContext();
        request.getSession().setAttribute(Constants.USERCONTEXT, userContext);
        addRequestParameter("recordLoanOfficerId", "1");
        addRequestParameter("recordOfficeId", "1");
        request.getSession(false).setAttribute("ActivityContext", TestObjectFactory.getActivityContext());
        flowKey = createFlow(request, ApplyChargeAction.class);
    }

    @Override
    public void tearDown() throws Exception {
        TestObjectFactory.cleanUp(accountBO);
        TestObjectFactory.cleanUp(client);
        TestObjectFactory.cleanUp(group);
        TestObjectFactory.cleanUp(center);
        StaticHibernateUtil.closeSession();
        super.tearDown();
    }

    public void testLoad() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialObjects();
        accountBO = getLoanAccount(client, meeting);
        setRequestPathInfo("/applyChargeAction.do");
        addRequestParameter("method", "load");
        addRequestParameter("accountId", accountBO.getAccountId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward("load_success");
        verifyNoActionErrors();
        verifyNoActionMessages();

        Assert.assertNotNull(SessionUtils.getAttribute(AccountConstants.APPLICABLE_CHARGE_LIST, request));
       Assert.assertEquals("Size of the list should be 2", 2, ((List<ApplicableCharge>) SessionUtils.getAttribute(
                AccountConstants.APPLICABLE_CHARGE_LIST, request)).size());
    }

    public void testCancel() throws Exception {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialObjects();
        accountBO = getLoanAccount(client, meeting);
        SessionUtils.setAttribute(Constants.BUSINESS_KEY, getAccountBusinessService().getAccount(
                accountBO.getAccountId()), request);
        setRequestPathInfo("/applyChargeAction.do");
        addRequestParameter("method", "cancel");
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyForward("loanDetails_success");
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    public void testUpdateSuccess() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialObjects();
        accountBO = getLoanAccount(client, meeting);
        setRequestPathInfo("/applyChargeAction.do");
        addRequestParameter("method", "update");
        addRequestParameter("chargeType", "-1");
        addRequestParameter("charge", "18");
        addRequestParameter("accountId", accountBO.getAccountId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
//        verifyForward("loanDetails_success");
        verifyNoActionErrors();
        verifyNoActionMessages();

    }
    
    public void testUpdateFailureDueToInvalidChargeAmount() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialObjects();
        accountBO = getLoanAccount(client, meeting);
        setRequestPathInfo("/applyChargeAction.do");
        addRequestParameter("method", "update");
        addRequestParameter("chargeType", "-1");
        addRequestParameter("charge", "12345678.21");
        addRequestParameter("accountId", accountBO.getAccountId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
        verifyNoActionMessages();
        Assert.assertEquals(3, getErrorSize());
        Assert.assertEquals("Charge Amount", 2, getErrorSize(AccountConstants.ACCOUNT_AMOUNT));

    }

    public void testUpdateFailureWith_Rate_GreaterThan999() {
        request.setAttribute(Constants.CURRENTFLOWKEY, flowKey);
        createInitialObjects();
        accountBO = getLoanAccount(client, meeting);
        setRequestPathInfo("/applyChargeAction.do");
        addRequestParameter("method", "update");
        addRequestParameter("chargeType", "-1");
        addRequestParameter("chargeAmount", "999999");
        addRequestParameter("selectedChargeFormula", "%LoanAmount");
        addRequestParameter("charge", "18");
        addRequestParameter("accountId", accountBO.getAccountId().toString());
        addRequestParameter(Constants.CURRENTFLOWKEY, (String) request.getAttribute(Constants.CURRENTFLOWKEY));
        actionPerform();
       Assert.assertEquals("Rate", 1, getErrorSize(AccountConstants.RATE));

    }

    public void testValidate() throws Exception {
        setRequestPathInfo("/applyChargeAction.do");
        addRequestParameter("method", "validate");
        request.setAttribute("methodCalled", "update");
        addRequestParameter(Constants.CURRENTFLOWKEY, flowKey);
        actionPerform();
        verifyNoActionErrors();
        verifyForward(ActionForwards.update_failure.toString());
    }

    private void createInitialObjects() {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        center = TestObjectFactory.createCenter("Center", meeting);
        group = TestObjectFactory.createGroupUnderCenter("Group", CustomerStatus.GROUP_ACTIVE, center);
        client = TestObjectFactory.createClient("Client", CustomerStatus.CLIENT_ACTIVE, group);
    }

    private LoanBO getLoanAccount(CustomerBO customer, MeetingBO meeting) {
        Date startDate = new Date(System.currentTimeMillis());
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(startDate, meeting);
        return TestObjectFactory.createLoanAccount("42423142341", customer, AccountState.LOAN_ACTIVE_IN_GOOD_STANDING,
                startDate, loanOffering);

    }

    private AccountBusinessService getAccountBusinessService() {
        return new AccountBusinessService();
    }

}
