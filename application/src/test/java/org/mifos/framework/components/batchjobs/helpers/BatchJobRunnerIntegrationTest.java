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

package org.mifos.framework.components.batchjobs.helpers;

import org.mifos.framework.MifosIntegrationTestCase;
import org.mifos.framework.exceptions.ApplicationException;
import org.mifos.framework.exceptions.SystemException;

public class BatchJobRunnerIntegrationTest extends MifosIntegrationTestCase {
    public BatchJobRunnerIntegrationTest() throws SystemException, ApplicationException {
        super();
    }

    public void testRunBatch() throws Exception {
        // new CollectionSheetHelper(new
        // CollectionSheetTask()).execute(System.currentTimeMillis());
        new BranchReportHelper(new BranchReportTask()).execute(System.currentTimeMillis());
    }
}
