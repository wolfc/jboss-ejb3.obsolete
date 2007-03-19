<%@ taglib uri="sitemesh-decorator" prefix="decorator" %>

<html>
<head>
	<title><decorator:title default="xPetStore" /></title>
    <decorator:head />
	<link href="style.css" rel="stylesheet" type="text/css">
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
		
<%
String username = ( String ) session.getAttribute( "username" );
if ( username != null )
{
%>
			<b><%= username %></b>
<%
}
%>
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