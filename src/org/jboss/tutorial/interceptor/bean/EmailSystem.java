/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tutorial.interceptor.bean;

import javax.ejb.Remote;

@Remote
public interface EmailSystem
{
   void emailLostPassword(String username);
}
