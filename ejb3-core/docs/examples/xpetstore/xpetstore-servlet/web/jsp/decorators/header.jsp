<table background="images/bkg-topbar.gif" cellspacing="0" cellpadding="5" width="100%">
<tr>
    <td>
    	<a href="index.action">
    		<img border="0" src="images/logo-topbar.gif" />
    	<a>
    </td>
    
    <td align="right" valign="middle">
    	<a href="cart.action">
    		<img border="0" src="images/cart.gif" /></a>

	    <img border="0" src="images/separator.gif" />

<%
if ( session.getAttribute( "userId" ) == null )
{
%>
      		<a href="signon.action">
      			<img border="0" src="images/sign-in.gif" /></a>    		
<%
}
else
{
%>
      		<a href="logout.action">
      			<img border="0" src="images/sign-out.gif" /></a>

			<img border="0" src="images/separator.gif" />

      		<a href="myaccount.action">
      			<img border="0" src="images/my_account.gif" /></a>
<%
}
%>
		<img border="0" src="images/separator.gif" />
	</td>
	
	<td align="left" valign="middle">
		<form action="search.action">
			<input name="keyword" />
			<input type="image" border="0" src="images/search.gif" />
		</form>
    </td>
    
</tr>

</table>
