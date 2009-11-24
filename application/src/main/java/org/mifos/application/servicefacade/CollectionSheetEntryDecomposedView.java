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
package org.mifos.application.servicefacade;

import java.io.Serializable;
import java.util.List;

import org.mifos.application.accounts.loan.util.helpers.LoanAccountsProductView;
import org.mifos.application.collectionsheet.business.CollectionSheetEntryView;
import org.mifos.application.customer.util.helpers.CustomerAccountView;

/**
 *
 */
public class CollectionSheetEntryDecomposedView implements Serializable {

    private final List<LoanAccountsProductView> loanAccountViews;
    private final List<CustomerAccountView> customerAccountViews;
    private final List<CollectionSheetEntryView> parentCollectionSheetEntryViews;

    public CollectionSheetEntryDecomposedView(List<LoanAccountsProductView> loanAccountViews,
            List<CustomerAccountView> customerAccountViews,
            List<CollectionSheetEntryView> parentCollectionSheetEntryViews) {
                this.loanAccountViews = loanAccountViews;
        this.customerAccountViews = customerAccountViews;
        this.parentCollectionSheetEntryViews = parentCollectionSheetEntryViews;
    }

    public List<LoanAccountsProductView> getLoanAccountViews() {
        return this.loanAccountViews;
    }

    public List<CustomerAccountView> getCustomerAccountViews() {
        return this.customerAccountViews;
    }

    public List<CollectionSheetEntryView> getParentCollectionSheetEntryViews() {
        return this.parentCollectionSheetEntryViews;
    }
}
