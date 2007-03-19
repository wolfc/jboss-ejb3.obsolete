<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<%@ taglib uri="struts-logic" prefix="logic" %>
<html>
<head>
	<title><bean:message key="login.title"/></title>
	<link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<html:form action="/login.jspa" method="post">

<bean:message key="login.message"/>
<p>
<table>
	<tr>
		<td><bean:message key="userId"/></td>
		<td><html:text name="signonForm" property="account.userId" /></td>
	</tr>

	<tr>
		<td><bean:message key="password"/></td>
		<td><html:password name="signonForm" property="account.password" /></td>
	</tr>

	<tr>
		<td colspan="2" align="center">
			<html:image border="0" page="/images/button_submit.gif"/>
		</td>
 	</tr>
</table>

<html:hidden name="signonForm" property="redirectUri" />

</html:form>

<html:form action="/register.jspa" method="post">
	<html:hidden name="signonForm" property="redirectUri" />
	
	<html:image border="0" page="/images/button_register_now.gif"/>
</html:form>

</body>
</html>