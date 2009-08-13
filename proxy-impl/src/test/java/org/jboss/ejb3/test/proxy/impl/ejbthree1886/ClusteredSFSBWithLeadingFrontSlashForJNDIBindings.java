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
package org.jboss.ejb3.test.proxy.impl.ejbthree1886;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulLocalBusiness;
import org.jboss.ejb3.test.proxy.impl.common.ejb.sfsb.MyStatefulRemoteBusiness;

@Stateful
@RemoteBinding(jndiBinding = ClusteredSFSBWithLeadingFrontSlashForJNDIBindings.REMOTE_JNDI_NAME)
@LocalBinding(jndiBinding = ClusteredSFSBWithLeadingFrontSlashForJNDIBindings.LOCAL_JNDI_NAME)
@Remote(MyStatefulRemoteBusiness.class)
@Local(MyStatefulLocalBusiness.class)
public class ClusteredSFSBWithLeadingFrontSlashForJNDIBindings implements MyStatefulRemoteBusiness
{
   public static final String REMOTE_JNDI_NAME = "/EJBTHREE-1886-ClusteredSFSBRemoteJNDIName";

   public static final String LOCAL_JNDI_NAME = "/EJBTHREE-1886-ClusteredSFSBLocalJNDIName";

   private int counter;

   public int getNextCounter()
   {
      return counter++;
   }

}
