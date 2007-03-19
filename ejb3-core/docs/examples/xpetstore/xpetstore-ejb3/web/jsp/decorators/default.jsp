<%@ taglib uri="struts-bean"  prefix="bean" %>
<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-logic"  prefix="logic" %>
<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<html>
<head>
    <title><decorator:title default="xPetStore" /></title>
    <decorator:head />
	<link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<table border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td class="header" colspan="2">
			<%@ include file="header.jsp" %>
		</td>
	</tr>
	<tr>
		<td class="navigation" valign="top" width="150">
			<%@ include file="navigation.jsp" %>
		</td>
		<td class="content" align="center" width="450">
			<logic:present name="username">
				<center><b><bean:write name="username" scope="session" /></b></center>
			</logic:present>
			<p>
			<div class="error">
				<html:errors/>
				<logic:present name="message">
					<bean:write name="message" />
				</logic:present>
			</div>
			<p>
			<decorator:body />
			<p>&nbsp;
			<p>&nbsp;
			<p>&nbsp;
		</td>
	</tr>
	<tr>
		<td class="footer" colspan="2">
			<%@ include file="footer.jsp" %>
		</td>
	</tr>
</table>

</body>
</html>