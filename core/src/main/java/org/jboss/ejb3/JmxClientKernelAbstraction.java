/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
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

import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.util.JMXExceptionDecoder;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public class JmxClientKernelAbstraction implements ClientKernelAbstraction
{
   private static final Logger log = Logger.getLogger(JmxKernelAbstraction.class);

   private MBeanServerConnection server;

   public JmxClientKernelAbstraction(MBeanServerConnection server)
   {
      this.server = server;
   }
   
   private Exception decode(Exception e)
   {
      Throwable t = JMXExceptionDecoder.decode(e);
      if(t instanceof Exception)
         return (Exception) t;
      throw (Error) t;
   }
   
   public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws Exception
   {
      try
      {
         return server.invoke(name, operationName, params, signature);
      }
      catch(Exception e)
      {
         throw decode(e);
      }
   }
   
   public Object getAttribute(ObjectName name, String attribute) throws Exception
   {
      try
      {
         return server.getAttribute(name, attribute);
      }
      catch(Exception e)
      {
         throw decode(e);
      }
   }
   
   public Set getMBeans(ObjectName query) throws Exception
   {
      Set mbeans = server.queryMBeans(query, null);
      
      return mbeans;
   }
}
