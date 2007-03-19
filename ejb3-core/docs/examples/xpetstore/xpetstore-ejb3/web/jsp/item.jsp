<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<html>
<head>
	<title><bean:message key="item.title"/></title>
	<link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<html:form action="/addItem.jspa">

<table class="grid">
	<tr>
		<td rowspan="6" width="100" align="center" valign="middle">
			<img border="0" 
			src="<%= request.getContextPath() %>/dataimages/<bean:write name="itemForm" property="item.imagePath"/>">
		</td>
	</tr>
	<tr>
		<td class="griddata">
			<b><bean:write name="itemForm" property="itemId"/></b>
			<input type="hidden" name="itemId" value="<bean:write name="itemForm" property="item.itemId" />">
		</td>
	</tr>
	<tr>
		<td class="griddata"><b><bean:write name="itemForm" property="item.description"/></b></td>
	</tr>
	<tr>
		<td class="griddata"><i><bean:write name="itemForm" property="product.description"/></i></td>
	</tr>
	<tr>
		<td class="griddata"><bean:write name="itemForm" property="item.listPrice"/></td>
	</tr>
	<tr>
		<td class="griddata" align="center" valign="center">
			<html:image border="0" page="/images/button_add_to_cart.gif"/>
		</td>
	</tr>

</table>
</html:form>

</body>
</html>