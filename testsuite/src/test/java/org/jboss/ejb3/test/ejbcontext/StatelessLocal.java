/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.ejbcontext;

import javax.ejb.EJBLocalObject;

public interface StatelessLocal extends EJBLocalObject
{
   String JNDI_NAME = "Stateless/local";
}
