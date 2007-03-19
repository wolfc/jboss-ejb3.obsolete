<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-bean"  prefix="bean" %>
<html>
<head>
	<title><bean:message key="index.title"/></title>
	<link href="<%= request.getContextPath() %>/style.css" rel="stylesheet" type="text/css">
</head>
<body>
	<map name="estoremap">
		<area alt="Birds" coords="72,2,280,250" shape="RECT" 
			href="category.jspa?categoryId=BIRDS"
		/>
		<area alt="Fish" coords="2,180,72,250" shape="RECT"
			href="category.jspa?categoryId=FISH"
		/>
		<area alt="Dogs" coords="60,250,130,320" shape="RECT" 
			href="category.jspa?categoryId=DOGS"
		/>
		<area alt="Reptiles" coords="140,270,210,340" shape="RECT"
			href="category.jspa?categoryId=REPTILES"
		/>
		<area alt="Cats" coords="225,240,295,310" shape="RECT"
			href="category.jspa?categoryId=CATS"
		/>
		<area alt="Birds" coords="280,180,350,250"  shape="RECT"
			href="category.jspa?categoryId=BIRDS"
		/>
	</map>	
	<html:img page="/images/splash.gif" 
		border="0" height="355" 
		align="center" usemap="#estoremap" width="350" />
</body>

</html>
