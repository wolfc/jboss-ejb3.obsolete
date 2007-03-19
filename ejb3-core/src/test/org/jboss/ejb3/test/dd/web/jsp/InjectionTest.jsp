<%@ page import="java.util.Hashtable,
                 javax.naming.*" %>
<%@ page import="java.lang.reflect.Proxy"%>
<%@ page import="org.jboss.ejb3.Ejb3Registry"%>
<%@ page import="org.jboss.ejb3.Container"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="javax.ejb.EJB"%>
<%@ page import="org.jboss.ejb3.test.dd.web.interfaces.Session30"%>
<%@ page import="javax.persistence.PersistenceContext"%>
<%@ page import="javax.persistence.EntityManager"%>
<%@ page import="javax.persistence.PersistenceUnit"%>
<%@ page import="javax.persistence.EntityManagerFactory"%>
<%@ page import="javax.annotation.Resource"%>
<%@ page import="javax.transaction.UserTransaction"%>
<%@ page import="org.jboss.ejb3.test.dd.web.ejb.Address"%>

<%!

   @EJB Session30 injectedSession30;
   @PersistenceContext(unitName="../dd-web-ejbs.jar#tempdb") EntityManager injectedEntityManager;
   @PersistenceUnit(unitName="../dd-web-ejbs.jar#tempdb") EntityManagerFactory injectedEntityManagerFactory;
   @Resource int nonOverridentConstant = 5;
   @Resource(name="overridenConstant") int overridenConstant = 1;
   @Resource UserTransaction tx;

%>

<html>
<body>
<%
   System.out.println("HERE!!!!!!!!!!!!!!!!!!!!!!!!");
   String access = injectedSession30.access();
   System.out.println("After injectSession30.access() HERE!!!!!!!!!!!!!!!!!!!!!!!!");

   Address address = new Address();
   address.setStreet("Clarendon Street");
   address.setCity("Boston");
   address.setState("MA");
   address.setZip("02116");

   Address address2 = new Address();
   address.setStreet("Clarendon Street");
   address.setCity("Boston");
   address.setState("MA");
   address.setZip("02116");

   Address address3 = new Address();
   address.setStreet("Clarendon Street");
   address.setCity("Boston");
   address.setState("MA");
   address.setZip("02116");

   //tx = (UserTransaction)ctx.lookup("UserTransaction");
   EntityManager em = injectedEntityManagerFactory.createEntityManager();
   tx.begin();
   injectedEntityManager.persist(address2);
   em.persist(address3);
   tx.commit();
   em.close();
%>
<h1>Test passed!</h1>
</body>
</html>