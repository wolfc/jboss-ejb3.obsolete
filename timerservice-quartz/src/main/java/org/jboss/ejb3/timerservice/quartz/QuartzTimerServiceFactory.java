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
package org.jboss.ejb3.timerservice.quartz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import javax.ejb.TimerService;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.ejb3.timerservice.spi.TimedObjectInvoker;
import org.jboss.ejb3.timerservice.spi.TimerServiceFactory;
import org.jboss.logging.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.utils.DBConnectionManager;
import org.quartz.utils.JNDIConnectionProvider;

/**
 * Creates timer service objects for use in EJB3 containers. For this
 * two methods are provided: createTimerService and removeTimerService.
 * 
 * The factory can be started and stopped within both an embedded and full stack.
 * 
 * For now only one scheduler is supported. Each bean container has its own
 * job and trigger group within Quartz.
 * 
 * @author <a href="mailto:carlo@nerdnet.nl">Carlo de Wolf</a>
 * @version $Revision: 56116 $
 */
public class QuartzTimerServiceFactory implements TimerServiceFactory
{
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(QuartzTimerServiceFactory.class);
   
   private TransactionManager tm;
   
   private static Scheduler scheduler;
   
   private Properties properties;
   
   /**
    * Contains the sql statements to create the database schema.
    */
   private Properties sqlProperties;
   
   private void createSchema()
   {
      try
      {
         tm.begin();
         try
         {
            Connection conn = getConnection();
            try
            {
               boolean success = execute(conn, "CREATE_TABLE_JOB_DETAILS");
               if(success)
               {
                  execute(conn, "CREATE_TABLE_JOB_LISTENERS");
                  execute(conn, "CREATE_TABLE_TRIGGERS");
                  execute(conn, "CREATE_TABLE_SIMPLE_TRIGGERS");
                  execute(conn, "CREATE_TABLE_CRON_TRIGGERS");
                  execute(conn, "CREATE_TABLE_BLOB_TRIGGERS");
                  execute(conn, "CREATE_TABLE_TRIGGER_LISTENERS");
                  execute(conn, "CREATE_TABLE_CALENDARS");
                  execute(conn, "CREATE_TABLE_PAUSED_TRIGGER_GRPS");
                  execute(conn, "CREATE_TABLE_FIRED_TRIGGERS");
                  execute(conn, "CREATE_TABLE_SCHEDULER_STATE");
                  execute(conn, "CREATE_TABLE_LOCKS");
                  
                  execute(conn, "INSERT_TRIGGER_ACCESS");
                  execute(conn, "INSERT_JOB_ACCESS");
                  execute(conn, "INSERT_CALENDAR_ACCESS");
                  execute(conn, "INSERT_STATE_ACCESS");
                  execute(conn, "INSERT_MISFIRE_ACCESS");
               }
            }
            finally
            {
               conn.close();
            }
            tm.commit();
         }
         catch(SQLException e)
         {
            throw new RuntimeException(e);
         }
         catch (RollbackException e)
         {
            throw new RuntimeException(e);
         }
         catch (HeuristicMixedException e)
         {
            throw new RuntimeException(e);
         }
         catch (HeuristicRollbackException e)
         {
            throw new RuntimeException(e);
         }
         finally
         {
            if(tm.getStatus() == Status.STATUS_ACTIVE)
               tm.rollback();
         }
      }
      catch(SystemException e)
      {
         throw new RuntimeException(e);
      }
      catch (NotSupportedException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Create a TimerService for use in a bean container.
    * 
    * @param objectName the name of the bean container
    * @param invoker    the invoker to call on timeouts
    * @return           an EJB TimerService
    */
   public TimerService createTimerService(TimedObjectInvoker invoker)
   {
      Scheduler scheduler = getScheduler();
      if (scheduler == null) return null;
      
      TimedObjectInvokerRegistry.register(invoker);
      
      return new TimerServiceImpl(scheduler, invoker);
   }
   
   private void destroyAllTimers() throws SchedulerException
   {
      String groupNames[] = scheduler.getJobGroupNames();
      for(String groupName : groupNames)
      {
         String jobNames[] = scheduler.getJobNames(groupName);
         for(String jobName : jobNames)
         {
            scheduler.deleteJob(jobName, groupName);
         }
      }
   }
   
   private boolean execute(Connection conn, String stmtName) throws SQLException
   {
      String sql = sqlProperties.getProperty(stmtName);
      if(sql == null)
         throw new IllegalStateException("No sql set for '" + stmtName + "'");
      
      try
      {
         PreparedStatement stmt = conn.prepareStatement(sql);
         try
         {
            stmt.execute();
            return true;
         }
         finally
         {
            stmt.close();
         }
      }
      catch(SQLException e)
      {
         log.warn("sql failed: " + sql);
         if(log.isDebugEnabled())
            log.debug("sql failed: " + sql, e);
         return false;
      }
   }
   
   private Connection getConnection() throws SQLException
   {
      return DBConnectionManager.getInstance().getConnection("myDS");
   }
   
   /**
    * @return   the scheduler for package use
    */
   protected static Scheduler getScheduler()
   {
      if(scheduler == null)
      {
         return null;
         //throw new IllegalStateException("TimerServiceFactory hasn't been started yet");
      }
      
      return scheduler;
   }
   
   public void removeTimerService(TimerService aTimerService)
   {
      TimerServiceImpl timerService = (TimerServiceImpl) aTimerService;
      timerService.shutdown();
   }
   
   public void restoreTimerService(TimerService aTimerService)
   {
      // TODO: implement Quartz restore timer service
   }
   
   public void setDataSource(String jndiName)
   {
      if(jndiName == null)
         return;
      JNDIConnectionProvider connectionProvider = new JNDIConnectionProvider(jndiName, false);
      // FIXME: remove hardcoding
      DBConnectionManager.getInstance().addConnectionProvider("myDS", connectionProvider);
   }
   
   public void setProperties(final Properties props)
   {
//      if(scheduler != null)
//         throw new IllegalStateException("already started");
      
      // TODO: precondition the prop
      properties = props;
   }
   
   public void setSqlProperties(Properties props)
   {
      this.sqlProperties = props;
   }
   
   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
   }
   
   public synchronized void start() throws Exception
   {
      if(scheduler != null)
         throw new IllegalStateException("already started");
      
      log.debug("properties = " + properties);
      
      if(tm == null)
         throw new IllegalStateException("transactionManager is not set");
            
      createSchema();
      
      // TODO: bind in JNDI, or is this done by the JMX bean?
      SchedulerFactory factory;
      if(properties == null)
         factory = new StdSchedulerFactory();
      else
         factory = new StdSchedulerFactory(properties);
      scheduler = factory.getScheduler();
      
      // There is a bug in Quartz that makes it unable to restore timers
      destroyAllTimers();
      
      // TODO: really start right away?
      scheduler.start();
   }
   
   public synchronized void stop() throws Exception
   {
      if(scheduler == null)
         throw new IllegalStateException("already stopped");
      
      // TODO: unbind from JNDI
      
      // TODO: standby or shutdown?
      scheduler.shutdown();
      
      scheduler = null;
   }
   
   public void suspendTimerService(TimerService aTimerService)
   {
      TimerServiceImpl timerService = (TimerServiceImpl) aTimerService;
      try
      {
         removeTimerService(timerService);
      }
      finally
      {
         TimedObjectInvokerRegistry.unregister(timerService.getTimedObjectInvoker());
      }
   }
}
