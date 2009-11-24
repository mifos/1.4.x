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

/**
 * This Exception is thrown when we try to convert action form to a value
 * object.
 */
public class ValueObjectConversionException extends SystemException {

    public ValueObjectConversionException(Throwable cause) {
        super(cause);
    }

    public ValueObjectConversionException(String key) {
        super(key);
    }

    @Override
    public String getKey() {
        return "exception.framework.SystemException.ValueObjectConversionException";
    }

}
