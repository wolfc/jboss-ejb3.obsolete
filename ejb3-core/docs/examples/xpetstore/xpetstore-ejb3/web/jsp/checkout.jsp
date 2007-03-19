<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<%@ taglib uri="struts-logic" prefix="logic" %>
<html>
<head>
    <title><bean:message key="order.title"/></title>
    <link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<html:form action="/createOrder.jspa" method="post">

<h2><bean:message key="order.subtitle"/></h2>

<!-- Customer informations -->
<table border="0" cellspacing="4">
	<!-- Address -->
	<tr>
		<td colspan="2" class="sectionheader"><bean:message key="address"/></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="street1"/>:</td>
		<td><bean:write name="cartForm" property="customer.street1" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="street2"/>:</td>
		<td><bean:write name="cartForm" property="customer.street2" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="city"/>:</td>
		<td><bean:write name="cartForm" property="customer.city" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="state"/>:</td>
		<td><bean:write name="cartForm" property="customer.state" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="zipcode"/>:</td>
		<td><bean:write name="cartForm" property="customer.zipcode" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="country"/>:</td>
		<td><bean:write name="cartForm" property="customer.country" /></td>
	</tr>

	<!-- Credit card -->
	<tr><td colspan="2">&nbsp;</td></tr>
	<tr>
		<td colspan="2"  class="sectionheader"><bean:message key="ccInfos"/></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="ccType"/>:</td>
		<td><bean:write name="cartForm" property="customer.creditCardType" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="ccNumber"/>:</td>
		<td><bean:write name="cartForm" property="customer.creditCardNumber" /></td>
	</tr>
	<tr>
		<td class="label"><bean:message key="ccExpiryDate"/>:</td>
		<td><bean:write name="cartForm" property="customer.creditCardExpiryDate" /></td>
	</tr>
</table>

<!-- Shopping cart -->
<p>
<table class="grid">
    <tr>
        <th class="gridheader"><bean:message key="itemId"/></th>
        <th class="gridheader"><bean:message key="description"/></th>
        <th class="gridheader"><bean:message key="unitCost"/></th>
        <th class="gridheader"><bean:message key="quantity"/></th>
        <th class="gridheader"><bean:message key="subTotal"/></th>
    </tr>
    <logic:empty name="cartForm" property="cartItems">
        <tr>
            <td class="griddata" colspan="5"><bean:message key="cart_empty"/></td>
        </tr>
    </logic:empty>

    <logic:notEmpty name="cartForm" property="cartItems">

		<!-- Items -->
        <logic:iterate id="item" name="cartForm" property="cartItems">
        <tr>
            <td class="griddata">
                <bean:write name="item" property="itemId"/>
                <html:hidden name="item" property="itemId" />
            </td>
            <td class="griddata">
                <bean:write name="item" property="description"/>
            </td>
            <td class="griddata" align="right">
                <bean:write name="item" property="unitCost"/>
            </td>
            <td class="griddata" align="right">
				<bean:write  name="item" property="quantity"/>
                <html:hidden name="item" property="quantity"/>
            </td>
            <td class="griddata"align="right">
                <bean:write name="item" property="totalCost"/>
            </td>
        </tr>
        </logic:iterate>

		<!-- Total -->
        <tr>
            <td class="griddata" colspan="4" align="right">
                <b><bean:message key="total"/></b>
            </td>
            <td class="griddata" align="right">
                <b><bean:write name="cartForm" property="total"/></b>
            </td>
        </tr>
    </logic:notEmpty>
</table>

<logic:notEmpty name="cartForm" property="cartItems">
    <p>
	<html:image page="/images/button_submit.gif" border="0" />
</logic:notEmpty>

</html:form>

</body>
</html>