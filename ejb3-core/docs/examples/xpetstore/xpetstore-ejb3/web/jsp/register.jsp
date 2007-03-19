<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<html>
<head>
    <title><bean:message key="register.title"/></title>
    <link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<html:form action="/createCustomer.jspa" method="post">

<h2><bean:message key="register.subtitle"/></h2>

<table border="0" cellspacing="4">
	<tr>
		<td colspan="2" class="sectionheader"><bean:message key="account_information"/></td>
	</tr>
	<tr>
		<td class="label"><span class="required">*</span><bean:message key="userId" /></td>
		<td><html:text name="customerForm" property="customer.account.userId" maxlength="10" /></td>
	</tr>

	<tr>
		<td class="label"><span class="required">*</span><bean:message key="password" /></td>
		<td><html:password name="customerForm" property="customer.account.password" maxlength="10" /></td>
	</tr>

	<tr><td colspan="2">&nbsp;</td></tr>
	<%@ include file="customerEditor.jsp" %>
</table>

<p>
<html:image page="/images/button_submit.gif" border="0" />

<html:hidden name="customerForm" property="redirectUri" />

</html:form>

</body>
</html>