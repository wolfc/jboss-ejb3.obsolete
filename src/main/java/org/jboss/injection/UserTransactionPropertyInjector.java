/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.injection;

import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;

import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.ejb3.tx.UserTransactionImpl;
import org.jboss.injection.lang.reflect.BeanProperty;

/**
 * Injects a user transaction into a bean property.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class UserTransactionPropertyInjector extends AbstractPropertyInjector
{
   public UserTransactionPropertyInjector(BeanProperty property, InjectionContainer container)
   {
      super(property);
      
      if (container instanceof EJBContainer)
      {
         TransactionManagementType type = TxUtil.getTransactionManagementType(((EJBContainer) container).getAdvisor());
         if (type != TransactionManagementType.BEAN)
            throw new IllegalStateException("Container " + ((Container) container).getEjbName() + ": it is illegal to inject UserTransaction into a CMT bean");
      }
   }

   public void inject(Object instance)
   {
      UserTransaction ut = new UserTransactionImpl();
      property.set(instance, ut);
   }
}
