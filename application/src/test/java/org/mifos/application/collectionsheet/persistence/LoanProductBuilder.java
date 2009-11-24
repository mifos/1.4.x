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
package org.mifos.application.collectionsheet.persistence;

import java.util.Date;

import org.joda.time.DateTime;
import org.mifos.application.accounts.financial.business.GLCodeEntity;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.application.productdefinition.business.NoOfInstallSameForAllLoanBO;
import org.mifos.application.productdefinition.business.ProductCategoryBO;
import org.mifos.application.productdefinition.util.helpers.ApplicableTo;
import org.mifos.application.productdefinition.util.helpers.InterestType;
import org.mifos.application.productdefinition.util.helpers.PrdStatus;
import org.mifos.framework.TestUtils;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Constants;

/**
 *
 */
public class LoanProductBuilder {

    // PRD_OFFERING FIELDS
    private final String globalProductNumber = "ZZZZZ-1111";
    private final Date startDate = new DateTime().minusDays(14).toDate();
    private final String name = "testLoanProduct";
    private final String shortName = "TLP1";
    
    private final Date createdDate = new DateTime().minusDays(14).toDate();
    private final Short createdByUserId = TestUtils.makeUserWithLocales().getId();
    
    private final GLCodeEntity depositGLCode = new GLCodeEntity(Short.valueOf("1"), "10000");
    private final GLCodeEntity interesetGLCode = new GLCodeEntity(Short.valueOf("2"), "11000");

    private final ApplicableTo applicableToCustomer = ApplicableTo.GROUPS;

    private ProductCategoryBO category = new ProductCategoryBO(Short.valueOf("1"), "testXX");

    // loan specific
    private final InterestType interestType = InterestType.FLAT;
    private final Double minInterestRate = Double.valueOf("0.0");
    private final Double maxInterestRate = Double.valueOf("5.0");
    private final Double defaultInterestRate = Double.valueOf("3.0");
    private final Short interestPaidAtDisbursement = Constants.NO;
    private final Short principalDueLastInstallment = Constants.NO;
    private final Short minNoOfInstallmentsForLoan = Short.valueOf("1");
    private final Short maxNoOfInstallmentsForLoan = Short.valueOf("11");
    private final Short defaultNoOfInstallmentsForLoan = Short.valueOf("6");
    private PrdStatus productStatus = PrdStatus.LOAN_ACTIVE;
    
    public LoanOfferingBO buildForUnitTests() {

        LoanOfferingBO loanProduct = build();

        return loanProduct;
    }

    private LoanOfferingBO build() {
        
        final LoanOfferingBO loanProduct = new LoanOfferingBO(depositGLCode, interesetGLCode, interestType,
                minInterestRate, maxInterestRate, defaultInterestRate, interestPaidAtDisbursement,
                principalDueLastInstallment, new NoOfInstallSameForAllLoanBO(), name, shortName, globalProductNumber,
                startDate, applicableToCustomer, category, productStatus);
        
        final NoOfInstallSameForAllLoanBO noOfInstallSameForAllLoan = new NoOfInstallSameForAllLoanBO(
                minNoOfInstallmentsForLoan, maxNoOfInstallmentsForLoan, defaultNoOfInstallmentsForLoan, loanProduct);
        
        loanProduct.setNoOfInstallSameForAllLoan(noOfInstallSameForAllLoan);
        
        return loanProduct;
    }

    public LoanOfferingBO buildForIntegrationTests() {
        
        category = (ProductCategoryBO) StaticHibernateUtil.getSessionTL().get(ProductCategoryBO.class,
                Short.valueOf("2"));

        LoanOfferingBO loanProduct = build();

        return loanProduct;
    }

    public LoanProductBuilder active() {
        this.productStatus = PrdStatus.LOAN_ACTIVE;
        return this;
    }

}
