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

package org.mifos.framework.components.batchjobs.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mifos.application.accounts.business.AccountActionDateEntity;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.persistence.AccountPersistence;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.customer.business.CustomerAccountBO;
import org.mifos.framework.components.batchjobs.MifosTask;
import org.mifos.framework.components.batchjobs.SchedulerConstants;
import org.mifos.framework.components.batchjobs.TaskHelper;
import org.mifos.framework.components.batchjobs.exceptions.BatchJobException;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.DateTimeService;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.config.GeneralConfig;

public class GenerateMeetingsForCustomerAndSavingsHelper extends TaskHelper {

    public GenerateMeetingsForCustomerAndSavingsHelper(MifosTask mifosTask) {
        super(mifosTask);
    }

    @Override
    public void execute(long timeInMillis) throws BatchJobException {
        long taskStartTime = new DateTimeService().getCurrentDateTime().getMillis();
        AccountPersistence accountPersistence = new AccountPersistence();
        List<Integer> customerAccountIds;
        int accountCount = 0;

        try {
            long time1 = new DateTimeService().getCurrentDateTime().getMillis();
            customerAccountIds = accountPersistence.getActiveCustomerAndSavingsAccounts();
            accountCount = customerAccountIds.size();
            long duration = new DateTimeService().getCurrentDateTime().getMillis() - time1;
            getLogger().info("Time to execute the query " + duration + " . Got " + accountCount + " accounts.");
            if (accountCount == 0) {
                return;
            }
        } catch (PersistenceException e) {
            throw new BatchJobException(e);
        }

        List<String> errorList = new ArrayList<String>();
        int currentRecordNumber = 0;
        int outputIntervalForBatchJobs = GeneralConfig.getOutputIntervalForBatchJobs();
        int batchSize = GeneralConfig.getBatchSizeForBatchJobs();
        int recordCommittingSize = GeneralConfig.getRecordCommittingSizeForBatchJobs();
        getLogger().info(
                "Using parameters:" + "\n  OutputIntervalForBatchJobs: " + outputIntervalForBatchJobs
                        + "\n  BatchSizeForBatchJobs: " + batchSize + "\n  RecordCommittingSizeForBatchJobs: "
                        + recordCommittingSize);
        String initial_message = "" + accountCount + " accounts to process, results output every "
                + outputIntervalForBatchJobs + " accounts";
        getLogger().info(initial_message);

        long startTime = new DateTimeService().getCurrentDateTime().getMillis();
        Integer currentAccountId = null;
        int updatedRecordCount = 0;
        try {
            StaticHibernateUtil.getSessionTL();
            StaticHibernateUtil.startTransaction();
            for (Integer accountId : customerAccountIds) {
                currentRecordNumber++;
                currentAccountId = accountId;
                AccountBO accountBO = accountPersistence.getAccount(accountId);
                if (isScheduleToBeGenerated(accountBO.getLastInstallmentId(), accountBO.getDetailsOfNextInstallment())) {
                    if (accountBO instanceof CustomerAccountBO) {
                        ((CustomerAccountBO) accountBO).generateNextSetOfMeetingDates();
                        updatedRecordCount++;
                    } else if (accountBO instanceof SavingsBO) {
                        ((SavingsBO) accountBO).generateNextSetOfMeetingDates();
                        updatedRecordCount++;
                    }
                }
                if (currentRecordNumber % batchSize == 0) {
                    StaticHibernateUtil.flushAndClearSession();
                    getLogger().info("completed HibernateUtil.flushAndClearSession()");
                }
                if (updatedRecordCount > 0) {
                    if (updatedRecordCount % recordCommittingSize == 0) {
                        StaticHibernateUtil.commitTransaction();
                        StaticHibernateUtil.getSessionTL();
                        StaticHibernateUtil.startTransaction();
                    }
                }
                if (currentRecordNumber % outputIntervalForBatchJobs == 0) {
                    long time = System.currentTimeMillis();
                    String message = "" + currentRecordNumber + " processed, " + (accountCount - currentRecordNumber)
                            + " remaining, " + updatedRecordCount + " updated, batch time: " + (time - startTime)
                            + " ms";
                    System.out.println(message);
                    getLogger().info(message);
                    startTime = time;
                }
            }
            StaticHibernateUtil.commitTransaction();

        } catch (Exception e) {
            getLogger().info("account " + currentAccountId.intValue() + " exception " + e.getMessage());
            StaticHibernateUtil.rollbackTransaction();
            if (currentAccountId != null) {
                errorList.add(currentAccountId.toString());
            }
            errorList.add(currentAccountId.toString());
            getLogger().error("Unable to generate schedules for account with ID " + currentAccountId, e);
        } finally {
            StaticHibernateUtil.closeSession();
        }

        if (errorList.size() > 0) {
            throw new BatchJobException(SchedulerConstants.FAILURE, errorList);
        }
        getLogger().info(
                "GenerateMeetingsForCustomerAndSavings ran in "
                        + (new DateTimeService().getCurrentDateTime().getMillis() - taskStartTime));

    }

    private boolean isScheduleToBeGenerated(int installmentSize, AccountActionDateEntity nextInstallment) {
        Date currentDate = DateUtils.getCurrentDateWithoutTimeStamp();
        short nextInstallmentId = (short) installmentSize;
        if (nextInstallment != null) {
            if (nextInstallment.getActionDate().compareTo(currentDate) == 0) {
                nextInstallmentId = (short) (nextInstallment.getInstallmentId().intValue() + 1);
            } else {
                nextInstallmentId = (short) (nextInstallment.getInstallmentId().intValue());
            }
        }
        int totalInstallmentDatesToBeChanged = installmentSize - nextInstallmentId + 1;
        return totalInstallmentDatesToBeChanged <= 5;
    }

    @Override
    public boolean isTaskAllowedToRun() {
        return true;
    }

}
