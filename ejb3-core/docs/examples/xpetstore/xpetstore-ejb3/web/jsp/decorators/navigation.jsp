<%@ taglib uri="struts-bean"  prefix="bean" %>

	<table border="0" cellspacing="0" cellpadding="0" width="200">
            <tr><td>&nbsp;</td></tr>
          	<tr>
                <td>
                    <a href="index.jsp">
                        <bean:message key="home" />
                    </a>
                </td>
            </tr>
            <tr><td>&nbsp;</td></tr>
            <tr>
                <td>
                    <a href="category.jspa?categoryId=FISH">
                        <bean:message key="fish" /></a>
                    <br>
                    <a href="category.jspa?categoryId=DOGS">
                        <bean:message key="dogs" /></a>
                    <br>
                    <a href="category.jspa?categoryId=REPTILES">
                        <bean:message key="reptiles" /></a>
                    <br>
                    <a href="category.jspa?categoryId=CATS">
                        <bean:message key="cats" /></a>
                    <br>
                    <a href="category.jspa?categoryId=BIRDS">
                        <bean:message key="birds" /></a>
                </td>
            </tr>
            <tr><td>&nbsp;</td></tr>
        </table>
