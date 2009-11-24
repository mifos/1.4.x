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
<!-- mainpage.jsp -->

<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>

<tiles:insert definition=".view">
<tiles:put name="body" type="string">
<span id="page.id" title="mainpage" />
<table width="95%" border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="bluetablehead05"><span class="fontnormal8pt">
					<html-el:link href="AdminAction.do?method=load">
						<mifos:mifoslabel name="admin.shortname" bundle="adminUIResources" />
					</html-el:link> / </span> 
					<span class="fontnormal8ptbold">					
					<mifos:mifoslabel name="admin.viewsurvey" bundle="adminUIResources" />					
					</span></td>
				</tr>
			</table>
      <table width="95%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td align="left" valign="top" class="paddingL15T15"><table width="95%" border="0" cellpadding="3" cellspacing="0">
            <tr>
              <td class="headingorange"><span class="headingorange"><mifos:mifoslabel name="Surveys.viewsurveys"/></span></td>

            </tr>
            <tr>
              <td class="fontnormalbold"><span class="fontnormal">
              <mifos:mifoslabel name="Surveys.mainpage_instructions"/>
               <a href="surveysAction.do?method=create_entry">
               <mifos:mifoslabel name="Surveys.definesurvey"/>
                </a><br>
                  <br>
                </span><span class="fontnormalbold"><span class="fontnormalbold"><br>
                </span></span>
                <span class="fontnormalbold"> </span>

<mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.Clientlabel"/>
<table width="90%" border="0" cellspacing="0" cellpadding="0">
  <c:if test="${empty requestScope.clientSurveysList}">
    <tr class="fontnormal">
      <td><em><mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.nosurveysmessage"/></em></td>
    </tr>
  </c:if>
  <c:forEach var="survey" items="${requestScope.clientSurveysList}">
    <tr class="fontnormal">
      <td width="1%"><img src="pages/framework/images/bullet_circle.gif" width="9" height="11"/></td>
      <td width="99%">
        <html-el:link href="surveysAction.do?method=get&value(surveyId)=${survey.surveyId}&randomNUm=${sessionScope.randomNUm}">
          <c:out value="${survey.name}"/>
        </html-el:link>
        <c:if test="${survey.state == 0}">
          <img src="pages/framework/images/status_closedblack.gif" width="8" height="9"> <mifos:mifoslabel name="Surveys.Inactive" bundle="SurveysUIResources"/></span>
        </c:if>
      </td>
    </tr>
  </c:forEach>
</table>

<br/>

<mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.Centerlabel"/>
<table width="90%" border="0" cellspacing="0" cellpadding="0">
  <c:if test="${empty requestScope.centerSurveysList}">
    <tr class="fontnormal">
      <td><em><mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.nosurveysmessage"/></em></td>
    </tr>
  </c:if>
  <c:forEach var="survey" items="${requestScope.centerSurveysList}">
    <tr class="fontnormal">
      <td width="1%"><img src="pages/framework/images/bullet_circle.gif" width="9" height="11"/></td>
      <td width="99%">
        <html-el:link href="surveysAction.do?method=get&value(surveyId)=${survey.surveyId}">
          <c:out value="${survey.name}"/>
        </html-el:link>
        <c:if test="${survey.state == 0}">
          <img src="pages/framework/images/status_closedblack.gif" width="8" height="9"> <mifos:mifoslabel name="Surveys.Inactive" bundle="SurveysUIResources"/></span>
        </c:if>
      </td>
    </tr>
  </c:forEach>
</table>

<br/>

<mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.Grouplabel"/>
<table width="90%" border="0" cellspacing="0" cellpadding="0">
  <c:if test="${empty requestScope.groupSurveysList}">
    <tr class="fontnormal">
      <td><em><mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.nosurveysmessage"/></em></td>
    </tr>
  </c:if>
  <c:forEach var="survey" items="${requestScope.groupSurveysList}">
    <tr class="fontnormal">
      <td width="1%"><img src="pages/framework/images/bullet_circle.gif" width="9" height="11"/></td>
      <td width="99%">
        <html-el:link href="surveysAction.do?method=get&value(surveyId)=${survey.surveyId}">
          <c:out value="${survey.name}"/>
        </html-el:link>
        <c:if test="${survey.state == 0}">
          <img src="pages/framework/images/status_closedblack.gif" width="8" height="9"> <mifos:mifoslabel name="Surveys.Inactive" bundle="SurveysUIResources"/></span>
        </c:if>
      </td>
    </tr>
  </c:forEach>
</table>

<br/>

<mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.Loanlabel"/>
<table width="90%" border="0" cellspacing="0" cellpadding="0">
  <c:if test="${empty requestScope.loanSurveysList}">
    <tr class="fontnormal">
      <td><em><mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.nosurveysmessage"/></em></td>
    </tr>
  </c:if>
  <c:forEach var="survey" items="${requestScope.loanSurveysList}">
    <tr class="fontnormal">
      <td width="1%"><img src="pages/framework/images/bullet_circle.gif" width="9" height="11"/></td>
      <td width="99%">
        <html-el:link href="surveysAction.do?method=get&value(surveyId)=${survey.surveyId}">
          <c:out value="${survey.name}"/>
        </html-el:link>
        <c:if test="${survey.state == 0}">
          <img src="pages/framework/images/status_closedblack.gif" width="8" height="9"> <mifos:mifoslabel name="Surveys.Inactive" bundle="SurveysUIResources"/></span>
        </c:if>
      </td>
    </tr>
  </c:forEach>
</table>

<br/>

<mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.Savingslabel"/>
<table width="90%" border="0" cellspacing="0" cellpadding="0">
  <c:if test="${empty requestScope.savingsSurveysList}">
    <tr class="fontnormal">
      <td><em><mifos:mifoslabel bundle="SurveysUIResources" name="Surveys.nosurveysmessage"/></em></td>
    </tr>
  </c:if>
  <c:forEach var="survey" items="${requestScope.savingsSurveysList}">
    <tr class="fontnormal">
      <td width="1%"><img src="pages/framework/images/bullet_circle.gif" width="9" height="11"/></td>
      <td width="99%">
        <html-el:link href="surveysAction.do?method=get&value(surveyId)=${survey.surveyId}">
          <c:out value="${survey.name}"/>
        </html-el:link>
        <c:if test="${survey.state == 0}">
          <img src="pages/framework/images/status_closedblack.gif" width="8" height="9"> <mifos:mifoslabel name="Surveys.Inactive" bundle="SurveysUIResources"/></span>
        </c:if>
      </td>
    </tr>
  </c:forEach>
</table>

</td>
            </tr>
          </table>            <br>
            </td>

</tiles:put>
</tiles:insert>
