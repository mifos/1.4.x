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
<%@ taglib uri="/mifos/custom-tags" prefix="customtags"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<tiles:insert definition=".clientsacclayoutsearchmenu">
	<tiles:put name="body" type="string">
	<span id="page.id" title="GroupSearchAddMember" />

		<script language="javascript">
  function goToCancelPage(){
	addGroupMembershipForm.submit();
  }
</script>
<fmt:setLocale value='${sessionScope["LOCALE"]}'/>
<fmt:setBundle basename="org.mifos.config.localizedResources.GroupUIResources"/>
		<html-el:form method="post"
			action="addGroupMembershipAction.do?method=cancel">
			<html-el:hidden property="currentFlowKey"
				value="${requestScope.currentFlowKey}" />
		</html-el:form>

		<html-el:form action="groupCustAction.do?method=search">
			<html-el:hidden property="currentFlowKey"
				value="${requestScope.currentFlowKey}" />
			<c:set var="BusinessKey"
				value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'BusinessKey')}" />
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
		      <tr>
			        <td class="bluetablehead05">
				        <span class="fontnormal8pt">
			            	 <customtags:headerLink/>	            	
			            </span>
			        </td>
		      </tr>
		    </table>
			<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td width="70%" align="left" valign="top" class="paddingL15T15">
				    <table width="96%" border="0" cellpadding="3" cellspacing="0">
		                <tr>
		                  <td width="62%" class="headingorange">
							<span class="heading">
							<c:out value="${BusinessKey.displayName}" /> </span> - 
								<fmt:message key="Group.addMembership">
								<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
								</fmt:message>
						  </td>
						</tr>
					</table>
					<table width="96%" border="0" cellpadding="3" cellspacing="0">
						<tr>
							<td class="fontnormalbold"><span class="fontnormal">
							
								<fmt:message key="Group.enterAndSearch">
								<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
								</fmt:message> 
								<fmt:message key="Group.editMag2ReturnToDetail">
								<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" /></fmt:param>
								</fmt:message>
								</span></td>
						</tr>
					</table>
					<table width="96%" border="0" cellpadding="3" cellspacing="0">

						<tr class="fontnormal">
							<td colspan="2" align="right">&nbsp;</td>
						</tr>
						<tr>
							<td colspan="2"><font class="fontnormalRedBold"><span id="group_search_addMember.error.message"><html-el:errors
								bundle="GroupUIResources" /></span></font></td>
						</tr>
						<tr class="fontnormal">
							<td align="right">
								<span id="group_search_addMember.label.search">
								<fmt:message key="Group.groupname">
									<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
								</fmt:message></span>:</td>
							<td><html-el:text styleId="group_search_addMember.input.search" property="searchString" maxlength="200" />
							</td>
						</tr>
						<tr class="fontnormal">
							<td width="20%">&nbsp;</td>
							<td width="80%"><br>
							<html-el:submit styleId="group_search_addMember.button.search" styleClass="buttn" >
								<mifos:mifoslabel name="button.search" bundle="GroupUIResources"></mifos:mifoslabel>
							</html-el:submit> &nbsp; <html-el:button styleId="group_search_addMember.button.cancel" property="cancelBtn"
								styleClass="cancelbuttn"
								onclick="goToCancelPage()">
								<mifos:mifoslabel name="button.cancel" bundle="GroupUIResources"></mifos:mifoslabel>
							</html-el:button> <br>
							<br>
							<br>

					</table>
					<table width="96%" border="0" cellpadding="3" cellspacing="0">
					
					</table>
					
					<br>
					</td>
				</tr>
			</table>
			
			<html-el:hidden property="input" value="GroupSearch_AddClientToGroup" />
		</html-el:form>
	</tiles:put>
</tiles:insert>
