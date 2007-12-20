/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb3.test.servicedependency;

/**
 * A PinNumber.
 * 
 * @author <a href="galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 44487 $
 */
public interface PinNumberMBean
{
  public short createRandom();
}
