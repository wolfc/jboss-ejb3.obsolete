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
package org.jboss.ejb3.core.test.ejbthree1738;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.annotation.Service;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@Service
public class TransactionalServiceBean implements TransactionalServiceLocal
{
   private static final Logger log = Logger.getLogger(TransactionalServiceBean.class);
   
   @Resource(mappedName="java:/TransactionManager")
   private TransactionManager tm;
   
   public static int postConstructs = 0;
   
   private boolean startCalledInTx = false;
   
   @PostConstruct
   public void postConstruct()
   {
      log.info("postConstruct");
      //new Exception("postConstruct").printStackTrace();
      postConstructs++;
   }
   
   /**
    * magic start
    */
   public void start()
   {
      log.info("start");
      //new Exception("start").printStackTrace();
      try
      {
         final Transaction tx = tm.getTransaction();
         log.info("tx = " + tx);
         if(tx == null){
            throw new IllegalStateException("EJBTHREE-1738: was expecting a tx to be present");
         }
         startCalledInTx = true;
      }
      catch(SystemException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /*
    * (non-Javadoc)
    * @see org.jboss.ejb3.core.test.ejbthree1738.TransactionalServiceLocal#isStartCalledInTx()
    */
   public boolean isStartCalledInTx()
   {
      return this.startCalledInTx;
   }
}
