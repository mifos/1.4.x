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

package org.mifos.framework.components.tabletag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.mifos.framework.exceptions.TableTagException;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.LabelTagUtils;

/**
 * This class renders the utility functions.
 */

public class TableTagUtils {

    /**
     * Default constructor
     */
    private TableTagUtils() {
        super();
    }

    private static TableTagUtils instance = new TableTagUtils();

    public static TableTagUtils getInstance() {
        return instance;
    }

    /**
     * Function to get the label.
     * 
     * @param label
     *            label to be displayed.
     * @param labelType
     *            label type of the label.
     * @param obj
     * @return string return the label
     * @throws TableTagException
     */
    public Object helper(PageContext pageContext, String label, String labelType, Object obj, Locale locale)
            throws TableTagException {

        /** variable to hold the getlist method */
        Method getList = null;

        /** variable to hold the value of the label */
        Object labelValue = null;

        /**
         * if label type is key then we are picking the label from resource
         * bundle. if label type is a string then we are taking it as it is. if
         * label type is a method then we are calling the get method on that
         * using reflection.
         */
        if ("key".equalsIgnoreCase(labelType)) {
            try {
                labelValue = LabelTagUtils.getInstance().getLabel(pageContext, "Resources",
                        LabelTagUtils.getInstance().getUserPreferredLocaleObject(pageContext), label, null);

            } catch (MissingResourceException mre) {
                throw new TableTagException(mre);
            } catch (JspException je) {
                throw new TableTagException(je);
            }

        } else if ("string".equalsIgnoreCase(labelType)) {
            labelValue = label;
        } else if ("method".equalsIgnoreCase(labelType)) {
            String c = label.substring(0, 1);
            try {
                getList = obj.getClass()
                        .getDeclaredMethod("get" + c.toUpperCase() + label.substring(1), (Class[]) null);
                labelValue = getList.invoke(obj, (Object[]) null);

            } catch (SecurityException se) {
                throw new TableTagException(se);
            } catch (NoSuchMethodException nsme) {
                throw new TableTagException(nsme);
            } catch (IllegalArgumentException iae) {
                throw new TableTagException(iae);
            } catch (IllegalAccessException iae) {
                throw new TableTagException(iae);
            } catch (InvocationTargetException ite) {
                throw new TableTagException(ite);
            }

        } else if ("date".equalsIgnoreCase(labelType)) {
            String c = label.substring(0, 1);
            try {
                getList = obj.getClass()
                        .getDeclaredMethod("get" + c.toUpperCase() + label.substring(1), (Class[]) null);
                labelValue = getList.invoke(obj, (Object[]) null);
                if (null != labelValue)
                    labelValue = DateUtils.getUserLocaleDate(locale, labelValue.toString());
            } catch (SecurityException se) {
                throw new TableTagException(se);
            } catch (NoSuchMethodException nsme) {
                throw new TableTagException(nsme);
            } catch (IllegalArgumentException iae) {
                throw new TableTagException(iae);
            } catch (IllegalAccessException iae) {
                throw new TableTagException(iae);
            } catch (InvocationTargetException ite) {
                throw new TableTagException(ite);
            }
        } else if ("parameter".equalsIgnoreCase(labelType)) {
            labelValue = pageContext.getRequest().getParameter(label);
        }
        return labelValue;
    }
}
