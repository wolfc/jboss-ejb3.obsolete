<%@ page import="javax.naming.*" %>

<%!
	private static void testJndi()
		throws Exception
   	{
   		InitialContext jndiContext = new InitialContext();
   		String object = new String("Yippee");
   		jndiContext.bind("TestJndiName", object);
		object = (String)jndiContext.lookup("TestJndiName");
		System.out.println("!! found " + object);
		
		lookup("");
   	}
   	
	private static void lookup(String name)
		throws Exception
	{
		System.out.println("!!lookup " + name);
	   
	    InitialContext jndiContext = new InitialContext();
		NamingEnumeration names = jndiContext.list(name);
		if (names != null){
			while (names.hasMore()){
				System.out.println("  " + names.next());
			}
		}
	}
%>

<html>
<body>

<%
   testJndi();
%>
DONE!
</body>
</html>