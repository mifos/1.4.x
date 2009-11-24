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
<%@ taglib uri="/tags/mifos-html" prefix = "mifos"%>
<%@ taglib uri="/mifos/custom-tags" prefix="customtags"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<tiles:insert definition=".clientsacclayoutsearchmenu">
<tiles:put name="body" type="string">
<span id="page.id" title="ConfirmBranchTransfer" />
<SCRIPT SRC="pages/application/group/js/groupcommon.js"></SCRIPT>
<script language="javascript">

  function goToCancelPage(){
	groupTransferActionForm.action="groupTransferAction.do?method=cancel";
	groupTransferActionForm.submit();
  }
</script>
 <SCRIPT SRC="pages/framework/js/CommonUtilities.js"></SCRIPT>
<fmt:setLocale value='${sessionScope["LOCALE"]}'/>
<fmt:setBundle basename="org.mifos.config.localizedResources.GroupUIResources"/>
<html-el:form action="/groupTransferAction.do?method=transferToBranch"  onsubmit="func_disableSubmitBtn('submitBtn')">
<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
<c:set var="BusinessKey" value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'BusinessKey')}"/>
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
          <td align="left" valign="top" class="paddingL15T15" >
            <table width="95%" border="0" cellpadding="0" cellspacing="0">
              <tr>
                <td width="83%" class="headingorange">
                	<span class="heading">
                		<c:out value="${BusinessKey.displayName}"/>  
	                -</span> 
	                <mifos:mifoslabel name="Group.editOfficeMembership" bundle="GroupUIResources"/>
	                <mifos:mifoslabel name="Group.confirm" bundle="GroupUIResources"></mifos:mifoslabel>
	                
                </td>
              </tr>
             <tr>
   				<td>
<font class="fontnormalRedBold"><span id="confirmbranchtransfer.error.message"><html-el:errors bundle="GroupUIResources"/></span></font>				</td>
			</tr>
              
            </table>
            <br>
            <table width="95%" border="0" cellpadding="3" cellspacing="0">
              <tr class="fontnormal">
                 <td width="94%"><span class="fontnormal">
                 <fmt:message key="Group.sureToTransferTo">
				  <fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
				  </fmt:message>
                 	 &quot;
	    		           <c:out value="${sessionScope.groupTransferActionForm.officeName}"/>
            		    &quot; ?<br>
    		                <br>
        		            <mifos:mifoslabel name="Group.transferbranchMsg3" bundle="GroupUIResources"></mifos:mifoslabel>
				  			<fmt:message key="Group.transferbranchMsg2">
				  			<fmt:param><mifos:mifoslabel name="${ConfigurationConstants.GROUP}" /></fmt:param>
				  			</fmt:message>  
        		             <br>
                		</span>
                 </td>
              </tr>
            </table>
            <table width="95%" border="0" cellpadding="0" cellspacing="0">
              <tr>
                <td align="center" class="blueline">&nbsp;</td>
              </tr>
            </table>
            <br>
            <table width="95%" border="0" cellpadding="0" cellspacing="0">
              <tr>
                <td align="center">
	                <html-el:submit styleId="confirmbranchtransfer.button.submit" property="submitBtn" styleClass="buttn" >
		                <mifos:mifoslabel name="button.submit" bundle="GroupUIResources"></mifos:mifoslabel>
	                </html-el:submit>
        	        	&nbsp; &nbsp;
    	            <html-el:button styleId="confirmbranchtransfer.button.cancel" property="cancelBtn"  styleClass="cancelbuttn"  onclick="goToCancelPage()">
	                    <mifos:mifoslabel name="button.cancel" bundle="GroupUIResources"></mifos:mifoslabel>
                    </html-el:button>
                </td></tr>
            </table>
            <br>
          </td>
        </tr>
      </table>
</html-el:form>
</tiles:put>
</tiles:insert>

