<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<%@ taglib uri="struts-logic" prefix="logic" %>
<html>
<head>
	<title><bean:message key="product.title"/></title>
	<link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<h2><bean:write name="productForm" property="product.name"/></h2>
<i>(<bean:write name="productForm" property="product.description"/>)</i>
<p>
<table class="grid">
	<tr>
		<th class="gridheader"><bean:message key="itemId"/></th>
		<th class="gridheader"><bean:message key="productId"/></th>
	  	<th class="gridheader"><bean:message key="description"/></th>
	  	<th class="gridheader"><bean:message key="listPrice"/></th>
		<th class="gridheader">&nbsp;</th>
	</tr>

	<logic:empty name="productForm" property="items">
		<tr>
			<td class="griddata" colspan="5"><bean:message key="no_items"/></td>
		</tr>
	</logic:empty>

	<logic:notEmpty name="productForm" property="items">
		<logic:iterate id="item" name="productForm" property="items">
		<tr>
			<td class="griddata">
				<html:link page="/item.jspa" paramId="itemId" paramName="item" paramProperty="itemId">
					<bean:write name="item" property="itemId"/>
				</html:link>
			</td>
			<td class="griddata">
				<bean:write name="productForm" property="product.productId"/>
			</td>
			<td class="griddata">
				<bean:write name="item" property="description"/>
			</td>
			<td class="griddata" align="right">
				<bean:write name="item" 	property="listPrice"/>
			</td>
			<td class="griddata" align="center">
				<html:link page="/addItem.jspa" paramId="itemId" paramName="item" paramProperty="itemId">
					<html:img border="0" page="/images/button_add_to_cart.gif"/>
				</html:link>
			</td>
		</tr>
		</logic:iterate>
	</logic:notEmpty>
</table>

</body>
</html>