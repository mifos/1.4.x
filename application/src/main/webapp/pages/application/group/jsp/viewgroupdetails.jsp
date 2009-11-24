<%--
Copyright (c) 2005-2008 Grameen Foundation USA
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
<%@ taglib uri="/userlocaledate" prefix="userdatefn"%>
<%@ taglib uri="/mifos/customtags" prefix="mifoscustom"%>
<%@ taglib uri="/customer/customerfunctions" prefix="customerfn"%>
<%@ taglib uri="/mifos/custom-tags" prefix="customtags"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<tiles:insert definition=".clientsacclayoutsearchmenu">
	<tiles:put name="body" type="string">
	<span id="page.id" title="ViewGroupDetails" />
	<fmt:setLocale value='${sessionScope["LOCALE"]}'/>
	<fmt:setBundle basename="org.mifos.config.localizedResources.GroupUIResources"/>
		<html-el:form action="groupCustAction.do">
			<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
			<c:set
				value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'BusinessKey')}"
				var="BusinessKey" />
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05"><span class="fontnormal8pt"><customtags:headerLink
						selfLink="false" /> </span>
				</tr>
			</table>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width="70%" align="left" valign="top" class="paddingL15T15">
					<table width="96%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td width="73%" class="headingorange"><c:out
								value="${BusinessKey.displayName}" /> <br>
							</td>
							<td width="27%" rowspan="2" align="right" valign="top"
								class="fontnormal"><c:if
								test="${BusinessKey.customerStatus.id != CustomerStatus.GROUP_CLOSED.value}">
								<a id="viewgroupdetails.link.editStatus" href="editCustomerStatusAction.do?method=loadStatus&customerId=<c:out value="${BusinessKey.customerId}"/>&input=group&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}">
									
									<fmt:message key="Group.editStatus">
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
					</fmt:message>
										
								</a>
							</c:if></td>
						</tr>
						<tr>
							<td colspan="2"><font class="fontnormalRedBold"><span id="viewgroupdetails.error.message"><html-el:errors
								bundle="GroupUIResources" /></span></font></td>
						</tr>
						<tr>
							<td class="fontnormalbold"><span class="fontnormal"> <mifoscustom:MifosImage
								id="${BusinessKey.customerStatus.id}" moduleName="customer" />
							<c:out value="${BusinessKey.customerStatus.name}" /> <c:forEach
								var="flagSet" items="${BusinessKey.customerFlags}">
								<span class="fontnormal"> <c:if
									test="${BusinessKey.blackListed}">
									<mifoscustom:MifosImage id="blackListed" moduleName="customer" />
								</c:if> <c:out value="${flagSet.statusFlag.name}" /> </span>
							</c:forEach> <span class="fontnormal"><br>
							<mifos:mifoslabel name="Group.systemId" bundle="GroupUIResources" />
							<c:out value="${BusinessKey.globalCustNum}" /> </span><br>
							<span class="fontnormal"> <mifos:mifoslabel
								name="Group.loanofficer" bundle="GroupUIResources"></mifos:mifoslabel>
							<c:out value="${BusinessKey.personnel.displayName}" /> </span><br>
							<br>
							<span class="fontnormalbold">
								<fmt:message key="Group.clientAssign" >
				  				<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" /></fmt:param>
				  				</fmt:message>
								</span>
							&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<c:if
								test="${BusinessKey.customerStatus.id != CustomerStatus.GROUP_CANCELLED.value and BusinessKey.customerStatus.id != CustomerStatus.GROUP_CLOSED.value}">
								<span class="fontnormal"> <a id="viewgroupdetails.link.add"
									href="clientCustAction.do?method=load&groupFlag=1&parentGroupId=${BusinessKey.customerId}&recordOfficeId=${BusinessKey.office.officeId}&recordLoanOfficerId=${BusinessKey.personnel.personnelId}&randomNUm=${sessionScope.randomNUm}">
								    <fmt:message key="Group.Add" >
				  				    <fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" /></fmt:param>
				  				    </fmt:message>
								</a>
								</span>
							</c:if> <br>
							<span class="fontnormal"> 
							
								
								<fmt:message key="Group.groupdetailViewMsg" >
				  				<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" /></fmt:param>
				  				</fmt:message>
							<br>
							</span>
							<div id="Layer2"
								style="border: 1px solid #CECECE; height:100px; width:250px; overflow: auto; padding:6px; margin-top:5px;">
							<span class="fontnormal"> <!-- Display all client under this group -->
							<c:choose>
								<c:when test="${!empty session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'clients')}">
									<c:choose>
										<c:when
											test="${!empty BusinessKey.personnel and !empty BusinessKey.personnel.personnelId}">
											<c:forEach var="client" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'clients')}">
												<a id="viewgroupdetails.link.client"
													href="clientCustAction.do?method=get&globalCustNum=${client.globalCustNum}&recordOfficeId=${BusinessKey.office.officeId}&recordLoanOfficerId=${BusinessKey.personnel.personnelId}">
												<c:out value="${client.displayName}" /> <c:out
													value="${customerfn:getClientPosition(BusinessKey.customerPositions,client)}" />
												<br>
												</a>
											</c:forEach>
										</c:when>
										<c:otherwise>
											<c:forEach var="client" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'clients')}">
												<a id="viewgroupdetails.link.client"
													href="clientCustAction.do?method=get&globalCustNum=${client.globalCustNum}&recordOfficeId=${UserContext.branchId}&recordLoanOfficerId=${UserContext.id}">
												<c:out value="${client.displayName}" /><%-- <c:out
													value="${customerfn:getClientPositions(requestScope.customerPositions,client)}" />--%>
												<br>
												</a>
											</c:forEach>
										</c:otherwise>
									</c:choose>

								</c:when>
								<c:otherwise>
									
										<fmt:message key="Group.noclientsavailable" >
				  						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" /></fmt:param>
				  						</fmt:message>
							<br>
								</c:otherwise>
							</c:choose> </span><br>
							</div>
							<table width="50%" border="0" cellspacing="0" cellpadding="0">
								<tr>
									<td><img src="pages/framework/images/trans.gif" width="10"
										height="10"></td>
								</tr>
							</table></td>
						</tr>
					</table>
					<table width="96%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td width="36%" class="headingorange"><mifos:mifoslabel
								name="Group.accountinformation" bundle="GroupUIResources"></mifos:mifoslabel>
							</td>
						</tr>
						<!-- bug id 28004. Added if condition to show link to open accounts if only client is active-->
						<c:if
							test="${BusinessKey.customerStatus.id == CustomerStatus.GROUP_ACTIVE.value}">
							<tr align="right">
								<td class="headingorange"><span class="fontnormal"> <mifos:mifoslabel
									name="Group.opennewaccount" bundle="GroupUIResources"></mifos:mifoslabel>

								&nbsp; <c:if test="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'isGroupLoanAllowed') == true}">
									<html-el:link styleId="viewgroupdetails.link.newLoanAccount"
										href="loanAccountAction.do?method=getPrdOfferings&customerId=${BusinessKey.customerId}&recordOfficeId=${UserContext.branchId}&recordLoanOfficerId=${UserContext.id}">
										<mifos:mifoslabel name="${ConfigurationConstants.LOAN}"></mifos:mifoslabel>
									</html-el:link> &nbsp;|&nbsp;
		              </c:if> <html-el:link styleId="viewgroupdetails.link.newSavingsAccount"
									href="savingsAction.do?method=getPrdOfferings&customerId=${BusinessKey.customerId}&recordOfficeId=${UserContext.branchId}&recordLoanOfficerId=${UserContext.id}">
									<mifos:mifoslabel name="${ConfigurationConstants.SAVINGS}"></mifos:mifoslabel>
								</html-el:link> </span></td>
							</tr>
						</c:if>
					</table>


					<c:if test="${!empty session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'groupLoanAccountsInUse')}">
						<table width="96%" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td width="63%" align="left" valign="top"
									class="tableContentLightBlue">
								<table width="100%" border="0" cellspacing="0" cellpadding="0">
									<tr>
										<td width="63%"><span class="fontnormalbold"> <mifos:mifoslabel
											name="${ConfigurationConstants.LOAN}" /> </span> <span
											class="fontnormal"></span></td>
									</tr>
								</table>
								<span class="fontnormal"></span>
								<table width="95%" border="0" align="center" cellpadding="0"
									cellspacing="0">
									<c:forEach items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'groupLoanAccountsInUse')}"
										var="loan">
										<tr>
											<td>
											<table width="100%" border="0" cellspacing="0"
												cellpadding="0">
												<tr>
													<td width="65%"><span class="fontnormal"> <html-el:link styleId="viewgroupdetails.link.viewLoanAccount"
														href="loanAccountAction.do?globalAccountNum=${loan.globalAccountNum}&customerId=${BusinessKey.customerId}&method=get&recordOfficeId=${param.recordOfficeId}&recordLoanOfficerId=${param.recordLoanOfficerId}">
														<c:out value="${loan.loanOffering.prdOfferingName}" />, <mifos:mifoslabel name="Group.acc" bundle="GroupUIResources" /><c:out
															value="${loan.globalAccountNum}" />
													</html-el:link> </span></td>
													<td width="35%"><span class="fontnormal"> <mifoscustom:MifosImage
														id="${loan.accountState.id}" moduleName="accounts.loan" />
													<c:out value="${loan.accountState.name}" /> </span></td>
												</tr>
											</table>
											<c:if
												test="${loan.accountState.id==5 || loan.accountState.id==9}">
												<span class="fontnormal"> <mifos:mifoslabel
													name="loan.outstandingbalance" />: <c:out
													value="${loan.loanSummary.oustandingBalance}" /><br>
												<mifos:mifoslabel name="loan.amount_due" />: <c:out
													value="${loan.totalAmountDue}" /> </span>
											</c:if></td>
										</tr>
										<tr>
											<td><img src="pages/framework/images/trans.gif" width="5"
												height="20"></td>
										</tr>
									</c:forEach>
								</table>
						</table>

						<table width="50%" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td><img src="pages/framework/images/trans.gif" width="10"
									height="10"></td>
							</tr>
						</table>
					</c:if> <c:if
						test="${!empty session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'groupSavingsAccountsInUse')}">
						<table width="96%" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td width="63%" align="left" valign="top"
									class="tableContentLightBlue">
								<table width="100%" border="0" cellspacing="0" cellpadding="0">
									<tr>
										<td width="63%"><span class="fontnormalbold"> <mifos:mifoslabel
											name="${ConfigurationConstants.SAVINGS}" /> </span> <span
											class="fontnormal"></span></td>
									</tr>
								</table>
								<span class="fontnormal"></span>
								<table width="95%" border="0" align="center" cellpadding="0"
									cellspacing="0">
									<c:forEach items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'groupSavingsAccountsInUse')}"
										var="savings">
										<tr>
											<td>
											<table width="100%" border="0" cellspacing="0"
												cellpadding="0">
												<tr>
													<td width="65%"><span class="fontnormal"> <html-el:link styleId="viewgroupdetails.link.viewSavingsAccount"
														href="savingsAction.do?globalAccountNum=${savings.globalAccountNum}&method=get&recordOfficeId=${param.recordOfficeId}&recordLoanOfficerId=${param.recordLoanOfficerId}">
														<c:out value="${savings.savingsOffering.prdOfferingName}" />, <mifos:mifoslabel name="Group.acc" bundle="GroupUIResources" /><c:out
															value="${savings.globalAccountNum}" />
													</html-el:link> </span></td>
													<td width="35%"><span class="fontnormal"> <mifoscustom:MifosImage
														id="${savings.accountState.id}"
														moduleName="accounts.savings" /> <c:out
														value="${savings.accountState.name}" /> </span></td>
												</tr>
											</table>
											<span class="fontnormal"><mifos:mifoslabel
												name="Group.balance" /> <c:out
												value="${savings.savingsBalance}" /> </span></td>
										</tr>
										<tr>
											<td><img src="pages/framework/images/trans.gif" width="5"
												height="20"></td>
										</tr>
									</c:forEach>
								</table>
								</td>
							</tr>

						</table>
						<table width="50%" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<td><img src="pages/framework/images/trans.gif" width="10"
									height="10"></td>
							</tr>
						</table>
					</c:if>

					<table width="96%" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td width="65%" align="left" valign="top"
								class="tableContentLightBlue"><span class="fontnormalbold"> <fmt:message key="Group.charges">
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
					</fmt:message>
							</span>
							<table width="95%" border="0" align="center" cellpadding="0"
								cellspacing="0">
								<tr>
									<td>
									<table width="100%" border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td width="53%"><span class="fontnormal"> <a id="viewgroupdetails.link.viewDetails"
												href="customerAccountAction.do?method=load&globalCustNum=<c:out value="${BusinessKey.globalCustNum}"/>">
											<mifos:mifoslabel name="Group.viewdetails"
												bundle="GroupUIResources" /> </a> </span></td>
										</tr>
										<tr>
											<td><span class="fontnormal"><mifos:mifoslabel
												name="Group.amountdue" bundle="GroupUIResources" /> <c:out
												value="${BusinessKey.customerAccount.nextDueAmount}" /> </span></td>
										</tr>
									</table>
									</td>
								</tr>
								<tr>
									<td><img src="pages/framework/images/trans.gif" width="5"
										height="5"></td>
								</tr>
							</table>
							</td>
						</tr>
					</table>
					<table width="50%" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td><img src="pages/framework/images/trans.gif" width="10"
								height="5"></td>
						</tr>
					</table>

					<table width="96%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td width="38%" align="right" class="fontnormal">
							<span class="fontnormal">
								<c:if test="${BusinessKey.customerStatus.id !=7 && BusinessKey.customerStatus.id !=8}">
									<html-el:link styleId="viewgroupdetails.link.viewAllClosedAccounts" href="custAction.do?method=getClosedAccounts&customerId=${BusinessKey.customerId}&input=group&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}">
										<mifos:mifoslabel name="Group.viewallclosedaccounts" bundle="GroupUIResources"></mifos:mifoslabel>
									</html-el:link>
								</c:if>
							</span>
							</td>
						</tr>
					</table>

					<table width="50%" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td><img src="pages/framework/images/trans.gif" width="10"
								height="10"></td>
						</tr>
					</table>
					<table width="96%" border="0" cellpadding="3" cellspacing="0">
						<%-- Surveys
              <tr>
                <td width="47%" height="23" class="headingorange">
                <mifos:mifoslabel name="Group.surveys"  bundle="GroupUIResources"></mifos:mifoslabel>
                </td>
                <td width="53%" align="right" class="fontnormal8pt">
                <a id="viewgroupdetails.link.attachSurvey" href="#">
	                <mifos:mifoslabel name="Group.attachsurvey"  bundle="GroupUIResources"></mifos:mifoslabel>
                </a></td>
              </tr>
              --%>
						<tr>

						</tr>
					</table>
					<table width="50%" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td><img src="pages/framework/images/trans.gif" width="10"
								height="10"></td>
						</tr>
					</table>

					<table width="96%" border="0" cellpadding="0" cellspacing="0">

						<tr>
							<td width="63%" height="23" class="headingorange"><fmt:message key="Group.groupinformation">
				<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
				</fmt:message>
							</td>
							<td width="37%" align="right" class="fontnormal"><html-el:link styleId="viewgroupdetails.link.editGroupInformation"
								action="groupCustAction.do?method=manage&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}">
								
									<fmt:message key="Group.editInformation">
									<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
									</fmt:message>
							</html-el:link></td>
						</tr>

						<tr>
							<td colspan="2" class="fontnormalbold"><span class="fontnormal">
							
								<fmt:message key="Group.approvaldate">
									<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
									</fmt:message>
								<c:out
								value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,BusinessKey.customerActivationDate)}" />
							<br></td>
						</tr>

						<tr id="Group.ExternalId">
							<td class="fontnormalbold"><span class="fontnormal"> <mifos:mifoslabel
								name="${ConfigurationConstants.EXTERNALID}"
								isColonRequired="Yes" keyhm="Group.ExternalId"
								isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel> <c:out
								value="${BusinessKey.externalId}" /> <br></td>
						</tr>




						<tr>
							<td class="fontnormalbold"><span class="fontnormal"> <mifos:mifoslabel
								name="Group.FormedBy" bundle="GroupUIResources"></mifos:mifoslabel>
							<c:out
								value="${BusinessKey.customerFormedByPersonnel.displayName}" /><br>

							<%-- Collection Sheet
				  <mifos:mifoslabel name="Group.collectionsheettype" bundle="GroupUIResources"></mifos:mifoslabel> Collection Sheet Type 1
				  <c:out value="${requestScope.GroupVO.collectionSheet.collectionSheetName}"/> --%>
							</span> <br>
							<span class="fontnormal"></span></td>
						</tr>

						<tr id="Group.TrainedDate">
							<td class="fontnormalbold"><mifos:mifoslabel
								name="Group.trainingstatus" bundle="GroupUIResources"
								isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel> <br>
							<span class="fontnormal"> <mifos:mifoslabel
								name="Group.trainedon" bundle="GroupUIResources"
								keyhm="Group.TrainedDate"
								isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel> <c:if
								test="${BusinessKey.trained}">
								<c:out
									value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,BusinessKey.trainedDate)}" />
							</c:if> </span><br>
							<br>
							<%--  programs
					<mifos:mifoslabel name="Group.programs" bundle="GroupUIResources"></mifos:mifoslabel>
						<span class="fontnormal"><span class="fontnormal"><br>
								<c:forEach var="program" items="${requestScope.customerPrograms}">
									<c:out value="${program.programName}"/><br>
								</c:forEach>
					 </span></span><br>
					<br> --%></td>
						</tr>

						<tr>
							<td class="fontnormalbold"><mifos:mifoslabel
								name="Group.officialtitlesassigned" bundle="GroupUIResources"></mifos:mifoslabel>
							<span class="fontnormal"><br>
							</span> <span class="fontnormal"><c:forEach var="pos"
								items="${BusinessKey.customerPositions}">
								<c:if test="${! empty pos.customer.customerId}">
									<c:out value="${pos.position.name}" />: <c:out
										value="${pos.customer.displayName}" />
									<br>
								</c:if>
							</c:forEach> </span><br>
							</td>
						</tr>


						<%--Address --%>
						<tr id="Group.Address">
							<td class="fontnormalbold"><mifos:mifoslabel name="Group.address"
								bundle="GroupUIResources" keyhm="Group.Address"
								isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel> <br>
							<c:if
								test="${!empty BusinessKey.customerAddressDetail.address.displayAddress}">
								<span class="fontnormal"> <c:out
									value="${BusinessKey.customerAddressDetail.address.displayAddress}" /><br>
								</span>
							</c:if> <c:if
								test="${!empty BusinessKey.customerAddressDetail.address.city}">
								<span class="fontnormal"> <c:out
									value="${BusinessKey.customerAddressDetail.address.city}" /><br>
								</span>
							</c:if> <c:if
								test="${!empty BusinessKey.customerAddressDetail.address.state}">
								<span class="fontnormal"> <c:out
									value="${BusinessKey.customerAddressDetail.address.state}" /><br>
								</span>
							</c:if> <c:if
								test="${!empty BusinessKey.customerAddressDetail.address.country}">
								<span class="fontnormal"> <c:out
									value="${BusinessKey.customerAddressDetail.address.country}" />
								<br>
								</span>
							</c:if> <c:if
								test="${!empty BusinessKey.customerAddressDetail.address.zip}">
								<span class="fontnormal"> <c:out
									value="${BusinessKey.customerAddressDetail.address.zip}" /><br>
								</span>
							</c:if></td>
						</tr>

						<tr id="Group.PhoneNumber">
							<td class="fontnormalbold"><span class="fontnormal"> <c:if
								test="${!empty BusinessKey.customerAddressDetail.address.phoneNumber}">
								<br>
								<span class="fontnormal"> <mifos:mifoslabel
									name="Group.telephone" bundle="GroupUIResources"
									keyhm="Group.PhoneNumber"
									isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel> <c:out
									value="${BusinessKey.customerAddressDetail.address.phoneNumber}" />
								</span>
								<br>
							</c:if> <br></td>
						</tr>

						<%--Additional information custom fields--%>
						<c:if test="${!empty session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'customFields')}">
						<tr>
							<td height="23" colspan="2" class="fontnormalbold"><br>
							<c:if test="${!empty BusinessKey.customFields}">
								<mifos:mifoslabel name="Group.additionalinformation"
									bundle="GroupUIResources"></mifos:mifoslabel>
							</c:if> <span class="fontnormal"><br>
							<c:forEach var="cfdef" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'customFields')}">
								<c:forEach var="cf" items="${BusinessKey.customFields}">
									<c:if test="${cfdef.fieldId==cf.fieldId}">
										<c:choose>
											<c:when test="${cfdef.fieldType == 3}">
												<mifos:mifoslabel name="${cfdef.lookUpEntity.entityType}"
													bundle="CenterUIResources"></mifos:mifoslabel>:
							    	<span class="fontnormal"><c:out
													value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,cf.fieldValue)}" />
											</c:when>
											<c:otherwise>
												<mifos:mifoslabel name="${cfdef.lookUpEntity.entityType}"
													bundle="CenterUIResources"></mifos:mifoslabel>:
							    	<span class="fontnormal"><c:out value="${cf.fieldValue}" />
											</c:otherwise>
										</c:choose>
										<br>
									</c:if>
								</c:forEach>
							</c:forEach> <br>
							</span></td>
						</tr>
						</c:if>
						<tr>
							<td colspan="2" class="fontnormal"><span class="fontnormal"> <!-- Dynamic links -->
							<c:choose>
								<c:when test="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'CenterHierarchyExist') == true}">
									<table width="100%" border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td width="59%" class="fontnormalbold">
											
												<fmt:message key="Group.centermembership&meetingdetails">
									<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CENTER}" /></fmt:param>
									</fmt:message><span
												class="fontnormalRed">
												
												<br>
											<mifos:mifoslabel name="Group.meetings"
												bundle="GroupUIResources" />&nbsp; <c:out
												value="${customerfn:getMeetingSchedule(BusinessKey.customerMeeting.meeting,sessionScope.UserContext)}" /></span>
											<c:set var="updatedMeetingScheduleMessage" value="${customerfn:getUpdatedMeetingSchedule(BusinessKey.customerMeeting,UserContext)}" />
											<c:if test="${!empty updatedMeetingScheduleMessage}"><span class="fontnormalRed"><br><c:out value="${updatedMeetingScheduleMessage}" /></span></c:if>
											<span class="fontnormal"><br></span>
											</span> <span class="fontnormal"> <c:if
												test="${BusinessKey.customerMeeting.meeting.meetingPlace!=null && !empty BusinessKey.customerMeeting.meeting.meetingPlace}">
												<c:out
													value="${BusinessKey.customerMeeting.meeting.meetingPlace}" />
												<br>
											</c:if> <c:out
												value="${BusinessKey.parentCustomer.displayName}" /><br>
											</span></td>
											<td width="41%" align="right" valign="top" class="fontnormal">
											<html-el:link styleId="viewgroupdetails.link.editCenterMembership" href="groupTransferAction.do?method=loadParents&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}"> 
											
												<fmt:message key="Group.editMembership">
												<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CENTER}" /></fmt:param>
												</fmt:message> 
												</html-el:link></td>
										</tr>
									</table>
								</c:when>
								<c:otherwise>
									<table width="100%" border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td width="59%" class="fontnormalbold"><mifos:mifoslabel
												name="Group.meetingdetails" bundle="GroupUIResources"></mifos:mifoslabel>
											<span class="fontnormalRed"><br>
											<mifos:mifoslabel name="Group.meetings"
												bundle="GroupUIResources" /> <c:out
												value="${customerfn:getMeetingSchedule(BusinessKey.customerMeeting.meeting,sessionScope.UserContext)}" />
											</span> <br>
											<span class="fontnormal"> <c:out
												value="${BusinessKey.customerMeeting.meeting.meetingPlace}" />
											</span></td>
											<td width="41%" align="right" valign="top" class="fontnormal"><br>

											<html-el:link styleId="viewgroupdetails.link.editMeetingSchedule"
												action="meetingAction.do?method=edit&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}&customerLevel=${BusinessKey.customerLevel.id}">
												<mifos:mifoslabel name="Group.editmeetingchedule"
													bundle="GroupUIResources" />
												<br>
											</html-el:link>

											<a id="viewgroupdetails.link.editOfficeMembership" href="groupTransferAction.do?method=loadBranches&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}"> <mifos:mifoslabel
												name="Group.editOfficeMembership" bundle="GroupUIResources" />
											</a></td>
										</tr>
									</table>

								</c:otherwise>
							</c:choose> <br>

							<%--Historical data link--%> <span class="fontnormal"><a id="viewgroupdetails.link.viewHistoricalData"
								href="custHistoricalDataAction.do?method=getHistoricalData&globalCustNum=<c:out value="${BusinessKey.globalCustNum}"/>&currentFlowKey=${requestScope.currentFlowKey}&randomNUm=${sessionScope.randomNUm}">
							<mifos:mifoslabel name="Group.viewhistoricaldata"
								bundle="GroupUIResources"></mifos:mifoslabel> </a> <br>
								<html-el:link styleId="viewgroupdetails.link.viewChangeLog" href="groupCustAction.do?method=loadChangeLog&entityType=Group&entityId=${BusinessKey.customerId}&currentFlowKey=${requestScope.currentFlowKey}">
							<mifos:mifoslabel name="Group.viewchangelog"
								bundle="GroupUIResources"></mifos:mifoslabel> </html-el:link> <br>
							</span></td>
						</tr>
					</table>

					</td>
					<td width="30%" align="left" valign="top" class="paddingleft1">
					<table width="100%" border="0" cellpadding="2" cellspacing="0"
						class="bluetableborder">
						<tr>
							<td class="bluetablehead05"><span class="fontnormalbold"> <mifos:mifoslabel
								name="Group.performancehistory" bundle="GroupUIResources"></mifos:mifoslabel>
							</span></td>
						</tr>

						<tr>
							<td class="paddingL10"><img
								src="pages/framework/images/trans.gif" width="10" height="2"></td>
						</tr>
						<tr>
							<td class="paddingL10"><span class="fontnormal8pt"> 
								
								<fmt:message key="Group.hashof">
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" /></fmt:param>
					</fmt:message> 
								<c:out
								value="${BusinessKey.performanceHistory.activeClientCount}" />
							</span></td>
						</tr>
						<tr>
							<td class="paddingL10"><span class="fontnormal8pt"> 
								<fmt:message key="Group.Amountoflast">
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}" /></fmt:param>
					</fmt:message>:  
								
							<c:out
								value="${BusinessKey.performanceHistory.lastGroupLoanAmount}" /></span></td>
						</tr>
						<tr>
							<td class="paddingL10"><span class="fontnormal8pt"> 
							
								<fmt:message key="Group.Avgindividual">
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}" /></fmt:param>
					</fmt:message>   
								<c:out
								value="${BusinessKey.performanceHistory.avgLoanAmountForMember}" /></span></td>
						</tr>
						<tr>
							<td class="paddingL10"><span class="fontnormal8pt"> 
								<fmt:message key="Group.totalPortfolio">
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}" /></fmt:param>
					</fmt:message>    
								
								<c:out
								value="${BusinessKey.performanceHistory.totalOutStandingLoanAmount}" /></span></td>
						</tr>
						<tr>
							<td class="paddingL10"><span class="fontnormal8pt"> <mifos:mifoslabel
								name="Group.PortfolioAtRisk" bundle="GroupUIResources" /> <c:out
								value="${BusinessKey.performanceHistory.portfolioAtRisk}" /></span></td>
						</tr>
						<tr>
							<td class="paddingL10"><span class="fontnormal8pt"> <mifos:mifoslabel
								name="Group.Total" bundle="GroupUIResources" /> <mifos:mifoslabel
								name="${ConfigurationConstants.SAVINGS}" isColonRequired="yes" />
							<c:out
								value="${BusinessKey.performanceHistory.totalSavingsAmount}" /></span></td>
						</tr>
						<c:if test="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'isGroupLoanAllowed') == true}">
							<tr>
								<td class="paddingL10"><span class="fontnormal8pt"> 
									<fmt:message key="Group.loancyclecounter">
										<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}"/></fmt:param>
									</fmt:message>:
								</span></td>
							</tr>

							<c:forEach items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanCycleCounter')}"
								var="loanCycleCounter">
								<tr>
									<td class="paddingL10"><span class="fontnormal8pt">&nbsp;&nbsp;&nbsp;<c:out
										value="${loanCycleCounter.offeringName}" />: <c:out
										value="${loanCycleCounter.counter}" /></span></td>
								</tr>
							</c:forEach>
						</c:if>
					</table>
					<table width="95%" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td><img src="pages/framework/images/trans.gif" width="7"
								height="8"></td>
						</tr>
					</table>
               <c:if test="${surveyCount}">
		<table width="100%" border="0" cellpadding="2" cellspacing="0" class="bluetableborder">
            <tr>
              <td colspan="2" class="bluetablehead05">
                <span class="fontnormalbold">
                  <mifos:mifoslabel name="Surveys.Surveys" bundle="SurveysUIResources"/>
                </span>
              </td>
            </tr>
            <tr>
              <td colspan="2" class="paddingL10"><img src="pages/framework/images/trans.gif" width="10" height="2"></td>
            </tr>
            <c:forEach items="${requestScope.customerSurveys}" var="surveyInstance">
              <tr>
                <td width="70%" class="paddingL10">
                  <span class="fontnormal8pt">
                    <a id="viewgroupdetails.link.survey" href="surveyInstanceAction.do?method=get&value(instanceId)=${surveyInstance.instanceId}&value(surveyType)=group">
                      <c:out value="${surveyInstance.survey.name}"/>
                    </a>
                  </span>
                </td>
                <td width="30%" align="left" class="paddingL10">
                  <span class="fontnormal8pt">
                    <c:out value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,surveyInstance.dateConducted)}" />
                  </span>
                </td>
              </tr>
            </c:forEach>
            <tr>
              <td colspan="2" align="right" class="paddingleft05">
                <span class="fontnormal8pt">
                  <a id="viewgroupdetails.link.attachSurvey" href="surveyInstanceAction.do?method=choosesurvey&globalNum=${BusinessKey.globalCustNum}&surveyType=group">
                    <mifos:mifoslabel name="Surveys.attachasurvey" bundle="SurveysUIResources"/>
                  </a> <br>
                  <a id="viewgroupdetails.link.viewallsurveys" href="surveysAction.do?method=mainpage">
                    <mifos:mifoslabel name="Surveys.viewallsurveys" bundle="SurveysUIResources"/>
                  </a>
              </td>
            </tr>
          </table>
          <table width="95%" border="0" cellspacing="0" cellpadding="0">
						<tr>
							<td><img src="pages/framework/images/trans.gif" width="7"
								height="8"></td>
						</tr>
		</table>
		</c:if>
					<table width="100%" border="0" cellpadding="2" cellspacing="0"
						class="bluetableborder">
						<tr>
							<td class="bluetablehead05"><span class="fontnormalbold"> <mifos:mifoslabel
								name="Group.recentnotes" bundle="GroupUIResources"></mifos:mifoslabel>
							</span></td>
						</tr>
						<tr>
							<td class="paddingL10"><img
								src="pages/framework/images/trans.gif" width="10" height="2"></td>
						</tr>
						<tr>
							<td class="paddingL10"><c:choose>
								<c:when test="${!empty BusinessKey.recentCustomerNotes}">
									<c:forEach var="note" items="${BusinessKey.recentCustomerNotes}">
										<span class="fontnormal8ptbold"> <c:out
											value="${userdatefn:getUserLocaleDate(sessionScope.UserContext.preferredLocale,note.commentDate)}" />:
										</span>
										<span class="fontnormal8pt"> <c:out value="${note.comment}" />
										-<em><c:out value="${note.personnelName}" /></em><br>
										<br>
										</span>
									</c:forEach>
								</c:when>
								<c:otherwise>
									<span class="fontnormal"> <mifos:mifoslabel
										name="Group.nonotesavailable" bundle="GroupUIResources" /> </span>
								</c:otherwise>
							</c:choose></td>
						</tr>
						<tr>
							<td align="right" class="paddingleft05"><span
								class="fontnormal8pt"> <c:if test="${!empty BusinessKey.customerNotes}">
								<a id="viewgroupdetails.link.seeAllNotes"
									href="customerNotesAction.do?method=search&customerId=<c:out value="${BusinessKey.customerId}"/>&globalAccountNum=<c:out value="${BusinessKey.globalCustNum}"/>&customerName=<c:out value="${BusinessKey.displayName}"/>&securityParamInput=Group&levelId=<c:out value="${BusinessKey.customerLevel.id}"/>&randomNUm=${sessionScope.randomNUm}&currentFlowKey=${requestScope.currentFlowKey}">
								<mifos:mifoslabel name="Group.seeallnotes"
									bundle="GroupUIResources"></mifos:mifoslabel> </a>
								<br>
							</c:if> <a id="viewgroupdetails.link.addNote"
								href="customerNotesAction.do?method=load&customerId=<c:out value="${BusinessKey.customerId}"/>&randomNUm=${sessionScope.randomNUm}&currentFlowKey=${requestScope.currentFlowKey}">
							<mifos:mifoslabel name="Group.addanote" bundle="GroupUIResources"></mifos:mifoslabel>
							</a> </span></td>
						</tr>
					</table>
					</td>
				</tr>
			</table>
			<br>
			<br>
			<html-el:hidden property="customerId"
				value="${BusinessKey.customerId}" />
			<html-el:hidden property="globalCustNum"
				value="${BusinessKey.globalCustNum}" />
			<html-el:hidden property="globalAccountNum"
				value="${BusinessKey.customerAccount.globalAccountNum}" />
			<html-el:hidden property="versionNo"
				value="${requestScope.GroupVO.versionNo}" />
			<html-el:hidden property="displayName"
				value="${BusinessKey.displayName}" />
		</html-el:form>
	</tiles:put>
</tiles:insert>


