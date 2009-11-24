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
<%@ taglib uri="/mifos/officetags" prefix="office"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<tiles:insert definition=".withoutmenu">
 <tiles:put name="body" type="string">
 <span id="page.id" title="chooseOffice" />


		
<script language="javascript">

  function goToCancelPage(){
	centerCustActionForm.action="centerCustAction.do?method=cancel";
	centerCustActionForm.submit();
  }

</script>
<html-el:form action="centerCustAction.do">
    <table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td align="center" class="heading">&nbsp;</td>
        </tr>
    </table>
      <table width="90%" border="0" align="center" cellpadding="0" cellspacing="0">
          <tr>
            <td class="bluetablehead">  <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td width="33%">
                    <table border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td><img src="pages/framework/images/timeline/bigarrow.gif" width="17" height="17"></td>
                        <td class="timelineboldorange">
                        <mifos:mifoslabel name="Center.Choose" bundle="CenterUIResources"/>
                        <mifos:mifoslabel name="${ConfigurationConstants.BRANCHOFFICE}"/>
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td width="34%" align="center">
                    <table border="0" cellspacing="0" cellpadding="0">
                      <tr>
                        <td><img src="pages/framework/images/timeline/orangearrow.gif" width="17" height="17"></td>
                        <td class="timelineboldorangelight">
                        	<mifos:mifoslabel name="${ConfigurationConstants.CENTER}" />
							<mifos:mifoslabel name="Center.Information" bundle="CenterUIResources" />
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td width="33%" align="right"><table border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td><img src="pages/framework/images/timeline/orangearrow.gif" width="17" height="17"></td>
                      <td class="timelineboldorangelight"><mifos:mifoslabel name="Center.ReviewSubmit" bundle="CenterUIResources"></mifos:mifoslabel></td>
                    </tr>
                  </table></td>
                </tr>
              </table></td>
          </tr>
        </table>
        <table width="90%" border="0" align="center" cellpadding="0" cellspacing="0" class="bluetableborder">
          <tr>
            <td align="left" valign="top" class="paddingleftCreates">
               <table width="93%" border="0" cellpadding="3" cellspacing="0">
                <tr>
                	<td class="headingorange"><span class="heading">
                  	<mifos:mifoslabel name="Center.CreateNew" bundle="CenterUIResources"/>
                  	<mifos:mifoslabel name="${ConfigurationConstants.CENTER}"/>
                  	<c:out value=" "/>
                  	<mifos:mifoslabel name="Center.dash" bundle="CenterUIResources"/>
                    <%-- <mifos:mifoslabel name="client.ClientLabel" bundle="CenterUIResources"></mifos:mifoslabel> --%>

                      </span>
                      <mifos:mifoslabel name="Center.Choose" bundle="CenterUIResources"></mifos:mifoslabel>
                      <mifos:mifoslabel name="${ConfigurationConstants.BRANCHOFFICE}"/>
                      </td>
                </tr>
                <tr>
                  <td class="fontnormal"><mifos:mifoslabel
								name="client.SelectBranchInstructions1"
								bundle="ClientUIResources"></mifos:mifoslabel> <mifos:mifoslabel
								name="${ConfigurationConstants.BRANCHOFFICE}"></mifos:mifoslabel>
							<mifos:mifoslabel name="client.SelectBranchInstructions2"
								bundle="ClientUIResources"></mifos:mifoslabel>
                    <mifos:mifoslabel
								name="Center.CreatePageCancelInstruction"
								bundle="CenterUIResources"></mifos:mifoslabel> </td>

                </tr>
              </table>
              <office:OfficeListTag methodName="load" actionName="centerCustAction.do" flowKey="${requestScope.currentFlowKey}" onlyBranchOffices="yes"/>
              <table width="93%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center" class="blueline">&nbsp;</td>
                </tr>
              </table>              <br>
              <table width="93%" border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center"> &nbsp;
	                  <html-el:button styleId="chooseOffice.button.cancel" property="cancelBtn"  styleClass="cancelbuttn" onclick="goToCancelPage()">
	                    <mifos:mifoslabel name="button.cancel" bundle="CenterUIResources"></mifos:mifoslabel>
    	              </html-el:button>
                  </td></tr>
            </table>
            <br></td>
          </tr>
        </table>
      <br>
<html-el:hidden property="input" value="create"/>
<html-el:hidden property="currentFlowKey" value="${requestScope.currentFlowKey}" />
</html-el:form>
</tiles:put>
</tiles:insert>
