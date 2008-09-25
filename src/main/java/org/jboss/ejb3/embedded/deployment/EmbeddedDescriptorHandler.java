/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.embedded.deployment;

import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.Ejb3DescriptorHandler;
import org.jboss.ejb3.mdb.ConsumerContainer;
import org.jboss.ejb3.mdb.MDB;
import org.jboss.ejb3.service.ServiceContainer;
import org.jboss.ejb3.stateful.StatefulContainer;
import org.jboss.metadata.ejb.jboss.JBossConsumerBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossServiceBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * Provide a wrapper around Ejb3DescriptorHandler to make it usable
 * in embedded.
 * 
 * Note that Ejb3DescriptorHandler is the legacy way of translating
 * meta data into annotations.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class EmbeddedDescriptorHandler extends Ejb3DescriptorHandler
{
   /**
    * @param deployment
    * @param dd
    */
   public EmbeddedDescriptorHandler(Ejb3Deployment deployment, JBossMetaData metaData)
   {
      super(deployment, metaData);
      
      for(JBossEnterpriseBeanMetaData bean : metaData.getEnterpriseBeans())
      {
         ejbNames.add(bean.getEjbName());
         ejbs.add(bean);
      }
   }

   public EJBContainer createEJBContainer(JBossEnterpriseBeanMetaData beanMetaData) throws Exception
   {
      int index = ejbNames.indexOf(beanMetaData.getEjbName());
      assert index != -1 : "Can't find bean " + beanMetaData.getEjbName() + " in " + ejbNames;
      
      EJB_TYPE ejbType = getEjbType(beanMetaData);
      
      className = beanMetaData.getEjbClass();
      ejbClass = di.getClassLoader().loadClass(className);
      if (ejbType == EJB_TYPE.STATELESS)
      {
         EJBContainer container = getStatelessContainer(index, (JBossSessionBeanMetaData) beanMetaData);
         container.setJaccContextId(getJaccContextId());
         return container;
      }
      else if (ejbType == EJB_TYPE.STATEFUL)
      {
         StatefulContainer container = getStatefulContainer(index, (JBossSessionBeanMetaData) beanMetaData);
         container.setJaccContextId(getJaccContextId());
         return container;
      }
      else if (ejbType == EJB_TYPE.MESSAGE_DRIVEN)
      {
         MDB container = getMDB(index, (JBossMessageDrivenBeanMetaData) beanMetaData);
         container.setJaccContextId(getJaccContextId());
         return container;
      }
      else if (ejbType == EJB_TYPE.SERVICE)
      {
         ServiceContainer container = getServiceContainer(index, (JBossServiceBeanMetaData) beanMetaData);
         container.setJaccContextId(getJaccContextId());
         return container;
      }
      else if (ejbType == EJB_TYPE.CONSUMER)
      {
         ConsumerContainer container = getConsumerContainer(index, (JBossConsumerBeanMetaData) beanMetaData);
         container.setJaccContextId(getJaccContextId());
         return container;
      }
      throw new UnsupportedOperationException("Can't create a container for type "  + ejbType);
   }
}
