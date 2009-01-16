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
 * MyStatefulBeanWithHomesBoundWithBusiness
 * 
 * A test SFSB where the default local and default remote business
 * interfaces are bound to the same addresses as the local home and
 * remote home interfaces, respectively
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @version $Revision: $
 */
@Stateful
@Remote (MyStatefulRemoteBusiness.class)
@Local (MyStatefulLocalBusiness.class)
@RemoteHome (MyStatefulRemoteHome.class)
@LocalHome (MyStatefulLocalHome.class)
@RemoteBinding (jndiBinding=MyStatefulBeanWithHomesBoundWithBusiness.REMOTE_JNDI_NAME)
@LocalBinding (jndiBinding=MyStatefulBeanWithHomesBoundWithBusiness.LOCAL_JNDI_NAME)
@LocalHomeBinding (jndiBinding=MyStatefulBeanWithHomesBoundWithBusiness.LOCAL_JNDI_NAME)
@RemoteHomeBinding (jndiBinding=MyStatefulBeanWithHomesBoundWithBusiness.REMOTE_JNDI_NAME)
public class MyStatefulBeanWithHomesBoundWithBusiness extends MyStatefulBeanBase implements MyStatefulRemoteBusiness, MyStatefulLocalBusiness
{
   /**
    * Remote Business / Remote Home JNDI Name
    */
   public static final String REMOTE_JNDI_NAME = "MyStatefulBeanWithHomesBoundWithBusiness/allRemotes";
   
   /**
    * Local Business / Local Home JNDI Name
    */
   public static final String LOCAL_JNDI_NAME = "MyStatefulBeanWithHomesBoundWithBusiness/allLocals";

}
