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

package org.mifos.application.importexport.persistence;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.mifos.application.NamedQueryConstants;
import org.mifos.application.importexport.business.ImportedFilesEntity;
import org.mifos.framework.persistence.Persistence;

public class HibernateImportedFilesDao extends Persistence implements ImportedFilesDao {

    @Override
    public void saveImportedFile(ImportedFilesEntity importedFile) throws Exception {
        Session session = getHibernateUtil().getSessionTL();
        session.save(importedFile);
    }

    @Override
    public ImportedFilesEntity findImportedFileByName(String fileName) throws Exception {
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put("importedFileName", fileName);
        Object queryResult = execUniqueResultNamedQuery(NamedQueryConstants.GET_IMPORTED_FILES_BY_NAME, queryParameters);
        return queryResult == null ? null : (ImportedFilesEntity) queryResult;
    }

}
