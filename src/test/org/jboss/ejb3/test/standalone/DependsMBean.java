/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.standalone;

/**
 * @version <tt>$Revision: 44596 $</tt>
 * @author <a href="mailto:bdecoste@jboss.com">William DeCoste</a>
 */
public interface DependsMBean
{
  public void create () throws Exception;
  public void start () throws Exception;
  public void stop () throws Exception;
  public void destroy () throws Exception;
  
}
