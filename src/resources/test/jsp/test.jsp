<%@ page import="javax.naming.*,
                  java.text.*,
                  java.lang.*,
                  java.util.*,
                  org.jboss.ejb3.test.jsp.StatelessRemote"%>
<%!
   private StatelessRemote stateless = null;
   public void jspInit () 
   {
     try 
     {
       InitialContext ctx = new InitialContext();
       stateless = (StatelessRemote) ctx.lookup("jsp-test/StatelessBean/remote");
       System.out.println("Found StatelessRemote " + stateless);
       int i = stateless.method(10);
       System.out.println("Executed method " + i);
     } 
     catch( Exception e ) { e.printStackTrace(); }
    }
%>

<html>
  <head>
	<title>EJB3 JSP Test</title>
  </head>
  
  <body bgcolor=#ffffff text=#000000>
    <h1>EJB3 JSP Test</h1>
  </body>
</html>