<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<%@ taglib uri="struts-logic" prefix="logic" %>

<html>
<head>
	<title><bean:message key="search.title"/></title>
	<link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<h2><bean:message key="search.subtitle"/></h2>
<p>
<table class="grid">
	<tr>
		<th class="gridheader"><bean:message key="productId"/></th>
	  	<th class="gridheader"><bean:message key="name"/></th>
	  	<th class="gridheader"><bean:message key="description"/></th>
	</tr>

	<logic:empty name="searchForm" property="products">
		<tr>
			<td class="griddata" colspan="3"><bean:message key="no_products"/></td>
		</tr>
	</logic:empty>

	<logic:notEmpty name="searchForm" property="products">
		<logic:iterate id="product" name="searchForm" property="products">
		<tr>
			<td class="griddata">
				<html:link page="/product.jspa" paramId="productId" paramName="product" paramProperty="productId">
					<bean:write name="product" property="productId"/>
				</html:link>
			</td>
			<td class="griddata"><bean:write name="product" property="name"/></td>
			<td class="griddata"><bean:write name="product" property="description"/></td>
		</tr>
		</logic:iterate>
	</logic:notEmpty>
</table>

</body>
</html>