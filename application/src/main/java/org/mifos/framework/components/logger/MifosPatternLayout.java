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

package org.mifos.framework.components.logger;

import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.PatternParser;

/**
 * This class extends from the pattern layout. This class overrides the
 * createPatternParser method which returns an instance of the
 * MifosPatternParser.
 */
public class MifosPatternLayout extends PatternLayout {
    /**
     * Overridden method which returns an instance of the MifosPatternParser
     */
    @Override
    protected PatternParser createPatternParser(String pattern) {
        return new MifosPatternParser(pattern);
    }
}
