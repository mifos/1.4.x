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

package org.mifos.framework.hibernate.helper;

import org.hibernate.Session;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.exceptions.HibernateProcessException;

/**
 * A thin wrapper around {@link StaticHibernateUtil}'s session methods, which
 * has reason to exist (I guess) because the session handling in searches is a
 * bit complicated.
 */
public class QuerySession {

    public static Session openSession() throws HibernateProcessException {
        MifosLogManager.getLogger(LoggerConstants.FRAMEWORKLOGGER).debug("opening session for search");
        return StaticHibernateUtil.openSession();
    }

    public static void closeSession(Session hibernateSession) throws HibernateProcessException {
        MifosLogManager.getLogger(LoggerConstants.FRAMEWORKLOGGER).debug("closing session after search");
        StaticHibernateUtil.closeSession(hibernateSession);
    }

}
