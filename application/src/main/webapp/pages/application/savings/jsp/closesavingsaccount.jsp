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
<%@ taglib uri="/mifos/customtags" prefix="mifoscustom"%>
<%@ taglib uri="/mifos/custom-tags" prefix="customtags"%>
<%@ taglib uri="/tags/date" prefix="date"%>
<%@ taglib uri="/userlocaledate" prefix="userdatefn"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<tiles:insert definition=".clientsacclayoutsearchmenu">
	<tiles:put name="body" type="string">
	<span id="page.id" title="closesavingsaccount" />
		<SCRIPT SRC="pages/framework/js/date.js"></SCRIPT>
		<script language="javascript">
		function funCancel(form){
			form.action="savingsClosureAction.do?method=cancel";
			form.submit();
		}
	</script>
		<html-el:form method="post" action="/savingsClosureAction.do?method=preview">
		 <html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
			<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'BusinessKey')}" var="BusinessKey" />
			<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'accountPayment')}" var="accountPayment" />

			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05">
						<span class="fontnormal8pt"> <customtags:headerLink /> </span>
					</td>
				</tr>
			</table>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width="70%" height="24" align="left" valign="top" class="paddingL15T15">
						<table width="96%" border="0" cellpadding="3" cellspacing="0">
							<tr>
								<td width="70%" class="headingorange">
									<span class="heading"><c:out value="${BusinessKey.savingsOffering.prdOfferingName}" /> # <c:out value="${BusinessKey.globalAccountNum}" /> - </span>
									<mifos:mifoslabel name="Savings.closeAccount" />
								</td>
							</tr>
							<tr>
								<td class="fontnormal">
									<br>
									<mifos:mifoslabel name="Savings.provideDetailsToClose" />
									<mifos:mifoslabel name="Savings.fieldsRequired" mandatory="yes"/>
								</td>
							</tr>

						</table>

						<table width="95%" border="0" cellspacing="0" cellpadding="3">
							<tr>
								<td colspan="2">
									<font class="fontnormalRedBold"><html-el:errors bundle="SavingsUIResources" /></font>
								</td>
							</tr>
							<tr>
								<td colspan="2" align="right" class="fontnormal">
									<img src="images/trans.gif" width="10" height="2">
								</td>
							</tr>
							<tr>
								<td align="right" class="fontnormal">
									<mifos:mifoslabel name="Savings.dateOfTrxn" isColonRequired="yes"/>
								</td>
								<td class="fontnormal">
									<%--<c:out value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,sessionScope.accountPayment.paymentDate)}" />--%>
									<c:out value="${sessionScope.savingsClosureForm.trxnDate}"/>
								</td>
							</tr>
							<tr>
								<td width="24%" align="right" class="fontnormal">
									<mifos:mifoslabel name="Savings.amount" isColonRequired="yes"/>
								</td>
								<td width="76%" valign="top" class="fontnormal">
									<c:out value="${accountPayment.amount}" />
								</td>
							</tr>
							<tr>
								<td align="right" class="fontnormal">
									<mifos:mifoslabel name="Savings.modeOfPayment" mandatory="yes" isColonRequired="yes"/>
								</td>
								<td>
									<mifos:select name="savingsClosureForm" property="paymentTypeId">
										<c:forEach var="Payment" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'PaymentType')}" >
											<html-el:option value="${Payment.id}">${Payment.name}</html-el:option>
										</c:forEach>
									</mifos:select>
								</td>
							</tr>
							<tr>
								<td align="right" class="fontnormal">
									<mifos:mifoslabel keyhm="Savings.ReceiptId" isColonRequired="Yes" name="Savings.receiptId" />
								</td>
								<td>
									<mifos:mifosalphanumtext  styleId="closesavingsaccount.input.receiptId" keyhm="Savings.ReceiptId" property="receiptId" />
								</td>
							</tr>
							<tr>
								<td align="right" class="fontnormal">
									<mifos:mifoslabel keyhm="Savings.ReceiptDate" isColonRequired="Yes"  name="Savings.receiptDate" />
								</td>
								<td class="fontnormal">
									<date:datetag keyhm="Savings.ReceiptDate" property="receiptDate" />
								</td>
							</tr>
							<c:set var="customerLevel" value="${BusinessKey.customer.customerLevel.id}" />
					  		<c:choose>
						  		<c:when test="${customerLevel==CustomerLevel.CENTER.value or 
						  				(customerLevel==CustomerLevel.GROUP.value and 
						  				BusinessKey.recommendedAmntUnit.id==RecommendedAmountUnit.PERINDIVIDUAL.value)}">
								<tr>
					                <td align="right" class="fontnormal">
			            				<mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" mandatory="yes"/>
										<mifos:mifoslabel name="Savings.clientName" isColonRequired="yes"/>
									</td>
					                <td>				  				
							  			<mifos:select name="savingsClosureForm" property="customerId">
											<c:forEach var="client" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'clientList')}">
												<html-el:option value="${client.customerId}">
													<c:out value="${client.displayName}" />
												</html-el:option>
											</c:forEach>
											<html-el:option value="${BusinessKey.customer.customerId}">
												<mifos:mifoslabel name="Savings.nonSpecified" />
											</html-el:option>
										</mifos:select>
									</td>
								</tr>
						  		</c:when>
						  		<c:otherwise>
							  		<html-el:hidden property="customerId" value="${BusinessKey.customer.customerId}" />
						  		</c:otherwise>
					  		</c:choose>
						<tr>
								<td align="right" valign="top" class="fontnormal">
									<mifos:mifoslabel name="Savings.notes" mandatory="yes" isColonRequired="yes"/>
								</td>
								<td>
									<html-el:textarea  styleId="closesavingsaccount.input.notes" property="notes" cols="37" style="width:320px; height:110px;" />
								</td>
							</tr>
						</table>
						<table width="96%" border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td align="center" class="blueline">
									&nbsp;
								</td>
							</tr>
							<tr>
								<td align="center">
									&nbsp;
								</td>
							</tr>
							<tr>
								<td align="center">
									<html-el:submit styleId="closesavingsaccount.button.submit" styleClass="buttn">
										<mifos:mifoslabel name="loan.preview" />
									</html-el:submit>
									&nbsp;
									<html-el:button styleId="closesavingsaccount.button.cancel" property="cancelButton" onclick="javascript:funCancel(this.form)" styleClass="cancelbuttn">
										<mifos:mifoslabel name="loan.cancel" />
									</html-el:button>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
			<html-el:hidden property="accountId" value="${BusinessKey.accountId}" />
			<html-el:hidden property="globalAccountNum" value="${BusinessKey.globalAccountNum}" />
		</html-el:form>
	</tiles:put>
</tiles:insert>
