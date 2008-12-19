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
package org.jboss.ejb3.core.test.common.security;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import org.jboss.security.authorization.PolicyRegistration;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimplePolicyRegistration implements PolicyRegistration, Serializable
{
   private static final long serialVersionUID = 1L;

   /* (non-Javadoc)
    * @see org.jboss.security.authorization.PolicyRegistration#deRegisterPolicy(java.lang.String, java.lang.String)
    */
   public void deRegisterPolicy(String contextID, String type)
   {
      // TODO Auto-generated method stub
      //
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.security.authorization.PolicyRegistration#getPolicy(java.lang.String, java.lang.String, java.util.Map)
    */
   public <T> T getPolicy(String contextID, String type, Map<String, Object> contextMap)
   {
      // TODO Auto-generated method stub
      //return null;
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.security.authorization.PolicyRegistration#registerPolicy(java.lang.String, java.lang.String, java.net.URL)
    */
   public void registerPolicy(String contextID, String type, URL location)
   {
      // TODO Auto-generated method stub
      //
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.security.authorization.PolicyRegistration#registerPolicy(java.lang.String, java.lang.String, java.io.InputStream)
    */
   public void registerPolicy(String contextID, String type, InputStream stream)
   {
      // TODO Auto-generated method stub
      //
      throw new RuntimeException("NYI");
   }

   /* (non-Javadoc)
    * @see org.jboss.security.authorization.PolicyRegistration#registerPolicyConfigFile(java.lang.String, java.lang.String, java.io.InputStream)
    */
   public void registerPolicyConfigFile(String contextId, String type, InputStream stream)
   {
      // TODO Auto-generated method stub
      //
      throw new RuntimeException("NYI");
   }

}
