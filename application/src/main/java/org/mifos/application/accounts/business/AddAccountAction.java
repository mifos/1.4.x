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

package org.mifos.application.accounts.business;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.mifos.application.master.business.MasterDataEntity;
import org.mifos.application.master.business.MifosLookUpEntity;
import org.mifos.framework.persistence.Upgrade;

public class AddAccountAction extends Upgrade {

    private final Short locale;
    private final String lookupValueKey;
    private final int action;
    private final String message;
    protected static final String wrongLookupValueKeyFormat = "The key format must be AccountAction-...";
    protected static final String keyFormat = "AccountAction-";

    /*
     * This constructor is used for version 174 and lower. And it must not be
     * used afterward
     */
    public AddAccountAction(int higherVersion, int action, Short locale, String message) {
        super(higherVersion);
        if (higherVersion > LOOKUP_VALUE_CHANGE_VERSION)
            throw new RuntimeException(wrongConstructor);
        this.action = action;
        this.locale = locale;
        this.message = message;
        this.lookupValueKey = " ";
    }

    /*
     * This constructor must be used after version 174. The lookupValueKey must
     * in the format AccountAction-...
     */
    public AddAccountAction(int higherVersion, int action, String lookupValueKey) {
        super(higherVersion);
        if (!validateLookupValueKey(keyFormat, lookupValueKey))
            throw new RuntimeException(wrongLookupValueKeyFormat);
        this.action = action;
        this.locale = MasterDataEntity.CUSTOMIZATION_LOCALE_ID;
        this.lookupValueKey = lookupValueKey;
        this.message = null;
    }

    @Override
    public void upgrade(Connection connection)
            throws IOException, SQLException {
        int lookupEntity = MifosLookUpEntity.ACCOUNT_ACTION;

        int lookupId = insertLookupValue(connection, lookupEntity, lookupValueKey);
        insertMessage(connection, lookupId, locale, message);
        addAction(connection, action, lookupId);
        upgradeVersion(connection);
    }

    private void addAction(Connection connection, int actionId, int lookupId) throws SQLException {
        PreparedStatement statement = connection
                .prepareStatement("INSERT INTO ACCOUNT_ACTION(ACCOUNT_ACTION_ID,LOOKUP_ID) " + "VALUES(?,?)");
        statement.setInt(1, actionId);
        statement.setInt(2, lookupId);
        statement.executeUpdate();
        statement.close();
    }
}
