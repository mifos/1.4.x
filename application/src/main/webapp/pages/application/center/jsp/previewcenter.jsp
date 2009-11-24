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

<%@taglib uri="/tags/mifos-html" prefix = "mifos"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-bean-el" prefix="bean-el"%>
<%@ taglib uri="/userlocaledate" prefix="userdatefn"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@ taglib uri="/customer/customerfunctions" prefix="customerfn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<tiles:insert definition=".withoutmenu">
<tiles:put name="body" type="string">
<span id="page.id" title="previewcenter" />

<fmt:setLocale value='${sessionScope["LOCALE"]}'/>
		<fmt:setBundle basename="org.mifos.config.localizedResources.CenterUIResources"/>
<SCRIPT SRC="pages/framework/js/CommonUtilities.js"></SCRIPT>
<script language="javascript" >
function goToEditPage(){
	centerCustActionForm.action="centerCustAction.do?method=previous";
	centerCustActionForm.submit();
  }
  function goToCancelPage(){
	centerCustActionForm.action="centerCustAction.do?method=cancel";
	centerCustActionForm.submit();
  }
</script>
<html-el:form action="centerCustAction.do?method=create" onsubmit="func_disableSubmitBtn('submitButton');">
<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'customerMeeting')}"  var="centerMeeting" />
<html-el:hidden property="input" value="create"/>
<!-- Body begins -->
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td height="350" align="left" valign="top" bgcolor="#FFFFFF">
      <table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
           <td align="center" class="heading">&nbsp;</td>
        </tr>
      </table>
      <!-- Pipeline Information -->
      <table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
        <tr>
          <td class="bluetablehead">
          	<table width="100%" border="0" cellspacing="0" cellpadding="0">
	            <tr>
	              <td width="33%"><table border="0" cellspacing="0" cellpadding="0">
	                  <tr>
	                    <td><img src="pages/framework/images/timeline/tick.gif" width="17" height="17"></td>
	                   <td class="timelineboldgray">
	                    <mifos:mifoslabel name="Center.Choose" bundle="CenterUIResources"/>
                        <mifos:mifoslabel name="${ConfigurationConstants.BRANCHOFFICE}"/></td>
	                  </tr>
	                </table>
	              </td>
	              <td width="34%" align="center">
	                <table border="0" cellspacing="0" cellpadding="0">
	                  <tr>
	                    <td><img src="pages/framework/images/timeline/tick.gif" width="17" height="17"></td>
	                     <td class="timelineboldgray"><mifos:mifoslabel name="${ConfigurationConstants.CENTER}"/> <mifos:mifoslabel name="Center.Information" bundle="CenterUIResources"/></td>

	                  </tr>
	                </table>
	              </td>
	              <td width="33%" align="right">
	                <table border="0" cellspacing="0" cellpadding="0">
	                  <tr>
	                    <td><img src="pages/framework/images/timeline/bigarrow.gif" width="17" height="17"></td>
	                    <td class="timelineboldorange"><mifos:mifoslabel name="Center.ReviewSubmit" bundle="CenterUIResources"></mifos:mifoslabel></td>
	                  </tr>
	                </table>
	              </td>
	            </tr>
          	</table>
         </td>
       </tr>
      </table>
      <!-- Pipeline Information Ends-->

      <!-- Preview information begins -->
      <table width="90%" border="0" align="center" cellpadding="0" cellspacing="0" class="bluetableborder">
          <tr>
            <td align="left" valign="top" class="paddingleftCreates">
              <!-- Preview page instruction -->
              <table width="93%" border="0" cellpadding="3" cellspacing="0">
                <tr>

                  <td class="headingorange">
                  <span class="heading">                  	<mifos:mifoslabel name="Center.CreateNew" bundle="CenterUIResources"/>
                  	<mifos:mifoslabel name="${ConfigurationConstants.CENTER}"/>
                  	<c:out value=" "/>
                  	<mifos:mifoslabel name="Center.dash" bundle="CenterUIResources"/>

                  </span>
                  <mifos:mifoslabel name="Center.ReviewSubmit" bundle="CenterUIResources"></mifos:mifoslabel></td>
                </tr>
                <tr>
                  <td class="fontnormal"><mifos:mifoslabel name="Center.CreatePreviewPageInstruction" bundle="CenterUIResources"></mifos:mifoslabel>
                    &nbsp; <mifos:mifoslabel name="Center.CreatePageCancelInstruction" bundle="CenterUIResources"></mifos:mifoslabel>
                  </td>
                </tr>
              </table>
               <!-- Preview page instruction ends-->

              <!-- Center information entered on the create page -->
              <table width="93%" border="0" cellspacing="0" cellpadding="3">
              <font class="fontnormalRedBold"><span id="previewcenter.error.message"><html-el:errors bundle="CenterUIResources" /></span> </font>
                <tr>
                  <td class="fontnormal">
                  	<span class="fontnormalbold">                  		<mifos:mifoslabel name="${ConfigurationConstants.BRANCHOFFICE}"/><c:out value=" "/>
                  		<mifos:mifoslabel name="Center.Selected" bundle="CenterUIResources" isColonRequired="yes"/>
</span>
                  	 <c:out value="${sessionScope.centerCustActionForm.officeName}"/></td>
                </tr>
              </table>

              <table width="93%" border="0" cellpadding="0" cellspacing="0">
                 <tr>
                  <td width="100%" height="23" class="fontnormalboldorange">
                  	<mifos:mifoslabel name="${ConfigurationConstants.CENTER}"/><c:out value=" "/><mifos:mifoslabel name="Center.Information" bundle="CenterUIResources"/>
                  </td>
                </tr>

                <tr>
                  <td class="fontnormal">
                  	<span class="fontnormalbold">
                  	<mifos:mifoslabel name="Center.Name" bundle="CenterUIResources" ></mifos:mifoslabel>
                  	<span class="fontnormal"><c:out value="${sessionScope.centerCustActionForm.displayName}"/> <br></span>
                  <td>
                </tr>

                <tr>
                <td class="fontnormal">
                <span class="fontnormalbold">
                    <mifos:mifoslabel name="Center.LoanOfficer" bundle="CenterUIResources"></mifos:mifoslabel>
					<span class="fontnormal">
					<c:forEach var="LO" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanOfficers')}">
						<c:if test = "${LO.personnelId == sessionScope.centerCustActionForm.loanOfficerId}">
							<c:out value="${LO.displayName}"/><br>
						</c:if>
					</c:forEach>
					</span>
				</td>
				</tr>

				<tr>
                <td class="fontnormal">
                <span class="fontnormalbold">
					<mifos:mifoslabel name="Center.MeetingSchedule" bundle="CenterUIResources"></mifos:mifoslabel>
                    <span class="fontnormal"><c:out value="${customerfn:getMeetingSchedule(centerMeeting,UserContext)}"/><br></span>
                 </td>
                 </tr>

                <tr>
                <td class="fontnormal">
                <span class="fontnormalbold">
                <mifos:mifoslabel name="Center.LocationOfMeeting" bundle="CenterUIResources"></mifos:mifoslabel>
                    <span class="fontnormal"> <c:out value="${centerMeeting.meetingPlace}"/> <br></span>
                 </td>
                 </tr>

                <tr id="Center.ExternalId">
                <td class="fontnormal">
                <span class="fontnormalbold">
                 	<mifos:mifoslabel name="${ConfigurationConstants.EXTERNALID}" keyhm="Center.ExternalId" isColonRequired="yes" isManadatoryIndicationNotRequired="yes"/>
					<span class="fontnormal"> <c:out value="${sessionScope.centerCustActionForm.externalId}"/> <br></span>
				</td>
				</tr>

				<tr>
                <td class="fontnormal">
                <span class="fontnormalbold">
					<mifos:mifoslabel name="Center.MfiJoiningDate" bundle="CenterUIResources"></mifos:mifoslabel>:
					<span class="fontnormal"> <c:out value="${sessionScope.centerCustActionForm.mfiJoiningDate}"/>
					<br></span><br></span>
				</td>
				</tr>

				<tr id="Center.Address">
                <td class="fontnormal">
					<span class="fontnormalbold">
					<mifos:mifoslabel name="Center.Address" bundle="CenterUIResources" keyhm="Center.Address" isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel>
					<span class="fontnormal"><br></span>
				</td>
				</tr>

				<tr id="Center.Address">
                <td class="fontnormal">
                <span class="fontnormalbold">
					<span class="fontnormal"><c:out value="${sessionScope.centerCustActionForm.address.displayAddress}"/><br></span>
				</td>
				</tr>

				<tr id="Center.City">
                <td class="fontnormal">
                <span class="fontnormalbold">
					<mifos:mifoslabel name="${ConfigurationConstants.CITY}" keyhm="Center.City" isColonRequired="yes" isManadatoryIndicationNotRequired="yes"/>
					<span class="fontnormal"> <c:out value="${sessionScope.centerCustActionForm.address.city}"/> <br></span>
				</td>
				</tr>

				<tr id="Center.State">
                <td class="fontnormal">
                <span class="fontnormalbold">
					<mifos:mifoslabel name="${ConfigurationConstants.STATE}" keyhm="Center.State" isColonRequired="yes" isManadatoryIndicationNotRequired="yes"/>
					<span class="fontnormal"> <c:out value="${sessionScope.centerCustActionForm.address.state}"/> <br></span>
				</td>
				</tr>

				<tr id="Center.Country">
                <td class="fontnormal">
                <span class="fontnormalbold">
					<mifos:mifoslabel name="Center.Country" keyhm="Center.Country" bundle="CenterUIResources" isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel>
					<span class="fontnormal"> <c:out value="${sessionScope.centerCustActionForm.address.country}"/><br></span>
				</td>
				</tr>

				<tr id="Center.PostalCode">
                <td class="fontnormal">
                <span class="fontnormalbold">
					<mifos:mifoslabel name="${ConfigurationConstants.POSTAL_CODE}" keyhm="Center.PostalCode" isColonRequired="yes" isManadatoryIndicationNotRequired="yes"/>
					<span class="fontnormal"><c:out value="${sessionScope.centerCustActionForm.address.zip}"/> </span>
					<br>
                </td>
				</tr>

				<tr id="Center.PhoneNumber">
                <td class="fontnormal">
                <span class="fontnormalbold"><br>
					<mifos:mifoslabel name="Center.Telephone" bundle="CenterUIResources" keyhm="Center.PhoneNumber" isManadatoryIndicationNotRequired="yes"></mifos:mifoslabel>
					<span class="fontnormal"><c:out value="${sessionScope.centerCustActionForm.address.phoneNumber}"/> </span>
					</span><br>
				</td>
				</tr>

				<!--CustomField addition -->
				<c:if test="${!empty session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'customFields')}">
				<tr>
                <td class="fontnormal">
					<span class="fontnormalbold">
					<br>
					<mifos:mifoslabel name="Center.AdditionalInformationHeading" bundle="CenterUIResources"></mifos:mifoslabel>
					<br>
					 <c:forEach var="cf" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'customFields')}">
						 <c:forEach var="customField" items="${sessionScope.centerCustActionForm.customFields}">
							<c:if test="${cf.fieldId==customField.fieldId}">
								<mifos:mifoslabel name="${cf.lookUpEntity.entityType}" bundle="CenterUIResources"></mifos:mifoslabel>:
					         	<span class="fontnormal"><c:out value="${customField.fieldValue}"/></span><br>
							</c:if>
						</c:forEach>
    				  </c:forEach>
					</span>
				</td>
				</tr>
				</c:if>
				<!-- Fee addition -->
				<tr>
                <td class="fontnormal">
					<span class="fontnormalbold">
					 <br>
		             <c:if test="${!empty sessionScope.centerCustActionForm.defaultFees}">
		             <span class="fontnormalbold"><mifos:mifoslabel name="Center.AdministrativeFeesHeading" bundle="CenterUIResources"/><br>
						<c:forEach var="adminFee" items="${sessionScope.centerCustActionForm.defaultFees}">
							<c:if test="${adminFee.removed == false}">
						  		 <c:out value="${adminFee.feeName}"/>:
						   		<span class="fontnormal">
						   			<c:out value="${adminFee.amount}"/>
						   			<mifos:mifoslabel name="Center.Periodicity" bundle="CenterUIResources"/>
							   		<c:choose>
										<c:when test="${adminFee.periodic}">
											<c:out value="${adminFee.feeSchedule}"/>
										</c:when>
										<c:otherwise>
											<mifos:mifoslabel name="Fees.onetime"/>
										</c:otherwise>
									</c:choose>
								</span><br>
							</c:if>
						</c:forEach>
					</span>
					</c:if>
					</td>
					</tr>


					<!-- Administrative fees end -->
					<!-- Fee Types --->
					<tr>
                <td class="fontnormal">
					<span class="fontnormalbold">
					<br>
					<span class="fontnormalbold"><mifos:mifoslabel name="Center.AdditionalFees" bundle="CenterUIResources" /><br>
					<c:forEach var="fee" items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'additionalFeeList')}" >
						<c:forEach var="selectedFee" items="${sessionScope.centerCustActionForm.additionalFees}" >
							<c:if test="${fee.feeId == selectedFee.feeId}">
			           	  		<c:out value="${fee.feeName}"/>:
								<span class="fontnormal"><span class="fontnormal">
								<c:out value="${selectedFee.amount}"/>
								<mifos:mifoslabel name="Center.Periodicity" bundle="CenterUIResources"/>
								<c:choose>
									<c:when test="${fee.periodic == true}">
										<c:out value="${fee.feeSchedule}"/>
									</c:when>
									<c:otherwise>
										<mifos:mifoslabel name="Fees.onetime"/>
									</c:otherwise>
								</c:choose></span><br></span>
							</c:if>
						</c:forEach>
					</c:forEach>

                          <br>
					<!-- Fee Type end -->
					</span>
					</td>

				  </tr>
				  <tr>
				  <td>
					<!-- FEe end -->
					<!-- Edit Button -->
					<html-el:button styleId="previewcenter.button.edit" onclick="goToEditPage()" property = "editButton" styleClass="insidebuttn" >
					<mifos:mifoslabel name="Center.Edit" bundle="CenterUIResources"/><c:out value=" "/><mifos:mifoslabel name="${ConfigurationConstants.CENTER}"/><c:out value=" "/><mifos:mifoslabel name="Center.Information" bundle="CenterUIResources"/>

					</html-el:button>
                  </td>
                </tr>

              </table>


              <!-- Submit and cancel buttons -->
              <table width="93%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center" class="blueline">&nbsp;</td>
                </tr>
              </table>              <br>
              <table width="93%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center">&nbsp;
                  <html-el:submit styleId="previewcenter.button.submit" styleClass="buttn" property = "submitButton"><mifos:mifoslabel name="button.submit" bundle="CenterUIResources"></mifos:mifoslabel></html-el:submit>
                  &nbsp;
				  &nbsp;
				  <html-el:button styleId="previewcenter.button.cancel" onclick="goToCancelPage();" property = "cancelButton" styleClass="cancelbuttn"><mifos:mifoslabel name="button.cancel" bundle="CenterUIResources"></mifos:mifoslabel></html-el:button>

                 </td>
                 </tr>
            </table>
            <!-- Submit and cancel buttons end -->
            <br></td>
			<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
          </tr>
        </table>
      <br></td>
  </tr>
</table>
</html-el:form>
</tiles:put>
</tiles:insert>

