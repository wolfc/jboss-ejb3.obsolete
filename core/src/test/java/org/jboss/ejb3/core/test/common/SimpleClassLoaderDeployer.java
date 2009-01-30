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
package org.jboss.ejb3.core.test.common;

import org.jboss.deployers.spi.deployer.helpers.AbstractTopLevelClassLoaderDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * TODO: Consilidate with org.jboss.ejb3.test.tx.common.SimpleClassLoaderDeployer
 * 
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class SimpleClassLoaderDeployer extends AbstractTopLevelClassLoaderDeployer
{
   @Override
   protected ClassLoader createTopLevelClassLoader(DeploymentUnit unit) throws Exception
   {
      return Thread.currentThread().getContextClassLoader();
   }
}