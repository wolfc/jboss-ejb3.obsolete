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
package org.jboss.ejb3.test.epcpropagation;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
@Stateful
@TransactionManagement(TransactionManagementType.BEAN)
@Remote(StatefulRemote.class)
public class InitEPCStatefulBean implements StatefulRemote
{
   private static final Logger log = Logger.getLogger(InitEPCStatefulBean.class);
   
   @PersistenceContext(type=PersistenceContextType.EXTENDED, unitName="mypc")
   EntityManager em;

   @Resource
   SessionContext sessionContext;

   @EJB(beanName="IntermediateStatefulBean")
   StatefulRemote cmtBean;
   
   public boolean execute(Integer id, String name) throws Exception
   {
      try
      {
         UserTransaction tx1 = sessionContext.getUserTransaction();
         tx1.begin();
         em.joinTransaction();
         MyEntity entity = em.find(MyEntity.class, id);
         entity.setName(name.toUpperCase());
         
         boolean result = cmtBean.execute(id, name.toLowerCase());
         tx1.commit();
         
         return result;
      }
      catch (Exception e)
      {
         try
         {
            sessionContext.getUserTransaction().rollback();
         }
         catch (Exception e1)
         {
            log.info("ROLLBACK: "+e1);
         }
         throw e;
      }
   }
}