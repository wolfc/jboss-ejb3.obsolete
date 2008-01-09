/*
  * JBoss, Home of Professional Open Source
  * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.ejb3.locator.client;

import javax.naming.NameNotFoundException;

/**
 * ServiceLocator
 * 
 * @version $Revision $
 * @author <a href="mailto:alr@alrubinger.com">ALR</a>
 */
public interface ServiceLocator
{

   /**
    * Obtains a stub to the the SLSB service with the specified business 
    * interface.  If this is the first request for this service, it will 
    * be obtained from JNDI and placed in a cache such that subsequent 
    * requests will not require the overhead of a JNDI lookup. 
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is no an interface 
    */
   public <T> T getStatelessBean(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException;

   /**
    * Obtains a stub to the the SFSB with the specified business 
    * interface.  This call will always result in a call to JNDI 
    * for a new stub; no caching will take place
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is no an interface 
    */
   public <T> T getStatefulBean(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException;

   /**
    * Obtains a stub to the the JMX (MBean, Singleton) service with 
    * the specified business interface.  If this is the first 
    * request for this service, it will be obtained from JNDI and 
    * placed in a cache such that subsequent requests will not 
    * require the overhead of a JNDI lookup.  Convenience
    * method; equivalent to <code>getStatelessBean</code>
    * 
    * @param <T>
    * @param clazz The business interface of the desired service
    * @return
    * @throws Ejb3NotFoundException 
    *   If no services implementing the specified business interface 
    *   could be found on any of the configured local/remote hosts
    * @throws IllegalArgumentException
    *   If the specified class is a business interface implemented by more than 
    *   one service across the configured local/remote hosts, or if the
    *   specified class is no an interface 
    */
   public <T> T getJmxService(Class<T> clazz) throws Ejb3NotFoundException, IllegalArgumentException;
   
   
   /**
    * Fetches the object bound at the specified JNDI Address
    * from the JNDI Host with the specified ID
    * 
    * @param hostId
    * @param jndiName
    * @return
    * @throws NameNotFoundException If the specified JNDI Address is 
    * 	not a valid binding for the specified host
    */
   public Object getObject(String hostId,String jndiName) throws NameNotFoundException;

}
