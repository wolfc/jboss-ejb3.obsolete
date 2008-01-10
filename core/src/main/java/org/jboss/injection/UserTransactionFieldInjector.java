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
package org.jboss.injection;

import java.lang.reflect.Field;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;
import org.jboss.aop.Advisor;
import org.jboss.ejb3.BeanContext;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.tx.TxUtil;
import org.jboss.ejb3.tx.UserTransactionImpl;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 * @deprecated use UserTransactionPropertyInjector
 */
public class UserTransactionFieldInjector implements Injector
{
   private Field field;

   public UserTransactionFieldInjector(Field field, InjectionContainer container)
   {
      if (container instanceof Container)
      {
         TransactionManagementType type = TxUtil.getTransactionManagementType(((Advisor) container));
         if (type != TransactionManagementType.BEAN)
            throw new IllegalStateException("Container " + ((Container) container).getEjbName() + ": it is illegal to inject UserTransaction into a CMT bean");
      }
      this.field = field;
      this.field.setAccessible(true);
   }

   public void inject(BeanContext ctx)
   {
      Object instance = ctx.getInstance();
      inject(instance);
   }

   public void inject(Object instance)
   {
      UserTransaction ut = new UserTransactionImpl();
      try
      {
         field.set(instance, ut);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);  //To change body of catch statement use Options | File Templates.
      }
      catch (IllegalArgumentException e)
      {
         throw new RuntimeException("Failed in setting EntityManager on setter field: " + field.toString());
      }
   }

   public Class getInjectionClass()
   {
      return field.getType();
   }
}
