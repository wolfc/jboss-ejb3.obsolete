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
package org.jboss.ejb3.test.proxy.common.ejb.sfsb;

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
 * 
 * MyStatefulBeanWithBindings
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateful
@Remote (MyStatefulRemoteBusiness.class)
@Local (MyStatefulLocalBusiness.class)
@RemoteHome (MyStatefulRemoteHome.class)
@LocalHome (MyStatefulLocalHome.class)
@RemoteBinding (jndiBinding=MyStatefulBeanWithBindings.REMOTE_JNDI_NAME)
@LocalBinding (jndiBinding=MyStatefulBeanWithBindings.LOCAL_JNDI_NAME)
@LocalHomeBinding (jndiBinding=MyStatefulBeanWithBindings.LOCAL_HOME_JNDI_NAME)
@RemoteHomeBinding (jndiBinding=MyStatefulBeanWithBindings.REMOTE_HOME_JNDI_NAME)
public class MyStatefulBeanWithBindings implements MyStatefulRemoteBusiness, MyStatefulLocalBusiness
{
   /**
    * Counter
    */
   private int counter;
   
   /**
    * Remote jndi name
    */
   public static final String REMOTE_JNDI_NAME = "AnyName";
   
   /**
    * Local jndi name
    */
   public static final String LOCAL_JNDI_NAME = "SomeLocalJndiName";
   
   /**
    * Local home jndi name
    */
   public static final String LOCAL_HOME_JNDI_NAME = "MyLocalHomeJndiName";
   
   /**
    * Remote home jndi name
    */
   public static final String REMOTE_HOME_JNDI_NAME = "SomeHome";
   
   /**
    * @see {@link MyStateful#getNextCounter()}
    */
   public int getNextCounter()
   {
      return counter ++;
   }

}
