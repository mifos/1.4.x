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

package org.mifos.application.productdefinition.business.service;

import java.util.Iterator;
import java.util.List;

import org.mifos.application.customer.business.CustomerLevelEntity;
import org.mifos.application.fees.business.FeeBO;
import org.mifos.application.fees.persistence.FeePersistence;
import org.mifos.application.fees.util.helpers.RateAmountFlag;
import org.mifos.application.master.business.MasterDataEntity;
import org.mifos.application.master.persistence.MasterPersistence;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.application.productdefinition.business.PrdApplicableMasterEntity;
import org.mifos.application.productdefinition.business.PrdStatusEntity;
import org.mifos.application.productdefinition.business.ProductCategoryBO;
import org.mifos.application.productdefinition.persistence.LoanPrdPersistence;
import org.mifos.application.productdefinition.persistence.PrdOfferingPersistence;
import org.mifos.application.productdefinition.util.helpers.ApplicableTo;
import org.mifos.application.productdefinition.util.helpers.PrdCategoryStatus;
import org.mifos.application.productdefinition.util.helpers.ProductType;
import org.mifos.framework.business.BusinessObject;
import org.mifos.framework.business.service.BusinessService;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.security.util.UserContext;

public class LoanPrdBusinessService implements BusinessService {

    @Override
    public BusinessObject getBusinessObject(UserContext userContext) {
        return null;
    }

    public List<ProductCategoryBO> getActiveLoanProductCategories() throws ServiceException {
        try {
            return new PrdOfferingPersistence().getApplicableProductCategories(ProductType.LOAN,
                    PrdCategoryStatus.ACTIVE);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public List<MasterDataEntity> getLoanApplicableCustomerTypes(Short localeId) throws ServiceException {
        try {
            List<MasterDataEntity> applList = new MasterPersistence().retrieveMasterEntities(
                    PrdApplicableMasterEntity.class, localeId);
            if (applList != null) {
                for (Iterator<MasterDataEntity> iter = applList.iterator(); iter.hasNext();) {
                    MasterDataEntity masterData = iter.next();
                    if (masterData.getId().equals(ApplicableTo.CENTERS.getValue()))
                        iter.remove();
                }
            }
            return applList;
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public LoanOfferingBO getLoanOffering(Short prdofferingId) throws ServiceException {
        try {
            return new LoanPrdPersistence().getLoanOffering(prdofferingId);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public List<PrdStatusEntity> getApplicablePrdStatus(Short localeId) throws ServiceException {
        try {
            return new PrdOfferingPersistence().getApplicablePrdStatus(ProductType.LOAN, localeId);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public LoanOfferingBO getLoanOffering(Short loanOfferingId, Short localeId) throws ServiceException {
        try {
            return new LoanPrdPersistence().getLoanOffering(loanOfferingId, localeId);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public List<LoanOfferingBO> getAllLoanOfferings(Short localeId) throws ServiceException {
        try {
            return new LoanPrdPersistence().getAllLoanOfferings(localeId);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public Short retrieveLatenessForPrd() throws ServiceException {
        try {
            return new LoanPrdPersistence().retrieveLatenessForPrd();
        } catch (PersistenceException pe) {
            throw new ServiceException(pe);
        }
    }

    public List<LoanOfferingBO> getApplicablePrdOfferings(CustomerLevelEntity customerLevel) throws ServiceException {
        try {
            return new LoanPrdPersistence().getApplicablePrdOfferings(customerLevel);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }
    }

    public FeeBO getfee(Short feeId, RateAmountFlag rateflag) throws ServiceException {

        try {
            return new FeePersistence().getFee(feeId, rateflag);
        } catch (PersistenceException e) {
            throw new ServiceException(e);
        }

    }
}
