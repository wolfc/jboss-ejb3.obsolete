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
package org.jboss.ejb3.endpoint.deployers;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.metadata.ear.spec.EarMetaData;

/**
 * DefaultEJBIdentifier
 *
 * Default implementation of an EJB Identifier; returns the name under
 * which a specified EJB (within some scoped DeploymentUnit) is bound into
 * MC 
 *
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class DefaultEJBIdentifier implements EJBIdentifier
{

   private static final Logger log = Logger.getLogger(DefaultEJBIdentifier.class);

   /* (non-Javadoc)
    * @see org.jboss.ejb3.endpoint.deployers.EJBIdentifier#identifyEJB(org.jboss.deployers.structure.spi.DeploymentUnit, java.lang.String)
    */
   public String identifyEJB(DeploymentUnit unit, String ejbName)
   {
      // TODO the base ejb3 jmx object name comes from Ejb3Module.BASE_EJB3_JMX_NAME, but
      // we don't need any reference to ejb3-core. Right now just hard code here, we need
      // a better way/place for this later
      StringBuilder containerName = new StringBuilder("jboss.j2ee:service=EJB3" + ",");

      // Get the top level unit for this unit (ex: the top level might be an ear and this unit might be the jar
      // in that ear
      DeploymentUnit toplevelUnit = unit.getTopLevel();
      if (toplevelUnit != null)
      {
         // if top level is an ear, then create the name with the ear reference
         if (toplevelUnit.getAttachment(EarMetaData.class) != null)
         {
            containerName.append("ear=");
            containerName.append(toplevelUnit.getSimpleName());
            containerName.append(",");

         }
      }
      // now work on the passed unit, to get the jar name
      if (unit.getSimpleName() == null)
      {
         containerName.append("*");
      }
      else
      {
         containerName.append("jar=");
         containerName.append(unit.getSimpleName());
      }
      // now the ejbname
      containerName.append(",name=");
      containerName.append(ejbName);

      log.info("Container name generated for ejb = " + ejbName + " in unit " + unit + " is " + containerName);

      try
      {
         ObjectName containerJMXName = new ObjectName(containerName.toString());
         return containerJMXName.getCanonicalName();
      }
      catch (MalformedObjectNameException e)
      {
         throw new RuntimeException(e);
      }
   }

}
