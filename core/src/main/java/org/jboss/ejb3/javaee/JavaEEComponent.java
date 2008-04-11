/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.ejb3.javaee;

/**
 * A JavaEE component (could be EJB container, client container etc)
 * 
 * EE 2.2
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @version $Revision$
 */
public interface JavaEEComponent
{
   /**
    * Create an object name for the given EJB name in the same JavaEE module.
    * 
    * @param ejbLink
    * @return
    */
   String createObjectName(String ejbName);

   /**
    * Create an object name for the given EJB name in another JavaEE module.
    * 
    * @param unitName
    * @param ejbName
    * @return
    */
   String createObjectName(String unitName, String ejbName);
   
   /**
    * Returns the module of which this component is a part.
    */
   JavaEEModule getModule();
}
