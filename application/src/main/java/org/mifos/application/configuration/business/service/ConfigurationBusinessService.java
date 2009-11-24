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

package org.mifos.application.configuration.business.service;

import java.util.List;

import org.mifos.framework.business.BusinessObject;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.components.configuration.business.ConfigurationKeyValueInteger;
import org.mifos.framework.components.configuration.persistence.ConfigurationPersistence;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.security.util.UserContext;

public class ConfigurationBusinessService implements BusinessService {

    private final ConfigurationPersistence configurationPersistence;

    ConfigurationBusinessService(ConfigurationPersistence configurationPersistence) {
        this.configurationPersistence = configurationPersistence;
    }

    public ConfigurationBusinessService() {
        this(new ConfigurationPersistence());
    }

    @Override
    public BusinessObject getBusinessObject(UserContext userContext) {
        return null;
    }

    public List<ConfigurationKeyValueInteger> getConfiguration() throws ServiceException {
        try {
            return configurationPersistence.getAllConfigurationKeyValueIntegers();
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public boolean isGlimEnabled() throws ServiceException {
        try {
            return configurationPersistence.isGlimEnabled();
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public boolean isRepaymentIndepOfMeetingEnabled() throws ServiceException {
        try {
            return new ConfigurationPersistence().isRepaymentIndepOfMeetingEnabled();
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

}
