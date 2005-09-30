/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.ejb21_client_adaptors.bean;

import javax.ejb.EJBObject;

/**
 * @version <tt>$Revision$</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public interface Session1Remote extends EJBObject
{
   String getInitValue();
   
   String getLocalSession2InitValue();
}

