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
import org.jboss.annotation.ejb.LocalHome;
import org.jboss.ejb3.Container;
import org.jboss.logging.Logger;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateful(name="Session2")
@Local(Session2Local.class)
@LocalHome(Session2LocalHome.class)
@LocalBinding(jndiBinding = "Session2Local")
public class Session2Bean 
{
   private static final Logger log = Logger.getLogger(Session2Bean.class);
   
   private String initValue = null;
   
   public String getInitValue()
   {
      return initValue;
   }
   
   @Init
   public void ejbCreate(String initValue)
   {
      this.initValue = initValue;
   }
   
}
