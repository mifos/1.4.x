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

package org.mifos.application.accounts.financial.business.service.activity;

import java.util.ArrayList;
import java.util.List;

import org.mifos.application.accounts.business.AccountTrxnEntity;
import org.mifos.application.accounts.financial.business.service.activity.accountingentry.BaseAccountingEntry;
import org.mifos.application.accounts.financial.business.service.activity.accountingentry.DepositAccountingEntry;

public class SavingsDepositFinancialActivity extends BaseFinancialActivity {

    public SavingsDepositFinancialActivity(AccountTrxnEntity accountTrxn) {
        super(accountTrxn);
    }

    @Override
    protected List<BaseAccountingEntry> getFinancialActionEntry() {
        List<BaseAccountingEntry> financialActionEntryList = new ArrayList<BaseAccountingEntry>();
        financialActionEntryList.add(new DepositAccountingEntry());
        return financialActionEntryList;
    }

}
