/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.tutorial.reference21_30.bean;

import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.ejb.EJB;
import javax.ejb.EJBs;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.ejb3.Container;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
@Stateless(name="Stateless3")
@Remote(Stateless3.class)
@RemoteBinding(jndiBinding="Stateless3")
@RemoteHome(Stateless3Home.class)
@EJBs({@EJB(name="injected/Stateless2",  mappedName="Stateless2")})
public class Stateless3Bean
   implements Stateless3
{
   private static final Logger log = Logger.getLogger(Stateless3Bean.class);
   
   @EJB(name="ejb/Stateless2")
   private Stateless2Home stateless2Home=null;
    
   public void testAccess() throws Exception
   {
      Stateless2 test2 = stateless2Home.create();
      try {
         InitialContext jndiContext = new InitialContext();
         Stateless2Home home = (Stateless2Home)jndiContext.lookup(Container.ENC_CTX_NAME + "/env/injected/Stateless2");
         test2 = home.create();
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
