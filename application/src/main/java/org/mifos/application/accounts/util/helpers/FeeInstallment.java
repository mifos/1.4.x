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

package org.mifos.application.accounts.util.helpers;

import org.mifos.application.accounts.business.AccountFeesEntity;
import org.mifos.framework.util.helpers.Money;

public class FeeInstallment {
    private Short installmentId;
    private Money accountFee;
    private AccountFeesEntity accountFeesEntity = null;

    public Money getAccountFee() {
        return accountFee;
    }

    public void setAccountFee(Money accountFee) {
        this.accountFee = accountFee;
    }

    public AccountFeesEntity getAccountFeesEntity() {
        return accountFeesEntity;
    }

    public void setAccountFeesEntity(AccountFeesEntity accountFeesEntity) {
        this.accountFeesEntity = accountFeesEntity;
    }

    public Short getInstallmentId() {
        return installmentId;
    }

    public void setInstallmentId(Short installmentId) {
        this.installmentId = installmentId;
    }

}
