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

package org.mifos.framework.util.helpers;

/**
 * Selected constants. This class is deprecated; constants should instead be in
 * a more specific place.
 */
public interface Constants {
    public final static String DELEGATOR = "Delegator";
    public final static String VALUEOBJECT = "ValueObject";
    public final static String BUSINESSPROCESSOR = "BusinessProcessor";
    public final static String CONTEXT = "Context";
    public final static String MIFOSLOCALE = "Mifos_Locale";
    public final static String PREVIOUS_REQUEST = "Previous_Request";
    public final static String PATH = "path";
    public final static String CREATE_SUCCESS = "create_success";
    public final static String UPDATE_SUCCESS = "update_success";
    public final static String NEXT_SUCCESS = "next_success";
    public final static String PREVIEW_SUCCESS = "preview_success";
    public final static String SEARCH_SUCCESS = "search_success";
    public static final String GET_SUCCESS = "get_success";
    public static final String LOAD_SUCCESS = "load_success";
    public static final String PREVIOUS_SUCCESS = "previous_success";
    public final static String CANCEL_SUCCESS = "cancel_success";
    public final static String MANAGE_SUCCESS = "manage_success";
    public final static String DELETE_SUCCESS = "delete_success";
    public final static String BUSINESSPROCESSORIMPLEMENTATION = "BusinessProcessorImplementation";
    public final static String SKIPVALIDATION = "skipValidation";
    public final static String MANAGE_PREVIOUS = "manage_previous";
    public final static String MANAGE_PREVIEW = "manage_preview";
    public final static String STORE_ATTRIBUTE = "store_attribute";
    public final static String FAILURE = "failure";
    public final static String MASTERINFO = "masterinfo";

    public final static String USER_ID = "user_id";
    public final static String BRANCH_ID = "branch_id";
    public final static String OFFICE_NAME = "office_name";
    public final static String SEARCH_NAME = "search_name";
    public final static String SEARCH_RESULTS = "search_name";

    public final static String SEARCH_STRING = "search_string";

    public final static String ALGORITHM = "Algorithm";
    public final static String CREATE = "create";
    public final static String UPDATE = "update";
    public final static String DELETE = "delete";
    public final static short ACTIVE = 1;
    public final static short LOCKED = 1;
    public final static short PASSWORDCHANGED = 1;
    public final static short LOANOFFICER = 1;
    public final static String ROLECHANGEEVENT = "RoleChange";
    public final static String ACTIVITYCHANGEEVENT = "ActivityChange";
    public final static String KEY = "123456789123456789123456";
    // User Context for storing in session
    public final static String USERCONTEXT = "UserContext";
    public final static String TEMPUSERCONTEXT = "Temp_UserContext";
    public final static String SELECTTAG = "select";

    public static final short NO = 0;
    public static final short YES = 1;
    // --------------------added for new framework
    public static final String USER_CONTEXT_KEY = "UserContext";
    public static final String ACTIVITYCONTEXT = "ActivityContext";
    public static final String BUSINESS_KEY = "BusinessKey";
    
    public static final String ACCOUNT_TYPE = "AccountType";    
    public static final String ACCOUNT_VERSION = "AccountVersion";
    public static final String ACCOUNT_ID = "AccountId";

    public static final String FIELD_CONFIGURATION = "FieldConfiguration";

    public static final String CURRENTFLOWKEY = "currentFlowKey";
    public static final String FLOWMANAGER = "flowManager";

    public static final String ACTION_MAPPING = "actionMapping";

    /**
     * There is a large amount of code which generates randomNum, writes it to
     * the session, and puts it in URLs. Mifos does not actually consult this
     * value - it is to turn off caching in the browser (why not just use the
     * various "don't cache" headers?) See
     * http://wiki.java.net/bin/view/Javatools/BackButton
     * 
     * A related machanism is {@link TransactionDemarcate}.
     */
    public static final String RANDOMNUM = "randomNUm";

    public static final String INPUT = "input";
    public static final String ERROR_VERSION_MISMATCH = "error.versionnodonotmatch";
    public static final String LOAN = "loan";
    public static final String EMPTY_STRING = "";
}
