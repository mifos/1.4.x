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

package org.mifos.application.holiday.struts.actionforms;

import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.mifos.application.holiday.business.HolidayBO;
import org.mifos.application.holiday.util.helpers.HolidayConstants;
import org.mifos.application.login.util.helpers.LoginConstants;
import org.mifos.application.util.helpers.Methods;
import org.mifos.framework.exceptions.InvalidDateException;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.struts.actionforms.BaseActionForm;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.SessionUtils;
import org.mifos.framework.util.helpers.FilePaths;

public class HolidayActionForm extends BaseActionForm {

    private String globalOfficeNum;

    private String input;

    private ResourceBundle resourceBundle = null;

    private String repaymentRuleId;

    private String holidayFromDateString;

    private String holidayThruDateString;

    private Date fromDate;

    private Date thruDate;

    private String holidayName;

    public HolidayActionForm() {
    }

    public String getHolidayFromDate() {
        return holidayFromDateString;
    }

    public String getHolidayThruDate() {
        return holidayThruDateString;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public String getRepaymentRuleId() {
        return repaymentRuleId;
    }

    public void setHolidayFromDate(String holidayFromDate) {
        this.holidayFromDateString = holidayFromDate;
    }

    public void setHolidayThruDate(String holidayThruDate) {
        this.holidayThruDateString = holidayThruDate;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public void setRepaymentRuleId(String repaymentRuleId) {
        this.repaymentRuleId = repaymentRuleId;
    }

    public void clear() {
        this.repaymentRuleId = "";
        this.holidayName = "";
        this.holidayFromDateString = "";
        this.holidayThruDateString = "";
        this.fromDate = null;
        this.thruDate = null;
    }

    private void verifyFields(ActionErrors actionErrors, UserContext userContext, Locale userLocale) {
        if (StringUtils.isBlank(holidayName))
            actionErrors.add(HolidayConstants.HOLIDAY_NAME, new ActionMessage(HolidayConstants.ERRORMANDATORYFIELD,
                    getLocaleString(HolidayConstants.HOLIDAYNAME, userContext)));

        if (holidayFromDateString != null && !holidayFromDateString.equals("")) {
            try {
                fromDate = DateUtils.getLocaleDate(userLocale, getHolidayFromDate());
            } catch (InvalidDateException dateException) {
                actionErrors.add(HolidayConstants.HOLIDAY_FROM_DATE, new ActionMessage(
                        HolidayConstants.ERRORMANDATORYFIELD,
                        getLocaleString(HolidayConstants.HOLIDAYFROMDATE, userContext)));
            }
        } else {
            actionErrors.add(HolidayConstants.HOLIDAY_FROM_DATE, new ActionMessage(
                    HolidayConstants.ERRORMANDATORYFIELD,
                    getLocaleString(HolidayConstants.HOLIDAYFROMDATE, userContext)));
        }

        if (holidayThruDateString != null && !holidayThruDateString.equals("")) {
            try {
                thruDate = DateUtils.getLocaleDate(userLocale, getHolidayThruDate());
            } catch(InvalidDateException dateException) {
                actionErrors.add(HolidayConstants.HOLIDAY_THRU_DATE, new ActionMessage(
                        HolidayConstants.HOLIDAY_THRU_DATE, userContext));
            }
        } else {
            thruDate = null;
        }

        if (repaymentRuleId == null || repaymentRuleId.equals("")) {
            actionErrors.add(HolidayConstants.REPAYMENT_RULE, new ActionMessage(HolidayConstants.ERRORMANDATORYFIELD,
                    getLocaleString(HolidayConstants.HOLIDAYREPAYMENTRULE, userContext)));
        }

    }

    private String getLocaleString(String key, UserContext userContext) {

        if (resourceBundle == null)
            try {

                resourceBundle = ResourceBundle
                        .getBundle(FilePaths.HOLIDAYSOURCEPATH, userContext.getPreferredLocale());
            } catch (MissingResourceException e) {

                resourceBundle = ResourceBundle
                        .getBundle(FilePaths.HOLIDAYSOURCEPATH, userContext.getPreferredLocale());

            }
        return resourceBundle.getString(key);

    }

    @Override
    protected UserContext getUserContext(HttpServletRequest request) {
        return (UserContext) SessionUtils.getAttribute(Constants.USER_CONTEXT_KEY, request.getSession());
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        String method = request.getParameter("method");

        Locale userLocale = getUserLocale(request);

        if (null != request.getParameter(Constants.CURRENTFLOWKEY)
                && null == request.getAttribute(Constants.CURRENTFLOWKEY))
            request.setAttribute(Constants.CURRENTFLOWKEY, request.getParameter(Constants.CURRENTFLOWKEY));

        if (method.equals(Methods.preview.toString()) || method.equals(Methods.editpreview.toString())) {

            verifyFields(errors, getUserContext(request), userLocale);

            if (getFromDate() != null
                    && DateUtils.getDateWithoutTimeStamp(getFromDate().getTime()).compareTo(
                            DateUtils.getCurrentDateWithoutTimeStamp()) <= 0) {
                addError(errors, "From Date", HolidayConstants.INVALIDFROMDATE);
            }

            if (getFromDate() != null
                    && getThruDate() != null
                    && DateUtils.getDateWithoutTimeStamp(getFromDate().getTime()).compareTo(
                            DateUtils.getDateWithoutTimeStamp(getThruDate().getTime())) > 0) {
                addError(errors, "Thru Date", HolidayConstants.INVALIDTHRUDATE);
            }

            errors.add(super.validate(mapping, request));
        }

        if (null != errors && !errors.isEmpty()) {
            request.setAttribute(Globals.ERROR_KEY, errors);
            request.setAttribute("methodCalled", method);
        }

        return errors;

    }

    public void populate(HolidayBO holidayBO) {

        this.repaymentRuleId = holidayBO.getRepaymentRuleId().toString();
        this.holidayName = holidayBO.getHolidayName();
        this.fromDate = holidayBO.getHolidayFromDate();
        this.thruDate = holidayBO.getHolidayThruDate();
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getGlobalOfficeNum() {
        return globalOfficeNum;
    }

    public void setGlobalOfficeNum(String globalOfficeNum) {
        this.globalOfficeNum = globalOfficeNum;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        this.holidayFromDateString = "";
        this.holidayName = "";
        this.holidayThruDateString = "";
        this.repaymentRuleId = "";

        if (null != request.getParameter(Constants.CURRENTFLOWKEY))
            request.setAttribute(Constants.CURRENTFLOWKEY, request.getParameter("currentFlowKey"));
    }

    protected Locale getUserLocale(HttpServletRequest request) {
        Locale locale = null;

        UserContext userContext = (UserContext) request.getSession().getAttribute(LoginConstants.USERCONTEXT);
        if (null != userContext) {
            locale = userContext.getCurrentLocale();

        }

        return locale;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getThruDate() {
        return thruDate;
    }

    public void setThruDate(Date thruDate) {
        this.thruDate = thruDate;
    }
}
