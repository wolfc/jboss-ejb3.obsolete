<%@ page import="java.util.Hashtable,
                 javax.ejb.EJBNoSuchObjectException,
                 javax.naming.InitialContext,
                 javax.naming.NamingException,
                 org.jboss.ejb3.test.standalone.Customer,
                 org.jboss.ejb3.test.standalone.CustomerDAO,
                 org.jboss.ejb3.test.standalone.ShoppingCart" %>

<%!

   private void executeEJBs(InitialContext ctx)
           throws NamingException
   {
      CustomerDAO local = (CustomerDAO) ctx.lookup(CustomerDAO.class.getName());
      long id = local.createCustomer();
      Customer cust = local.findCustomer(id);

      if (!"Bill".equals(cust.getName())) throw new RuntimeException("FAILURE");

      ShoppingCart cart = (ShoppingCart) ctx.lookup(ShoppingCart.class.getName());
      cart.getCart().add("beer");
      cart.getCart().add("wine");
      if (2 != cart.getCart().size()) throw new RuntimeException("FAILURE");

      cart.checkout();

      boolean exceptionThrown = false;
      try
      {
         cart.getCart();
      }
      catch (EJBNoSuchObjectException e)
      {
         exceptionThrown = true;
      }
      if (!exceptionThrown) throw new RuntimeException("Exception should have been thrown");
   }


%>

<html>
<body>
<%
   Hashtable properties = new Hashtable();
   properties.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
   properties.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
   executeEJBs(new InitialContext(properties));
%>
DONE!
</body>
</html>