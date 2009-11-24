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

package org.mifos.application.customer.business;

import org.mifos.framework.business.PersistentObject;

/**
 * A Customer can hold various positions like president ,vice president etc.
 * This class holds the relationship between the customer and the postion he
 * holds
 */
public class CustomerPositionEntity extends PersistentObject {

    private final Integer customerPositionId;

    private final PositionEntity position;

    private CustomerBO customer;

    private final CustomerBO parentCustomer;

    public CustomerPositionEntity(PositionEntity position, CustomerBO customer, CustomerBO parentCustomer) {
        this.position = position;
        this.customer = customer;
        this.parentCustomer = parentCustomer;
        this.customerPositionId = null;
    }

    protected CustomerPositionEntity() {
        this.customerPositionId = null;
        this.position = null;
        this.parentCustomer = null;
    }

    public CustomerBO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerBO customer) {
        this.customer = customer;
    }

    public PositionEntity getPosition() {
        return position;
    }
}
