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
<%@taglib uri="/tags/mifos-html" prefix="mifos"%>
<%@taglib uri="http://struts.apache.org/tags-html-el" prefix="html-el"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="/sessionaccess" prefix="session"%>

<tiles:insert definition=".view">
	<tiles:put name="body" type="string">
	<span id="page.id" title="createProductsMixConfirmation" />
		<table width="95%" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td width="70%" align="left" valign="top" class="paddingL15T15">
					<table width="98%" border="0" cellspacing="0" cellpadding="3">
						<tr>
							<td class="headingorange">
								<mifos:mifoslabel name="product.successfullydefinedanew" bundle="ProductDefUIResources" />
								<mifos:mifoslabel name="product.pro"  bundle="ProductDefUIResources" />
								<mifos:mifoslabel name="product.mix" bundle="ProductDefUIResources" />
								<br>
							</td>
						</tr>
						<tr>
							<td class="fontnormalbold">
								<span class="fontnormal"><br> </span><span class="fontnormal"><br> <br> </span>
									<html-el:link href="productMixAction.do?method=get&prdOfferingId=${requestScope.prdOfferingBO.prdOfferingId}&productType=${requestScope.prdOfferingBO.prdType.productTypeID}&randomNUm=${sessionScope.randomNUm}">
									<mifos:mifoslabel name="product.view" bundle="ProductDefUIResources" />
									<mifos:mifoslabel name="product.pro"  bundle="ProductDefUIResources" />
									<mifos:mifoslabel name="product.mix" bundle="ProductDefUIResources" />
									<mifos:mifoslabel name="product.prdmixdetailsnow" bundle="ProductDefUIResources" />
								</html-el:link>
								<span class="fontnormal"><br> <br> </span><span class="fontnormal"> <html-el:link href="productMixAction.do?method=load&recordOfficeId=${UserContext.branchId}&recordLoanOfficerId=${UserContext.id}&randomNUm=${sessionScope.randomNUm}">
									<mifos:mifoslabel name="product.DefineMixForNewProduct" bundle="ProductDefUIResources" />
									</html-el:link></span>
							</td>
						</tr>
					</table>
					<br>
				</td>
			</tr>
		</table>

	</tiles:put>
</tiles:insert>
