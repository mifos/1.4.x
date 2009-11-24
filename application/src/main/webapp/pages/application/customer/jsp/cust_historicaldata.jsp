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
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@ taglib uri="/userlocaledate" prefix="userdatefn"%>
<%@ taglib uri="/mifos/custom-tags" prefix="customtags"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>


<tiles:insert definition=".clientsacclayoutsearchmenu">
	<tiles:put name="body" type="string">
	<span id="page.id" title="CustHistoricalData" />

		<script language="javascript">
  function goToCancelPage(){
	custHistoricalDataActionForm.action="custHistoricalDataAction.do?method=cancelHistoricalData";
	custHistoricalDataActionForm.submit();
  }
</script>
		<html-el:form action="custHistoricalDataAction.do">
		<c:set
				value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'BusinessKey')}"
				var="BusinessKey" />
		<c:set
				value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'mfiJoiningDate')}"
				var="mfiDate" />
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05"><span class="fontnormal8pt"> <customtags:headerLink /></span>
				</tr>
			</table>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td align="left" valign="top" class="paddingL15T15">
					<table width="95%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td width="41%" class="headingorange"><span class="heading"> <c:out
								value="${BusinessKey.displayName}" /> - </span> <mifos:mifoslabel
								name="label.historicaldata" bundle="CustomerUIResources"></mifos:mifoslabel></td>
							<td width="42%" align="right" class="fontnormal"><html-el:link styleId="cust_historicaldata.link.addEditHistoricalData"
								action="custHistoricalDataAction.do?method=loadHistoricalData&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}">
								<mifos:mifoslabel name="label.add_edit_hd"
									bundle="CustomerUIResources"></mifos:mifoslabel>
							</html-el:link></td>
						</tr>
						<tr>
							<td colspan="2"><font class="fontnormalRedBold"><span id="cust_historicaldata.error.message"><html-el:errors
								bundle="CustomerUIResources" /></span></font></td>
						</tr>

					</table>
					<br>
					<table width="95%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="left" valign="top" class="fontnormalbold"><span
								class="fontnormal"></span> <mifos:mifoslabel
								name="label.MFIjoiningdate" bundle="CustomerUIResources"></mifos:mifoslabel>
							<span class="fontnormal"> <c:out value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,mfiDate)}" />
							<br>
							</span> <mifos:mifoslabel name="${ConfigurationConstants.LOAN}" />
							<mifos:mifoslabel name="label.loancyclenumber"
								bundle="CustomerUIResources"></mifos:mifoslabel> <span
								class="fontnormal"> <c:out
								value="${BusinessKey.historicalData.loanCycleNumber}" /><br>
							</span> <mifos:mifoslabel name="label.productname"
								bundle="CustomerUIResources"></mifos:mifoslabel> <span
								class="fontnormal"> <c:out
								value="${BusinessKey.historicalData.productName}" /><br>
							</span> <mifos:mifoslabel name="label.amountof"
								bundle="CustomerUIResources" /><mifos:mifoslabel
								name="${ConfigurationConstants.LOAN}" /><mifos:mifoslabel
								name="label.colon" bundle="CustomerUIResources" /> <span
								class="fontnormal"> <c:out
								value="${BusinessKey.historicalData.loanAmount}" /><br>
							</span> <mifos:mifoslabel name="label.totalamountpaidLabel"
								bundle="CustomerUIResources" /><mifos:mifoslabel
								name="label.colon" bundle="CustomerUIResources" /> <span
								class="fontnormal"><c:out
								value="${BusinessKey.historicalData.totalAmountPaid}" /><br>
							</span> <mifos:mifoslabel
								name="${ConfigurationConstants.INTEREST}" /> <mifos:mifoslabel
								name="label.interestpaidLabel" bundle="CustomerUIResources" /><mifos:mifoslabel
								name="label.colon" bundle="CustomerUIResources" /> <span
								class="fontnormal"> <c:out
								value="${BusinessKey.historicalData.interestPaid}" /><br>
							</span> <mifos:mifoslabel name="label.numberofmissedpayments"
								bundle="CustomerUIResources" /><mifos:mifoslabel
								name="label.colon" bundle="CustomerUIResources" /> <span
								class="fontnormal"> <c:out
								value="${BusinessKey.historicalData.missedPaymentsCount}" /><br>
							</span> <mifos:mifoslabel name="label.totalnumberofpayments"
								bundle="CustomerUIResources" /><mifos:mifoslabel
								name="label.colon" bundle="CustomerUIResources" /> <span
								class="fontnormal"><c:out
								value="${BusinessKey.historicalData.totalPaymentsCount}" /><br>
							<br>
							</span> <mifos:mifoslabel name="label.notes"
								bundle="CustomerUIResources"></mifos:mifoslabel>: <span
								class="fontnormal"><c:out
								value="${BusinessKey.historicalData.notes}" /><br>
							</span></td>
						</tr>
					</table>
					<table width="96%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center" class="blueline">&nbsp;</td>
						</tr>
					</table>
					<br>
			<table width="96%" border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center"><html-el:button styleId="cust_historicaldata.button.cancel" property="btn"
								styleClass="buttn"
								onclick="goToCancelPage()">
								<mifos:mifoslabel name="label.backtodetailpage"
									bundle="CustomerUIResources"></mifos:mifoslabel>
							</html-el:button></td>
						</tr>
					</table>
					<br>
					<br>
					<br>
					</td>
				</tr>
			</table>
			<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
			<html-el:hidden property="globalCustNum" value="${BusinessKey.globalCustNum}" />
		</html-el:form>
	</tiles:put>
</tiles:insert>
