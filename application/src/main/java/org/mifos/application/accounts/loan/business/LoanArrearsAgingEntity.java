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

package org.mifos.application.accounts.loan.business;

import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.framework.business.PersistentObject;
import org.mifos.framework.util.helpers.Money;

public class LoanArrearsAgingEntity extends PersistentObject {
    @SuppressWarnings("unused")
    // see .hbm.xml file
    private final Integer id;

    @SuppressWarnings("unused")
    // see .hbm.xml file
    private final LoanBO loan;

    private final CustomerBO customer;

    private String customerName;

    private CustomerBO parentCustomer;

    private final OfficeBO office;

    private Short daysInArrears;

    private Money overduePrincipal;

    private Money overdueInterest;

    private Money overdueBalance;

    private Money unpaidPrincipal;

    private Money unpaidInterest;

    private Money unpaidBalance;

    protected LoanArrearsAgingEntity() {
        this.id = null;
        this.loan = null;
        this.customer = null;
        this.office = null;
    }

    public LoanArrearsAgingEntity(LoanBO loan, Short daysInArrears, Money unpaidPrincipal, Money unpaidInterest,
            Money overduePrincipal, Money overdueInterest) {
        this.id = null;
        this.loan = loan;
        this.customer = loan.getCustomer();
        this.customerName = loan.getCustomer().getDisplayName();
        this.parentCustomer = loan.getCustomer().getParentCustomer();
        this.office = loan.getOffice();
        this.daysInArrears = daysInArrears;
        this.unpaidPrincipal = unpaidPrincipal;
        this.unpaidInterest = unpaidInterest;
        this.unpaidBalance = unpaidPrincipal.add(unpaidInterest);
        this.overduePrincipal = overduePrincipal;
        this.overdueInterest = overdueInterest;
        this.overdueBalance = overduePrincipal.add(overdueInterest);
    }

    public CustomerBO getCustomer() {
        return customer;
    }

    public String getCustomerName() {
        return customerName;
    }

    void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Short getDaysInArrears() {
        return daysInArrears;
    }

    void setDaysInArrears(Short daysInArrears) {
        this.daysInArrears = daysInArrears;
    }

    public OfficeBO getOffice() {
        return office;
    }

    public CustomerBO getParentCustomer() {
        return parentCustomer;
    }

    void setParentCustomer(CustomerBO parentCustomer) {
        this.parentCustomer = parentCustomer;
    }

    public Money getOverdueBalance() {
        return overdueBalance;
    }

    void setOverdueBalance(Money overdueBalance) {
        this.overdueBalance = overdueBalance;
    }

    public Money getOverdueInterest() {
        return overdueInterest;
    }

    void setOverdueInterest(Money overdueInterest) {
        this.overdueInterest = overdueInterest;
    }

    public Money getOverduePrincipal() {
        return overduePrincipal;
    }

    void setOverduePrincipal(Money overduePrincipal) {
        this.overduePrincipal = overduePrincipal;
    }

    public Money getUnpaidBalance() {
        return unpaidBalance;
    }

    void setUnpaidBalance(Money unpaidBalance) {
        this.unpaidBalance = unpaidBalance;
    }

    public Money getUnpaidInterest() {
        return unpaidInterest;
    }

    void setUnpaidInterest(Money unpaidInterest) {
        this.unpaidInterest = unpaidInterest;
    }

    public Money getUnpaidPrincipal() {
        return unpaidPrincipal;
    }

    void setUnpaidPrincipal(Money unpaidPrincipal) {
        this.unpaidPrincipal = unpaidPrincipal;
    }

    void update(Short daysInArrears, Money unpaidPrincipal, Money unpaidInterest, Money overduePrincipal,
            Money overdueInterest, CustomerBO customer) {
        setCustomerName(customer.getDisplayName());
        setParentCustomer(customer.getParentCustomer());
        setDaysInArrears(daysInArrears);
        setUnpaidPrincipal(unpaidPrincipal);
        setUnpaidInterest(unpaidInterest);
        setUnpaidBalance(unpaidPrincipal.add(unpaidInterest));
        setOverduePrincipal(overduePrincipal);
        setOverdueInterest(overdueInterest);
        setOverdueBalance(overduePrincipal.add(overdueInterest));
    }
}
