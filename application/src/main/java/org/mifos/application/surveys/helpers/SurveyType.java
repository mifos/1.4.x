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

package org.mifos.application.surveys.helpers;

public enum SurveyType {

    CLIENT("client"), GROUP("group"), CENTER("center"), LOAN("loan"), SAVINGS("savings"), ALL("all");

    private String type;

    private SurveyType(String type) {
        this.type = type;
    }

    public String getValue() {
        return type;
    }

    public static SurveyType fromString(String type) {
        for (SurveyType candidate : SurveyType.values()) {
            if (type.toLowerCase().equals(candidate.getValue())) {
                return candidate;
            }
        }

        throw new RuntimeException("no survey type " + type);
    }

}
