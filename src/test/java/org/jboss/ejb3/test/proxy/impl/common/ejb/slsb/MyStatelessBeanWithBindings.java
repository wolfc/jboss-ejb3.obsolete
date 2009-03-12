/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.proxy.impl.common.ejb.slsb;

import javax.ejb.Local;
import javax.ejb.LocalHome;
import javax.ejb.Remote;
import javax.ejb.RemoteHome;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.LocalHomeBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.annotation.RemoteHomeBinding;

/**
 * 
 * MyStatelessBeanWithBindings
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote(MyStatelessRemote.class)
@Local(MyStatelessLocal.class)
@LocalHome (MyStatelessLocalHome.class)
@LocalBinding(jndiBinding = MyStatelessBeanWithBindings.LOCAL_JNDI_NAME)
@LocalHomeBinding(jndiBinding = MyStatelessBeanWithBindings.LOCAL_HOME_JNDI_NAME)
@RemoteHome(MyStatelessRemoteHome.class)
@RemoteBinding(jndiBinding = MyStatelessBeanWithBindings.REMOTE_JNDI_NAME)
@RemoteHomeBinding(jndiBinding = MyStatelessBeanWithBindings.REMOTE_HOME_JNDI_NAME)
public class MyStatelessBeanWithBindings implements MyStatelessLocal, MyStatelessRemote
{

   /**
    * Remote JNDI Name. 
    */
   public static final String REMOTE_JNDI_NAME = "SomeRemoteName";

   /**
    * Local JNDI name.
    */
   public static final String LOCAL_JNDI_NAME = "MyLocalJNDIName";

   /**
    * Local Home JNDI Name.
    */
   public static final String LOCAL_HOME_JNDI_NAME = "MyLocalHomeJNDIName";

   /**
    * Remote home JNDI Name 
    */
   public static final String REMOTE_HOME_JNDI_NAME = "MyRemoteHomeJNDIName";

   /**
    * @see {@link MyStateless#sayHi(String)}
    */
   public String sayHi(String name)
   {
      return "Hi " + name;
   }

}
