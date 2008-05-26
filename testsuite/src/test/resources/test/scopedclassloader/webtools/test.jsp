<%@ page import="java.util.Hashtable,
                 javax.ejb.EJBNoSuchObjectException,
                 javax.naming.InitialContext,
                 javax.naming.NamingException" %>
<%@ page import="org.jboss.ejb3.test.regression.scopedclassloader.StatelessRemote"%>
<%@ page import="org.jboss.ejb3.test.regression.scopedclassloader.ValueObject"%>

<%!

%>

<html>
<body>
<%
   InitialContext ctx = new InitialContext();
   StatelessRemote remote = (StatelessRemote)ctx.lookup(StatelessRemote.class.getName());
   ValueObject vo = remote.doit();

%>
DONE!
</body>
</html>