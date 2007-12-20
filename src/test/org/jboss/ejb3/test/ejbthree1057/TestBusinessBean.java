/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbthree1057;

import javax.annotation.Resource;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

@Stateless
@Local(TestBusinessLocal.class)
@Remote(TestBusinessRemote.class)
@LocalBinding(jndiBinding=TestBusinessLocal.JNDI_NAME_LOCAL)
@RemoteBinding(jndiBinding=TestBusinessRemote.JNDI_NAME_REMOTE)
public class TestBusinessBean implements TestBusinessLocal, TestBusinessRemote
{
   
   // Instance Members
   @Resource
   private SessionContext ctx;

   // Required Implementations
   
   public EJBLocalObject testGetEjbLocalObject()
   {
      return ctx.getEJBLocalObject();
   }

   public EJBObject testGetEjbObject()
   {
      return ctx.getEJBObject();
   }

   
}
