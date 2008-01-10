/*
* JBoss, Home of Professional Open Source
* Copyright 2006, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.ejb3;

import org.jboss.naming.ENCFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 1.1 $
 */
public class DefaultEjbEncFactory implements EjbEncFactory
{

   public Context getEnc(EJBContainer container)
   {
      pushEnc(container);
      InitialContext ctx = container.getInitialContext();
      try
      {
         return (Context)ctx.lookup("java:comp");
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         popEnc(container);   
      }
   }

   public void pushEnc(EJBContainer container)
   {
      ENCFactory.pushContextId(container.getObjectName());
   }

   public void popEnc(EJBContainer container)
   {
      ENCFactory.popContextId();
   }


   public void cleanupEnc(EJBContainer container)
   {
      ENCFactory.getEncById().remove(container.getObjectName());
   }
}
