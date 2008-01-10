<%@ page import="java.util.Hashtable,
                 javax.jms.Queue" %>
<%@ page import="javax.jms.QueueConnection" %>
<%@ page import="javax.jms.QueueConnectionFactory" %>
<%@ page import="javax.jms.QueueSender" %>
<%@ page import="javax.jms.QueueSession" %>
<%@ page import="javax.jms.TextMessage" %>
<%@ page import="javax.jms.Topic" %>
<%@ page import="javax.jms.TopicConnection" %>
<%@ page import="javax.jms.TopicConnectionFactory" %>
<%@ page import="javax.jms.TopicPublisher" %>
<%@ page import="javax.jms.TopicSession" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="org.jboss.ejb3.test.wls.embeddedwar.Customer" %>
<%@ page import="org.jboss.ejb3.test.wls.embeddedwar.CustomerDAOLocal" %>
<%@ page import="org.jboss.ejb3.test.wls.embeddedwar.CustomerDAORemote" %>

<%!

   public static InitialContext getInitialContext() throws Exception
   {
      Hashtable props = getInitialContextProperties();
      return new InitialContext(props);
  //	  return new InitialContext();
   }

   private static Hashtable getInitialContextProperties()
   {
      Hashtable props = new Hashtable();
      props.put("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
      props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
      return props;
   }
      
   private static void executeQueue()
           throws Exception
   {

      QueueConnection cnn = null;
      QueueSender sender = null;
      QueueSession session = null;

      Queue queue = (Queue) getInitialContext().lookup("queue/mdbtest");
      QueueConnectionFactory factory = (QueueConnectionFactory) getInitialContext().lookup("java:/ConnectionFactory");
      cnn = factory.createQueueConnection();
      session = cnn.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createSender(queue);
      sender.send(msg);

      Thread.sleep(1000);
      session.close();
      cnn.close();
   }

   private static void executeTopic()
           throws Exception
   {

      TopicConnection cnn = null;
      TopicPublisher sender = null;
      TopicSession session = null;

      Topic topic = (Topic) getInitialContext().lookup("topic/topictest");
      TopicConnectionFactory factory = (TopicConnectionFactory) getInitialContext().lookup("java:/ConnectionFactory");
      cnn = factory.createTopicConnection();
      session = cnn.createTopicSession(false, QueueSession.AUTO_ACKNOWLEDGE);

      TextMessage msg = session.createTextMessage("Hello World");

      sender = session.createPublisher(topic);
      sender.send(msg);

      Thread.sleep(1000);
      session.close();
      cnn.close();
   }
%>

<html>
<body>
<%
   InitialContext ctx = getInitialContext();
   CustomerDAOLocal local = (CustomerDAOLocal) ctx.lookup(CustomerDAOLocal.class.getName());
   CustomerDAORemote remote = (CustomerDAORemote) ctx.lookup(CustomerDAORemote.class.getName());

   int id = local.createCustomer("Gavin");
   Customer cust = local.findCustomer(id);
%>
<p>
   Successfully created and found Gavin from @Local interface: <%=cust.getName()%>
</p>
<%
   id = remote.createCustomer("Emmanuel");
   cust = remote.findCustomer(id);
%>

<p>
   Successfully created and found Emmanuel from @Remote interface
</p>

<%
   executeQueue();
   executeTopic();
%>
DONE!
</body>
</html>