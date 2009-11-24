<%--
Copyright (c) 2005-2009 Grameen Foundation USA
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License.

See also http://www.apache.org/licenses/LICENSE-2.0.html for an
explanation of the license and how it is applied.
--%>

<%@ taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@ taglib uri="/tags/date" prefix="date"%>
<%@ taglib uri="/mifos/customtags" prefix="mifoscustom"%>
<%@ taglib uri="/mifos/custom-tags" prefix="customtags"%>


<tiles:insert definition=".clientsacclayoutsearchmenu">
<%@ taglib uri="/sessionaccess" prefix="session"%>

	<tiles:put name="body" type="string">
	<span id="page.id" title="ApplyPayment" />
		<script src="pages/framework/js/conversion.js"></script>
		<script src="pages/framework/js/con_en.js"></script>
		<script src="pages/framework/js/con_${sessionScope['UserContext'].currentLocale}.js"></script>
		<SCRIPT>
	function ViewDetails(){
		customerAccountActionForm.action="customerAccountAction.do?method=load";
		customerAccountActionForm.submit();
	}

	function ViewLoanDetails(form){
		form.action="loanAccountAction.do?method=get";
		form.submit();
	}
</SCRIPT>
		<SCRIPT SRC="pages/framework/js/date.js"></SCRIPT>
		<html-el:form method="post"
			action="/applyPaymentAction.do?method=preview"
			focus="paymentTypeId">
			<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'AccountType')}" var="AccountType" />
			<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'AccountId')}" var="AccountId" />
			<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05"><span class="fontnormal8pt"> <customtags:headerLink/>

					<c:choose>
						<c:when test="${param.input == 'loan'}">
						</c:when>
						<c:otherwise>

							<html-el:link styleId="applypayment.link.viewCharges" href="customerAccountAction.do?method=load&globalCustNum=${param.globalCustNum}" >
					          	<c:if test="${param.input == 'ViewCenterCharges'}">
	          						<mifos:mifoslabel name="Center.CenterCharges" bundle="CenterUIResources"/>
	          					</c:if>
	          					<c:if test="${param.input == 'ViewGroupCharges'}">
					          		<mifos:mifoslabel name="Center.GroupCharges" bundle="CenterUIResources"/>
					          	</c:if>
					          	<c:if test="${param.input == 'ViewClientCharges'}">
	          						<mifos:mifoslabel name="Center.ClientCharges" bundle="CenterUIResources"/>
	          					</c:if>
							</html-el:link>

						</c:otherwise>
					</c:choose> </span></td>
				</tr>
			</table>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width="70%" height="24" align="left" valign="top"
						class="paddingL15T15">
					<table width="96%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td width="70%" class="headingorange"><span class="heading"> <c:out
								value="${param.prdOfferingName}" /> - </span><mifos:mifoslabel
								name="accounts.apply_payment" />
								</td>
						</tr>
					</table>
					<br>
					<table width="95%" border="0" cellspacing="0" cellpadding="3">
						<tr>
							<td colspan="2"><font class="fontnormalRedBold"> <span id="applypayment.error.message"><html-el:errors
								bundle="accountsUIResources" /></span> </font></td>
						</tr>
						<tr>
							<td colspan="2" class="fontnormal"><mifos:mifoslabel
								name="accounts.asterisk" /><br>
							<br>
							</td>
						</tr>
						<tr>
							<td align="right" class="fontnormal"><mifos:mifoslabel
								mandatory="yes" isColonRequired="Yes" name="accounts.date_of_trxn" /></td>
							<td class="fontnormal"><date:datetag renderstyle="simple" property="transactionDate" /></td>
						</tr>
						<tr>
							<td width="24%" align="right" class="fontnormal"><span id="applypayment.label.amount"><mifos:mifoslabel
								mandatory="yes" name="accounts.amount" isColonRequired="Yes" /></span></td>
							<td width="76%">
							<c:choose>
								<c:when test="${AccountType=='LOAN_ACCOUNT'}">
								<html-el:text property="amount"
								styleId="applypayment.input.amount"
								name="applyPaymentActionForm" />
								</c:when>
								<c:otherwise>
								<html-el:text property="amount"
								styleId="applypayment.input.amount"
								disabled="true" name="applyPaymentActionForm" />
								</c:otherwise>
							</c:choose>
							</td>
						</tr>
						<tr>
							<td align="right" class="fontnormal"><mifos:mifoslabel
								name="accounts.mode_of_payment" mandatory="yes" isColonRequired="Yes" /></td>

							<td class="fontnormal"><mifos:select
								name="applyPaymentActionForm" styleId="applypayment.input.paymentType" property="paymentTypeId">
								<c:forEach var="PT" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'PaymentType')}" >
									<html-el:option value="${PT.id}">${PT.displayValue}</html-el:option>
								</c:forEach>
							</mifos:select></td>
						</tr>
						<tr>
							<td align="right" class="fontnormal"><span id="applypayment.label.receiptId"><mifos:mifoslabel
								name="accounts.receiptid" isColonRequired="Yes" /></span></td>
							<td class="fontnormal"><mifos:mifosalphanumtext
								styleId="applypayment.input.receiptId"
								property="receiptId" name="applyPaymentActionForm" maxlength="25"/></td>
						</tr>
						<tr>
							<td align="right" class="fontnormal"><mifos:mifoslabel
								name="accounts.receiptdate" isColonRequired="Yes" /></td>
							<td class="fontnormal"><date:datetag renderstyle="simple" property="receiptDate" /></td>
						</tr>
					</table>
					<table width="96%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center" class="blueline">&nbsp;</td>
						</tr>
						<tr>
							<td align="center">&nbsp;</td>
						</tr>
						<tr>
							<td align="center"><c:choose>
								<c:when
									test="${(AccountType!='LOAN_ACCOUNT') && (applyPaymentActionForm.amount == '0.0'||applyPaymentActionForm.amount=='0')}">
									<html-el:submit styleId="applypayment.button.submit" styleClass="buttn" disabled="true"
										property="Preview">
										<mifos:mifoslabel name="accounts.reviewtransaction">
										</mifos:mifoslabel>
									</html-el:submit>
								</c:when>
								<c:otherwise>
									<html-el:submit styleId="applypayment.button.reviewTransaction" styleClass="buttn" property="Preview">
										<mifos:mifoslabel name="accounts.reviewtransaction">
										</mifos:mifoslabel>
									</html-el:submit>
								</c:otherwise>
							</c:choose> &nbsp; <c:choose>
								<c:when test="${param.input == 'loan'}">
									<html-el:button styleId="applypayment.button.cancel" styleClass="cancelbuttn" property="Cancel"
										onclick="ViewLoanDetails(this.form)">
										<mifos:mifoslabel name="accounts.cancel"></mifos:mifoslabel>
									</html-el:button>

								</c:when>
								<c:otherwise>
									<html-el:button styleId="applypayment.button.cancel" styleClass="cancelbuttn" property="Cancel"
										onclick="ViewDetails()">
										<mifos:mifoslabel name="accounts.cancel"></mifos:mifoslabel>
									</html-el:button>

								</c:otherwise>
							</c:choose></td>
						</tr>
					</table>
					</td>
				</tr>
			</table>
			<html-el:hidden property="prdOfferingName" value="${param.prdOfferingName}" />
			<html-el:hidden property="input" value="${param.input}" />
			<html-el:hidden property="globalCustNum" value="${param.globalCustNum}" />
			<html-el:hidden property="globalAccountNum" value="${param.globalAccountNum}" />
			<html-el:hidden property="accountType" value="${AccountType}" />			
			<html-el:hidden property="accountId" value="${AccountId}" />
		</html-el:form>
		<html-el:form action="customerAccountAction.do?method=load">
			<html-el:hidden property="globalCustNum" value="${param.globalCustNum}" />
		</html-el:form>
	</tiles:put>
</tiles:insert>
