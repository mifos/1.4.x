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

package org.mifos.application.productdefinition.struts.actionforms;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mifos.application.productdefinition.util.helpers.ApplicableTo;
import org.mifos.framework.components.logger.TestLogger;
import org.testng.annotations.Test;

@Test(groups={"unit", "fastTestsSuite"},  dependsOnGroups={"productMixTestSuite"})
public class LoanPrdActionFormTest extends TestCase {

    public void testApplicableMaster() throws Exception {
        LoanPrdActionForm form = new LoanPrdActionForm(new TestLogger());
        form.setPrdApplicableMaster("" + ApplicableTo.CLIENTS.getValue());
       Assert.assertEquals(ApplicableTo.CLIENTS, form.getPrdApplicableMasterEnum());
    }

    public void testSetFromEnum() throws Exception {
        LoanPrdActionForm form = new LoanPrdActionForm(new TestLogger());
        form.setPrdApplicableMaster(ApplicableTo.ALLCUSTOMERS);
       Assert.assertEquals(ApplicableTo.ALLCUSTOMERS, form.getPrdApplicableMasterEnum());
    }

}
