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

<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="/mifos/customtags" prefix="mifoscustom" %>
<%@ taglib uri="/userlocaledate" prefix="userdatefn"%>
<%@taglib uri="/loan/loanfunctions" prefix="loanfn"%>
<%@ taglib uri="/mifos/custom-tags" prefix="customtags"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>

<fmt:setLocale value='${sessionScope["LOCALE"]}'/>
<fmt:setBundle basename="org.mifos.config.localizedResources.ClientUIResources"/>

<tiles:insert definition=".clientsacclayoutsearchmenu">
 <tiles:put name="body" type="string">
 <span id="page.id" title="ViewClientActivity" />

	<script>
		function fun_cancel(){
			customerAccountActionForm.action="customerAccountAction.do?method=load";
			customerAccountActionForm.submit();
		}
</script>
		<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
		<c:set value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'BusinessKey')}"
			   var="BusinessKey" />
        <table width="95%" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td class="bluetablehead05"><span class="fontnormal8pt"> <customtags:headerLink/>
				<html-el:link styleId="viewClientActivity.link.viewCharges" href="customerAccountAction.do?method=load&randomNUm=${sessionScope.randomNUm}">
					<fmt:message key="client.clientcharges">
						<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.CLIENT}" /></fmt:param>
					</fmt:message>
	          	</html-el:link></span>

			</tr>
		</table>
      <table width="95%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td width="70%" height="24" align="left" valign="top" class="paddingL15T15"><table width="96%" border="0" cellpadding="3" cellspacing="0">
              <font class="fontnormalRedBold"><span id="viewClientActivity.error.message"><html-el:errors bundle="CenterUIResources" /></span></font>
              <tr>
	            <td width="70%" class="headingorange">
	            	<span class="heading">
					<c:out value="${param.prdOfferingName}" /> -
	                 </span>
	                 <mifos:mifoslabel name="Customer.Accountstatementasof" bundle="CustomerUIResources"/>
				     <c:out value="${loanfn:getCurrrentDate(sessionScope.UserContext.preferredLocale)}" />
                </td>
                </tr>
              <tr>
                <td align="right" class="headingorange"><img src="images/trans.gif" width="10" height="5"></td>
              </tr>
            </table>
            <c:if test="${param.statusId == 1 || param.statusId == 2 || param.statusId == 3 || param.statusId == 4}">
	           <table width="96%" border="0" cellpadding="0" cellspacing="0">
	              <tr>
	                <td bgcolor="#F0D4A5" style="padding-left:10px; padding-bottom:3px;">
	                	<span class="fontnormalbold">
	                	<mifos:mifoslabel name="Center.ApplyTransaction" bundle="CenterUIResources"/></span>
	                &nbsp;&nbsp;&nbsp;&nbsp;
	                	<html-el:link styleId="viewClientActivity.link.applyPayment" href="applyPaymentAction.do?method=load&searchInput=ClientChargesDetails&statusId=${param.statusId}&globalCustNum=${param.globalCustNum}&prdOfferingName=${param.prdOfferingName}&input=ViewClientCharges&globalAccountNum=${param.globalAccountNum}&accountType=${param.accountType}&accountId=${param.accountId}&randomNUm=${sessionScope.randomNUm}&currentFlowKey=${requestScope.currentFlowKey}">
	                    	<mifos:mifoslabel name="accounts.apply_payment" />
	                    </html-el:link>
	                <c:if test="${param.statusId == 3 || param.statusId == 4}">
	                	&nbsp;&nbsp;&nbsp;&nbsp;
	                    <html-el:link styleId="viewClientActivity.link.applyAdjustment" href="custApplyAdjustment.do?method=loadAdjustment&statusId=${param.statusId}&globalCustNum=${param.globalCustNum}&prdOfferingName=${param.prdOfferingName}&input=ViewClientCharges&globalAccountNum=${param.globalAccountNum}&accountType=${param.accountType}&accountId=${param.accountId}&randomNUm=${sessionScope.randomNUm}&currentFlowKey=${requestScope.currentFlowKey}">
	                    	<mifos:mifoslabel name="Center.ApplyAdjustment" bundle="CenterUIResources"/>
	                    </html-el:link>
	                 </c:if>
		             <c:if test="${param.statusId == 1 || param.statusId == 2 || param.statusId == 3 || param.statusId == 4}">
	                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	                    <html-el:link styleId="viewClientActivity.link.applyCharges" href="applyChargeAction.do?method=load&statusId=${param.statusId}&globalCustNum=${param.globalCustNum}&prdOfferingName=${param.prdOfferingName}&input=ViewClientCharges&globalAccountNum=${param.globalAccountNum}&accountType=${param.accountType}&accountId=${param.accountId}&randomNUm=${sessionScope.randomNUm}&currentFlowKey=${requestScope.currentFlowKey}">
	                    	<mifos:mifoslabel name="Center.ApplyCharges" />
						</html-el:link>
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					</c:if>
						</td>
	              </tr>
	            </table>
	            <br>
	           </c:if>
 			<mifoscustom:mifostabletag moduleName="customer" scope="session" source="customerAccountActivityList" xmlFileName="CustomerRecentActivity.xml" passLocale="true"/>
            <br>
             <table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td align="center">
					   <html-el:button styleId="viewClientActivity.button.return" property="returnToAccountDetailsbutton"
					       onclick="fun_cancel();"
						     styleClass="buttn">
						<mifos:mifoslabel name="label.backtodetailspage" bundle="CustomerUIResources"/>
						</html-el:button>
					</td>
				</tr>
    		</table>
        </tr>
      </table>
		<html:form action="customerAccountAction.do">
        	<html-el:hidden property="globalCustNum" value="${BusinessKey.customer.globalCustNum}" />
     	</html:form>
</tiles:put>
</tiles:insert>
