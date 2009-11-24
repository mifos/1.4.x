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

package org.mifos.application.accounts.business;

import java.util.Set;

import junit.framework.Assert;

import org.hibernate.Session;
import org.mifos.application.accounts.util.helpers.AccountActionTypes;
import org.mifos.application.master.MessageLookup;
import org.mifos.application.master.business.LookUpValueEntity;
import org.mifos.application.master.business.LookUpValueLocaleEntity;
import org.mifos.application.master.business.MifosLookUpEntity;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;

/**
 * Also see {@link AccountActionEntityTest}.
 */
public class AccountActionEntityIntegrationTest extends MifosIntegrationTestCase {

    public AccountActionEntityIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    private Session session;
    private AccountActionEntity accountActionEntity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        session = StaticHibernateUtil.getSessionTL();
    }

    @Override
    protected void tearDown() throws Exception {
        StaticHibernateUtil.closeSession();
        session = null;
        super.tearDown();
    }

    public void testGetAccountAction() {
        Short id = 1;
        accountActionEntity = getAccountActionEntityObject(id);
       Assert.assertEquals("Loan Repayment", accountActionEntity.getName());
    }

    public void testBasics() throws Exception {
        AccountActionEntity action = (AccountActionEntity) session.get(AccountActionEntity.class,
                AccountActionTypes.PAYMENT.getValue());

        LookUpValueEntity lookUpValue = action.getLookUpValue();
       Assert.assertEquals("AccountAction-Payment", lookUpValue.getLookUpName());
       Assert.assertEquals(new Integer(191), lookUpValue.getLookUpId());

        MifosLookUpEntity lookUpEntity = lookUpValue.getLookUpEntity();
       Assert.assertEquals(MifosLookUpEntity.ACCOUNT_ACTION, lookUpEntity.getEntityId().shortValue());
       Assert.assertEquals("AccountAction", lookUpEntity.getEntityType());

        Set<LookUpValueLocaleEntity> valueLocales = lookUpValue.getLookUpValueLocales();
       Assert.assertEquals(1, valueLocales.size());
        LookUpValueLocaleEntity valueLocale = valueLocales.iterator().next();
       Assert.assertEquals(1, (int) valueLocale.getLocaleId());
       Assert.assertEquals("Payment", MessageLookup.getInstance().lookup(lookUpValue));

       Assert.assertEquals("Payment", action.getName());
        session.close();
    }

    public void testEnum() throws Exception {
        AccountActionTypes myEnum = AccountActionTypes.FEE_REPAYMENT;
        AccountActionEntity entity = new AccountActionEntity(myEnum);
       Assert.assertEquals(myEnum.getValue(), entity.getId());

        AccountActionTypes out = entity.asEnum();
       Assert.assertEquals(myEnum, out);
    }

    public void testFromBadInt() throws Exception {
        try {
            AccountActionTypes.fromInt(9999);
            Assert.fail();
        } catch (RuntimeException e) {
           Assert.assertEquals("no account action 9999", e.getMessage());
        }
    }

    private AccountActionEntity getAccountActionEntityObject(Short id) {
        return (AccountActionEntity) session.get(AccountActionEntity.class, id);
    }

}
