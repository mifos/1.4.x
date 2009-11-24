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

package org.mifos.application.master.business;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.framework.TestUtils;
import org.mifos.framework.components.logger.MifosLogManager;

public class CustomFieldDefinitionEntityTest extends TestCase {

    public void setUp() throws Exception {
        MifosLogManager.configureLogging();
        TestUtils.initializeSpring();
    }

    public void testMandatory() {
        MifosLookUpEntity customFieldName = new MifosLookUpEntity();
        customFieldName.setEntityId((short) 1);
        customFieldName.setEntityType("something");

        CustomFieldDefinitionEntity customFieldNotMandatory = new CustomFieldDefinitionEntity(customFieldName,
                (short) 0, CustomFieldType.ALPHA_NUMERIC, EntityType.CLIENT, "default value", YesNoFlag.NO);
        CustomFieldDefinitionEntity customFieldMandatory = new CustomFieldDefinitionEntity(customFieldName, (short) 0,
                CustomFieldType.ALPHA_NUMERIC, EntityType.CLIENT, "default value", YesNoFlag.YES);

        Assert.assertFalse(customFieldNotMandatory.isMandatory());
       Assert.assertEquals(customFieldNotMandatory.getMandatoryStringValue(), "no");

       Assert.assertTrue(customFieldMandatory.isMandatory());
       Assert.assertEquals(customFieldMandatory.getMandatoryStringValue(), "yes");
    }

}
