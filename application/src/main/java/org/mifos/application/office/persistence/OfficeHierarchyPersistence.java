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

package org.mifos.application.office.persistence;

import java.util.HashMap;
import java.util.List;

import org.hibernate.HibernateException;
import org.mifos.application.NamedQueryConstants;
import org.mifos.application.office.business.OfficeLevelEntity;
import org.mifos.application.office.util.helpers.OfficeLevel;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.persistence.Persistence;

public class OfficeHierarchyPersistence extends Persistence {

    public OfficeHierarchyPersistence() {
        super();
    }

    public List<OfficeLevelEntity> getOfficeLevels(Short localeId) throws PersistenceException {
        List<OfficeLevelEntity> officeLevels = executeNamedQuery(NamedQueryConstants.GET_OFFICE_LEVELS, null);
        for (OfficeLevelEntity officeLevelEntity : officeLevels) {
            officeLevelEntity.setLocaleId(localeId);
            officeLevelEntity.getName();
        }
        return officeLevels;
    }

    public boolean isOfficePresentForLevel(Short levelId) throws PersistenceException {
        try {
            HashMap<String, Object> queryParameters = new HashMap<String, Object>();
            queryParameters.put("LEVEL_ID", levelId);
            Number count = (Number) execUniqueResultNamedQuery(NamedQueryConstants.GET_OFFICE_COUNT, queryParameters);
            if (count != null) {
                return count.longValue() > 0;
            }
        } catch (HibernateException e) {
            throw new PersistenceException(e);
        }

        return false;
    }

    public boolean isOfficePresentForLevel(OfficeLevel level) throws PersistenceException {
        return isOfficePresentForLevel(level.getValue());
    }

}
