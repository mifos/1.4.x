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

package org.mifos.config;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.testng.annotations.Test;

/**
 * FIXME: These tests are probably entirely unnecessary--we should be confident
 * that Apache Commons Configuration works and only test Mifos-specific code.
 */
@Test(groups="unit")
public class GeneralConfigTest extends TestCase {

    public void testGetMaxPointsPerPPISurvey() {
        int configuredValue = GeneralConfig.getMaxPointsPerPPISurvey();
        ConfigurationManager configMgr = ConfigurationManager.getInstance();
        int currentValue = 30;
        configMgr.setProperty(GeneralConfig.MaxPointsPerPPISurvey, currentValue);
       Assert.assertEquals(currentValue, GeneralConfig.getMaxPointsPerPPISurvey());
        // clear the MaxPointsPerPPISurvey property from the config file so
        // should get the default value
        configMgr.clearProperty(GeneralConfig.MaxPointsPerPPISurvey);
        int defaultValue = GeneralConfig.getMaxPointsPerPPISurvey();
        int expectedDefaultValue = 101;
       Assert.assertEquals(defaultValue, expectedDefaultValue);
        // save it back
        configMgr.setProperty(GeneralConfig.MaxPointsPerPPISurvey, configuredValue);

    }

    public void testGetBatchSizeForBatchJobs() {
        int configuredValue = GeneralConfig.getBatchSizeForBatchJobs();
        ConfigurationManager configMgr = ConfigurationManager.getInstance();
        int currentValue = 40;
        configMgr.setProperty(GeneralConfig.BatchSizeForBatchJobs, currentValue);
       Assert.assertEquals(currentValue, GeneralConfig.getBatchSizeForBatchJobs());
        // clear the BatchSizeForBatchJobs property from the config file so
        // should get the default value
        configMgr.clearProperty(GeneralConfig.BatchSizeForBatchJobs);
        int defaultValue = GeneralConfig.getBatchSizeForBatchJobs();
        int expectedDefaultValue = 40;
       Assert.assertEquals(defaultValue, expectedDefaultValue);
        // save it back
        configMgr.setProperty(GeneralConfig.BatchSizeForBatchJobs, configuredValue);

    }

    public void testGetRecordCommittingSizeForBatchJobs() {
        int configuredValue = GeneralConfig.getRecordCommittingSizeForBatchJobs();
        ConfigurationManager configMgr = ConfigurationManager.getInstance();
        int currentValue = 500;
        configMgr.setProperty(GeneralConfig.RecordCommittingSizeForBatchJobs, currentValue);
       Assert.assertEquals(currentValue, GeneralConfig.getRecordCommittingSizeForBatchJobs());
        // clear the BatchSizeForBatchJobs property from the config file so
        // should get the default value
        configMgr.clearProperty(GeneralConfig.RecordCommittingSizeForBatchJobs);
        int defaultValue = GeneralConfig.getRecordCommittingSizeForBatchJobs();
        int expectedDefaultValue = 1000;
       Assert.assertEquals(defaultValue, expectedDefaultValue);
        // save it back
        configMgr.setProperty(GeneralConfig.RecordCommittingSizeForBatchJobs, configuredValue);

    }
    
    public void testGetOutputIntervalForBatchJobs() {
        int configuredValue = GeneralConfig.getOutputIntervalForBatchJobs();
        ConfigurationManager configMgr = ConfigurationManager.getInstance();
        int currentValue = 500;
        configMgr.setProperty(GeneralConfig.OutputIntervalForBatchJobs, currentValue);
       Assert.assertEquals(currentValue, GeneralConfig.getOutputIntervalForBatchJobs());
        configMgr.clearProperty(GeneralConfig.OutputIntervalForBatchJobs);
        int defaultValue = GeneralConfig.getOutputIntervalForBatchJobs();
        int expectedDefaultValue = 1000;
       Assert.assertEquals(defaultValue, expectedDefaultValue);
        // save it back
        configMgr.setProperty(GeneralConfig.OutputIntervalForBatchJobs, configuredValue);
        
    }

}
