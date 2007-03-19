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
package org.jboss.tutorial.quartz;

import org.jboss.annotation.ejb.ResourceAdapter;
import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.ejb.MessageDriven;
import javax.ejb.ActivationConfigProperty;

/**
 *
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bill@jboss.com">Bill Burke</a>
 */
@MessageDriven(activationConfig =
{
   @ActivationConfigProperty(propertyName="cronTrigger", propertyValue="0/2 * * * * ?")
})
@ResourceAdapter("quartz-ra.rar")
public class AnnotatedQuartzMDBBean implements Job
{
   private static final Logger log = Logger.getLogger(AnnotatedQuartzMDBBean.class);


   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
   {
      AnnotatedQuartzMDBBean.log.info("************** here in annotated!!!!");
   }
}
