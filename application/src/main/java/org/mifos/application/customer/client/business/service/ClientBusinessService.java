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

package org.mifos.application.customer.client.business.service;

import java.util.List;

import org.mifos.application.customer.client.business.ClientBO;
import org.mifos.application.customer.client.persistence.ClientPersistence;
import org.mifos.application.productdefinition.business.SavingsOfferingBO;
import org.mifos.framework.business.BusinessObject;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.security.util.UserContext;

public class ClientBusinessService implements BusinessService {

    @Override
    public BusinessObject getBusinessObject(UserContext userContext) {
        return null;
    }

    public List<SavingsOfferingBO> retrieveOfferingsApplicableToClient() throws ServiceException {
        try {
            return new ClientPersistence().retrieveOfferingsApplicableToClient();
        } catch (PersistenceException pe) {
            throw new ServiceException(pe);
        }
    }

    public ClientBO getClient(Integer customerId) throws ServiceException {
        try {
            return new ClientPersistence().getClient(customerId);
        } catch (PersistenceException pe) {
            throw new ServiceException(pe);
        }
    }

    public List<ClientBO> getActiveClientsUnderParent(String searchId, Short officeId) throws ServiceException {
        try {
            return new ClientPersistence().getActiveClientsUnderParent(searchId, officeId);
        } catch (PersistenceException pe) {
            throw new ServiceException(pe);
        }
    }

    public List<ClientBO> getActiveClientsUnderGroup(Integer groupId) throws ServiceException {
        try {
            return new ClientPersistence().getActiveClientsUnderGroup(groupId);
        } catch (PersistenceException pe) {
            throw new ServiceException(pe);

        }
    }

}
