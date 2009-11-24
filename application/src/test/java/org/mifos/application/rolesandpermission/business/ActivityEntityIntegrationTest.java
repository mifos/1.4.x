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

package org.mifos.application.rolesandpermission.business;

import junit.framework.Assert;

import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class ActivityEntityIntegrationTest extends MifosIntegrationTestCase {

    public ActivityEntityIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    private ActivityEntity activityEntity = null;

    public void testGetActivity() {
        activityEntity = getActivityEntity(Short.valueOf("1"));
       Assert.assertEquals("Organization Management", activityEntity.getActivityName());
       Assert.assertEquals("Organization Management", activityEntity.getDescription());
    }

    private ActivityEntity getActivityEntity(Short id) {
        return (ActivityEntity) TestObjectFactory.getObject(ActivityEntity.class, id);
    }

}
