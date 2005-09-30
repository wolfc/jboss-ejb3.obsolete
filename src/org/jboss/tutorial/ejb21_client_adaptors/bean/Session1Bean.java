/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.ejb21_client_adaptors.bean;

import javax.naming.*;
import javax.annotation.EJB;
import javax.annotation.EJBs;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.Local;
import javax.ejb.Init;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteHome;
import org.jboss.ejb3.Container;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateful(name="Session1")
@Remote(Session1Remote.class)
@RemoteHome(Session1RemoteHome.class)
@RemoteBinding(jndiBinding = "Session1Remote")
@EJBs({@EJB(name="session2", businessInterface=org.jboss.tutorial.ejb21_client_adaptors.bean.Session2Local.class, beanName="Session2")})
public class Session1Bean 
{
   private static final Logger log = Logger.getLogger(Session1Bean.class);
   
   private String initValue = null;
   
   public String getInitValue()
   {
      return initValue;
   }
   
   public String getLocalSession2InitValue() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Session2LocalHome home = (Session2LocalHome)jndiContext.lookup(Container.ENC_CTX_NAME + "/env/session2");
      Session2Local session2 = home.create("initialized");
      return session2.getInitValue();
   }
   
   @Init
   public void ejbCreate()
   {
      initValue = "initialized";
   }
   
}
