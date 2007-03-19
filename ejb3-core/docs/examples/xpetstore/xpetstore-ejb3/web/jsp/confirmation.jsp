<%@ taglib uri="struts-bean"  prefix="bean" %>
<html>
<head>
    <title><bean:message key="confirmation.title"/></title>
    <link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>

<h2><bean:message key="confirmation.subtitle"/></h2>

<p>
<bean:message key="confirmation.message"/>

</body>
</html>