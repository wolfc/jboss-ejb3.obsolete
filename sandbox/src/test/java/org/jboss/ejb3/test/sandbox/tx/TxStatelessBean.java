/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.test.sandbox.tx;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.jboss.ejb3.sandbox.interceptorcontainer.ContainerInterceptors;
import org.jboss.ejb3.sandbox.local.LocalBusinessInterfaceInterceptor;
import org.jboss.ejb3.sandbox.stateless.StatelessInterceptor;
import org.jboss.logging.Logger;

/**
 * Comment
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@ContainerInterceptors({ LocalBusinessInterfaceInterceptor.class, TxInterceptor.class, StatelessInterceptor.class })
@Stateless
public class TxStatelessBean implements TxStatelessLocal
{
   private static final Logger log = Logger.getLogger(TxStatelessBean.class);
   
   @TransactionAttribute(TransactionAttributeType.MANDATORY)
   public String sayHi(String name)
   {
      log.info("sayHi " + name);
      return "Hi " + name;
   }
}
