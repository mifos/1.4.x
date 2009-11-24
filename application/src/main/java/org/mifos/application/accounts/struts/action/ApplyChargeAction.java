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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.business.service.AccountBusinessService;
import org.mifos.application.accounts.savings.util.helpers.SavingsConstants;
import org.mifos.application.accounts.struts.actionforms.ApplyChargeActionForm;
import org.mifos.application.accounts.util.helpers.AccountConstants;
import org.mifos.application.accounts.util.helpers.AccountTypes;
import org.mifos.application.customer.exceptions.CustomerException;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.util.helpers.ActionForwards;
import org.mifos.application.util.helpers.Methods;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.security.util.ActionSecurity;
import org.mifos.framework.security.util.ActivityMapper;
import org.mifos.framework.security.util.SecurityConstants;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.struts.action.BaseAction;
import org.mifos.framework.util.helpers.CloseSession;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.TransactionDemarcate;

public class ApplyChargeAction extends BaseAction {

    @Override
    protected BusinessService getService() throws ServiceException {
        return getAccountBusinessService();
    }

    @Override
    protected boolean skipActionFormToBusinessObjectConversion(String method) {
        return true;
    }

    public static ActionSecurity getSecurity() {
        ActionSecurity security = new ActionSecurity("applyChargeAction");
        security.allow("load", SecurityConstants.VIEW);
        security.allow("update", SecurityConstants.VIEW);
        return security;
    }

    @TransactionDemarcate(joinToken = true)
    public ActionForward load(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        doCleanUp(request, form);
        UserContext userContext = (UserContext) SessionUtils.getAttribute(Constants.USER_CONTEXT_KEY, request
                .getSession());
        Integer accountId = Integer.valueOf(request.getParameter("accountId"));
        SessionUtils.setCollectionAttribute(AccountConstants.APPLICABLE_CHARGE_LIST, getAccountBusinessService()
                .getAppllicableFees(accountId, userContext), request);
        SessionUtils.setAttribute(Constants.BUSINESS_KEY, getAccountBusinessService().getAccount(accountId), request);
        return mapping.findForward(ActionForwards.load_success.toString());
    }

    @TransactionDemarcate(validateAndResetToken = true)
    @CloseSession
    public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        UserContext userContext = (UserContext) SessionUtils.getAttribute(Constants.USER_CONTEXT_KEY, request
                .getSession());
        ApplyChargeActionForm applyChargeActionForm = (ApplyChargeActionForm) form;
        Short chargeType = Short.valueOf(applyChargeActionForm.getChargeType());
        Double chargeAmount = getDoubleValue(request.getParameter("charge"));
        AccountBO accountBO = getAccountBusinessService().getAccount(
                Integer.valueOf(applyChargeActionForm.getAccountId()));
        accountBO.setUserContext(userContext);

        CustomerLevel customerLevel = null;
        if (accountBO.getType().equals(AccountTypes.CUSTOMER_ACCOUNT))
            customerLevel = accountBO.getCustomer().getLevel();
        if (accountBO.getPersonnel() != null)
            checkPermissionForApplyCharges(accountBO.getType(), customerLevel, userContext, accountBO.getOffice()
                    .getOfficeId(), accountBO.getPersonnel().getPersonnelId());
        else
            checkPermissionForApplyCharges(accountBO.getType(), customerLevel, userContext, accountBO.getOffice()
                    .getOfficeId(), userContext.getId());

        accountBO.applyCharge(chargeType, chargeAmount);
        accountBO.update();
        return mapping.findForward(getDetailAccountPage(accountBO));
    }

    @TransactionDemarcate(validateAndResetToken = true)
    public ActionForward cancel(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request.removeAttribute(AccountConstants.APPLICABLE_CHARGE_LIST);
        AccountBO accountBO = (AccountBO) SessionUtils.getAttribute(Constants.BUSINESS_KEY, request);
        return mapping.findForward(getDetailAccountPage(accountBO));
    }

    private AccountBusinessService getAccountBusinessService() throws ServiceException {
        return new AccountBusinessService();
    }

    private void doCleanUp(HttpServletRequest request, ActionForm form) {
        ApplyChargeActionForm applyChargeActionForm = (ApplyChargeActionForm) form;
        applyChargeActionForm.setAccountId(null);
        applyChargeActionForm.setChargeType(null);
        applyChargeActionForm.setChargeAmount(null);
        request.removeAttribute(AccountConstants.APPLICABLE_CHARGE_LIST);
    }

    @TransactionDemarcate(joinToken = true)
    public ActionForward validate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String method = (String) request.getAttribute(SavingsConstants.METHODCALLED);
        String forward = null;
        if (method != null) {
            if (method.equals(Methods.update.toString()))
                forward = ActionForwards.update_failure.toString();
        }
        return mapping.findForward(forward);
    }

    private String getDetailAccountPage(AccountBO account) {
        if (account.getType() == AccountTypes.LOAN_ACCOUNT) {
            return "loanDetails_success";
        } else {
            if (account.getCustomer().getLevel() == CustomerLevel.CLIENT) {
                return "clientDetails_success";
            } else if (account.getCustomer().getLevel() == CustomerLevel.GROUP) {
                return "groupDetails_success";
            } else {
                return "centerDetails_success";
            }
        }
    }

    private void checkPermissionForApplyCharges(AccountTypes accountTypes, CustomerLevel customerLevel,
            UserContext userContext, Short recordOfficeId, Short recordLoanOfficerId) throws ApplicationException {
        if (!isPermissionAllowed(accountTypes, customerLevel, userContext, recordOfficeId, recordLoanOfficerId))
            throw new CustomerException(SecurityConstants.KEY_ACTIVITY_NOT_ALLOWED);
    }

    private boolean isPermissionAllowed(AccountTypes accountTypes, CustomerLevel customerLevel,
            UserContext userContext, Short recordOfficeId, Short recordLoanOfficerId) {
        return ActivityMapper.getInstance().isApplyChargesPermittedForAccounts(accountTypes, customerLevel,
                userContext, recordOfficeId, recordLoanOfficerId);
    }
}
