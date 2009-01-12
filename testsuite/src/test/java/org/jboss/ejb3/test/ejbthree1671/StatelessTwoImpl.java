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
package org.jboss.ejb3.test.ejbthree1671;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * StatelessTwoImpl
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@Stateless
@Remote(StatelessTwo.class)
@RemoteBinding(jndiBinding = StatelessTwoImpl.JNDI_NAME)
public class StatelessTwoImpl implements StatelessTwo
{

   /**
    * Exposing this through the bean implementation, exposes the
    * impl to the client. But we are just doing some tests, so 
    * doesn't matter
    * 
    * JNDI Name
    */
   public static final String JNDI_NAME = "DoesNotMatter";

   /**
    * @see StatelessTwo#add(int, int)
    */
   public int add(int a, int b)
   {
      return a + b;
   }

}
