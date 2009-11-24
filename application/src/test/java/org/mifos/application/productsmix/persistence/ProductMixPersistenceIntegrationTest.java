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

package org.mifos.application.productsmix.persistence;

import junit.framework.Assert;

import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.productdefinition.business.SavingsOfferingBO;
import org.mifos.application.productsmix.business.ProductMixBO;
import org.mifos.application.productsmix.util.ProductMixTestHelper;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.testng.annotations.Test;

@Test(groups={"integration", "productMixTestSuite"},  dependsOnGroups={"configTestSuite"})
public class ProductMixPersistenceIntegrationTest extends MifosIntegrationTestCase {

    public ProductMixPersistenceIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    MeetingBO meeting;
    MeetingBO meeting1;
    SavingsOfferingBO saving1;
    SavingsOfferingBO saving2;
    ProductMixBO prdmix;
    ProductMixPersistence productMixPersistence = new ProductMixPersistence();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        TestObjectFactory.cleanUp(prdmix);
        TestObjectFactory.cleanUp(saving1);
        TestObjectFactory.cleanUp(saving2);
        StaticHibernateUtil.closeSession();
        super.tearDown();
    }

    public void testGetAllProductMix() throws PersistenceException {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        meeting1 = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        saving1 = ProductMixTestHelper.createSavingOffering("Savings Product1", "S1", meeting, meeting);
        saving2 = ProductMixTestHelper.createSavingOffering("Savings Product2", "S2", meeting1, meeting1);
        prdmix = TestObjectFactory.createAllowedProductsMix(saving1, saving2);
       Assert.assertEquals(1, (productMixPersistence.getAllProductMix()).size());

    }

    public void testGetNotAllowedProducts() throws PersistenceException {
        meeting = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        meeting1 = TestObjectFactory.createMeeting(TestObjectFactory.getTypicalMeeting());
        saving1 = ProductMixTestHelper.createSavingOffering("Savings Product1", "S1", meeting, meeting);
        saving2 = ProductMixTestHelper.createSavingOffering("Savings Product2", "S2", meeting1, meeting1);
        prdmix = TestObjectFactory.createAllowedProductsMix(saving1, saving2);
       Assert.assertEquals(1, (productMixPersistence.getNotAllowedProducts(saving1.getPrdOfferingId())).size());

    }
}
