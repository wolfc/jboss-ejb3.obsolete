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
package org.jboss.ejb3;

import java.util.Collection;

import javax.naming.NameNotFoundException;

import org.jboss.ejb3.javaee.JavaEEApplication;

/**
 * Abstraction for accessing contents of an EAR
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision$
 */
public interface DeploymentScope extends JavaEEApplication
{
   public Collection<Ejb3Deployment> getEjbDeployments();
   void register(Ejb3Deployment deployment);
   void unregister(Ejb3Deployment deployment);

   /**
    *  Find a deployment based on its relative deployment name
    *
    * @param relativeName expects "../foo.jar"  so expects the .. in front
    * @return
    */
   Ejb3Deployment findRelativeDeployment(String relativeName);
   /**
    * Obtain the EJBContainer best matching the business interface
    * @param businessIntf - the business interface to match
    * @param vfsContext - the vfs path to the deploment initiating the request
    * @return the matching EJBContainer if found, null otherwise.
    */
   EJBContainer getEjbContainer(Class businessIntf, String vfsContext)
      throws NameNotFoundException;
   /**
    * Obtain the EJBContainer best matching the business interface
    * @param ejbLink - the referencing ejb-link
    * @param businessIntf - the business interface to match
    * @param vfsContext - the vfs path to the deploment initiating the request
    * @return the matching EJBContainer if found, null otherwise.
    */
   EJBContainer getEjbContainer(String ejbLink, Class businessIntf, String vfsContext);

   String getShortName();
   String getBaseName();
}
