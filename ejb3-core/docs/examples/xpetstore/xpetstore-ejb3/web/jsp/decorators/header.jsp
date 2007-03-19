<%@ taglib uri="struts-bean"  prefix="bean" %>
<%@ taglib uri="struts-html"  prefix="html" %>
<%@ taglib uri="struts-logic"  prefix="logic" %>

<table background="<%= request.getContextPath() %>/images/bkg-topbar.gif" cellspacing="0" cellpadding="5" width="100%">
<tr>
    <td>
    	<html:link page="/index.jsp">
    		<html:img border="0" page="/images/logo-topbar.gif" />
    	</html:link>
    </td>
    <td align="right" valign="middle">
    	<html:link page="/cart.jspa">
    		<html:img border="0" page="/images/cart.gif" />
    	</html:link>

	    <html:img border="0" page="/images/separator.gif" />

		<logic:notPresent name="userId" scope="session">
      		<html:link page="/signon.jspa">
      			<html:img border="0" page="/images/sign-in.gif" />
      		</html:link>
		</logic:notPresent>
		<logic:present name="userId" scope="session">
      		<html:link page="/logout.jspa">
      			<html:img border="0" page="/images/sign-out.gif" />
			</html:link>

			<html:img border="0" page="/images/separator.gif" />

      		<html:link page="/myaccount.jspa">
      			<html:img border="0" page="/images/my_account.gif" />
			</html:link>
		</logic:present>

		<html:img border="0" page="/images/separator.gif" />
	</td>
	<td align="left" valign="middle">
		<html:form action="search.jspa">
			<html:text property="keyword" />
			<html:image border="0" page="/images/search.gif" />
		</html:form>
    </td>
</tr>
</table>
