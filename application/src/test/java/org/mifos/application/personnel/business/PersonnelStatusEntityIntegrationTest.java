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

package org.mifos.application.personnel.business;

import junit.framework.Assert;

import org.mifos.application.master.persistence.MasterPersistence;
import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class PersonnelStatusEntityIntegrationTest extends MifosIntegrationTestCase {

    public PersonnelStatusEntityIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    public void testGetPersonnelStatusEntity() throws Exception {
        MasterPersistence masterPersistenceService = new MasterPersistence();
        PersonnelStatusEntity personnelStatusEntity = (PersonnelStatusEntity) masterPersistenceService
                .getPersistentObject(PersonnelStatusEntity.class, (short) 1);
       Assert.assertEquals(Short.valueOf("1"), personnelStatusEntity.getId());
        personnelStatusEntity.setLocaleId(TestObjectFactory.TEST_LOCALE);
       Assert.assertEquals("Active", personnelStatusEntity.getName());
    }

}
