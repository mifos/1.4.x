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

package org.mifos.framework.exceptions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;

/**
 * This class extends from ExceptionHandler class provided by Struts.Finally all
 * exceptions would be handled using Struts declarative Exception handling.
 * Where we might define an Exception handler per module or you can have a
 * Exception handler per action class which will again extend from this
 * MifosExceptionHandler . In this class the methods are overridden to perform
 * the custom exception handling.
 */
public class MifosExceptionHandler extends ExceptionHandler {

    /**
     * This method is used to log the exceptions. If the exception is of type
     * {@link SystemException} or {@link ApplicationException} it logs exception
     * using MifosLogger else calls <code>super.logException()</code>
     */
    @Override
    protected void logException(Exception e) {
        if (e instanceof ApplicationException) {
            ApplicationException appException = (ApplicationException) e;
            MifosLogManager.getLogger(LoggerConstants.ROOTLOGGER).error(appException.getKey(), true,
                    appException.getValues(), e);
        } else if (e instanceof SystemException) {
            SystemException sysException = (SystemException) e;
            MifosLogManager.getLogger(LoggerConstants.ROOTLOGGER).error(sysException.getKey(), true,
                    sysException.getValues(), e);
        } else {
            super.logException(e);
        }

    }

    /**
     * Figures the type of exception and thus gets the page to which it should
     * be forwarded. If the exception is of type {@link SystemException} it is
     * forwarded to standard page which is obtained using Exception Config. If
     * the exception is of type {@link ApplicationException} the page to which
     * the request is forwarded is figured out using the string
     * <method_invoked>+failure which should be defined in the ActionConfig.
     */
    @Override
    public ActionForward execute(Exception ex, ExceptionConfig ae, ActionMapping mapping, ActionForm formInstance,
            HttpServletRequest request, HttpServletResponse response) throws ServletException {

        ActionForward forwardToBeReturned = null;
        String input = null;
        String parameter = null;
        ActionMessage error = null;
        if (ex instanceof ServiceUnavailableException) {
            forwardToBeReturned = new ActionForward(ae.getPath());
            error = new ActionMessage(((ServiceUnavailableException) ex).getKey(), ((ServiceUnavailableException) ex)
                    .getValues());
        }
        if (ex instanceof ConnectionNotFoundException) {
            forwardToBeReturned = new ActionForward(ae.getPath());
            error = new ActionMessage(((ConnectionNotFoundException) ex).getKey(), ((ConnectionNotFoundException) ex)
                    .getValues());
        }
        if (ex instanceof SystemException) {
            forwardToBeReturned = new ActionForward(ae.getPath());
            error = new ActionMessage(((SystemException) ex).getKey(), ((SystemException) ex).getValues());
        } else if (ex instanceof PageExpiredException) {
            forwardToBeReturned = new ActionForward(ae.getPath());
            error = new ActionMessage(((PageExpiredException) ex).getKey(), ((PageExpiredException) ex).getValues());
        } else if (ex instanceof ApplicationException) {
            error = new ActionMessage(((ApplicationException) ex).getKey(), ((ApplicationException) ex).getValues());
            parameter = request.getParameter("method");
            // jsp to which the user should be returned is identified by
            // methodname_failure e.g. if there is an exception in create the
            // failure forward would be create_failure if input is not null it
            // also tries to append that to parameter to find the action
            // forward.If that is not availablee it still tries to find the
            // forward with the actionforward being parameter_failure
            input = request.getParameter("input");

            if (null != input) {
                parameter = parameter + "_" + input;
            }
            forwardToBeReturned = mapping.findForward(parameter + "_failure");
            if (null == forwardToBeReturned) {
                forwardToBeReturned = mapping.findForward(request.getParameter("method") + "_failure");
            }
            // if this returns null we get the path by the input parameter which
            // is the hidden variable passed by the jsp from which the request
            // is coming
            if (null == forwardToBeReturned) {
                input = mapping.getInput();
                if (null != input) {
                    forwardToBeReturned = new ActionForward("ExceptionForward", input, false, null);
                } else {
                    forwardToBeReturned = super.execute(ex, ae, mapping, formInstance, request, response);
                    // if we call super execute we do not want it to execute
                    // further statements
                    return forwardToBeReturned;
                }

            }
        }
        logException(ex);
        // This will store the exception in the scope mentioned so that
        // it can be displayed on the UI
        this.storeException(request, error.getKey(), error, forwardToBeReturned, ae.getScope());
        return forwardToBeReturned;
    }

}
