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
package org.jboss.ejb3.test.statelesscreation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.Pool;
import org.jboss.ejb3.annotation.RemoteBinding;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateless
@Remote(DestroyRemote.class)
@RemoteBinding(clientBindUrl="socket://127.0.0.1:3875")
@Pool(value="ThreadlocalPool", maxSize=20, timeout=10000)
public class DestroyStatelessBean implements DestroyRemote
{
   private static final Logger log = Logger.getLogger(DestroyStatelessBean.class);
   
   private static int beanCount = 0;
   
   public int getBeanCount()
   {
      return beanCount;
   }
   
   @PostConstruct
   public void construct()
   {
      ++beanCount;
      log.trace("construct");
   }
   
   @PreDestroy
   public void destroy()
   {
      --beanCount;
      log.trace("destroy");
   }
}
