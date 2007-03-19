<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<%@ taglib uri="struts-logic" prefix="logic" %>
<html>
<head>
    <title><bean:message key="cart.title"/></title>
    <link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<html:form action="/updateCart.jspa" method="post">

<h2><bean:message key="cart.subtitle"/></h2>

<table class="grid">
    <tr>
        <th class="gridheader"><bean:message key="itemId"/></th>
        <th class="gridheader"><bean:message key="description"/></th>
        <th class="gridheader"><bean:message key="unitCost"/></th>
        <th class="gridheader"><bean:message key="quantity"/></th>
        <th class="gridheader"><bean:message key="subTotal"/></th>
        <th class="gridheader">&nbsp;</th>
    </tr>

    <logic:empty name="cartForm" property="cartItems">
        <tr>
            <td class="griddata" colspan="6"><bean:message key="cart_empty"/></td>
        </tr>
    </logic:empty>

	<!-- Items -->
    <logic:notEmpty name="cartForm" property="cartItems">
        <logic:iterate id="item" name="cartForm" property="cartItems">
        <tr>
            <td class="griddata">
                <html:link page="/item.jspa" paramId="itemId" paramName="item" paramProperty="itemId">
                    <bean:write name="item" property="itemId"/>
                </html:link>
                <html:hidden name="item" property="itemId" />
            </td>
            <td class="griddata">
                <bean:write name="item" property="description"/>
            </td>
            <td class="griddata" align="right">
                <bean:write name="item" property="unitCost"/>
            </td>
            <td class="griddata" align="center">
                <html:text name="item" property="quantity" size="3" maxlength="3"/>
            </td>
            <td class="griddata"align="right">
                <bean:write name="item" property="totalCost"/>
            </td>
            <td class="griddata" align="center">
                <html:link page="/removeItem.jspa" paramId="itemId" paramName="item" paramProperty="itemId">
                    <html:img border="0" page="/images/button_remove.gif"/>
                </html:link>
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
            <td class="griddata">&nbsp;</td>
        </tr>
        <tr>
            <td class="griddata" colspan="6" align="center">
                <html:image page="/images/button_update_cart.gif" border="0" />
            </td>
        </tr>
    </logic:notEmpty>
</table>

<logic:notEmpty name="cartForm" property="cartItems">
    <p>
    <html:link page="/checkout.jspa">
        <html:img page="/images/button_checkout.gif" border="0" />
    </html:link>
</logic:notEmpty>

</html:form>

</body>
</html>