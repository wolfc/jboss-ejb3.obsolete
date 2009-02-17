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
package org.jboss.ejb3.test.reference21_30;

import javax.ejb.EJB;
import javax.ejb.EJBs;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteBindings;
import org.jboss.ejb3.annotation.RemoteHomeBinding;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 * @version <tt>$Revision$</tt>
 */
@Stateless(name="Test3")
@Remote(Test3Business.class)
@RemoteBindings({@RemoteBinding(jndiBinding="Test3Remote"),@RemoteBinding(jndiBinding="Test3/remote")})
@RemoteHome(Test3Home.class)
@RemoteHomeBinding(jndiBinding="Test3/home")
@EJBs({@EJB(name="injected/Test2",  mappedName="Test2")})
public class Test3Bean
   implements Test3Business
{
   private static final Logger log = Logger.getLogger(Test3Bean.class);
   
   @EJB(name="ejb/Test2")
   private Test2Home test2Home=null;
    
   public void testAccess() throws Exception
   {
      Test2 test2 = test2Home.create();
      try {
         InitialContext jndiContext = new InitialContext();
         Test2Home home = (Test2Home)jndiContext.lookup(Container.ENC_CTX_NAME + "/env/injected/Test2");
         test2 = home.create();
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
