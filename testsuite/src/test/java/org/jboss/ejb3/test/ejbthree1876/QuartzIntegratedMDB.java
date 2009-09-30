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
package org.jboss.ejb3.test.ejbthree1876;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.ejb3.test.ejbthree1876.unit.EJBInvocationClassLoaderTestCase;
import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * QuartzIntegratedMDB
 *
 * MDB used in test case for EJBTHREE-1876 {@link EJBInvocationClassLoaderTestCase}
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
@MessageDriven(activationConfig =
{@ActivationConfigProperty(propertyName = "cronTrigger", propertyValue = "*/2 * * * * ?")})
@ResourceAdapter("quartz-ra.rar")
public class QuartzIntegratedMDB implements Job
{

   /**
    * Logger
    */
   private static Logger logger = Logger.getLogger(QuartzIntegratedMDB.class);

   /**
    * Bean
    */
   @EJB
   private StatelessRemote bean;

   /**
    * Uses the injected bean to invoke an method on it
    */
   public void execute(JobExecutionContext jobContext) throws JobExecutionException
   {
      logger.info("Job " + jobContext + " at " + System.currentTimeMillis());
      try
      {
         // just call the bean
         bean.doNothing();
         logger.debug("Bean successfully invoked in MDB");
         // worked fine, so set a state in a singleton
         ResultTracker.getInstance().setPassed();
      }
      catch (Exception e)
      {
         logger.error("Exception in MDB: ", e);
         ResultTracker.getInstance().setException(e);
      }

   }

}
