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

package org.mifos.accounts.api;

import java.util.ArrayList;
import java.util.List;

import org.mifos.application.acceptedpaymenttype.persistence.AcceptedPaymentTypePersistence;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.exceptions.AccountException;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.loan.persistance.LoanPersistence;
import org.mifos.application.accounts.persistence.AccountPersistence;
import org.mifos.application.accounts.util.helpers.AccountTypes;
import org.mifos.application.accounts.util.helpers.PaymentData;
import org.mifos.application.master.business.PaymentTypeEntity;
import org.mifos.application.util.helpers.TrxnTypes;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.Money;

/**
 * A service class implementation to expose basic functions on loans. As an
 * external API, this class should not expose business objects, only DTOs.
 */
public class StandardAccountService implements AccountService {
    private AccountPersistence accountPersistence;
    private LoanPersistence loanPersistence;
    private AcceptedPaymentTypePersistence acceptedPaymentTypePersistence;

    public StandardAccountService() {

    }

    public StandardAccountService(AccountPersistence accountPersistence, LoanPersistence loanPersistence,
            AcceptedPaymentTypePersistence acceptedPaymentTypePersistence) {
        this.accountPersistence = accountPersistence;
        this.loanPersistence = loanPersistence;
        this.acceptedPaymentTypePersistence = acceptedPaymentTypePersistence;
    }

    public LoanPersistence getLoanPersistence() {
        return this.loanPersistence;
    }

    public void setLoanPersistence(LoanPersistence loanPersistence) {
        this.loanPersistence = loanPersistence;
    }

    public AccountPersistence getAccountPersistence() {
        return this.accountPersistence;
    }

    public void setAccountPersistence(AccountPersistence accountPersistence) {
        this.accountPersistence = accountPersistence;
    }

    public AcceptedPaymentTypePersistence getAcceptedPaymentTypePersistence() {
        return this.acceptedPaymentTypePersistence;
    }

    public void setAcceptedPaymentTypePersistence(AcceptedPaymentTypePersistence acceptedPaymentTypePersistence) {
        this.acceptedPaymentTypePersistence = acceptedPaymentTypePersistence;
    }

    @Override
    public void makePayment(AccountPaymentParametersDto accountPaymentParametersDto) throws PersistenceException,
            AccountException {
        StaticHibernateUtil.startTransaction();
        makePaymentNoCommit(accountPaymentParametersDto);
        StaticHibernateUtil.commitTransaction();
    }

    @Override
    public void makePayments(List<AccountPaymentParametersDto> accountPaymentParametersDtoList)
            throws PersistenceException, AccountException {
        StaticHibernateUtil.startTransaction();
        for (AccountPaymentParametersDto accountPaymentParametersDTO : accountPaymentParametersDtoList) {
            makePaymentNoCommit(accountPaymentParametersDTO);
        }
        StaticHibernateUtil.commitTransaction();
    }

    public void makePaymentNoCommit(AccountPaymentParametersDto accountPaymentParametersDto)
            throws PersistenceException, AccountException {
        final int accountId = accountPaymentParametersDto.getAccount().getAccountId();
        final AccountBO account = getAccountPersistence().getAccount(accountId);
        List<InvalidPaymentReason> validationErrors = validatePayment(accountPaymentParametersDto);
        if (validationErrors.contains(InvalidPaymentReason.INVALID_DATE)) {
            throw new AccountException("errors.invalidTxndate");
        }

        Money amount = new Money(accountPaymentParametersDto.getPaymentAmount());

        PaymentData paymentData = account.createPaymentData(accountPaymentParametersDto.getUserMakingPayment()
                .getUserId(), amount, accountPaymentParametersDto.getPaymentDate().toDateMidnight().toDate(), null,
                null, accountPaymentParametersDto.getPaymentType().getValue());
        paymentData.setComment(accountPaymentParametersDto.getComment());

        account.applyPayment(paymentData);

        getAccountPersistence().createOrUpdate(account);

    }

    @Override
    public AccountReferenceDto lookupLoanAccountReferenceFromExternalId(String externalId) throws PersistenceException {
        LoanBO loan = getLoanPersistence().findByExternalId(externalId);
        if (null == loan) {
            throw new PersistenceException("loan not found for external id " + externalId);
        }
        return new AccountReferenceDto(loan.getAccountId());
    }

    @Override
    public List<InvalidPaymentReason> validatePayment(AccountPaymentParametersDto payment) throws PersistenceException,
            AccountException {
        List<InvalidPaymentReason> errors = new ArrayList<InvalidPaymentReason>();
        AccountBO accountBo = getAccountPersistence().getAccount(payment.getAccount().getAccountId());
        if (!accountBo.isTrxnDateValid(payment.getPaymentDate().toDateMidnight().toDate())) {
            errors.add(InvalidPaymentReason.INVALID_DATE);
        }
        if (AccountTypes.getAccountType(accountBo.getAccountType().getAccountTypeId()) == AccountTypes.LOAN_ACCOUNT) {
            if (!getLoanPaymentTypes().contains(payment.getPaymentType())) {
                errors.add(InvalidPaymentReason.UNSUPPORTED_PAYMENT_TYPE);
            }
        } else if (AccountTypes.getAccountType(accountBo.getAccountType().getAccountTypeId()) == AccountTypes.CUSTOMER_ACCOUNT) {
            if (!getFeePaymentTypes().contains(payment.getPaymentType())) {
                errors.add(InvalidPaymentReason.UNSUPPORTED_PAYMENT_TYPE);
            }
        }
        if (!accountBo.paymentAmountIsValid(new Money(payment.getPaymentAmount()))) {
            errors.add(InvalidPaymentReason.INVALID_PAYMENT_AMOUNT);
        }
        return errors;
    }

    public List<PaymentTypeDto> getFeePaymentTypes() throws PersistenceException {
        return getPaymentTypes(TrxnTypes.fee.getValue());
    }

    @Override
    public List<PaymentTypeDto> getLoanPaymentTypes() throws PersistenceException {
        return getPaymentTypes(TrxnTypes.loan_repayment.getValue());
    }

    private List<PaymentTypeDto> getPaymentTypes(short transactionType) throws PersistenceException {
        final Short IGNORED_LOCALE_ID = 1;
        List<PaymentTypeEntity> paymentTypeEntities = getAcceptedPaymentTypePersistence()
                .getAcceptedPaymentTypesForATransaction(IGNORED_LOCALE_ID, transactionType);
        List<PaymentTypeDto> paymentTypeDtos = new ArrayList<PaymentTypeDto>();
        for (PaymentTypeEntity paymentTypeEntity : paymentTypeEntities) {
            paymentTypeDtos.add(new PaymentTypeDto(paymentTypeEntity.getId(), paymentTypeEntity.getName()));
        }
        return paymentTypeDtos;
    }

    @Override
    public AccountReferenceDto lookupLoanAccountReferenceFromGlobalAccountNumber(String globalAccountNumber)
            throws PersistenceException {
        AccountBO accountBo = getAccountPersistence().findBySystemId(globalAccountNumber);
        if (null == accountBo) {
            throw new PersistenceException("loan not found for global account number " + globalAccountNumber);
        }
        return new AccountReferenceDto(accountBo.getAccountId());
    }

}
