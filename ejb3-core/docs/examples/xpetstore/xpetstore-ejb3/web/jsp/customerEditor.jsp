<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<%@ taglib uri="struts-logic" prefix="logic" %>
<%@ taglib uri="xpetstore"    prefix="xpetstore" %>

	<!-- Personal information -->
	<tr>
		<td colspan="2" class="sectionheader"><bean:message key="personal_information"/></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="firstname"/>:</td>
		<td><html:text name="customerForm" property="customer.firstname" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="lastname"/>:</td>
		<td><html:text name="customerForm" property="customer.lastname" /></td>
	</tr>
	<tr>
		<td class="label"><span class="required">*</span><bean:message key="email"/>:</td>
		<td><html:text name="customerForm" property="customer.email" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="telephone"/>:</td>
		<td><html:text name="customerForm" property="customer.telephone" /></td>
	</tr>

	<!-- Address -->
	<tr><td colspan="2">&nbsp;</td></tr>
	<tr>
		<td colspan="2" class="sectionheader"><bean:message key="address"/></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="street1"/>:</td>
		<td><html:text name="customerForm" property="customer.street1" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="street2"/>:</td>
		<td><html:text name="customerForm" property="customer.street2" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="city"/>:</td>
		<td><html:text name="customerForm" property="customer.city" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="state"/>:</td>
		<td><html:text name="customerForm" property="customer.state" maxlength="3" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="zipcode"/>:</td>
		<td><html:text name="customerForm" property="customer.zipcode" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="country"/>:</td>
		<td><html:text name="customerForm" property="customer.country" maxlength="3" /></td>
	</tr>

	<!-- Credit card -->
	<tr><td colspan="2">&nbsp;</td></tr>
	<tr>
		<td colspan="2"  class="sectionheader"><bean:message key="ccInfos"/></td>
	</tr>
	<tr>
		<td class="label"><span class="required">*</span><bean:message key="ccType"/>:</td>
		<td>
			<logic:notEmpty name="customerForm" property="customer.creditCardType">
				<bean:define id="_creditCardType" name="customerForm" property="customer.creditCardType" />
			</logic:notEmpty>
			<% String creditCardType = ( String )pageContext.getAttribute("_creditCardType"); %>
			<xpetstore:creditcard name="customer.creditCardType" value="<%= creditCardType %>"/>
		</td>
	</tr>
	<tr>
		<td class="label"><span class="required">*</span><bean:message key="ccNumber"/>:</td>
		<td><html:text name="customerForm" property="customer.creditCardNumber" /></td>
	</tr>
	<tr>
		<td class="label"><span class="required">*</span><bean:message key="ccExpiryDate"/> (<small>MM-YY</small>):</td>
		<td><html:text name="customerForm" property="customer.creditCardExpiryDate" maxlength="5" /></td>
	</tr>