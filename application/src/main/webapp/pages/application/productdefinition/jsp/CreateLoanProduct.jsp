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
<!-- CreateLoanProduct.jsp -->

<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@taglib uri="/tags/date" prefix="date"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<fmt:setLocale value='${sessionScope["LOCALE"]}' />
<fmt:setBundle basename="org.mifos.config.localizedResources.ProductDefinitionResources" />


<tiles:insert definition=".create">
	<tiles:put name="body" type="string">
        <span id="page.id" title="CreateLoanProduct" />
		<script>
		<!--
			function showMeetingFrequency(){
				if (document.getElementsByName("freqOfInstallments")[1].checked == true){
					document.getElementById("week").style.display = "none";
					document.getElementById("month").style.display = "block";
				}
				else {
					document.getElementById("week").style.display = "block";
					document.getElementsByName("freqOfInstallments")[0].checked = true;
					document.getElementById("month").style.display = "none";
				}
			}
			function fnCancel(form) {
				form.method.value="cancelCreate";
				form.action="loanproductaction.do";
				form.submit();
			}
			
			function fnIntDesbr() {
				if(document.getElementsByName("intDedDisbursementFlag")[0].checked==true) {
					document.getElementsByName("gracePeriodType")[0].disabled=true;
					document.getElementsByName("gracePeriodType")[0].selectedIndex=0;
					document.getElementsByName("gracePeriodDuration")[0].value="";
					document.getElementsByName("gracePeriodDuration")[0].disabled=true;
				}
				else {
					document.getElementsByName("gracePeriodType")[0].disabled=false;
					document.getElementsByName("gracePeriodDuration")[0].disabled=false;
				}
			}
			function fnGracePeriod() {
				if(document.getElementsByName("gracePeriodType")[0].selectedIndex==0 ||
					document.getElementsByName("gracePeriodType")[0].value==1) {
					document.getElementsByName("gracePeriodDuration")[0].value="";
					document.getElementsByName("gracePeriodDuration")[0].disabled=true;
				}else {
					document.getElementsByName("gracePeriodDuration")[0].disabled=false;
				}
			}
		
			function checkRow(){
			if (document.loanproductactionform.loanAmtCalcType[0].checked == true){
				document.getElementById("option0").style.display = "block";
				document.getElementById("option1").style.display = "none";
				document.getElementById("option2").style.display = "none";
				}
			else if (document.loanproductactionform.loanAmtCalcType[1].checked == true){
				document.getElementById("option0").style.display = "none";
				document.getElementById("option1").style.display = "block";
				document.getElementById("option2").style.display = "none";
				}
				
			else if (document.loanproductactionform.loanAmtCalcType[2].checked == true){
				document.getElementById("option0").style.display = "none";
				document.getElementById("option1").style.display = "none";
				document.getElementById("option2").style.display = "block";		
				}				
			}
			
			function checkType(){
			if (document.loanproductactionform.calcInstallmentType[0].checked == true){
				document.getElementById("install0").style.display = "block";
				document.getElementById("install1").style.display = "none";
				document.getElementById("install2").style.display = "none";
				}
			else if (document.loanproductactionform.calcInstallmentType[1].checked == true){
				document.getElementById("install0").style.display = "none";
				document.getElementById("install1").style.display = "block";
				document.getElementById("install2").style.display = "none";			
				//if(document.getElementsByName("freqOfInstallments")[1].checked)
				//{
						//installments_as_month_value();
				//}
				//else
				//{
						//installments_as_week_value();
				//}
				}				
			else if (document.loanproductactionform.calcInstallmentType[2].checked == true){
				document.getElementById("install0").style.display = "none";
				document.getElementById("install1").style.display = "none";
				document.getElementById("install2").style.display = "block";		
				}				
			}
		
			function changeValue(event, editbox, endvalue, rownum)
			{
				
				if(!(endvalue=="")){
				if (FnCheckNumber(event,'','',editbox) == false)
					return false;
				for(var i=rownum;i<rownum+1;i++)
					{					
						eval("document.loanproductactionform.startRangeLoanAmt"+(i+1)).value = parseFloat(endvalue) +1;	
					}	
					}
			}	
			
			function changeInstallmentValue(event, editbox, endvalue, rownum)
			{
			   
				if(!(endvalue=="")){
				if (FnCheckNumber(event,'','',editbox) == false)
					return false;
				for(var i=rownum;i<rownum+1;i++)
					{					
						eval("document.loanproductactionform.startInstallmentRange"+(i+1)).value = parseFloat(endvalue) +1;			
						
					}	
					}						
			}
			
			function showLoanAmountType() {
		    var loanAmtCalcType = document.getElementsByName("loanAmtCalcType");
		    if (loanAmtCalcType[1].checked == false && loanAmtCalcType[2].checked == false) {
		        loanAmtCalcType[0].checked = true;
		    }
		    }
			function showInstallType() {
		    var calcInstallmentType = document.getElementsByName("calcInstallmentType");
		    if (calcInstallmentType[1].checked == false && calcInstallmentType[2].checked == false) {
		        calcInstallmentType[0].checked = true;
		    }
		    
			}
		//-->
		</script>
		<script src="pages/framework/js/date.js"></script>
		<script src="pages/framework/js/func.js"></script>
		<html-el:form action="/loanproductaction"
			onsubmit="return (validateMyForm(startDate,startDateFormat,startDateYY) &&
				validateMyForm(endDate,endDateFormat,endDateYY))"
			focus="prdOfferingName">
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td height="350" align="left" valign="top" bgcolor="#FFFFFF">
					<table width="90%" border="0" align="center" cellpadding="0"
						cellspacing="0">
						<tr>
							<td align="center" class="heading">&nbsp;</td>
						</tr>
					</table>
					<table width="90%" border="0" align="center" cellpadding="0"
						cellspacing="0">
						<tr>
							<td class="bluetablehead">
							<table width="100%" border="0" cellspacing="0" cellpadding="0">
								<tr>
									<td width="27%">
									<table border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td><img
												src=" pages/framework/images/timeline/bigarrow.gif"
												width="17" height="17"></td>
											<td class="timelineboldorange"><fmt:message
												key="product.loanProductInfo">
												<fmt:param>
													<mifos:mifoslabel name="${ConfigurationConstants.LOAN}"
														bundle="ProductDefUIResources" />
												</fmt:param>
											</fmt:message></td>
										</tr>
									</table>
									</td>
									<td width="73%" align="right">
									<table border="0" cellspacing="0" cellpadding="0">
										<tr>
											<td><img
												src="pages/framework/images/timeline/orangearrow.gif"
												width="17" height="17"></td>
											<td class="timelineboldorangelight"><mifos:mifoslabel
												name="product.review" bundle="ProductDefUIResources" /></td>
										</tr>
									</table>
									</td>
								</tr>
							</table>
							</td>
						</tr>
					</table>
					<table width="90%" border="0" align="center" cellpadding="0"
						cellspacing="0" class="bluetableborder">
						<tr>
							<td align="left" valign="top" class="paddingleftCreates">
							<table width="93%" border="0" cellpadding="3" cellspacing="0">
								<tr>
									<td class="headingorange"><span class="heading" id="createLoanProduct.heading"> <fmt:message
										key="product.addNewLoanProduct">
										<fmt:param>
											<mifos:mifoslabel name="${ConfigurationConstants.LOAN}"
												bundle="ProductDefUIResources" />
										</fmt:param>
									</fmt:message> - </span> <fmt:message key="product.enterLoanProductInfo">
										<fmt:param>
											<mifos:mifoslabel name="${ConfigurationConstants.LOAN}"
												bundle="ProductDefUIResources" />
										</fmt:param>
									</fmt:message></td>
								</tr>
								<tr>
									<td class="fontnormal"><mifos:mifoslabel
										name="product.completeFieldsInstructional"
										bundle="ProductDefUIResources" /> <br>
									<mifos:mifoslabel name="product.fieldsrequired" mandatory="yes"
										bundle="ProductDefUIResources" /></td>
								</tr>
							</table>
							<br>
							<font class="fontnormalRedBold"><html-el:errors
								bundle="ProductDefUIResources" /></font>
							<table width="93%" border="0" cellpadding="3" cellspacing="0">
								<tr>
									<td colspan="2" class="fontnormalbold"><fmt:message
										key="product.loanProductDetails">
										<fmt:param>
											<mifos:mifoslabel name="${ConfigurationConstants.LOAN}"
												bundle="ProductDefUIResources" />
										</fmt:param>
									</fmt:message><br>
									<br>
									</td>
								</tr>
								<tr class="fontnormal">
									<td width="30%" align="right"><span
										id=createLoanProduct.label.prdOfferingName><mifos:mifoslabel
										name="product.prodinstname" mandatory="yes"
										bundle="ProductDefUIResources" /> </span>:</td>
									<td width="70%" valign="top"><mifos:mifosalphanumtext
										styleId= "createLoanProduct.input.prdOffering" property="prdOfferingName" /></td>
								</tr>
								<tr class="fontnormal">
									<td align="right"><mifos:mifoslabel
										name="product.shortname" mandatory="yes"
										bundle="ProductDefUIResources" /> :</td>
									<td valign="top"><mifos:mifosalphanumtext
										styleId= "createLoanProduct.input.prdOfferingShortName" property="prdOfferingShortName" maxlength="4" size="4" /></td>
								</tr>
								<tr class="fontnormal">
									<td align="right" valign="top"><span class="mandatorytext"></span>
									<mifos:mifoslabel name="product.desc"
										bundle="ProductDefUIResources" /> :</td>
									<td valign="top"><html-el:textarea styleId= "createLoanProduct.input.description" property="description"
										style="width:320px; height:110px;">
									</html-el:textarea></td>
								</tr>
								<tr class="fontnormal">
									<td align="right"><mifos:mifoslabel name="product.prodcat"
										mandatory="yes" bundle="ProductDefUIResources" /> :</td>
									<td valign="top"><mifos:select property="prdCategory">
										<c:forEach
											items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanProductCategoryList')}"
											var="category">
											<html-el:option value="${category.productCategoryID}">${category.productCategoryName}</html-el:option>
										</c:forEach>
									</mifos:select></td>
								</tr>
								<tr class="fontnormal">
									<td align="right"><mifos:mifoslabel
										name="product.startdate" mandatory="yes"
										bundle="ProductDefUIResources" /> :</td>
									<td valign="top"><date:datetag property="startDate" /></td>
								</tr>
								<tr class="fontnormal">
									<td align="right"><span class="mandatorytext"></span> <mifos:mifoslabel
										name="product.enddate" bundle="ProductDefUIResources" /> :</td>
									<td valign="top"><date:datetag property="endDate" /></td>
								</tr>
								<tr class="fontnormal">
									<td align="right"><mifos:mifoslabel mandatory="yes"
										name="product.applfor" bundle="ProductDefUIResources" /> :</td>
									<td valign="top"><mifos:select
										property="prdApplicableMaster">
										<c:forEach
											items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanApplForList')}"
											var="prdAppl">
											<html-el:option value="${prdAppl.id}">${prdAppl.name}</html-el:option>
										</c:forEach>
									</mifos:select></td>
								</tr>
								<tr class="fontnormal">
									<td align="right"><fmt:message
										key="product.inclInLoanCycleCounter">
										<fmt:param>
											<mifos:mifoslabel name="${ConfigurationConstants.LOAN}"
												bundle="ProductDefUIResources" />
										</fmt:param>
									</fmt:message> :</td>
									<td valign="top"><html-el:checkbox styleId="createLoanProduct.checkbox.loanCounter" property="loanCounter"
										value="1" /></td>
								</tr>

								<!--<tr class="fontnormal">
									<td align="right">
									<span class="mandatorytext"> <font color="#FF0000">*</font></span>
									<fmt:message key="product.maxAmount">
									<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.LOAN}"
										bundle="ProductDefUIResources" /></fmt:param>
									</fmt:message>:</td>
									<td valign="top"><mifos:decimalinput
										property="maxLoanAmount" /></td>
								</tr>

								<tr class="fontnormal">
									<td align="right">
									<span class="mandatorytext"> <font color="#FF0000">*</font></span>
									<fmt:message key="product.minAmount">
									<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.LOAN}"
										bundle="ProductDefUIResources" /></fmt:param>
									</fmt:message>:</td>
									<td valign="top"><mifos:decimalinput
										property="minLoanAmount" /></td>
								</tr>

								<tr class="fontnormal">
									<td align="right">
									<fmt:message key="product.defaultAmount">
									<fmt:param><mifos:mifoslabel
										name="${ConfigurationConstants.LOAN}"
										bundle="ProductDefUIResources" /></fmt:param>
									</fmt:message>:</td>
									<td valign="top"><mifos:decimalinput
										property="defaultLoanAmount" /></td>
								</tr>
							-->
								<table width="93%" border="0" cellpadding="3" cellspacing="0">
									<tr>
										<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
											name="product.loanamount" bundle="ProductDefUIResources" /><br>
										<br>
										</td>
									</tr>

									<tr class="fontnormal">
										<td width="30%" align="right"><mifos:mifoslabel
											name="product.calcloanamount" mandatory="yes"
											bundle="ProductDefUIResources" />:</td>
										<td width="70%" valign="top"><html-el:radio styleId="createLoanProduct.radio.calcLoanAmountsameForAllLoans"
											property="loanAmtCalcType" value="1" onclick="checkRow();" />
										<mifos:mifoslabel name="product.sameforallloans"
											bundle="ProductDefUIResources" /> &nbsp;&nbsp;&nbsp; <html-el:radio styleId="createLoanProduct.radio.calcLoanAmountByLastLoanAmount"
											property="loanAmtCalcType" value="2" onclick="checkRow();" />
										<mifos:mifoslabel name="product.bylastloanamount"
											bundle="ProductDefUIResources" /> &nbsp;&nbsp;&nbsp; <html-el:radio styleId="createLoanProduct.radio.calcLoanAmountByLoanCycle"
											property="loanAmtCalcType" value="3" onclick="checkRow();" />
										<mifos:mifoslabel name="product.byloancycle"
											bundle="ProductDefUIResources" /> &nbsp;&nbsp;&nbsp;</td>
									</tr>
								</table>
								<div id="option0" style="display: block;">
								<table width="93%" border="0" cellpadding="3" cellspacing="0">
									<tr class="fontnormal">
										<td width="30%" align="right">&nbsp;</td>
										<td width="70%" valign="top">
										<table width="100%" border="0" cellpadding="3" cellspacing="0">
											<tr>
												<td width="33%" class="drawtablehd"><mifos:mifoslabel
													name="product.minloanamt" bundle="ProductDefUIResources" /></td>
												<td width="34%" class="drawtablehd"><mifos:mifoslabel
													name="product.maxloanamt" bundle="ProductDefUIResources" /></td>
												<td width="33%" class="drawtablehd"><mifos:mifoslabel
													name="product.defamt" bundle="ProductDefUIResources" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="minLoanAmount" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="maxLoanAmount" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="defaultLoanAmount" /></td>
											</tr>

										</table>
										</td>
									</tr>
								</table>
								</div>
								<div id="option1" style="display: none;">
								<table width="93%" border="0" cellpadding="3" cellspacing="0">
									<tr class="fontnormal">
										<td width="30%" align="right">&nbsp;</td>
										<td width="70%" valign="top">
										<table width="100%" border="0" cellpadding="3" cellspacing="0">
											<tr>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.lastloanamount"
													bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.minloanamt" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.maxloanamt" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.defamt" bundle="ProductDefUIResources" /></td>
											</tr>

											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													size="10" property="startRangeLoanAmt1" style="border:0"
													readonly="true" value="0" /> - <mifos:mifosnumbertext
													size="10" property="endRangeLoanAmt1"
													onblur="changeValue(event, this, this.value,1)" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMinLoanAmt1" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMaxLoanAmt1" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanDefaultLoanAmt1" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													size="10" property="startRangeLoanAmt2" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													property="endRangeLoanAmt2"
													onblur="changeValue(event, this, this.value,2)" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMinLoanAmt2" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMaxLoanAmt2" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanDefaultLoanAmt2" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													size="10" property="startRangeLoanAmt3" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													property="endRangeLoanAmt3"
													onblur="changeValue(event, this, this.value,3)" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMinLoanAmt3" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMaxLoanAmt3" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanDefaultLoanAmt3" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													size="10" property="startRangeLoanAmt4" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													property="endRangeLoanAmt4"
													onblur="changeValue(event, this, this.value,4)" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMinLoanAmt4" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMaxLoanAmt4" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanDefaultLoanAmt4" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													size="10" property="startRangeLoanAmt5" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													property="endRangeLoanAmt5"
													onblur="changeValue(event, this, this.value,5)" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMinLoanAmt5" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMaxLoanAmt5" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanDefaultLoanAmt5" /></td>
											</tr>
											<tr>
												<td class="drawtablerow"><mifos:mifosnumbertext
													size="10" property="startRangeLoanAmt6" style="border:0"
													readonly="true" /> - <mifos:mifosnumbertext size="10"
													property="endRangeLoanAmt6" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMinLoanAmt6" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanMaxLoanAmt6" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="lastLoanDefaultLoanAmt6" /></td>
											</tr>
										</table>
										</td>
									</tr>
								</table>
								</div>
								<div id="option2" style="display: none;">
								<table width="93%" border="0" cellpadding="3" cellspacing="0">
									<tr class="fontnormal">
										<td width="30%" align="right">&nbsp;</td>
										<td width="70%" valign="top">
										<table width="100%" border="0" cellspacing="0" cellpadding="3">
											<tr>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.loancycleno" bundle="ProductDefUIResources" />
												</td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.minloanamt" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.maxloanamt" bundle="ProductDefUIResources" /></td>
												<td width="25%" class="drawtablehd"><mifos:mifoslabel
													name="product.defamt" bundle="ProductDefUIResources" /></td>
											</tr>
											<tr>
												<td class="drawtablerow">0</td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMinLoanAmt1" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMaxLoanAmt1" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanDefaultLoanAmt1" /></td>
											</tr>
											<tr>
												<td class="drawtablerow">1</td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMinLoanAmt2" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMaxLoanAmt2" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanDefaultLoanAmt2" /></td>
											</tr>
											<tr>
												<td class="drawtablerow">2</td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMinLoanAmt3" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMaxLoanAmt3" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanDefaultLoanAmt3" /></td>
											</tr>
											<tr>
												<td class="drawtablerow">3</td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMinLoanAmt4" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMaxLoanAmt4" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanDefaultLoanAmt4" /></td>
											</tr>
											<tr>
												<td class="drawtablerow">4</td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMinLoanAmt5" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMaxLoanAmt5" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanDefaultLoanAmt5" /></td>
											</tr>
											<tr>
												<td class="drawtablerow">>4</td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMinLoanAmt6" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanMaxLoanAmt6" /></td>
												<td class="drawtablerow"><mifos:decimalinput size="10"
													property="cycleLoanDefaultLoanAmt6" /></td>
											</tr>
										</table>

										</td>
									</tr>
								</table>
								</div>
								<br>

								<table width="93%" border="0" cellpadding="3" cellspacing="0">
									<tr>
										<td colspan="2" class="fontnormalbold"><fmt:message
											key="product.productRate">
											<fmt:param>
												<mifos:mifoslabel
													name="${ConfigurationConstants.SERVICE_CHARGE}"
													bundle="ProductDefUIResources" />
											</fmt:param>
										</fmt:message> <br>
										<br>
										</td>
									</tr>
									<tr class="fontnormal">
										<td width="30%" align="right"><span class="mandatorytext">
										<font color="#FF0000">*</font></span> <fmt:message
											key="product.rateType">
											<fmt:param>
												<mifos:mifoslabel
													name="${ConfigurationConstants.SERVICE_CHARGE}"
													bundle="ProductDefUIResources" />
											</fmt:param>
										</fmt:message>:</td>
										<td width="70%" valign="top"><mifos:select
											property="interestTypes">
											<c:forEach
												items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'InterestTypesList')}"
												var="intType">
												<html-el:option value="${intType.id}">${intType.name}</html-el:option>
											</c:forEach>

										</mifos:select></td>
									</tr>
									<tr class="fontnormal">
										<td align="right"><span class="mandatorytext"> <font
											color="#FF0000">*</font></span> <fmt:message key="product.maxRate">
											<fmt:param>
												<mifos:mifoslabel
													name="${ConfigurationConstants.SERVICE_CHARGE}"
													bundle="ProductDefUIResources" />
											</fmt:param>
										</fmt:message>:</td>
										<td valign="top"><mifos:decimalinput
											styleId= "createLoanProduct.input.maxInterestRate" property="maxInterestRate" /> <mifos:mifoslabel
											name="product.rate" bundle="ProductDefUIResources" /></td>
									</tr>
									<tr class="fontnormal">
										<td align="right"><span class="mandatorytext"> <font
											color="#FF0000">*</font></span> <fmt:message key="product.minRate">
											<fmt:param>
												<mifos:mifoslabel
													name="${ConfigurationConstants.SERVICE_CHARGE}"
													bundle="ProductDefUIResources" />
											</fmt:param>
										</fmt:message>:</td>
										<td valign="top"><mifos:decimalinput
											styleId= "createLoanProduct.input.minInterestRate" property="minInterestRate" /> <mifos:mifoslabel
											name="product.rate" bundle="ProductDefUIResources" /></td>
									</tr>
									<tr class="fontnormal">
										<td align="right"><span class="mandatorytext"> <font
											color="#FF0000">*</font></span> <fmt:message
											key="product.defaultRate">
											<fmt:param>
												<mifos:mifoslabel
													name="${ConfigurationConstants.SERVICE_CHARGE}"
													bundle="ProductDefUIResources" />
											</fmt:param>
										</fmt:message>:</td>
										<td valign="top"><mifos:decimalinput
											styleId= "createLoanProduct.input.defInterestRate"  property="defInterestRate" /> <mifos:mifoslabel
											name="product.rate" bundle="ProductDefUIResources" /></td>
									</tr>
								</table>
								<br>
								<table width="93%" border="0" cellpadding="3" cellspacing="0">
									<tr>
										<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
											name="product.repaysch" bundle="ProductDefUIResources" /> <br>
										<br>
										</td>
									</tr>
									<tr class="fontnormal">
										<td width="30%" align="right" valign="top"><mifos:mifoslabel
											mandatory="yes" name="product.freqofinst"
											bundle="ProductDefUIResources" /> :</td>
										<td width="70%" valign="top">

										<table width="90%" border="0" cellpadding="3" cellspacing="0">
											<tr class="fontnormal">
												<td align="left" valign="top"
													style="border-top: 1px solid #CECECE; border-left: 1px solid #CECECE; border-right: 1px solid #CECECE;">
												<table width="98%" border="0" cellspacing="0"
													cellpadding="2">
													<tr valign="top" class="fontnormal">
														<td width="24%"><html-el:radio styleId="createLoanProduct.radio.freqOfInstallmentsWeeks"
															property="freqOfInstallments" value="1"
															onclick="showMeetingFrequency();" /> <mifos:mifoslabel
															name="product.weeks" bundle="ProductDefUIResources" /></td>
														<td width="55%"><html-el:radio styleId="createLoanProduct.radio.freqOfInstallmentsMonths"
															property="freqOfInstallments" value="2"
															onclick="showMeetingFrequency();" /> <mifos:mifoslabel
															name="product.months" bundle="ProductDefUIResources" /></td>
													</tr>
												</table>
												</td>
											</tr>
											<tr class="fontnormal">
												<td width="59%" align="left" valign="top"
													style="border: 1px solid #CECECE;">
												<div id="weekDIV" style="height: 40px; width: 380px;"><mifos:mifoslabel
													name="product.enterfoll" bundle="ProductDefUIResources"
													isColonRequired="yes" />
												<table border="0" cellspacing="0" cellpadding="2">
													<tr class="fontnormal">
														<td colspan="3"><mifos:mifoslabel
															name="product.recur" bundle="ProductDefUIResources" /> <mifos:mifosnumbertext
															styleId="createLoanProduct.input.recur" property="recurAfter" size="3" maxlength="3" /></td>
														<td><span id="week"> <mifos:mifoslabel
															name="product.week" bundle="ProductDefUIResources" /> </span> <span
															id="month"> <mifos:mifoslabel name="product.month"
															bundle="ProductDefUIResources" /> </span></td>
													</tr>
												</table>
												</div>
												</td>
											</tr>
										</table>
										<script>
										showMeetingFrequency();
									</script></td>
									</tr>
									<!--<tr class="fontnormal">
										<td align="right"><mifos:mifoslabel mandatory="yes"
											name="product.maxinst" bundle="ProductDefUIResources" /> :</td>
										<td valign="top"><mifos:mifosnumbertext
											property="maxNoInstallments" /></td>
									</tr>
									<tr class="fontnormal">
										<td align="right"><mifos:mifoslabel mandatory="yes"
											name="product.mininst" bundle="ProductDefUIResources" /> :</td>
										<td valign="top"><mifos:mifosnumbertext
											property="minNoInstallments" /></td>
									</tr>
									<tr class="fontnormal">
										<td align="right"><span class="mandatorytext"></span> <mifos:mifoslabel
											name="product.definst" bundle="ProductDefUIResources"
											mandatory="yes" /> :</td>
										<td valign="top"><mifos:mifosnumbertext
											property="defNoInstallments" /></td>
									</tr>
									-->
									<table width="93%" border="0" cellpadding="3" cellspacing="0">
										<tr class="fontnormal">
											<td width="30%" align="right"><mifos:mifoslabel
												name="product.calcInstallment" mandatory="yes"
												bundle="ProductDefUIResources" />:</td>
											<td width="70%" valign="top"><html-el:radio
												styleId="createLoanProduct.radio.calcInstallmentSameForAll"
												property="calcInstallmentType" value="1"
												onclick="checkType();" /> <mifos:mifoslabel
												name="product.sameforallinstallment"
												bundle="ProductDefUIResources" /> &nbsp;&nbsp;&nbsp; <html-el:radio
												styleId="createLoanProduct.radio.calcInstallmentByLastLoanAmount"
												property="calcInstallmentType" value="2"
												onclick="checkType();" /> <mifos:mifoslabel
												name="product.installbylastloanamount"
												bundle="ProductDefUIResources" /> &nbsp;&nbsp;&nbsp; <html-el:radio
												styleId="createLoanProduct.radio.calcInstallmentByLoanCycle"
												property="calcInstallmentType" value="3"
												onclick="checkType();" /> <mifos:mifoslabel
												name="product.installbyloancycle"
												bundle="ProductDefUIResources" /> &nbsp;&nbsp;&nbsp;</td>
										</tr>
										<tr>
											<td colspan="2">
											<div id="install0" style="display: block;">
											<table width="93%" border="0" cellpadding="3" cellspacing="0">
												<tr class="fontnormal">
													<td width="30%" align="right">&nbsp;</td>
													<td width="70%" valign="top">
													<table width="100%" border="0" cellpadding="3"
														cellspacing="0">
														<tr>
															<td width="33%" class="drawtablehd"><mifos:mifoslabel
																name="product.mininst" bundle="ProductDefUIResources" /></td>
															<td width="34%" class="drawtablehd"><mifos:mifoslabel
																name="product.maxinst" bundle="ProductDefUIResources" /></td>
															<td width="33%" class="drawtablehd"><mifos:mifoslabel
																name="product.definst" bundle="ProductDefUIResources" /></td>
														</tr>

														<tr>
															<td class="drawtablerow"><mifos:mifosnumbertext
																property="minNoInstallments" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																property="maxNoInstallments" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																property="defNoInstallments" /></td>

														</tr>
													</table>
													</td>
												</tr>
											</table>
											</div>
											<div id="install1" style="display: none;">
											<table width="93%" border="0" cellpadding="3" cellspacing="0">
												<tr class="fontnormal">
													<td width="30%" align="right">&nbsp;</td>
													<td width="70%" valign="top">
													<table width="100%" border="0" cellpadding="3"
														cellspacing="0">
														<tr>
															<td width="25%" class="drawtablehd"><mifos:mifoslabel
																name="product.lastloanamount"
																bundle="ProductDefUIResources" /></td>
															<td width="25%" class="drawtablehd"><mifos:mifoslabel
																name="product.mininst" bundle="ProductDefUIResources" /></td>
															<td width="25%" class="drawtablehd"><mifos:mifoslabel
																name="product.maxinst" bundle="ProductDefUIResources" /></td>
															<td width="25%" class="drawtablehd"><mifos:mifoslabel
																name="product.definst" bundle="ProductDefUIResources" /></td>
														</tr>
														<tr>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="startInstallmentRange1"
																style="border:0" readonly="true" value="0" /> - <mifos:mifosnumbertext
																size="10" property="endInstallmentRange1"
																onblur="changeInstallmentValue(event, this, this.value,1)" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minLoanInstallment1" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxLoanInstallment1" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defLoanInstallment1" /></td>
														</tr>
														<tr>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="startInstallmentRange2"
																style="border:0" readonly="true" /> - <mifos:mifosnumbertext
																size="10" property="endInstallmentRange2"
																onblur="changeInstallmentValue(event, this, this.value,2)" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minLoanInstallment2" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxLoanInstallment2" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defLoanInstallment2" /></td>
														</tr>
														<tr>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="startInstallmentRange3"
																style="border:0" readonly="true" /> - <mifos:mifosnumbertext
																size="10" property="endInstallmentRange3"
																onblur="changeInstallmentValue(event, this, this.value,3)" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minLoanInstallment3" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxLoanInstallment3" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defLoanInstallment3" /></td>
														</tr>
														<tr>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="startInstallmentRange4"
																style="border:0" readonly="true" /> - <mifos:mifosnumbertext
																size="10" property="endInstallmentRange4"
																onblur="changeInstallmentValue(event, this, this.value,4)" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minLoanInstallment4" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxLoanInstallment4" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defLoanInstallment4" /></td>
														</tr>
														<tr>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="startInstallmentRange5"
																style="border:0" readonly="true" /> - <mifos:mifosnumbertext
																size="10" property="endInstallmentRange5"
																onblur="changeInstallmentValue(event, this, this.value,5)" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minLoanInstallment5" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxLoanInstallment5" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defLoanInstallment5" /></td>
														</tr>
														<tr>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="startInstallmentRange6"
																style="border:0" readonly="true" /> - <mifos:mifosnumbertext
																size="10" property="endInstallmentRange6" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minLoanInstallment6" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxLoanInstallment6" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defLoanInstallment6" /></td>
														</tr>
													</table>
													</td>
												</tr>
											</table>
											<br>
											</div>
											<div id="install2" style="display: none;">
											<table width="93%" border="0" cellpadding="3" cellspacing="0">
												<tr class="fontnormal">
													<td width="30%" align="right">&nbsp;</td>
													<td width="70%" valign="top">
													<table width="100%" border="0" cellspacing="0"
														cellpadding="3">
														<tr>
															<td width="25%" class="drawtablehd"><mifos:mifoslabel
																name="product.loancycleno"
																bundle="ProductDefUIResources" /></td>
															<td width="25%" class="drawtablehd"><mifos:mifoslabel
																name="product.mininst" bundle="ProductDefUIResources" /></td>
															<td width="25%" class="drawtablehd"><mifos:mifoslabel
																name="product.maxinst" bundle="ProductDefUIResources" /></td>
															<td width="25%" class="drawtablehd"><mifos:mifoslabel
																name="product.definst" bundle="ProductDefUIResources" /></td>
														</tr>
														<tr>
															<td class="drawtablerow">0</td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minCycleInstallment1" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxCycleInstallment1" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defCycleInstallment1" /></td>
														</tr>
														<tr>
															<td class="drawtablerow">1</td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minCycleInstallment2" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxCycleInstallment2" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defCycleInstallment2" /></td>
														</tr>
														<tr>
															<td class="drawtablerow">2</td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minCycleInstallment3" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxCycleInstallment3" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defCycleInstallment3" /></td>
														</tr>
														<tr>
															<td class="drawtablerow">3</td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minCycleInstallment4" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxCycleInstallment4" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defCycleInstallment4" /></td>
														</tr>
														<tr>
															<td class="drawtablerow">4</td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minCycleInstallment5" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxCycleInstallment5" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defCycleInstallment5" /></td>
														</tr>
														<tr>
															<td class="drawtablerow">>4</td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="minCycleInstallment6" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="maxCycleInstallment6" /></td>
															<td class="drawtablerow"><mifos:mifosnumbertext
																size="10" property="defCycleInstallment6" /></td>
														</tr>
													</table>

													</td>
												</tr>
											</table>
											</div>
										</tr>
										<c:set
											value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'repaymentSchedulesIndependentOfMeetingIsEnabled')}"
											var="repaymentSchedulesIndependentOfMeetingIsEnabled" />

										<tr class="fontnormal" id="intdeddis">
											<!--  
											<td align="right">
											<fmt:message key="product.deductedatdis">
											<fmt:param><mifos:mifoslabel
												name="${ConfigurationConstants.SERVICE_CHARGE}" 
												bundle="ProductDefUIResources" /></fmt:param>
											</fmt:message>
											:</td>
											-->
											<td valign="top"><c:if
												test="${repaymentSchedulesIndependentOfMeetingIsEnabled == '0'}">


												<html-el:checkbox style="visibility:hidden"
													property="intDedDisbursementFlag" value="1"
													onclick="fnIntDesbr();" />
											</c:if> <c:if
												test="${repaymentSchedulesIndependentOfMeetingIsEnabled != '0'}">


												<html-el:checkbox property="intDedDisbursementFlag"
													style="visibility:hidden" value="1" onclick="fnIntDesbr();" />
											</c:if></td>
										</tr>
										<tr class="fontnormal">
											<!--  
											<td align="right"><mifos:mifoslabel
												name="product.prinlastinst" bundle="ProductDefUIResources" />
											:</td>
											-->
											<td valign="top"><html-el:checkbox
												property="prinDueLastInstFlag" value="1"
												style="visibility:hidden" /></td>
										</tr>
										<tr class="fontnormal" id="gracepertype">
											<td align="right"><mifos:mifoslabel
												name="product.gracepertype" bundle="ProductDefUIResources" />
											:</td>
											<td valign="top"><mifos:select
												property="gracePeriodType" onchange="fnGracePeriod();">
												<c:forEach
													items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanGracePeriodTypeList')}"
													var="graceType">
													<html-el:option value="${graceType.id}">${graceType.name}</html-el:option>
												</c:forEach>

											</mifos:select></td>
										</tr>
										<tr class="fontnormal" id="graceperdur">
											<td align="right"><mifos:mifoslabel
												name="product.graceperdur" bundle="ProductDefUIResources" />
											:</td>
											<td valign="top"><mifos:mifosnumbertext
												property="gracePeriodDuration" /> <mifos:mifoslabel
												name="product.installments" bundle="ProductDefUIResources" />
											</td>
										</tr>

									</table>
									<br>
									<script type="text/javascript">
										fnIntDesbr();
										fnGracePeriod();
										checkRow();
										checkType();
										showLoanAmountType();
										showInstallType();
									</script>

									<table width="93%" border="0" cellpadding="3" cellspacing="0">
										<tr>
											<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
												name="product.fees&pen" bundle="ProductDefUIResources" /> <br>
											<br>
											</td>
										</tr>
										<tr class="fontnormal">
											<td align="right" valign="top"><mifos:mifoslabel
												name="product.attachfeestypes"
												bundle="ProductDefUIResources" isColonRequired="yes" /></td>
											<td valign="top">
											<table width="80%" border="0" cellspacing="0" cellpadding="0">
												<tr>
													<td class="fontnormal"><mifos:mifoslabel
														name="product.clickfeetypes"
														bundle="ProductDefUIResources" /></td>
												</tr>
												<tr>
													<td><img src="pages/framework/images/trans.gif"
														width="1" height="1"></td>
												</tr>
											</table>

											<c:set var="LoanFeesList" scope="request"
												value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'LoanFeesList')}" />
											<c:set var="selectedFeeList" scope="request"
												value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanprdfeeselectedlist')}" />
											<mifos:MifosSelect property="prdOfferinFees"
												input="LoanFeesList" output="selectedFeeList"
												property1="feeId" property2="feeName" multiple="true">
											</mifos:MifosSelect></td>
										</tr>
									</table>

									<table width="93%" border="0" cellpadding="3" cellspacing="0">
										<tr>
											<td colspan="2" class="fontnormalbold"><mifos:mifoslabel
												name="product.accounting" bundle="ProductDefUIResources" />
											<br>
											<br>
											</td>
										</tr>
										<tr class="fontnormal">
											<td width="30%" align="right" valign="top"><mifos:mifoslabel
												name="product.srcfunds" bundle="ProductDefUIResources"
												isColonRequired="yes" /></td>
											<td width="70%" valign="top">
											<table width="80%" border="0" cellspacing="0" cellpadding="0">
												<tr>
													<td class="fontnormal"><mifos:mifoslabel
														name="product.clickfunds" bundle="ProductDefUIResources" />
													</td>
												</tr>
												<tr>
													<td><img src="pages/framework/images/trans.gif"
														width="1" height="1"></td>
												</tr>
											</table>
											<c:set var="SrcFundsList" scope="request"
												value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'SrcFundsList')}" />
											<c:set var="selectedFundList" scope="request"
												value="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'loanprdfundselectedlist')}" />
											<mifos:MifosSelect property="loanOfferingFunds"
												input="SrcFundsList" output="selectedFundList"
												property1="fundId" property2="fundName" multiple="true">
											</mifos:MifosSelect></td>
										</tr>
										<tr class="fontnormal">
											<td align="right" valign="top" style="padding-top: 8px;"><mifos:mifoslabel
												mandatory="yes" name="product.productglcode"
												bundle="ProductDefUIResources" /> :</td>
											<td valign="top">
											<table width="80%" border="0" cellspacing="0" cellpadding="2">
												<tr class="fontnormal">
													<td width="15%"><mifos:mifoslabel
														name="${ConfigurationConstants.SERVICE_CHARGE}"
														bundle="ProductDefUIResources" isColonRequired="yes" /></td>
													<td width="85%"><mifos:select
														property="interestGLCode">
														<c:forEach
															items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'interestGLCodes')}"
															var="glCodes">
															<html-el:option value="${glCodes.glcodeId}">${glCodes.glcode}</html-el:option>
														</c:forEach>
													</mifos:select></td>
												</tr>
												<tr class="fontnormal">
													<td><mifos:mifoslabel name="product.principal"
														bundle="ProductDefUIResources" isColonRequired="yes" /></td>
													<td><mifos:select property="principalGLCode">
														<c:forEach
															items="${session:getFromSession(sessionScope.flowManager,requestScope.currentFlowKey,'principalGLCodes')}"
															var="glCodes">
															<html-el:option value="${glCodes.glcodeId}">${glCodes.glcode}</html-el:option>
														</c:forEach>
													</mifos:select></td>
												</tr>
											</table>
											</td>
										</tr>
									</table>
									<table width="93%" border="0" cellpadding="0" cellspacing="0">
										<tr>
											<td align="center" class="blueline">&nbsp;</td>
										</tr>
									</table>

									<br>
									<table width="93%" border="0" cellpadding="0" cellspacing="0">
										<tr>
											<td align="center"><html-el:submit styleId="createLoanProduct.button.preview" styleClass="buttn"
												onclick="transferData(this.form.loanOfferingFunds);transferData(this.form.prdOfferinFees);">
												<mifos:mifoslabel name="product.preview"
													bundle="ProductDefUIResources" />
											</html-el:submit> &nbsp; <html-el:button styleId="createLoanProduct.button.cancel" property="cancel"
												styleClass="cancelbuttn"
												onclick="javascript:fnCancel(this.form)">
												<mifos:mifoslabel name="product.cancel"
													bundle="ProductDefUIResources" />
											</html-el:button></td>
										</tr>
									</table>
									<br>
									</td>
									</tr>
								</table>
								<html-el:hidden property="method" value="preview" />
								<html-el:hidden property="currentFlowKey"
									value="${requestScope.currentFlowKey}" />
								</html-el:form>
								</tiles:put>
								</tiles:insert>
