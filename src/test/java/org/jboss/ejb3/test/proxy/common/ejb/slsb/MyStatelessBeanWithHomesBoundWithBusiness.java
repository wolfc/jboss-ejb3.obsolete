/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.common.ejb.slsb;

import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.LocalHomeBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteHomeBinding;

/**
 * MyStatelessBeanWithHomesBoundWithBusiness
 * 
 * A test SLSB where the default local and default remote business
 * interfaces are bound to the same addresses as the local home and
 * remote home interfaces, respectively
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Remote(MyStatelessRemote.class)
@Local(MyStatelessLocal.class)
@RemoteHome(MyStatelessRemoteHome.class)
@LocalHome(MyStatelessLocalHome.class)
@RemoteBinding(jndiBinding = MyStatelessBeanWithHomesBoundWithBusiness.REMOTE_JNDI_NAME)
@LocalBinding(jndiBinding = MyStatelessBeanWithHomesBoundWithBusiness.LOCAL_JNDI_NAME)
@LocalHomeBinding(jndiBinding = MyStatelessBeanWithHomesBoundWithBusiness.LOCAL_JNDI_NAME)
@RemoteHomeBinding(jndiBinding = MyStatelessBeanWithHomesBoundWithBusiness.REMOTE_JNDI_NAME)
public class MyStatelessBeanWithHomesBoundWithBusiness implements MyStatelessRemote, MyStatelessLocal
{
   /**
    * Remote Business / Remote Home JNDI Name
    */
   public static final String REMOTE_JNDI_NAME = "MyStatelessBeanWithHomesBoundWithBusiness/allRemotes";

   /**
    * Local Business / Local Home JNDI Name
    */
   public static final String LOCAL_JNDI_NAME = "MyStatelessBeanWithHomesBoundWithBusiness/allLocals";

   /**
    * @see {@link MyStateless#sayHi(String)}
    */
   public String sayHi(String name)
   {
      return "Hi " + name;
   }

}
