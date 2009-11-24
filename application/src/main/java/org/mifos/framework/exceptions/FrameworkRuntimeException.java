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

import org.mifos.framework.util.helpers.ExceptionConstants;

/**
 * This class is a RunTimeException for the framework.
 */
public class FrameworkRuntimeException extends SystemException {

    /**
     * This is a string which points to the actual message in the resource
     * bundle. So the exception message to be shown to the user would be taken
     * from the resource bundle and hence could be localized.
     */
    protected String key = null;

    /**
     * This is an array of object which might be needed to pass certain
     * parameters to the string in the resource bundle.
     */
    protected Object[] values = null;

    public FrameworkRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * @param key
     *            A key for looking up the message in
     *            ExceptionResources.properties, or null to specify a generic
     *            message.
     * @param internalMessage
     *            A message which is just intended for developers; the user will
     *            not see this message but instead the message corresponding to
     *            key. Because the message is only for developers, it is not
     *            translated into different languages.
     */
    public FrameworkRuntimeException(String key, String internalMessage) {
        super(null, internalMessage);
        this.key = key;
    }

    /**
     * Returns the key which maps to an entry in ExceptionResources file. The
     * message corresponding to this key is used for logging purposes as well as
     * for displaying message to the user
     */
    @Override
    public String getKey() {
        if (null == key) {
            return ExceptionConstants.FRAMEWORKRUNTIMEEXCEPTION;
        } else {
            return this.key;
        }
    }

    @Override
    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

}
