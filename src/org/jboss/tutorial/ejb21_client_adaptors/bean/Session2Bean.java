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
package org.jboss.tutorial.ejb21_client_adaptors.bean;

import javax.ejb.Stateful;
import javax.ejb.Local;
import javax.ejb.Init;
import javax.ejb.LocalHome;

import org.jboss.annotation.ejb.LocalBinding;
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
