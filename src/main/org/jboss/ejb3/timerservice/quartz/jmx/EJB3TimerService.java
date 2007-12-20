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
package org.jboss.ejb3.timerservice.quartz.jmx;

import java.util.Properties;

import javax.management.ObjectName;

import org.jboss.ejb3.timerservice.quartz.QuartzTimerServiceFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * Comment
 *
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version $Revision: 61136 $
 */
public class EJB3TimerService extends ServiceMBeanSupport
   implements EJB3TimerServiceMBean
{
   private QuartzTimerServiceFactory delegate;
   private ObjectName dataSource;
   
   public EJB3TimerService()
   {
      delegate = new QuartzTimerServiceFactory();
   }
   
   @Override
   protected void createService() throws Exception
   {
      super.createService();
   }

   @Override
   protected void destroyService() throws Exception
   {
      //this.delegate.shutdown();
      super.destroyService();
   }
   
   /**
    * @jmx:managed-attribute
    * 
    * @return   the object name of the data source to use
    */
   public ObjectName getDataSource()
   {
      return dataSource;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public void setDataSource(ObjectName dataSource)
   {
      this.dataSource = dataSource;
   }
   
   /**
    * @jmx:managed-attribute
    * 
    * @param props
    */
   public void setProperties(final Properties props)
   {
      delegate.setProperties(props);
   }
   
   /**
    * @jmx:managed-attribute
    * 
    * @param props
    */
   public void setSqlProperties(Properties props)
   {
      delegate.setSqlProperties(props);
   }
   
   @Override
   protected void startService() throws Exception
   {
      super.startService();
      String jndiName = (String) server.getAttribute(dataSource, "BindName");
      delegate.setDataSource(jndiName);
      delegate.start();
   }
   
   @Override
   protected void stopService() throws Exception
   {
      delegate.stop();
      super.stopService();
   }
}
