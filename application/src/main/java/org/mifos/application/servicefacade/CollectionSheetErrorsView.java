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

import java.util.List;

/**
 * I am a DTO for tracking problems with savings/customer accounts and
 * persisting to the database during a collection sheet save/create.
 */
public class CollectionSheetErrorsView {

    private final List<String> savingsDepNames;
    private final List<String> savingsWithNames;
    private final List<String> loanDisbursementAccountNumbers;
    private final List<String> loanRepaymentAccountNumbers;
    private final List<String> customerAccountNumbers;
    private final boolean isDatabaseError;
    private final Throwable databaseError;

    public CollectionSheetErrorsView(final List<String> savingsDepNames, final List<String> savingsWithNames,
            final List<String> loanDisbursementAccountNumbers, final List<String> loanRepaymentAccountNumbers,
            final List<String> customerAccountNumbers,
            final boolean databaseErrorOccurred, final Throwable databaseError) {
        this.savingsDepNames = savingsDepNames;
        this.savingsWithNames = savingsWithNames;
        this.loanDisbursementAccountNumbers = loanDisbursementAccountNumbers;
        this.loanRepaymentAccountNumbers = loanRepaymentAccountNumbers;
        this.customerAccountNumbers = customerAccountNumbers;
        this.isDatabaseError = databaseErrorOccurred;
        this.databaseError = databaseError;
    }

    public List<String> getSavingsDepNames() {
        return this.savingsDepNames;
    }

    public List<String> getSavingsWithNames() {
        return this.savingsWithNames;
    }

    public List<String> getLoanDisbursementAccountNumbers() {
        return this.loanDisbursementAccountNumbers;
    }

    public List<String> getLoanRepaymentAccountNumbers() {
        return this.loanRepaymentAccountNumbers;
    }

    public List<String> getCustomerAccountNumbers() {
        return this.customerAccountNumbers;
    }

    public boolean isDatabaseError() {
        return this.isDatabaseError;
    }

    public Throwable getDatabaseError() {
        return this.databaseError;
    }
}