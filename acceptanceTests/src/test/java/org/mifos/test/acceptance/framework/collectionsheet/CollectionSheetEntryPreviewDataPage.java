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
 
package org.mifos.test.acceptance.framework.collectionsheet;

import org.mifos.test.acceptance.framework.AbstractPage;

import com.thoughtworks.selenium.Selenium;

public class CollectionSheetEntryPreviewDataPage extends AbstractPage {

	public CollectionSheetEntryPreviewDataPage() {
		super();
	}

	public CollectionSheetEntryPreviewDataPage(Selenium selenium) {
		super(selenium);
	}
	

	public CollectionSheetEntryPreviewDataPage verifyPage(CollectionSheetEntrySelectPage.SubmitFormParameters parameters) {
        this.verifyPage("BulkEntryPreview");
		return this;
	}

    public CollectionSheetEntryEnterDataPage editAndGoToCollectionSheetEntryEnterDataPage() {
        selenium.click("bulkentry_preview.button.editdata");
        waitForPageToLoad();
        return new CollectionSheetEntryEnterDataPage(selenium);
    }

    public CollectionSheetEntryConfirmationPage submitAndGotoCollectionSheetEntryConfirmationPage() {
        selenium.click("bulkentry_preview.button.submit");
        waitForPageToLoad();
        return new CollectionSheetEntryConfirmationPage(selenium);
    }
	

}
