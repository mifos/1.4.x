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

package org.mifos.application.office.persistence;

import java.util.List;

import junit.framework.Assert;

import org.mifos.application.office.business.OfficeLevelEntity;
import org.mifos.application.office.util.helpers.OfficeLevel;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class OfficeHierarchyPersistenceIntegrationTest extends MifosIntegrationTestCase {

    private static final int OFFICE_LEVELS = 5;

    public OfficeHierarchyPersistenceIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    public void testGetOfficeLevels() throws Exception {
        List<OfficeLevelEntity> officeLevels = new OfficeHierarchyPersistence()
                .getOfficeLevels(TestObjectFactory.TEST_LOCALE);
       Assert.assertEquals(OFFICE_LEVELS, officeLevels.size());
        for (OfficeLevelEntity officeLevelEntity : officeLevels) {
           Assert.assertTrue(officeLevelEntity.isConfigured());
        }
    }

    public void testIsOfficePresentForLevel() throws Exception {
        OfficeHierarchyPersistence persistence = new OfficeHierarchyPersistence();
       Assert.assertTrue(persistence.isOfficePresentForLevel(OfficeLevel.HEADOFFICE));
       Assert.assertTrue(persistence.isOfficePresentForLevel(OfficeLevel.BRANCHOFFICE));
        Assert.assertFalse(persistence.isOfficePresentForLevel(OfficeLevel.REGIONALOFFICE));

    }

    public void testIsOfficePresentForLevelFailure() throws Exception {
        OfficeHierarchyPersistence persistence = new OfficeHierarchyPersistence();
        TestObjectFactory.simulateInvalidConnection();
        try {
            persistence.isOfficePresentForLevel(OfficeLevel.HEADOFFICE);
            Assert.fail();
        } catch (PersistenceException expected) {
        } finally {
            StaticHibernateUtil.closeSession();
        }
    }

}
