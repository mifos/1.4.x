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
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@ taglib uri="/mifos/customtags" prefix="mifoscustom"%>
<%@ taglib uri="/loan/loanfunctions" prefix="loanfn"%>
<%@ taglib uri="/tags/date" prefix="date"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<tiles:insert definition=".withoutmenu">
	<tiles:put name="body" type="string">
		<span id="page.id" title="LoanCreationPrdOfferingSelect" />
		<SCRIPT SRC="pages/application/loan/js/CreateLoanAccount.js"></SCRIPT>
        <SCRIPT SRC="pages/framework/js/CommonUtilities.js"></SCRIPT>
        <c:if test="${requestScope.perspective == 'redoLoan'}">
            <SCRIPT>
            function fun_cancel(form)
            {
                location.href="AdminAction.do?method=load";
            }
            </SCRIPT>
        </c:if>
        <fmt:setLocale value='${sessionScope["LOCALE"]}'/>
		<fmt:setBundle basename="org.mifos.config.localizedResources.LoanUIResources"/>
        <html-el:form action="/loanAccountAction.do" >
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td height="470" align="left" valign="top" bgcolor="#FFFFFF">
						<table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
							<tr>
								<td align="center" class="heading">
									&nbsp;
								</td>
							</tr>
						</table>
						<table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
							<tr>
								<td class="bluetablehead">
									<table width="100%" border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td width="25%">
												<table border="0" cellspacing="0" cellpadding="0">
													<tr>
														<td>
															<img src="pages/framework/images/timeline/tick.gif" width="17" height="17">
														</td>
														<td class="timelineboldgray">
															<fmt:message key="loan.selection">
																<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" /></fmt:param>
																<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
															</fmt:message>
														</td>
													</tr>
												</table>
											</td>
											<td width="25%" align="center">
												<table border="0" cellspacing="0" cellpadding="0">
													<tr>
														<td>
															<img src="pages/framework/images/timeline/bigarrow.gif" width="17" height="17">
														</td>
														<td class="timelineboldorange">
															<fmt:message key="loan.entityAccInfo">
																<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}" /></fmt:param>
															</fmt:message>
														</td>
													</tr>
												</table>
											</td>
											<td width="25%" align="right">
												<table border="0" cellspacing="0" cellpadding="0">
													<tr>
														<td>
															<img src="pages/framework/images/timeline/orangearrow.gif" width="17" height="17">
														</td>
														<td class="timelineboldorangelight">
															<mifos:mifoslabel name="loan.review/edit_ins" />
														</td>
													</tr>
												</table>
											</td>
											<td width="25%" align="right">
												<table border="0" cellspacing="0" cellpadding="0">
													<tr>
														<td>
															<img src="pages/framework/images/timeline/orangearrow.gif" width="17" height="17">
														</td>
														<td class="timelineboldorangelight">
															<mifos:mifoslabel name="loan.review&submit" />
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						</table>

						<table width="90%" border="0" align="center" cellpadding="0" cellspacing="0" class="bluetableborder">
							<tr>
								<td width="70%" height="24" align="left" valign="top" class="paddingleftCreates">
									<table width="98%" border="0" cellspacing="0" cellpadding="3">
										<c:if test="${requestScope.perspective == 'redoLoan'}">
                                        <tr>
                                            <td><span class="fontnormalRedBold"><mifos:mifoslabel name="loan.redo_loan_note"/></span></td>
                                        </tr>
                                        </c:if>
                                        <tr>
											<td class="headingorange">
												<span class="heading">
                                                    <c:choose>
                                                    <c:when test="${requestScope.perspective == 'redoLoan'}">
                                                    	<fmt:message key="loan.redoAccount">
                                                    		<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}" /></fmt:param>
                                                    	</fmt:message>
                                                    </c:when>
                                                    <c:otherwise>
                                                    	<fmt:message key="loan.createAccount">
                                                    		<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}" /></fmt:param>
                                                    	</fmt:message>
                                                    </c:otherwise>
                                                    </c:choose>
													&nbsp;-&nbsp;
                                                </span>
                                                <fmt:message key="loan.enterAccInfo">
													<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}" /></fmt:param>
												</fmt:message>
											</td>
										</tr>
										<tr>
											<td class="fontnormal">
												<fmt:message key="loan.selectLoanInstance">
													<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}" /></fmt:param>
												</fmt:message>
												<br>
												<mifos:mifoslabel name="loan.asterisk" mandatory="yes"/>
											</td>

										</tr>
										<tr>
											<td>
												<font class="fontnormalRedBold"> <span id="loancreationprdofferingselect.error.message"><html-el:errors bundle="loanUIResources" /></span> </font>
											</td>
										</tr>
										<tr>
											<td class="fontnormal">
												<br>
												<span class="fontnormalbold"> <mifos:mifoslabel name="loan.acc_owner" isColonRequired="yes"/> </span>
												<c:out value="${sessionScope.loanAccountOwner.displayName}" />
											</td>
										</tr>
									</table>
									<br>
									<table width="93%" border="0" cellpadding="3" cellspacing="0">
										<tr class="fontnormal">
											<td width="30%" align="right" class="fontnormal">
												<span class="mandatorytext"><font color="#FF0000">*</font>
												<fmt:message key="loan.loanInstanceName">
													<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.LOAN}"/></fmt:param>
												</fmt:message>
												:
												</span>
											</td>
											<td width="70%">
												<mifos:select property="prdOfferingId" styleId="loancreationprodofferingselect.select.loanProduct">
													<c:forEach var="loanPrdOffering" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanPrdOfferings')}" >
															<html-el:option value="${loanPrdOffering.prdOfferingId}">${loanPrdOffering.prdOfferingName}</html-el:option>
														</c:forEach>
												</mifos:select>
											</td>
										</tr>
									</table>
									<table width="93%" border="0" cellpadding="0" cellspacing="0">
										<tr>
											<td align="center" class="blueline">
												&nbsp;
											</td>
										</tr>
									</table>
									<br>

									<table width="93%" border="0" cellpadding="0" cellspacing="0">
										<tr>
											<td align="center">
												<html-el:submit styleId="loancreationprdofferingselect.button.continue" property="continueBtn" styleClass="buttn" >
													<mifos:mifoslabel name="loan.continue" />
												</html-el:submit>
												&nbsp;

												<html-el:button styleId="loancreationprdofferingselect.button.cancel" property="cancelButton" onclick="javascript:fun_cancel(this.form)" styleClass="cancelbuttn" >
													<mifos:mifoslabel name="loan.cancel" />
												</html-el:button>
											</td>
										</tr>
									</table>
									<br>
								</td>
							</tr>
						</table>
						<br>
					</td>
				</tr>
			</table>
			<html-el:hidden property="method" value="load" />
			<html-el:hidden property="input" value="loan" />
            <html-el:hidden property="perspective" value="${requestScope.perspective}" />
            <c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanAccountOwner')}" var="customer" />
			<html-el:hidden property="customerId" value="${customer.customerId}" />
			<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
		</html-el:form>
	</tiles:put>
</tiles:insert>
