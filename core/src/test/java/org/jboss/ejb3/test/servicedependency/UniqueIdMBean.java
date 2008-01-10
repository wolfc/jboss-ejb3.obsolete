/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.servicedependency;

import java.util.UUID;

/**
 * A UUID.
 * 
 * @author <a href="galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision$
 */
public interface UniqueIdMBean
{
  public UUID generate();
  
  public void create () throws Exception;
  public void start () throws Exception;
  public void stop () throws Exception;
  public void destroy () throws Exception;
  
}
