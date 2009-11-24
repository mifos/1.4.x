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

package org.mifos.framework.util.helpers;

import java.lang.reflect.Method;

import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.MethodInvocationException;
import org.mifos.framework.exceptions.SystemException;

/**
 * This is a helper class used to invoke methods by reflection.It has two
 * variants of the same method: One which throws exceptions if the specified
 * method is not found and another which swallows the exception if the method is
 * not found but both of these methods do throw the exception thrown by the
 * underlying method which is invoked.
 */
public class MethodInvoker {

    /**
     * Invokes the method specified on the object. It throws
     * <code>MethodInvocationException</code> if the method we are trying to
     * invoke is not available or inaccessible.
     * 
     * @param targetObject
     *            - Object on which method is to be invoked.
     * @param methodName
     *            - Name of the method to be called.
     * @param parametersToBePassed
     *            - Array of parameters to be passed.
     * @param parameterTypes
     *            - Array of parameter types.
     * @return - Returns the object returned by the method which is invoked.
     * @throws SystemException
     * @throws ApplicationException
     */
    public static Object invoke(Object targetObject, String methodName, Object[] parametersToBePassed,
            Class... parameterTypes) throws SystemException, ApplicationException {

        Method method = null;
        Object invocationResult = null;

        try {

            method = targetObject.getClass().getMethod(methodName, parameterTypes);
            invocationResult = method.invoke(targetObject, parametersToBePassed);
        } catch (Exception e) {
            throw new MethodInvocationException(e);
        }
        return invocationResult;

    }

    /**
     * Invokes the method specified on the object. Unlike invoke it does not
     * throw any exception even if the method we are trying to invoke is not
     * available or inaccessible.
     * 
     * @param targetObject
     *            - Object on which method is to be invoked.
     * @param methodName
     *            - Name of the method to be called.
     * @param parametersToBePassed
     *            - Array of parameters to be passed.
     * @param parameterTypes
     *            - Array of parameter types.
     * @return - Returns the object returned by the method which is invoked.
     * @throws SystemException
     * @throws ApplicationException
     */
    public static Object invokeWithNoException(Object targetObject, String methodName, Object[] parametersToBePassed,
            Class... parameterTypes) throws SystemException, ApplicationException {

        Method method = null;
        Object invocationResult = null;

        try {
            method = targetObject.getClass().getMethod(methodName, parameterTypes);
            invocationResult = method.invoke(targetObject, parametersToBePassed);
        } catch (Exception e) {
        }
        return invocationResult;

    }

}
